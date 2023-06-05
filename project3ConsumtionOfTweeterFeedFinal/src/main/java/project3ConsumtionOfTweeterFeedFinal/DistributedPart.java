package project3ConsumtionOfTweeterFeedFinal;

import mpi.MPI;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentClass;

public class DistributedPart {


	private static int totalTweets;

	public static void main(String[] args) throws IOException {
		double totalAnalysisStart;
		double totalAnalysisEnd;
		double totalAnalysis;
		MPI.Init(args);

		int rank = MPI.COMM_WORLD.Rank();

		totalAnalysisStart = System.currentTimeMillis();

		if (rank == 0) {
			masterProcess();


		} else {
			workerProcess();

		}

		totalAnalysisEnd = System.currentTimeMillis();

		// Waiting for all the workers to finish analyzing the tweets.
		MPI.COMM_WORLD.Barrier();
		if (rank == 0) {

			// Calculating the time of the slowest worker.
			// Dividing the time of the slowest worker who finishes last,
			// with the ammount of overall tweets we are analyzing gives
			// us the average time per tweet.


			double largestDouble = Calculations.findMax();										
			System.out.println("\n\n\nThe time of the slowest worker is: " + largestDouble);		    
			System.out.println("\nTweet per second for the whole program: " + largestDouble/totalTweets);	

			double averagePipeline = Calculations.calculateAverageFromFile("distributedPipelineAverage.txt");
			System.out.println("\nAverage time to get the pipeline is: " + averagePipeline);

			int numOfTweets = Calculations.countLines("tweetsForDistributed1.txt");
			System.out.println("\nNumber of tweets we analyze: " + numOfTweets);
		} else {}

		MPI.Finalize();
	}


	//				MASTER PROCESSOR
	//______________________________________________________________________________________________________________________________________________________
	private static void masterProcess() throws IOException {


		// _________________________________________________________________________________________________________________________________________________
		// Deleting previous sentiments from previous tests from the file.												     CLEARING THE SENTIMENT FILE

		try {
			BufferedWriter sentimentWriter = new BufferedWriter(new FileWriter("distributedSentiment.txt"));
			sentimentWriter.write("");
			sentimentWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//																													 CLEARING THE SENTIMENT FILE
		// _________________________________________________________________________________________________________________________________________________

		// _________________________________________________________________________________________________________________________________________________
		// Deleting previous average pipeline from the file.												 			 CLEARING THE AVERAGE PIPELINE FILE

		try {
			BufferedWriter sentimentWriter = new BufferedWriter(new FileWriter("distributedPipelineAverage.txt"));
			sentimentWriter.write("");
			sentimentWriter.close();
		} catch (IOException e) {e.printStackTrace();}

		//																										 		 CLEARING THE AVERAGE PIPELINE FILE
		// _________________________________________________________________________________________________________________________________________________


		// _________________________________________________________________________________________________________________________________________________
		// Deleting previous analysis per process from the file.											 		 CLEARING THE ANALYSIS PER PROCESS FILE

		try {
			BufferedWriter sentimentWriter = new BufferedWriter(new FileWriter("analysisTimePerProcess.txt"));
			sentimentWriter.write("");
			sentimentWriter.close();
		} catch (IOException e) {e.printStackTrace();}

		//																										 	 CLEARING THE ANALYSIS PER PROCESS FILE
		// _________________________________________________________________________________________________________________________________________________


		// Number of workers: Overall - 1 => Since we have 1 master.
		int numWorkers = MPI.COMM_WORLD.Size() - 1;

		String filename = "tweetsForDistributed1.txt"; 

		// Read tweets from tweetsForDistributed.txt:
		BufferedReader reader = new BufferedReader(new FileReader(filename));

		String line;

		StringBuilder sb = new StringBuilder();

		while ((line = reader.readLine()) != null) {
			sb.append(line).append("\n");
		}
		reader.close();

		String[] tweets = sb.toString().split("\n");
		int numTweets = tweets.length;
		totalTweets = numTweets;

		// Splitting the workload between the workers:
		int tweetsPerWorker = numTweets / numWorkers;
		int remainingTweets = numTweets % numWorkers;

		for (int i = 1; i <= numWorkers; i++) {
			int startIndex = (i - 1) * tweetsPerWorker;
			int endIndex = startIndex + tweetsPerWorker;

			if (i == numWorkers) {
				endIndex += remainingTweets;
			}

			int currentTweetsPerWorker = endIndex - startIndex;

			String[] workerTweets = new String[currentTweetsPerWorker];
			System.arraycopy(tweets, startIndex, workerTweets, 0, currentTweetsPerWorker);


			// Sending information to the workers:
			MPI.COMM_WORLD.Send(new int[]{currentTweetsPerWorker}, 0, 1, MPI.INT, i, 0);
			MPI.COMM_WORLD.Send(workerTweets, 0, workerTweets.length, MPI.OBJECT, i, 1);

		}
	}

	// 				MASTER PROCESSOR
	//______________________________________________________________________________________________________________________________________________________



	//______________________________________________________________________________________________________________________________________________________
	//				WORKER PROCESSOR

	private static void workerProcess() throws IOException {

		// Timers for the calculations:
		double timerStartWorkerThread;
		double timerEndWorkerThread;
		int[] numTweets = new int[1];
		double timerAnalyzeTweetsStart;
		double timerAnalyzeTweetsEnd;
		double timerCreatePipelineStart;
		double timerCreatePipelineEnd;
		timerStartWorkerThread = System.currentTimeMillis();

		// Receiving information from the master processor:
		MPI.COMM_WORLD.Recv(numTweets, 0, 1, MPI.INT, 0, 0);
		int rank = MPI.COMM_WORLD.Rank();

		String[] tweets = new String[numTweets[0]];
		MPI.COMM_WORLD.Recv(tweets, 0, numTweets[0], MPI.OBJECT, 0, 1);



		//____________________________________________________________________________________
		//		SENTIMENT ANALYSIS

		// Creating the pipeline:
		timerCreatePipelineStart = System.currentTimeMillis();
		StanfordCoreNLP pipeline = Pipeline.createPipeline();
		timerCreatePipelineEnd = System.currentTimeMillis();

		// Write sentiment information to sentimentDistributed.txt:
		BufferedWriter writer = new BufferedWriter(new FileWriter("distributedSentiment.txt", true));

		timerAnalyzeTweetsStart = System.currentTimeMillis();

		for (int i = 0; i < tweets.length; i++) {
			String tweet = tweets[i];
			Annotation annotation = new Annotation(tweet);
			pipeline.annotate(annotation);

			// Get the sentiment value (e.g., "Positive", "Negative") for the tweet:
			String sentiment = annotation.get(CoreAnnotations.SentencesAnnotation.class).get(0)
					.get(SentimentCoreAnnotations.SentimentClass.class);


			writer.write("" + sentiment);
			writer.newLine();

			System.out.println("Processor: " + rank);
			System.out.println("Tweet " + i  + ": "+ tweet + "\nSentiment: " + sentiment);

		}

		writer.close();

		//		SENTIMENT ANALYSIS
		//____________________________________________________________________________________

		timerAnalyzeTweetsEnd = System.currentTimeMillis();
		timerEndWorkerThread = System.currentTimeMillis();




		//____________________________________________________________________________________
		//		CALCULATIONS

		double creationOfPipeline = timerCreatePipelineEnd- timerCreatePipelineStart;
		double analysisTime = timerAnalyzeTweetsEnd - timerAnalyzeTweetsStart;
		double averagePerTweet = analysisTime / numTweets[0] ;
		double workerThread = timerEndWorkerThread - timerStartWorkerThread;

		System.out.println("\nProcessor number: " + rank + " -Time to create the pipeline: " + creationOfPipeline/1000 + "s");
		System.out.println("\nProcessor: " + rank + " -Number of tweets: " + numTweets[0]);
		System.out.println("\nProcessor number: " + rank + " -Time for analyzing the tweets: " + analysisTime/1000 + "s");
		System.out.println("\nProcessor: " + rank + " -Average per tweet: " + averagePerTweet/1000 + "s");
		System.out.println("\nProcessor: " + rank + " -Worker processor total time: " + workerThread/1000 + "s");


		// THIS IS USED TO GET THE TWEET PER SECOND
		BufferedWriter analysisTimePerProcess = new BufferedWriter(new FileWriter("analysisTimePerProcess.txt", true));
		analysisTimePerProcess.write("" + workerThread/1000);
		analysisTimePerProcess.newLine();
		analysisTimePerProcess.close();

		// THIS IS TO GET THE AVERAGE TIME TO PULL THE PIPELINE
		BufferedWriter averagePipeline = new BufferedWriter(new FileWriter("distributedPipelineAverage.txt", true));
		averagePipeline.write("" + creationOfPipeline/1000);
		averagePipeline.newLine();
		averagePipeline.close();




		//		CALCULATIONS
		//____________________________________________________________________________________
	}



	//				WORKER PROCESSOR
	//______________________________________________________________________________________________________________________________________________________

}

package project3ConsumtionOfTweeterFeedFinal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class ParallelPart {



	public static void ProcessTweets() throws TwitterException {


		// We need to clear the file manually since multiple threads will write to it.
		// That is different from the sequential part, where we do not have to clear the
		//file since it is overwritten everytime we run the sequential mode.

		// _________________________________________________________________________________________________________________________________________________
		// Deleting previous sentiments from previous tests from the file.												       CLEARING THE SENTIMENT FILE:
		
		try {
			BufferedWriter sentimentWriter = new BufferedWriter(new FileWriter("parallelSentiment.txt"));
			sentimentWriter.write("");
			sentimentWriter.close();
		} catch (IOException e) {e.printStackTrace();}
		
		//																													   CLEARING THE SENTIMENT FILE:
		// _________________________________________________________________________________________________________________________________________________

		// _________________________________________________________________________________________________________________________________________________
		// 																														   CREATING THE PIPELINE:
																						  
		System.out.println("\nPLEASE WAIT: The pipeline is being created.\n");
		long startCreatePipeline = System.currentTimeMillis();
		
		// Create the StanfordCoreNLP pipeline:	
		StanfordCoreNLP pipeline = Pipeline.createPipeline();
		
		long finishCreatePipeline = System.currentTimeMillis();
		
		System.out.println("\nPipeline created.\n");
		//																														   CREATING THE PIPELINE:
		// _________________________________________________________________________________________________________________________________________________




		// _________________________________________________________________________________________________________________________________________________
		//																													  COUNNTING THE TOTAL TWEETS:

		// Count the total number of lines in the file
		int totalLines = Tweets.countTweets();
		System.out.println("\nThe total number of tweets is: " + totalLines);
		
		//																													  COUNNTING THE TOTAL TWEETS:
		// _________________________________________________________________________________________________________________________________________________



		// _________________________________________________________________________________________________________________________________________________
		//																																 	 TWEET ANALYSIS:
		
		// Get the number of tweets to analyze from the user
		System.out.println("\nEnter the number of tweets to analyze: ");
		Scanner scanner = new Scanner(System.in);
		int numTweetsToAnalyze = scanner.nextInt();

		// Get the number of threads to create from the user
		System.out.println("Enter the number of threads to create: ");
		int numThreads = scanner.nextInt();

		
		//____________________________________________________________________________________
		//		DIVIDING THE WORKLOAD

		// Calculate the number of tweets per thread
		int tweetsPerThread = numTweetsToAnalyze / numThreads;
		int remainingTweets = numTweetsToAnalyze % numThreads;
		
		//		DIVIDING THE WORKLOAD
		//____________________________________________________________________________________
			

		//____________________________________________________________________________________
		//		CREATION OF THE THREADS
		
		// Create and start the threads
		int tweetCount = 0;
		
		
		long startTotal = System.currentTimeMillis();
		
		
		List<Thread> threads = new ArrayList<>(); // Create a list to hold the thread objects

		for (int i = 0; i < numThreads; i++) {
			int tweetsInThread = tweetsPerThread;
			if (remainingTweets > 0) {
				tweetsInThread++;
				remainingTweets--;
			}
			Thread thread = new Thread(new AnalyzerThread(tweetCount, tweetsInThread, i + 1, pipeline));
			thread.start();
			threads.add(thread); // Add the thread object to the list
			tweetCount += tweetsInThread;
		}
		
			

		// Wait for all the threads to finish
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		//		CREATION OF THE THREADS
		//____________________________________________________________________________________
			
		
		//		 																															 TWEET ANALYSIS:
		// _________________________________________________________________________________________________________________________________________________
		
		
		

		// _________________________________________________________________________________________________________________________________________________ 
		//																																		 RESULTS:
		//Initialization variables for results:
		double finishTotal = System.currentTimeMillis();


		//Calculations:
		double totalDuration = finishTotal - startTotal;
		double averageTimePerTweet = (double) totalDuration / numTweetsToAnalyze;
		double creationOfPipeline = finishCreatePipeline - startCreatePipeline;



		//Print statements to test in the console:

		System.out.println("\nTime to pull the pipeline: " + (creationOfPipeline/1000) + " seconds.");
		System.out.println("\nTime needed to do the sentiment analysis of: " + numTweetsToAnalyze + " tweets, using " + numThreads + " threads: " + (totalDuration/1000) + "s.");
		System.out.println("\nThe average time per tweet is: " + (averageTimePerTweet/1000) + "s");


		//Writing results into the parallelResults.txt file:
		BufferedWriter resultsWriter;
		try {
			resultsWriter = new BufferedWriter(new FileWriter("parallelResults.txt", true));

			resultsWriter.newLine();
			resultsWriter.write("\nTime to pull the pipeline: " + (creationOfPipeline/1000) + " seconds.");
			resultsWriter.write("\nTime needed to do analysis on " + numTweetsToAnalyze + " using " + numThreads + " threads: " + (totalDuration/1000) + " seconds.");
			resultsWriter.write("\nAverage time per tweet: " + (averageTimePerTweet/1000) + " seconds.");
			resultsWriter.newLine();

			resultsWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		BufferedWriter averageWriter;
		try {
			averageWriter = new BufferedWriter(new FileWriter("parallelAverage.txt", true));


			averageWriter.write(""+(averageTimePerTweet/1000));
			averageWriter.newLine();

			averageWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// _________________________________________________________________________________________________________________________________________________ 

	}

	// _________________________________________________________________________________________________________________________________________________ 
	//																																	 RUNNABLE CLASS:
	static class AnalyzerThread implements Runnable {
		private int startingTweetIndex;
		private int numOfTweets;
		private int threadNumber;
		private StanfordCoreNLP pipeline;

		AnalyzerThread(int startingTweetIndex, int numOfTweets, int threadNumber, StanfordCoreNLP pipeline) {
			this.startingTweetIndex = startingTweetIndex;
			this.numOfTweets = numOfTweets;
			this.threadNumber = threadNumber;
			this.pipeline = pipeline;
		}

		@Override
		public void run() {
			// Read the tweets from the file and analyze the assigned range of tweets
			try {
				BufferedReader reader = new BufferedReader(new FileReader("tweets.txt"));
				String line;
				int tweetCount = 0;

				// Skip lines until reaching the starting tweet for this thread
				for (int i = 0; i < startingTweetIndex; i++) {
					reader.readLine();
				}

				BufferedWriter sentimentWriter = new BufferedWriter(new FileWriter("parallelSentiment.txt", true));
				long startTime = System.currentTimeMillis();
				while ((line = reader.readLine()) != null && tweetCount < numOfTweets) {
					// Process the tweet
					System.out.println("Thread " + threadNumber + ": Tweet " + (startingTweetIndex + tweetCount) + ": " + line);

					// Perform sentiment analysis on the tweet.
					Annotation annotation = pipeline.process(line);

					// Retrieve the sentiment of the tweet.
					String sentiment = annotation.get(CoreAnnotations.SentencesAnnotation.class).get(0)
							.get(SentimentCoreAnnotations.SentimentClass.class);

					// Print the sentiment of the tweet.
					System.out.println("Sentiment: " + sentiment);

					sentimentWriter.write(sentiment); // Write the sentiment to the file.
					sentimentWriter.newLine(); // Add a newline character after each sentiment.
					System.out.println(); // Add a new line for readability.

					tweetCount++;
				}
				sentimentWriter.close();
				reader.close();
				long endTime = System.currentTimeMillis();
				double durationInSeconds = (endTime - startTime) / 1000.0;
				System.out.println("Thread " + threadNumber + " analysis time: " + durationInSeconds + " seconds");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

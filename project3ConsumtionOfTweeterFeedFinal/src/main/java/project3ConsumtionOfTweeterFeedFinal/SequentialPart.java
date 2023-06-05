package project3ConsumtionOfTweeterFeedFinal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
//import edu.stanford.nlp.util.CoreMap;
import java.util.Scanner;

public class SequentialPart {

	// Variables used for testing the time needed for the operations:

	public static double TimerSentimentStart;
	public static double TimerSentimentEnd;
	public static double TimerGetPipelineStart;
	public static double TimerGetPipelineEnd;
	public static int totalTweets;



	//_____________________________________________________________________________________________________________________________________________
	//																														  PROCESSING THE TWEETS
	public static void ProcessTweets() throws TwitterException {

		int counter = 1;

		try {

			//____________________________________________________________________________________
			//		CREATING THE PIPELINE

			BufferedWriter sentimentWriter;
			try {


				//____________________________________________________________________________________
				//				CREATING THE PIPELINE
				System.out.println("\nPLEASE WAIT: The pipeline is being created.\n");


				TimerGetPipelineStart = System.currentTimeMillis();
				StanfordCoreNLP pipeline = Pipeline.getPipeline();
				TimerGetPipelineEnd = System.currentTimeMillis();

				System.out.println("\nPipeline has been created.\n");

				//				CREATING THE PIPELINE
				//____________________________________________________________________________________


				//____________________________________________________________________________________
				//		SENTIMENT ANALYSIS


				//
				// Count the total number of lines in the file
				int totalLines = Tweets.countTweets();

				// Display the total number of lines
				System.out.println("Total number of tweets in the file: " + totalLines);

				sentimentWriter = new BufferedWriter(new FileWriter("sequentialSentiment.txt"));
				BufferedReader reader = new BufferedReader(new FileReader("tweets.txt"));

				try {
					// Ask the user for how many tweets he would like to analyze
					String line;
					System.out.println("Enter the number of tweets to analyze: ");
					@SuppressWarnings("resource")
					Scanner scanner = new Scanner(System.in);
					int numTweetsToAnalyze = scanner.nextInt();

					TimerSentimentStart = System.currentTimeMillis();

					int tweetCount = 0;
					while ((line = reader.readLine()) != null && tweetCount < numTweetsToAnalyze) {
						// Remove leading and trailing whitespace.
						line = line.trim();

						if (!line.isEmpty()) {
							// Process the tweet.
							System.out.println("Tweet " + counter + ": " + line);
							counter++;
							tweetCount++;

							// Perform sentiment analysis on the tweet.
							Annotation annotation = pipeline.process(line);

							// Retrieve the sentiment of the tweet.
							String sentiment = annotation.get(CoreAnnotations.SentencesAnnotation.class).get(0).get(SentimentCoreAnnotations.SentimentClass.class);

							// Print the sentiment of the tweet.
							System.out.println("Sentiment: " + sentiment);

							sentimentWriter.write(sentiment); // Write the sentiment to the file.
							sentimentWriter.newLine(); // Add a newline character after each sentiment.
							System.out.println(); // Add a new line for readability.
						}
					}
					reader.close();
					sentimentWriter.close();
				} catch (IOException e) {e.printStackTrace();}
			} catch (FileNotFoundException e) {e.printStackTrace();}
		} catch (IOException e1) {e1.printStackTrace();}
		TimerSentimentEnd = System.currentTimeMillis();


		//			SENTIMENT ANALYSIS
		//____________________________________________________________________________________



		//____________________________________________________________________________________
		//			CALCULATIONS

		double SentimentTime = TimerSentimentEnd - TimerSentimentStart;
		double GetPipelineTime = TimerGetPipelineEnd - TimerGetPipelineStart;


		// Average time per tweet.
		BufferedWriter averageTweetAnalysisWriter;
		double averageTimePerTweet = SentimentTime / (counter - 1) / 1000;
		try {
			averageTweetAnalysisWriter = new BufferedWriter(new FileWriter("sequentialAverage.txt", true));

			averageTweetAnalysisWriter.write(""+ averageTimePerTweet);
			averageTweetAnalysisWriter.newLine();
			averageTweetAnalysisWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		BufferedWriter resultsWriter;
		try {
			resultsWriter = new BufferedWriter(new FileWriter("sequentialResults.txt", true));
			resultsWriter.newLine();
			resultsWriter.newLine();
			resultsWriter.write("Time needed to get the pipeline: " + GetPipelineTime/1000+"s");
			resultsWriter.newLine();
			resultsWriter.write("Time needed to do the sentiment analysis of " + (counter-1) + " tweets: " + SentimentTime/1000+"s");
			resultsWriter.newLine();
			resultsWriter.write("Average time per tweet: " + averageTimePerTweet+"s");
			resultsWriter.newLine();
			resultsWriter.newLine();

			resultsWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}




		System.out.println("Time needed to get the pipeline: " + GetPipelineTime/1000+"s");
		System.out.println("Time needed to do the sentiment analysis of: " + (counter-1) + " tweets: " + SentimentTime/1000+"s");
		System.out.println("The average time per tweet is: " + averageTimePerTweet + "s");

	}

	//				CALCULATIONS
	//____________________________________________________________________________________


	//	 																													  PROCESSING THE TWEETS
	//_____________________________________________________________________________________________________________________________________________
}

package project3ConsumtionOfTweeterFeedFinal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class Tweets {
	//_____________________________________________________________________________________________________________________________________________
	//																													 COUNT THE NUMBER OF TWEETS
	public static int countTweets() {
		// Count the total number of lines in the file
		int totalLines = 0;
		try (BufferedReader lineReader = new BufferedReader(new FileReader("tweets.txt"))) {
			while (lineReader.readLine() != null) {
				totalLines++;
			}
		} catch (IOException e) {e.printStackTrace();}
		return totalLines;
	}
	//																													 COUNT THE NUMBER OF TWEETS
	//_____________________________________________________________________________________________________________________________________________


	//_____________________________________________________________________________________________________________________________________________
	//																																PULL NEW TWEETS
	public static void PullTweets() throws TwitterException {
		double TimerPullTweetsStart;
		double TimerPullTweetsEnd;



		TimerPullTweetsStart = System.currentTimeMillis();
		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();

		// Authentication of the profile from where we pull the tweets.
		String consumerKey = "[enter-consumerKey]";
		String consumerKeySecret = "[enter-consumerKeySecret]";
		String accessKey = "[[enter-accessKey]";
		String accessKeySecret = "[enter-acessKeySecret]";

		configurationBuilder.setDebugEnabled(true)
		.setOAuthConsumerKey(consumerKey)
		.setOAuthConsumerSecret(consumerKeySecret)
		.setOAuthAccessToken(accessKey)
		.setOAuthAccessTokenSecret(accessKeySecret);

		// Getting an instance of the library and then pulling the tweets.
		TwitterFactory tf = new TwitterFactory(configurationBuilder.build());
		twitter4j.Twitter twitter = tf.getInstance();

		// Number of tweets we want to pull. We can also choose here from which # of page we want to pull them from.
		Paging paging = new Paging();
		paging.setCount(200); // Set the count per request to 200

		List<Status> status = new ArrayList<Status>(); // Create an empty list to store the tweets

		int pageNum = 1;
		int totalTweets = 0;

		while (totalTweets < 1000) {
			paging.setPage(pageNum);
			try {
				List<Status> tweets = twitter.getHomeTimeline(paging);
				if (tweets.isEmpty()) {
					break; // No more tweets available, exit the loop
				}
				status.addAll(tweets);
				totalTweets += tweets.size();
				pageNum++;
			} catch (TwitterException e) {
				e.printStackTrace();
				break; // An error occurred, exit the loop
			}
		}
		// Now you have up to 1000 tweets stored in the 'status' list
		try {
			BufferedWriter tweetWriter = new BufferedWriter(new FileWriter("tweets.txt", true)); // Append mode ("output.txt" , true) 

			// Iterate through every tweet.
			for (Status s : status) {
				// Iterate through the list of tweets.
				String text = s.getText();
				text = text.replace("\n", " ").replace("\r", ""); // Remove newline characters from the tweet text.
				tweetWriter.write(text); // Write the modified tweet text to the file.
				tweetWriter.newLine();   // Add a newline character after each tweet.
			}
			tweetWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}


		TimerPullTweetsEnd = System.currentTimeMillis();

		double PullTweetsTime =  TimerPullTweetsEnd - TimerPullTweetsStart;
		System.out.println("Time needed to pull " + totalTweets + " new tweets: " +  + PullTweetsTime/1000+"s");

	}

	//	                                                                                                                            PULL NEW TWEETS
	//_____________________________________________________________________________________________________________________________________________

}

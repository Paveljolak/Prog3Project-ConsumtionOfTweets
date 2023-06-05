package project3ConsumtionOfTweeterFeedFinal;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import mpi.*;
import twitter4j.TwitterException;

public class Main {
	
	public static void main(String[] args) {

		int numOfTweets = Tweets.countTweets();
		
		System.out.println("There are currently: " + numOfTweets + " tweets.");
		System.out.println("1 - Pull more.");
		System.out.println("2 - These are enough.");
		
		Scanner scan = new Scanner(System.in);
		int pullTweets = scan.nextInt();
		
		
		if(pullTweets == 1) {
			try {
				Tweets.PullTweets();
				numOfTweets = Tweets.countTweets();
				System.out.println("There are currently: " + numOfTweets + " tweets.\n");
			} catch (TwitterException e) {e.printStackTrace();}
		}
		
		System.out.println("Please enter in which mode you want to run the program: ");
		System.out.println("1 - Sequential");
		System.out.println("2 - Parallel");
		System.out.println("3 - Distributed");
		


		Scanner scanner = new Scanner(System.in);
		int userInput;

		boolean isValidInput = false;

		while (!isValidInput) {
			System.out.print("Please enter 1, 2, or 3: ");
			userInput = scanner.nextInt();
			scanner.nextLine(); // Consume the newline character

			switch (userInput) {
			case 1:
				try {
					
					SequentialPart.ProcessTweets();

				} catch (TwitterException e) {e.printStackTrace();}
				isValidInput = true;
				break;
			case 2:
				try {
					ParallelPart.ProcessTweets();
				} catch (TwitterException e) {e.printStackTrace();}
				isValidInput = true;
				break;
			case 3:

				System.out.println("Starting distributed part.");

				System.out.println("Apologies for the inconvinience. Please run the distributed mode manually.");
				System.out.println("Left click on the DistributedPart.java in the Package Explorer. -> Run as  -> Java Application.");

				isValidInput = true;
				break;
			default:
				System.out.println("Invalid input. Please try again.");
				break;
			} 
		}
		scanner.close();


	}

}




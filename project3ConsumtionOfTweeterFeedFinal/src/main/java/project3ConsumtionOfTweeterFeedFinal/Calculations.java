package project3ConsumtionOfTweeterFeedFinal;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Calculations {
	
	
	static double findMax() {
		double largestDouble = Double.MIN_VALUE;
		try (BufferedReader reader = new BufferedReader(new FileReader("analysisTimePerProcess.txt"))) {
			String line;
			while ((line = reader.readLine()) != null) {
				double number = Double.parseDouble(line);
				if (number > largestDouble) {
					largestDouble = number;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return largestDouble;
	}
	
	public static int countLines(String filePath) {
		// Count the total number of lines in the file
		int totalLines = 0;
		try (BufferedReader lineReader = new BufferedReader(new FileReader(filePath))) {
			while (lineReader.readLine() != null) {
				totalLines++;
			}
		} catch (IOException e) {e.printStackTrace();}
		return totalLines;
	}
	
	public static double calculateAverageFromFile(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        double sum = 0.0;
        int count = 0;

        while ((line = reader.readLine()) != null) {
            double value = Double.parseDouble(line.trim());
            sum += value;
            count++;
        }

        reader.close();

        if (count == 0) {
            throw new IllegalArgumentException("The file is empty");
        }

        return sum / count;
    }
}



import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Scanner;

import org.jsoup.Jsoup;

/**
 * Tells you 25 most-frequently-used words on a webpage
 * 
 * When you run the program, it will ask for a url. Then it will take that url and the words on the
 * URL's webpage and insert them into a table called "websites" in a mySQL database called 
 * "websiteWords". Then it will find the number of times each word is used and, add each word and
 * its use count into a table in the same database called "words". Finally, it will print out the 
 * 25 words with the highest use count. If there is already an entry for the input URL, and it was 
 * created in the last 7 days, skips all the other steps and just returns the 25 words with the
 * highest use count linked to the input URL
 * 
 * @author Bryan Williams
 *
 */
public class TopTwentyFiveWords {
	
	/**
	 * SOME OF THIS COULD BE BROKEN OUT INTO METHODS. DO THIS ONCE I'VE WORKED THROUGH ALL FUNCTIONALITY.
	 * 
	 * @param args Don't do anything with passed-in args
	 */
	public static void main(String[] args) {
		//create DbConnect object con
		DbConnect con = new DbConnect();
		
		// initialize userInput so we can accept information from the user
		Scanner userInput = new Scanner(System.in);
		
		System.out.println("What database would you like to use to store our results?");
		String database = userInput.next();
		
		System.out.println("What is your mySQL username?");
		String user = userInput.next();
		
		System.out.println("What is your mySQL password?");
		String pass = userInput.next();
		
		System.out.println("What URL would you like to find the 25 most frequently-used words from?");
		String url = userInput.next();
		
		// close userInput to avoid memory leak
		userInput.close();
		
		// get words from the submitted webpage
		String urlWords = "";
		try {
			urlWords = Jsoup.connect(url).get().text();
		} catch(Exception ex) {
			System.out.println("Error with Jsoup.get().text():" + ex);
		}
		// escape all apostrophes in urlWords to avoid issues passing into database
		String urlWordsEscaped = urlWords.replaceAll("'", "\\\\'");
		
		// create connection to database of user's choice called userDb. Will be created if it doesn't exist.
		Connection userDb = con.connectTo(database, user, pass);
		
		// SQL statements to create the two tables we need (if they don't already exist) in the input database and pass in the url and the page's words
		String useDatabase = "USE " + database + ";";
		
		String createWebsitesTable = "CREATE TABLE IF NOT EXISTS websites" +
										"(" +
										 "url varchar(255) NOT NULL," +
										 "content longtext NOT NULL," +
										 "PRIMARY KEY (url)" +
										");";
		
		String createWordsTable = "CREATE TABLE IF NOT EXISTS words" +
									"(" +
									  "word varchar(255) NOT NULL," +
									  "count int NOT NULL," +
									  "url varchar(255) NOT NULL," +
									  "uniqueId varchar(255)," +
									  "PRIMARY KEY (uniqueId)," +
									  "FOREIGN KEY (url) REFERENCES websites(url)" +
									");";
		
		String addUrl = "INSERT INTO websites VALUES ('" + url + "', '" + urlWordsEscaped + "');";
		
		// use our connection to the input DB to create the two tables that will hold the info from the url: websites and words
		try {
			userDb.createStatement().executeQuery(useDatabase);
			userDb.createStatement().executeUpdate(createWebsitesTable);
			userDb.createStatement().executeUpdate(createWordsTable);
			userDb.createStatement().executeUpdate(addUrl);
		} catch (Exception ex) {
			System.out.println("Error creating tables or adding record to websites: " + ex);
			// if exception that url already exists, skip to final step of printing out words
		}
		
		// String that is websites(url)[content]. Keep only words and numbers (delete punctuation except apostrophes and single periods) - use regex
		String noPunctuation = urlWords.replaceAll("(?:(?<!\\S)\\p{Punct}+)|(?:\\p{Punct}+(?!\\S)|\\.)", "");
		noPunctuation = noPunctuation.toLowerCase();
		String noPunctuationEscaped = noPunctuation.replaceAll("'", "\\\\'");
		
		// break database into Array of strings using " " as separator
		String[] wordArray = noPunctuationEscaped.split(" ");
		
		// iterate over Array and check if word is in words[word] with words[url] matching our url. If so, change that entry's count to count+1. If not,
		// create new entry with word, count=1, url=input url
		for (String word : wordArray) {
			String wordUniqueId = word + url;
			String addWordOrAddToCount = "INSERT INTO words VALUES ('" + word + "', '1', '" + url + "', '" + wordUniqueId + "') ON DUPLICATE KEY UPDATE count = count + 1;";
			if(!word.isEmpty()) {
				try {
					userDb.createStatement().executeUpdate(addWordOrAddToCount);
				} catch(Exception ex) {
					System.out.println("Error adding or updating count on word: " + word + " in words table: " + ex);
				}
			}
		}
		
		// use SQL query to get resultset of the 25 words with the highest counts whose url=inputurl & their count in descending order of count
		String getTopTwentyFive = "SELECT word, count FROM words WHERE url='" + url + "' ORDER BY count DESC LIMIT 25;";
		ResultSet topTwentyFive;
		try {
			topTwentyFive = userDb.createStatement().executeQuery(getTopTwentyFive);
			//print out the words and their counts from our result set
			ResultSetMetaData rsmd = topTwentyFive.getMetaData();
			int numColumns = rsmd.getColumnCount();
			int numbering = 1;
			while (topTwentyFive.next()) {
				for (int i = 1; i <= numColumns; i++) {
					if (i > 1) {
						System.out.print(", ");
					} else if (i == 1) {
						System.out.print(numbering + ".) " );
						numbering++;
					}
					String colValue = topTwentyFive.getString(i);
					System.out.print(rsmd.getColumnName(i) + ": " + colValue);
				}
				System.out.println();
			}
		} catch (Exception ex) {
			System.out.println("Error getting ResultSet topTwentyFive: " + ex);
		}
				
		
		// close connection to database;\
		try {
			userDb.close();
		} catch (SQLException e) {
			System.out.print("Encountered error while closing connection to " + database);
		}
	}

}

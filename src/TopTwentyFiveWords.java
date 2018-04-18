import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

import org.jsoup.Jsoup;

/**
 * Tells you 25 most-frequently-used words on a webpage
 *
 * @author Bryan Williams
 *
 */
public class TopTwentyFiveWords {

	/**
	 * Takes various user inputs and spits out 25 most frequently-used words on a webpage
	 *
	 * Uses user-input credentials to connect to their mySQL. Connects to or creates database of 
	 * their choosing, and creates two tables in it: 'websites' and 'words'. Then takes a url of
	 * their choice and gets all of that url's webpage's text and stores the text and url in 
	 * 'websites'. Then records each individual word from that webpage's text, along with
	 * the number of times that word appears and the url it came from in 'words'. Finally, outputs
	 * the 25 most frequently-used words and the number of times they were used. If user's url is
	 * already in the database and was added fewer than 2 days ago, re-uses existing data.
	 *
	 * @param args Don't do anything with passed-in args
	 */
	public static void main(String[] args) {		
		Scanner userInput = new Scanner(System.in);
		RequiredInfo userInfo = new RequiredInfo();
		userInfo.getMySql(userInput);
		userInfo.getDbUrlInfo(userInput);
		Connection userDb = connectTo(userInfo, userInput, "useSSL=false");
		userInfo.password = null;
		if(userDb != null) {	
			if(createTables(userDb, userInfo.databaseName)); {
				userInfo.getUrl(userInput);			
				userInput.close();
				System.out.println("Retrieving all text from url: " + userInfo.url + " ...");
				String urlText = getWords(userInfo.url);
				System.out.println("Successfully retrieved text!");
				if(addUrlToWebsites(userDb, userInfo.url, urlText, userInfo.databaseName)) {
					String noPunctuation = prepForCount(urlText);
					String[] wordArray = noPunctuation.split(" ");
					System.out.println("Adding each word from " + userInfo.url + " to words table in " + 
										userInfo.databaseName + " ...");
					countWords(wordArray, userInfo.url, userDb);
					System.out.println("Successfully added each word and its count to words table!");
					printTopTwentyFive(userInfo.url, userDb);
				} // if (addUrlToWebsites)
			} // if (CreateTables)
			try {
				System.out.println("\nClosing connection to your database...");
				userDb.close();
				System.out.println("Successfully closed connection to your database!");
			} catch (SQLException e) {
				System.out.print("Encountered error while closing connection to " + userInfo.databaseName);
			} 
		} // if (userDb)
		System.out.println("Program ending. Thanks for using TopTwentyFiveWords!");
	} // main
	
	/**
	 * Creates or connects to specified database
	 * 
	 * First connects to the JDBC driver. Then Creates connection to mySQL. Then connects to
	 * database if it already exists - if not, creates the database then connects to it. Can retry
	 * entering credentials if connection fails as many times as you choose. Also allows 
	 * customization of mySQL hostname and port if it can't connect using the defaults 
	 * hostname: localhost and port: 3306.
	 * 
	 * @param obj Object containing all information needed to connect to their mySQL
	 * @param scan Scanner object for recieving user input
	 * @param dbUrlSuffix Anything to tack onto the mySQL database URL after "databaseName?"
	 * @return Connection to database specified by databaseName
	 * @return null if unable to connect
	 */
	private static Connection connectTo(RequiredInfo obj, Scanner scan, String dbUrlSuffix) {
		String mySql = buildDbUrl(obj.hostName, obj.port, "", dbUrlSuffix);
		String dbUrl = buildDbUrl(obj.hostName, obj.port, obj.databaseName, dbUrlSuffix);
		String sql = "CREATE DATABASE IF NOT EXISTS " + obj.databaseName;
		try {
			System.out.println("\nConnecting to JDBC driver...");
			Class.forName("com.mysql.jdbc.Driver");
			System.out.println("Successfully connected to JDBC driver!");
			System.out.println("\nConnecting to mySQL with your credentials...");
			Connection con = DriverManager.getConnection(mySql, obj.username, new String(obj.password));
			System.out.println("Successfully connected to mySQL!");
			System.out.println("Creating your database (if it doesn't exist already)...");
			con.createStatement().executeUpdate(sql);
			System.out.println("\nConnecting to your database " + obj.databaseName + " ...");
			Connection returnCon = DriverManager.getConnection(dbUrl, obj.username, new String(obj.password));
			System.out.println("Connected to your database " + obj.databaseName + "!");
			return returnCon;
		} catch(SQLException ex) {
			if (ex.getErrorCode() == 1045) { // if there's a problem with users' credentials
				Boolean retry = tryAgain(scan, "It looks like there was a problem with your mySQL "
												+ "credentials. Would you like try re-entering them"
												+ "? ('yes' or 'no')");
				if (retry) {
					obj.getMySql(scan);
					return connectTo(obj, scan, dbUrlSuffix);
				} else {
					System.out.println("You've opted not to try again. No mySQL connection made.");
					return null;
				}
			} else if (ex.getErrorCode() == 0) { // if default mySQL url fails
				Boolean retry = tryAgain(scan, "Unable to connect to your mySQL instance using "
												+ "URL: " + mySql + ". Would you like to correct "
												+ "this and try again? ('yes' or 'no')");
				if(retry) {
					obj.getDbUrlInfo(scan);
					return connectTo(obj, scan, dbUrlSuffix);
				}
			} else {
				System.out.println("Error connecting to mySQL: " + ex.getMessage());
				System.out.println("Error code was: " + ex.getErrorCode());
			}
			return null;
		} catch (ClassNotFoundException ex) {
			System.out.println("Error loading JDBC Drive: " + ex.getMessage());
			return null;
		}
	}
	
	/**
	 * Returns all text from a web page, with apostrophes escaped
	 * 
	 * Escapes apostrophes to prevent issues when inserting returned string into a SQL database
	 * 
	 * @param url The url of the web page you want to retrieve words from
	 * @return String with apostrophes escaped of all text url's web page
	 */
	private static String getWords(String url) {
		String urlText = "";
		try {
			urlText = Jsoup.connect(url).get().text();
		} catch(Exception ex) {
			System.out.println("Failed to get webpage text. Error: " + ex.getMessage());
		}
		return urlText.replaceAll("'", "\\\\'");
	}
	
	/**
	 * Returns input with all punctuation not inside a word removed and all characters lower case
	 * 
	 * @param text The string you want to remove punctuation from, make all lower case
	 * @return The string "text" with punctuation removed and all letters lower-case
	 */
	private static String prepForCount(String text) {
		String noPunctuation = text.replaceAll("(?:(?<!\\S)\\p{Punct}+)|(?:\\p{Punct}+(?!\\S)|\\.)", "");
		return noPunctuation.toLowerCase();
	}
	
	/**
	 * Add each word from url to words table in database
	 * 
	 * Iterate over arr of words and add each word in it to the words table in the database db. If
	 * the word has already been added to the table from the same url, add 1 to its count.
	 * 
	 * @param arr Array of values you want to add to the words table.
	 * @param url Url array of values were pulled from.
	 * @param db Database containing "words" table values are being added to.
	 */
	private static void countWords(String[] arr, String url, Connection db) {
		for (String word : arr) {
			String wordUniqueId = word + url;
			String addWordOrAddToCount = "INSERT INTO words (word, count, url, unique_id) VALUES "
											+ "('" + word + "', '1', '" + url + "', '"
											+ wordUniqueId + "') ON DUPLICATE KEY UPDATE "
											+ "count = count + 1;";
			if(!word.isEmpty()) {
				try {
					db.createStatement().executeUpdate(addWordOrAddToCount);
				} catch(Exception ex) {
					System.out.println("Error adding or updating count on word: " + word + " in "
										  + "words table: " + ex);
				}
			}
		}
		
	}
	
	/**
	 * Add a url and all text from its web page to table 'websites' in a database
	 * 
	 * Inserts a new record into table 'websites' in database db. Record includes a url and all of
	 * the text from that url's web page as a single string. If Insert fails because a record
	 * already exists with the same url (i.e., it has been looked up before), delete the old record
	 * and use new data if the existing record was created more than 2 days ago and return true. 
	 * If the existing record is less than 2 days old, skip any steps occurring after this method
	 * was called by printing out the top 25 words from the url passed to this method and returning
	 * false.
	 * 
	 * @param db Database containing table 'websites'
	 * @param url A url
	 * @param urlText Url's web page's text
	 * @param databaseName Name of database db
	 * @return True if main should continue after calling this method
	 * @return False if main should stop after calling this method
	 */
	private static Boolean addUrlToWebsites(Connection db, String url, String urlText, String databaseName) {
		String addUrl = "INSERT INTO websites (url, content) VALUES ('" + url + "', '" + urlText + "');";
		try {
			System.out.println("\nAdding " + url + " to websites table in " + 
								databaseName + " ...");
			db.createStatement().executeUpdate(addUrl);
			System.out.println("Successfully added " + url + " and its contents to websites table!");
			return true;
		} catch (SQLException ex) {
			if (ex.getErrorCode() == 1062) {				
				System.out.println("It appears you've looked this url up before...");
				String getDateCreated = "SELECT create_date FROM websites WHERE url='" + url + "';";
				ResultSet createDateLookup;
				try {
					createDateLookup = db.createStatement().executeQuery(getDateCreated);
					createDateLookup.first();
					Timestamp dateCreatedTimestamp = createDateLookup.getTimestamp(1);
					LocalDate created = dateCreatedTimestamp.toLocalDateTime().toLocalDate();
					LocalDate now = LocalDate.now();
					Long elapsed = ChronoUnit.DAYS.between(created, now);
					if (elapsed > 2) {
						System.out.println("You checked this over two days ago, so we'll update "
											+ "our records in case it changed since then");
						String deleteUrl = "DELETE FROM websites WHERE url='" + url + "';";
						System.out.println("\nDeleting existing url, its content and any linked "
											+ "entries from the 'words' table...");
						db.createStatement().executeUpdate(deleteUrl);
						System.out.println("Successfully deleted existing url record!");
						return addUrlToWebsites(db, url, urlText, databaseName);
					} else {
						System.out.println("It's been fewer than two days since you looked this "
											+ "up. No need to re-do all our work! Using existing "
											+ "data.");
						printTopTwentyFive(url, db);
						return false;
					} // if (elapsed > 2)/else
				} catch (SQLException sqlEx) {
					System.out.println("Wasn't able to lookup create date for record with url "
										+ url + ". Error message: " + sqlEx.getMessage() + "."
										+ "\nError code: " + sqlEx.getErrorCode());
					return false;
				}
			} else {
				System.out.println("Error adding record to websites: " + ex.getMessage() + ". Error code: " + ex.getErrorCode());
				return false;
			} // if (error code 1062)/else
		} // catch SQLException
	} // addUrlToWebsites
	
	/**
	 * Print the 25 words from 'words' table in database db with highest count
	 * 
	 * Get ResultSet of the 25 words with the largest count values from table "words" in database
	 * db. Then iterate over that ResultSet and print out each word and its count. 
	 * 
	 * @param url Url the words were retrieved from
	 * @param db Database the "words" table is in
	 */
	private static void printTopTwentyFive(String url, Connection db) {
		String getTopTwentyFive = "SELECT word, count FROM words WHERE url='" + url + "' ORDER BY count DESC LIMIT 25;";
		ResultSet topTwentyFive;
		try {
			topTwentyFive = db.createStatement().executeQuery(getTopTwentyFive);
			ResultSetMetaData rsmd = topTwentyFive.getMetaData();
			int numColumns = rsmd.getColumnCount();
			int numbering = 1;
			System.out.println("\nHere are the 25 most frequently used words on " + url + ":\n");
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
	}
	
	/**
	 * Outputs message and returns next user response
	 * 
	 * @param scan Scanner object for collecting the response
	 * @param message Message string to output to user
	 * @return String with user's response
	 */
	private static String promptForString(Scanner scan, String message) {
		System.out.println(message);
		return scan.next();
	}
	
	/**
	 * Constructs a url for connecting to mySQL database via JDBC
	 * 
	 * @param host MySQL hostname
	 * @param port MySQL port
	 * @param name Database Name
	 * @param suffix Anything that goes after the '?' following database name
	 * @return Full database url
	 */
	private static String buildDbUrl(String host, String port, String name, String suffix) {
			return "jdbc:mysql://" + host + ":" + port + "/" + name + "?" + suffix;
	}
	
	/**
	 * Creates tables 'websites' and 'words' in input database (if don't exist already)
	 * 
	 * If it fails to create these tables, returns false to prevent main from running further.
	 * Otherwise, returns true, letting main know it can keep going
	 * 
	 * @param db Connection to a SQL database to create the tables in
	 * @param databaseName Name of db
	 */
	private static Boolean createTables(Connection db, String databaseName) {
		String useDatabase = "USE " + databaseName + ";";
		String createWebsitesTable = "CREATE TABLE IF NOT EXISTS websites" +
										"(" +
										 "url varchar(255) NOT NULL," +
										 "content longtext NOT NULL," +
										 "create_date DATETIME DEFAULT CURRENT_TIMESTAMP," + 
										 "PRIMARY KEY (url)" +
										");";
		String createWordsTable = "CREATE TABLE IF NOT EXISTS words" +
									"(" +
									  "word varchar(255) NOT NULL," +
									  "count int NOT NULL," +
									  "url varchar(255) NOT NULL," +
									  "unique_id varchar(255)," +
									  "create_date DATETIME DEFAULT CURRENT_TIMESTAMP," + 
									  "PRIMARY KEY (unique_id)," +
									  "FOREIGN KEY (url) REFERENCES websites(url) ON DELETE CASCADE" +
									");";
		try {
			System.out.println("\nAdding table 'websites' to " + databaseName + " ...");
			db.createStatement().executeQuery(useDatabase);
			db.createStatement().executeUpdate(createWebsitesTable);
			System.out.println("Successfully added 'websites'!");
		} catch (SQLException ex) {
			System.out.println("Error creating table 'websites' in your database "
								+ databaseName + ": " + ex.getMessage());
			return false;
		}
		try {
			System.out.println("\nAdding table 'words' to " + databaseName + " ...");
			db.createStatement().executeQuery(useDatabase);
			db.createStatement().executeUpdate(createWordsTable);
			System.out.println("Successfully added'words'!");
		} catch (SQLException ex) {
			System.out.println("Error creating table 'words' in your database "
								+ databaseName + ": " + ex.getMessage());
			return false;
		}
		return true;

	}
	
	/**
	 * Prompts user for a "yes" or "no" (case-insensitive) and returns true or false accordingly
	 * 
	 * Will not take any input other than yes or no, and will ask until it receives on of these
	 * valid responses.
	 * 
	 * @param scan Scanner object for collecting user input
	 * @param message Message that will be used to prompt for user response
	 * @return Boolean. True if user answers "yes", false if "no".
	 */
	private static Boolean tryAgain(Scanner scan, String message) {
		String continueAnswer = null;
		while (true) {
			continueAnswer = promptForString(scan, message);
			if (continueAnswer.equalsIgnoreCase("yes")) {
				return true;
			} else if (continueAnswer.equalsIgnoreCase("no")) {
				return false;
			} else {
				System.out.println("Invalid response. Please write 'yes' or 'no'.\n");
			}
		}
	}

}

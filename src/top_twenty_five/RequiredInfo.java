package top_twenty_five;

import java.io.Console;
import java.util.Scanner;

/**
 * Holder object - holds information input by user needed by TopTwentyFiveWords 
 * 
 * @author Bryan Williams
 *
 */
public class RequiredInfo {

	String username;
	char[] password;
	String url;
	String databaseName;
	String hostName;
	String port;
	
	public RequiredInfo() {
		
	}

	/** 
	 * Get databaseName, username, and password from user
	 * 
	 * @param scan The scanner object used to collect their responses
	 */
	public void getMySql(Scanner scan) {
		Console cnsl = System.console();
		System.out.println("\nWhat is the name of the database you would like to use to store our "
						      + "results?\nWarning: this program will create two tables - websites "
						      + "and words - within that database.\nIf you don't already have a "
						      + "database by that name, it will create a new database with the "
						      + "name you specify.");
		this.databaseName = scan.next();
		System.out.println("What is your mySQL username?");
		this.username = scan.next();
		this.password = cnsl.readPassword("Enter your mySQL Password: ");
	}
	
	/**
	 * Get url from user
	 * 
	 * @param scan The scanner object used to collect their responses
	 */
	public void getUrl(Scanner scan) {				
		System.out.println("\nWhat URL would you like to find the 25 most frequently-used words from?");
		this.url = scan.next();
		System.out.println("\n");
	}
	
	/**
	 * Get hostName and port from user and overwrite defaults
	 * 
	 * @param scan The scanner object used to collect their responses
	 */
	public void getDbUrlInfo(Scanner scan) {
		System.out.println("\nWhat is your mySQL hostname? (if you're not sure, it's probably 'localhost')");
		this.hostName = scan.next();
		System.out.println("What is your mySQL Port? (if you're not sure, it's probably '3306')");
		this.port = scan.next();
	}
	
}

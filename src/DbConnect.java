import java.sql.Connection;
import java.sql.DriverManager;

public class DbConnect {
		
	
	public DbConnect() {}
	
	/**
	 * Creates or connects to specified database
	 * 
	 * @param databaseName Name of the database
	 * @param user Username for mySQL
	 * @param password Password for mySQL
	 * @return Connection to database specified by databaseName 
	 * @return null if exception thrown
	 */
	public Connection connectTo(String databaseName, String user, String password) {
		// url to connec to no mySQL but no specific database
		String dbUrl = "jdbc:mysql://localhost:3306/?useSSL=false";
		// sql statement to create new database if it doesn't already exist
		String sql = "CREATE DATABASE IF NOT EXISTS " + databaseName;
		try {
			// register the jdbc driver
			Class.forName("com.mysql.jdbc.Driver");
			// connect to database
			Connection con = DriverManager.getConnection(dbUrl, user, password);
			// create database if it doesn't already exist
			con.createStatement().executeUpdate(sql);
			// update dbUrl
			dbUrl = "jdbc:mysql://localhost:3306/" + databaseName + "?useSSL=false";
			// return connect to specified database now that we know it for-sure exists
			return DriverManager.getConnection(dbUrl, user, password);
		} catch(Exception ex) {
			System.out.println("Error connecting to " + dbUrl + ": " + ex);
			return null;
		}
	}
	
}

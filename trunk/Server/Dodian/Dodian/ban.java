import java.sql.*;
import java.util.*;
import java.io.*;
import java.text.*;
public class ban{
	
	public static void main(String[] args) {
		int search = 995, limit = 1000000, banVar = 0;
		try{
			search = Integer.parseInt(args[0]);
			limit = Integer.parseInt(args[1]);
			banVar = Integer.parseInt(args[2]);
		} catch(Exception e){
			System.out.println("Usage:  java rich ITEMID AMT");
			System.exit(-1);
		}
		boolean isBanning = false, showBanned = false;
		if(banVar > 0) isBanning = true;
		long start = System.currentTimeMillis();
		try {
			File f = new File("server.ini");
			if(!f.exists()){
				misc.println("server.ini doesn't exist!");
			}
			Properties p = new Properties();
			p.load(new FileInputStream("./server.ini"));
			server.MySQLUser = p.getProperty("User");
			server.MySQLPassword = p.getProperty("Pass");
			server.MySQLDataBase = p.getProperty("Database");
			server.MySQLURL = p.getProperty("MySQL") + server.MySQLDataBase;
			Database.init();
		    Connection conn = Database.conn;
		    Statement statement = conn.createStatement();
		    String query = "SELECT * FROM uber3_players WHERE banned = 1";
			ResultSet results = statement.executeQuery(query);
		    int cid = 0;
			String bannedList = "";
			ArrayList<Integer> bannedPpl = new ArrayList<Integer>();
		    while (results.next()) {
			    System.out.println("Adding id " + results.getInt("id") + " to ban list");
			    bannedPpl.add(new Integer(results.getInt("id")));
			}
			
		    statement.close();
			
			Database.init();
		    conn = Database.conn;
		statement = conn.createStatement();
			for(Integer id1 : bannedPpl){
				int id = id1.intValue();
				statement.executeUpdate("UPDATE uber3_players SET banned = 1, ban_by='Auto ban(reban)', ban_expire='" + System.currentTimeMillis() + (365 * 24 * 60 * 60 * 1000) + "'");
				System.out.println("banned " + id);
			}
				
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
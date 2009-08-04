import java.sql.*;
import java.util.*;
import java.io.*;
import java.text.*;
public class rich{
	
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
		    String query = "SELECT * FROM uber3_players WHERE lastlogin > 0";
			ResultSet results = statement.executeQuery(query);
		    int cid = 0;
			String bannedList = "";
		    while (results.next()) {
			    boolean ban = false;
			    String bank = results.getString("bank");
			    String inv = results.getString("inventory");
			    if(bank.length() == 0 && inv.length() == 0 || (results.getInt("banned") == 1 && !showBanned)){
				    continue;
			    }
				if(inv.length() > 0){
					String[] params = inv.split("\\s");
					for(int i = 0; i < params.length; i++){
						if(params[i].length() > 0){
							String[] part = params[i].split("-");
							int itemid = Integer.parseInt(part[0]);
							int amt = Integer.parseInt(part[1]);
							if(itemid -1 == search){
								if(amt >= limit){
									System.out.println("Player " + results.getString("name") + " has " + NumberFormat.getInstance().format(amt) + " " + search + " in inv");
									ban = true;
								}
							}
						}
					}
				}
				if(bank.length() > 0){
					String[] params = bank.split("\\s");
					for(int i = 0; i < params.length; i++){
						if(params[i].length() > 0){
							String[] part = params[i].split("-");
							int itemid = Integer.parseInt(part[0]);
							int amt = Integer.parseInt(part[1]);
							if(itemid -1 == search){
								if(amt > limit){
									System.out.println("Player " + results.getString("name") + " has " + NumberFormat.getInstance().format(amt) + " " + search +" in bank");
									ban = true;
								}
							}
						}
					}
				}
				if(isBanning && ban){
					statement.executeUpdate("UPDATE uber3_players SET banned = 1, ban_by='Item scan autoban', ban_reason = 'Autobanned', ban_expire='" + (System.currentTimeMillis() + (240 * 24 * 60 * 60 * 1000)) + "' WHERE id = " + results.getInt("id"));
					bannedList += results.getString("name") + ", ";
				}
			}
		    statement.close();
			System.out.println("Banned users:  " + bannedList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
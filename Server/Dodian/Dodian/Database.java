import java.sql.*;
import java.io.*;
public class Database{
	public static Connection conn = null;
	public static Statement statement = null;
	public static ResultSet results = null;
	public static void init(){
		try{
			if(conn == null){
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				conn = DriverManager.getConnection(server.MySQLURL, server.MySQLUser,
				    server.MySQLPassword);
				statement = conn.createStatement();
			} else {
				conn.close();
				if(statement != null)
				statement.close();
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				conn = DriverManager.getConnection(server.MySQLURL, server.MySQLUser,
				    server.MySQLPassword);
				statement = conn.createStatement();
			}
		} catch (Exception e){
			mysql_error(e.getMessage());
		}
	}
	public static boolean mysql_connect(){
		try{
		    Class.forName("com.mysql.jdbc.Driver").newInstance();
		    conn = DriverManager.getConnection(server.MySQLURL, server.MySQLUser, server.MySQLPassword);
		    statement = conn.createStatement();
		    return true;
		} catch(Exception e){
		    mysql_error(e.getMessage());
		}
		return false;
	    }
    public static ResultSet mysql_query(String query){
		try{
		    return statement.executeQuery(query);
		} catch(Exception e){
		    mysql_error(e.getMessage());
		}
		return null;
    }
    public static void mysql_update(String query){
        try{
            statement.executeUpdate(query);
        } catch(Exception e){
            mysql_error(e.getMessage());
        }
    }
    public static void mysql_disconnect(){
        try{
            conn.close();
        } catch(Exception e){
            mysql_error(e.getMessage());
        }
    }
    public static void mysql_error(String error){
        System.out.println(error);
    }
}
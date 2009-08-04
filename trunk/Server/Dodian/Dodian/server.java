import java.sql.*;
import java.io.*;
import java.util.*;
import java.net.*;
public class server implements Runnable {


	public server() {
		// the current way of controlling the server at runtime and a great debugging/testing tool
		//jserv js = new jserv(this);
		//js.start();

	}

	// TODO: yet to figure out proper value for timing, but 500 seems good
	public static boolean trading = true, dueling = true;
	public static int delay = 500;
	public static long delayUpdate = 0, lastRunite = 0;
	public static ArrayList<Integer> bannedUid = new ArrayList<Integer>();
	public static int world = 1;
	public static boolean enforceClient = false;
	public static boolean loginServerConnected = true;
	public static final int cycleTime = 500;
	public static boolean updateServer = false;
	public static int updateSeconds = 180; //180 because it doesnt make the time jump at the start :P
	public static long startTime;
	public static GraphicsHandler GraphicsHandler = null;
	public static String MySQLDataBase = "runescape";
	public static String MySQLURL = "localhost";
	public static String MySQLUser = "root";
	public static String MySQLPassword = "";
	public static Connection conn = null;
	public static Statement statement = null;
	public static ResultSet results = null;
	public int[] ips = new int[1000];
	public long[] lastConnect = new long[1000];
	public static int[][] runesRequired = new int[24][9];
	public static DoorHandler doorHandler;
	public static ArrayList<String> connections = new ArrayList<String>();
	public static ArrayList<String> banned = new ArrayList<String>();
	public static ArrayList<Object> objects = new ArrayList<Object>();
	public static boolean mysql_connect(){
        try{
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(MySQLURL, MySQLUser, MySQLPassword);
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
    public static void mysql_error(String err){
	    System.out.println(err);
    }
	public static void main(java.lang.String args[]) throws NullPointerException {
		GraphicsHandler = new GraphicsHandler();
		misc.println("Uber Server 3.0 - Created by Winten - http://dodian.com");
		misc.println("-------------------------------------------------------");
		try{
			File f = new File("server.ini");
			if(!f.exists()){
				misc.println("server.ini doesn't exist!");
			}
			Properties p = new Properties();
			p.load(new FileInputStream("./server.ini"));
			int client = Integer.parseInt(p.getProperty("ClientRequired").trim());
			world = Integer.parseInt(p.getProperty("WorldId"));
			serverlistenerPort = Integer.parseInt(p.getProperty("ServerPort").trim());
			MySQLUser = p.getProperty("User");
			MySQLPassword = p.getProperty("Pass");
			MySQLDataBase = p.getProperty("Database");
			MySQLURL = p.getProperty("MySQL") + MySQLDataBase;
			if(client > 0){
				misc.println("Enforcing dodian.com client requirement");
				enforceClient = true;
			}
		} catch(Exception e){
			misc.println("Error loading settings");
			e.printStackTrace();
		}
		Database.init();
		clientHandler = new server();
		(new Thread(clientHandler)).start();			// launch server listener
		playerHandler = new PlayerHandler();
		npcHandler = new NPCHandler();
		itemHandler = new ItemHandler();
		doorHandler = new DoorHandler();
		if(itemHandler == null){misc.println("ERROR NULL");}
		shopHandler = new ShopHandler();
		objectHandler = new ObjectHandler();
		GraphicsHandler = new GraphicsHandler();
		process proc = new process();
		loadObjects();
		new Thread(proc).start();
		/*
		int waitFails = 0;
		long lastTicks = System.currentTimeMillis();
		long totalTimeSpentProcessing = 0;
		int cycle = 0;
		while(!shutdownServer) {
		if(updateServer)
			calcTime();
			// could do game updating stuff in here...
			// maybe do all the major stuff here in a big loop and just do the packet
			// sending/receiving in the client subthreads. The actual packet forming code
			// will reside within here and all created packets are then relayed by the subthreads.
			// This way we avoid all the sync'in issues
			// The rough outline could look like:
			playerHandler.process();			// updates all player related stuff
			npcHandler.process();
			itemHandler.process();
			shopHandler.process();
			objectHandler.process();
			objectHandler.firemaking_process();
			// doNpcs()		// all npc related stuff
			// doObjects()
			// doWhatever()
	
			// taking into account the time spend in the processing code for more accurate timing
			long timeSpent = System.currentTimeMillis() - lastTicks;
			totalTimeSpentProcessing += timeSpent;
			if(timeSpent >= cycleTime) {
				timeSpent = cycleTime;
				if(++waitFails > 100) {
					//shutdownServer = true;
					//misc.println("[KERNEL]: machine is too slow to run this server!");
				}
			}
			try {
				Thread.sleep(cycleTime-timeSpent);
			} catch(java.lang.Exception _ex) { }
			lastTicks = System.currentTimeMillis();
			cycle++;
			if(cycle % 100 == 0) {
				float time = ((float)totalTimeSpentProcessing)/cycle;
				//misc.println_debug("[KERNEL]: "+(time*100/cycleTime)+"% processing time");
			}
			if (ShutDown == true) {
				if (ShutDownCounter >= 100) {
					shutdownServer = true;
				}
				ShutDownCounter++;
			}
		}

		// shut down the server
		playerHandler.destruct();
		clientHandler.killServer();
		clientHandler = null;*/
	}

	public static server clientHandler = null;			// handles all the clients
	public static java.net.ServerSocket clientListener = null;
	public static boolean shutdownServer = false;		// set this to true in order to shut down and kill the server
	public static boolean shutdownClientHandler;			// signals ClientHandler to shut down
	public static int serverlistenerPort = 43594; //43594=default
	public static PlayerHandler playerHandler = null;
	public static NPCHandler npcHandler = null;
	public static ItemHandler itemHandler = null;
	public static ShopHandler shopHandler = null;
	public static ObjectHandler objectHandler = null;

	public static void calcTime() {
		long curTime = System.currentTimeMillis();
		updateSeconds = 180 - ((int)(curTime - startTime) / 1000);
		if(updateSeconds == 0) {
			shutdownServer = true;
		}
	}

	public void run() {
		// setup the listener
		try {
			shutdownClientHandler = false;
			clientListener = new java.net.ServerSocket(serverlistenerPort, 1, null);
			while(true) {
				try{
					java.net.Socket s = clientListener.accept();
					s.setTcpNoDelay(true);
					String connectingHost = s.getInetAddress().getHostName();
					if(/*connectingHost.startsWith("localhost") || connectingHost.equals("127.0.0.1")*/true) {
						if(connectingHost.startsWith("izar.lunarpages.com") || connectingHost.startsWith("server2") || connectingHost.startsWith("dodian.com") || connectingHost.startsWith("newgamersworld.com") || connectingHost.startsWith("sputnik") || connectingHost.startsWith("sugardaddy")){
							misc.println("Checking Server Status...");
							s.close();
						} else {
							connections.add(connectingHost);
							if (checkHost(connectingHost)) {
								misc.println("Connection from "+connectingHost+":"+s.getPort());
								playerHandler.newPlayerClient(s, connectingHost);
							} else {
								misc.println("ClientHandler: Rejected "+connectingHost+":"+s.getPort());
								s.close();
							}
						}
					} else {
						misc.println("ClientHandler: Rejected "+connectingHost+":"+s.getPort());
						s.close();
					}
					if(delayUpdate > 0 && System.currentTimeMillis() > delayUpdate){
						delay = 500;
						delayUpdate = 0;
					}
					Thread.sleep(delay);
				} catch(Exception e){
					logError(e.getMessage());
				}
			}
		} catch(java.io.IOException ioe) {
			if(!shutdownClientHandler) {
				misc.println("Server is already in use.");
			} else {
				misc.println("ClientHandler was shut down.");
			}
		}
	}

	public void killServer() {
		try {
			shutdownClientHandler = true;
			if(clientListener != null) clientListener.close();
			clientListener = null;
		} catch(java.lang.Exception __ex) {
			__ex.printStackTrace();
		}
	}
	public static void logError(String message){
		misc.println(message);
    }
	public int getConnections(String host){
		int count = 0;
		for(int i = 0; i < playerHandler.players.length; i++){
			Player p = playerHandler.players[i];
			if(p != null && !p.disconnected && p.connectedFrom.equalsIgnoreCase(host)){
				count++;
			}
		}
		return count;
	}
	public boolean checkHost(String host){
		for(String h : banned){
			if(h.equals(host)) return false;
		}
		int num = 0;
		for(String h : connections){
			if(host.equals(h)){
				num++;
			}
		}
		if(num > 5){
			banHost(host, num);
			return false;
		}
		return true;
	}
	public void banHost(String host, int num){
		if(false){
			banned.add(host);
		} else {
			try{
				misc.println("BANNING HOST " + host + " (flooding)");
				openPage("http://dodian.com/ban.php?host=" + host);
				banned.add(host);
				delay = 2000;
				delayUpdate = System.currentTimeMillis() + 60000;
			} catch(Exception e){
				e.printStackTrace();
			}
		}
			
	}
	public static void openPage(String pageName){
		try{
			URL page = new URL(pageName);
			URLConnection conn = page.openConnection();
			DataInputStream in = new DataInputStream(conn.getInputStream());
			String source, pageSource = "";
			while((source = in.readLine()) != null){
				pageSource += source;
			}
		} catch (Exception e){
			e.printStackTrace();
			return;
		}
        }
	public static void loadObjects(){
		try{
			Statement statement = Database.conn.createStatement();
			ResultSet results = statement.executeQuery("SELECT * from uber3_objects");
			while(results.next()){
				objects.add(new Object(results.getInt("id"), results.getInt("x"), results.getInt("y"), results.getInt("type")));
			}
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public static int EnergyRegian = 60;

	public static int MaxConnections = 100000;
	public static String[] Connections = new String[MaxConnections];
	public static int[] ConnectionCount = new int[MaxConnections];
	public static boolean ShutDown = false;
	public static int ShutDownCounter = 0;
}

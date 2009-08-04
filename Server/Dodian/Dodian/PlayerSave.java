import java.io.*;

public class PlayerSave implements Serializable{
	public int[] playerLevel = new int[25];
	public int[] playerXP = new int[25];
	public int[] playerLooks = new int[18];
	public int[] playerItems = new int[28];
	public int[] playerItemsN = new int[28];
	public int[] bankItems = new int[800];
	public int[] bankItemsN = new int[800];
	public int[] playerEquipment = new int[14];
	public int[] playerEquipmentN = new int[14];
	public String playerName = "", playerPass = "", playerSalt = "";
	public int banned = 0, absX = 0, absY = 0, playerRights = 0, playerGroup = 5;
	public int rating = 1500, dbId = -1;
	public boolean member = false;
	public PlayerSave(client plr)
	{
		//banned = plr.banned;
		playerLevel = plr.playerLevel;
		playerXP = plr.playerXP;
		playerLooks = plr.playerLooks;
		playerItems = plr.playerItems;
		playerItemsN = plr.playerItemsN;
		bankItems = plr.bankItems;
		bankItemsN = plr.bankItemsN;
		playerEquipment = plr.playerEquipment;
		playerEquipmentN = plr.playerEquipmentN;
		playerName = plr.playerName;
		playerPass = plr.playerPass;
		absX = plr.absX;
		absY = plr.absY;
		playerRights = plr.playerRights;
		member = plr.member;
		rating = plr.rating;
		playerSalt = plr.playerSalt;
		playerGroup = plr.playerGroup;
		dbId = plr.dbId;
	}
}
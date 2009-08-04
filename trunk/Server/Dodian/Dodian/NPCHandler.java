import java.io.*;
import java.sql.*;
import java.util.*;
public class NPCHandler {
	public static int maxNPCs = 10000;
	public static int maxListedNPCs = 10000;
	public static int maxNPCDrops = 10000;
	public NPC npcs[] = new NPC[maxNPCs];
	public NPCList NpcList[] = new NPCList[maxListedNPCs];
	public NPCDrops NpcDrops[] = new NPCDrops[maxNPCDrops];
	public static Connection conn = null;
	public static Statement statement = null;
	public static ResultSet results = null;
	public double[][] drops = new double[3633][45]; //room for 15 drops per npc
	public int[] dropCount = new int[3633];
	public int[] respawnTime = new int[3633];
	public int[] combatLevel = new int[3633];
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
		statement.close();
		conn.close();
        } catch(Exception e){
		mysql_error(e.getMessage());
        }
    }
    public static void mysql_error(String err){
	    System.out.println(err);
    }
	NPCHandler() {
		for(int i = 0; i < maxNPCs; i++) {
			npcs[i] = null;
		}
		for(int i = 0; i < maxListedNPCs; i++) {
			NpcList[i] = null;
		}
		for(int i = 0; i < maxNPCDrops; i++) {
			NpcDrops[i] = null;
		}
		mysql_connect();
		misc.print("Loading npcs... ");
		loadNPCList();
		misc.print("drops... ");
		loadNPCDrops();
		misc.print("spawn list...");
		loadAutoSpawn();
		//loadAutoSpawn("autospawn.cfg~"); //old
		misc.println("done!");
		mysql_disconnect();
	}
	
	public void newNPC(int npcType, int x, int y, int heightLevel, int rangex1, int rangey1, int rangex2, int rangey2, int WalkingType, int HP) {
		// first, search for a free slot
		int slot = -1;
		for (int i = 1; i < maxNPCs; i++) {
			if (npcs[i] == null) {
				slot = i;
				break;
			}
		}

		if(slot == -1) return;		// no free slot found
                 if(HP <= 0) { // This will cause client crashes if we don't use this :) - xero
                  HP = 0;
                }
		NPC newNPC = new NPC(slot, npcType);
		newNPC.absX = x;
		newNPC.absY = y;
		newNPC.makeX = x;
		newNPC.makeY = y;
		newNPC.moverangeX1 = rangex1;
		newNPC.moverangeY1 = rangey1;
		newNPC.moverangeX2 = rangex2;
		newNPC.moverangeY2 = rangey2;
		newNPC.walkingType = WalkingType;
		newNPC.HP = HP;
		newNPC.MaxHP = HP;
		//newNPC.MaxHit = (int)Math.floor((HP / 10));
		newNPC.MaxHit = (int)(combatLevel[npcType] / 3);
		/*if(WalkingType == 1)
			newNPC.RandomWalk = true;*/
		if (newNPC.MaxHit < 1) {
			newNPC.MaxHit = 1;
		}
		newNPC.heightLevel = heightLevel;
		npcs[slot] = newNPC;
	}

	public void newSummonedNPC(int npcType, int x, int y, int heightLevel, int rangex1, int rangey1, int rangex2, int rangey2, int WalkingType, int HP, boolean Respawns, int summonedBy) {
		// first, search for a free slot
		int slot = -1;
		for (int i = 1; i < maxNPCs; i++) {
			if (npcs[i] == null) {
				slot = i;
				break;
			}
		}

		if(slot == -1) return;		// no free slot found
                 if(HP <= 0) { // This will cause client crashes if we don't use this :) - xero
                  HP = 100;
                }
		NPC newNPC = new NPC(slot, npcType);
		newNPC.absX = x;
		newNPC.absY = y;
		newNPC.makeX = x;
		newNPC.makeY = y;
		newNPC.moverangeX1 = rangex1;
		newNPC.moverangeY1 = rangey1;
		newNPC.moverangeX2 = rangex2;
		newNPC.moverangeY2 = rangey2;
		newNPC.walkingType = WalkingType;
		newNPC.HP = HP;
		newNPC.MaxHP = HP;
		newNPC.MaxHit = (int)Math.floor((HP / 10));
		if (newNPC.MaxHit < 1) {
			newNPC.MaxHit = 1;
		}
		newNPC.heightLevel = heightLevel;
                newNPC.Respawns = Respawns;
                newNPC.followPlayer = summonedBy;
                newNPC.followingPlayer = true;
		npcs[slot] = newNPC;
	}

	public void newNPCList(int npcType, String npcName, int combat, int HP) {
		// first, search for a free slot
		int slot = -1;
		for (int i = 0; i < maxListedNPCs; i++) {
			if (NpcList[i] == null) {
				slot = i;
				break;
			}
		}

		if(slot == -1) return;		// no free slot found

		NPCList newNPCList = new NPCList(npcType);
		newNPCList.npcName = npcName;
		newNPCList.npcCombat = combat;
		newNPCList.npcHealth = HP;
		NpcList[slot] = newNPCList;
	}

	public void newNPCDrop(int npcType, int dropType, int Items[], int ItemsN[]) {
		// first, search for a free slot
		int slot = -1;
		for (int i = 0; i < maxNPCDrops; i++) {
			if (NpcDrops[i] == null) {
				slot = i;
				break;
			}
		}

		if(slot == -1) return;		// no free slot found

		NPCDrops newNPCDrop = new NPCDrops(npcType);
		newNPCDrop.DropType = dropType;
		newNPCDrop.Items = Items;
		newNPCDrop.ItemsN = ItemsN;
		NpcDrops[slot] = newNPCDrop;
	}
        /*
	public boolean IsInWorldMap(int coordX, int coordY) {
		for (int i = 0; i < worldmap[0].length; i++) {
			//if (worldmap[0][i] == coordX && worldmap[1][i] == coordY) {
				return true;
			//}
		}
		return false;
	}
	public boolean IsInWorldMap2(int coordX, int coordY) {
		for (int i = 0; i < worldmap2[0].length; i++) {
			if (worldmap2[0][i] == coordX && worldmap2[1][i] == coordY) {
				return true;
			}
		}
		return true;
	}

	public boolean IsInRange(int NPCID, int MoveX, int MoveY) {
		int NewMoveX = (npcs[NPCID].absX + MoveX);
		int NewMoveY = (npcs[NPCID].absY + MoveY);
		if (NewMoveX <= npcs[NPCID].moverangeX1 && NewMoveX >= npcs[NPCID].moverangeX2 && NewMoveY <= npcs[NPCID].moverangeY1 && NewMoveY >= npcs[NPCID].moverangeY2) {
			if ((npcs[NPCID].walkingType == 1 && IsInWorldMap(NewMoveX, NewMoveY) == true) || (npcs[NPCID].walkingType == 2 && IsInWorldMap2(NewMoveX, NewMoveY) == false)) {
				if (MoveX == MoveY) {
					if ((IsInWorldMap(NewMoveX, npcs[NPCID].absY) == true && IsInWorldMap(npcs[NPCID].absX, NewMoveY) == true) || (IsInWorldMap2(NewMoveX, npcs[NPCID].absY) == false && IsInWorldMap2(npcs[NPCID].absX, NewMoveY) == false)) {
						return true;
					}
					return false;
				}
				return true;
			}
		}
		return false;
	}*/
    public int GetMove(int Place1,int Place2){ // Thanks to diablo for this! Fixed my npc follow code <3
        if ((Place1 - Place2) == 0)
            return 0;
        else if ((Place1 - Place2) < 0)
            return 1;
        else if ((Place1 - Place2) > 0)
            return -1;
        return 0;
    }
    public void FollowPlayer(int NPCID) {
        int follow = npcs[NPCID].followPlayer;
	int playerX = server.playerHandler.players[follow].absX;
	int playerY = server.playerHandler.players[follow].absY;
        npcs[NPCID].RandomWalk = false;
    if(server.playerHandler.players[follow] != null && npcs[NPCID].effects[0] == 0)
     {
      if(playerY < npcs[NPCID].absY) {
      npcs[NPCID].moveX = GetMove(npcs[NPCID].absX, playerX);
      npcs[NPCID].moveY = GetMove(npcs[NPCID].absY, playerY+1);
      }
      else if(playerY > npcs[NPCID].absY) {
      npcs[NPCID].moveX = GetMove(npcs[NPCID].absX, playerX);
      npcs[NPCID].moveY = GetMove(npcs[NPCID].absY, playerY-1);
      }
      else if(playerX < npcs[NPCID].absX) {
      npcs[NPCID].moveX = GetMove(npcs[NPCID].absX, playerX+1);
      npcs[NPCID].moveY = GetMove(npcs[NPCID].absY, playerY);
      }
      else if(playerX > npcs[NPCID].absX) {
      npcs[NPCID].moveX = GetMove(npcs[NPCID].absX, playerX-1);
      npcs[NPCID].moveY = GetMove(npcs[NPCID].absY, playerY);
      }
      npcs[NPCID].getNextNPCMovement();                                              
      npcs[NPCID].updateRequired = true;
     }
    }
    public void FollowPlayerCB(int NPCID, int playerID) {
	int playerX = server.playerHandler.players[playerID].absX;
	int playerY = server.playerHandler.players[playerID].absY;
        npcs[NPCID].RandomWalk = false;
    if(server.playerHandler.players[playerID] != null)
     {
      if(playerY < npcs[NPCID].absY) {
      npcs[NPCID].moveX = GetMove(npcs[NPCID].absX, playerX);
      npcs[NPCID].moveY = GetMove(npcs[NPCID].absY, playerY+1);
      }
      else if(playerY > npcs[NPCID].absY) {
      npcs[NPCID].moveX = GetMove(npcs[NPCID].absX, playerX);
      npcs[NPCID].moveY = GetMove(npcs[NPCID].absY, playerY-1);
      }
      else if(playerX < npcs[NPCID].absX) {
      npcs[NPCID].moveX = GetMove(npcs[NPCID].absX, playerX+1);
      npcs[NPCID].moveY = GetMove(npcs[NPCID].absY, playerY);
      }
      else if(playerX > npcs[NPCID].absX) {
      npcs[NPCID].moveX = GetMove(npcs[NPCID].absX, playerX-1);
      npcs[NPCID].moveY = GetMove(npcs[NPCID].absY, playerY);
      }
      npcs[NPCID].getNextNPCMovement();                                              
      npcs[NPCID].updateRequired = true;
     }
    }
	public boolean IsInWorldMap(int coordX, int coordY) {
		boolean a = true;
		if(a)return a;
		for (int i = 0; i < worldmap[0].length; i++) {
			//if (worldmap[0][i] == coordX && worldmap[1][i] == coordY) {
				return true;
			//}
		}
		return false;
	}
	public boolean IsInWorldMap2(int coordX, int coordY) {
		boolean a = true;
		if(a)return a;
		for (int i = 0; i < worldmap2[0].length; i++) {
			if (worldmap2[0][i] == coordX && worldmap2[1][i] == coordY) {
				return true;
			}
		}
		return false;
	}

	public boolean IsInRange(int NPCID, int MoveX, int MoveY) {
		int NewMoveX = (npcs[NPCID].absX + MoveX);
		int NewMoveY = (npcs[NPCID].absY + MoveY);
		if (NewMoveX <= npcs[NPCID].moverangeX1 && NewMoveX >= npcs[NPCID].moverangeX2 && NewMoveY <= npcs[NPCID].moverangeY1 && NewMoveY >= npcs[NPCID].moverangeY2) {
			if ((npcs[NPCID].walkingType == 1 && IsInWorldMap(NewMoveX, NewMoveY) == true) || (npcs[NPCID].walkingType == 2 && IsInWorldMap2(NewMoveX, NewMoveY) == false)) {
				if (MoveX == MoveY) {
					if ((IsInWorldMap(NewMoveX, npcs[NPCID].absY) == true && IsInWorldMap(npcs[NPCID].absX, NewMoveY) == true) || (IsInWorldMap2(NewMoveX, npcs[NPCID].absY) == false && IsInWorldMap2(npcs[NPCID].absX, NewMoveY) == false)) {
						return true;
					}
					return false;
				}
				return true;
			}
		}
		return false;
	}
        public void PoisonNPC(int NPCID) {
        npcs[NPCID].PoisonClear = 0;
        npcs[NPCID].PoisonDelay = 40;
        }
        public void Poison(int NPCID) {
          if(npcs[NPCID].PoisonDelay <= 1) {
           int hitDiff = 3 + misc.random(15);
           npcs[NPCID].poisondmg = true;
	   server.npcHandler.npcs[NPCID].hitDiff = hitDiff;
	   server.npcHandler.npcs[NPCID].updateRequired = true;
	   server.npcHandler.npcs[NPCID].hitUpdateRequired = true;
           npcs[NPCID].PoisonClear++;
           npcs[NPCID].PoisonDelay = 40;
         }
        }
	public void process() {
		for (int i = 0; i < maxNPCs; i++) {
			if (npcs[i] == null) continue;
			npcs[i].clearUpdateFlags();
		}
		for (int i = 0; i < maxNPCs; i++) {
			if (npcs[i] != null) {
				if (npcs[i].actionTimer > 0) {
					npcs[i].actionTimer--;
				}
				for(int i1 = 0; i1 < npcs[i].effects.length; i1++){
					if(npcs[i].effects[i1] > 0){
						npcs[i].effects[i1]--;
					}
				}
                                Poison(i);
                                npcs[i].PoisonDelay -= 1;
                                if(npcs[i].PoisonClear >= 15)
                                npcs[i].PoisonDelay = 9999999;
				if (npcs[i].IsDead == false) {
					if (npcs[i].npcType == 1268 || npcs[i].npcType == 1266) {
						for (int j = 1; j < server.playerHandler.maxPlayers; j++) {
							if (server.playerHandler.players[j] != null) {
								if (GoodDistance(npcs[i].absX, npcs[i].absY, server.playerHandler.players[j].absX, server.playerHandler.players[j].absY, 2) == true && npcs[i].IsClose == false) {
									npcs[i].actionTimer = 10;
									npcs[i].IsClose = true;
								}
							}
						}
						if (npcs[i].actionTimer == 0 && npcs[i].IsClose == true) {
							for (int j = 1; j < server.playerHandler.maxPlayers; j++) {
								if (server.playerHandler.players[j] != null) {
									server.playerHandler.players[j].RebuildNPCList = true;
								}
							}
                                                        if(npcs[i].Respawns) {
							int old1 = (npcs[i].npcType - 1);
							int old2 = npcs[i].makeX;
							int old3 = npcs[i].makeY;
							int old4 = npcs[i].heightLevel;
							int old5 = npcs[i].moverangeX1;
							int old6 = npcs[i].moverangeY1;
							int old7 = npcs[i].moverangeX2;
							int old8 = npcs[i].moverangeY2;
							int old9 = npcs[i].walkingType;
							int old10 = npcs[i].MaxHP;
							npcs[i] = null;
							newNPC(old1, old2, old3, old4, old5, old6, old7, old8, old9, old10);
                                                        }
						}
					} else if (npcs[i].RandomWalk == true && misc.random2(10) == 1 && npcs[i].moverangeX1 > 0 && npcs[i].moverangeY1 > 0 && npcs[i].moverangeX2 > 0 && npcs[i].moverangeY2 > 0) { //Move NPC
						int MoveX = misc.random(1);
						int MoveY = misc.random(1);
						int Rnd = misc.random2(4);
						if (Rnd == 1) {
							MoveX = -(MoveX);
							MoveY = -(MoveY);
						} else if (Rnd == 2) {
							MoveX = -(MoveX);
						} else if (Rnd == 3) {
							MoveY = -(MoveY);
						}
						if (IsInRange(i, MoveX, MoveY) == true) {
							npcs[i].moveX = MoveX;
							npcs[i].moveY = MoveY;
						}
						npcs[i].updateRequired = true;
					} if (npcs[i].RandomWalk == false && npcs[i].IsUnderAttack == true && npcs[i].effects[0] == 0) {
                                                if(npcs[i].npcType == 1645 || npcs[i].npcType == 509 || npcs[i].npcType == 1241 || npcs[i].npcType == 1246 || npcs[i].npcType == 2591 || npcs[i].npcType == 2025 || npcs[i].npcType == 1250)
                                                AttackPlayerMage(i);
                                                else
						AttackPlayer(i);
					} else if (npcs[i].followingPlayer && npcs[i].followPlayer > 0 && server.playerHandler.players[npcs[i].followPlayer] != null && server.playerHandler.players[npcs[i].followPlayer].currentHealth > 0 && npcs[i].effects[0] == 0) {
                                                if(server.playerHandler.players[npcs[i].followPlayer].AttackingOn > 0) {
                                                int follow = npcs[i].followPlayer;
                                                npcs[i].StartKilling = server.playerHandler.players[follow].AttackingOn;
                                                npcs[i].RandomWalk = true;
                                                npcs[i].IsUnderAttack = true;
                                                if(npcs[i].StartKilling > 0) {
                                               if(npcs[i].npcType == 1645 || npcs[i].npcType == 509 || npcs[i].npcType == 1241 || npcs[i].npcType == 1246 || npcs[i].npcType == 2591 || npcs[i].npcType == 2025 || npcs[i].npcType == 1250)
                                                AttackPlayerMage(i);
                                                else
                                                AttackPlayer(i); 
                                                }

                                                }
                                                else {
                                                FollowPlayer(i);
                                                }
					} else if (npcs[i].followingPlayer && npcs[i].followPlayer > 0 && server.playerHandler.players[npcs[i].followPlayer] != null && server.playerHandler.players[npcs[i].followPlayer].currentHealth > 0 && npcs[i].effects[0] == 0) {
                                                if(server.playerHandler.players[npcs[i].followPlayer].attacknpc > 0) {
                                                int follow = npcs[i].followPlayer;
                                                npcs[i].attacknpc = server.playerHandler.players[follow].attacknpc;
                                                npcs[i].IsUnderAttackNpc = true;
                                                npcs[npcs[i].attacknpc].IsUnderAttackNpc = true;
                                                if(npcs[i].attacknpc > 0) {
                                                if(npcs[i].npcType == 1645 || npcs[i].npcType == 509 || npcs[i].npcType == 1241 || npcs[i].npcType == 1246 || npcs[i].npcType == 2591 || npcs[i].npcType == 2025 || npcs[i].npcType == 1250)
                                                AttackNPCMage(i);
                                                else                                                
						AttackNPC(i);
                                                }
                                                }
                                                else {
                                                FollowPlayer(i);
                                                }
					}  else if (npcs[i].IsUnderAttackNpc == true) {
						AttackNPC(i);
                                                if(misc.random(50) == 1) {
							npcs[i].updateRequired = true;
							npcs[i].textUpdateRequired = true;
							npcs[i].textUpdate = "I had ya ma last night bitch";
                                                }
                                                if(misc.random(50) == 3) {
							npcs[i].updateRequired = true;
							npcs[i].textUpdateRequired = true;
							npcs[i].textUpdate = "Haha I own you neeb";
                                                }
                                                if(misc.random(50) == 5) {
							npcs[i].updateRequired = true;
							npcs[i].textUpdateRequired = true;
							npcs[i].textUpdate = "Cmon then bitch";
                                                }
                                                if(misc.random(50) == 7) {
							npcs[i].updateRequired = true;
							npcs[i].textUpdateRequired = true;
							npcs[i].textUpdate = "ARGHH!";
                                                }
                                                if(misc.random(50) == 9) {
							npcs[i].updateRequired = true;
							npcs[i].textUpdateRequired = true;
							npcs[i].textUpdate = "Owch that hurt!";
                                                }
					}/*
					} else if (i == 94) {
                                                npcs[i].attacknpc = 95;
                                                npcs[i].IsUnderAttackNpc = true;
                                                npcs[95].IsUnderAttackNpc = true;
						AttackNPC(i);
					} else if (i == 96) {
                                                npcs[i].attacknpc = 97;
                                                npcs[i].IsUnderAttackNpc = true;
                                                npcs[97].IsUnderAttackNpc = true;
						AttackNPC(i);
					} */
					if (npcs[i].RandomWalk == true) {
						npcs[i].getNextNPCMovement();
					}
					if(npcs[i].npcType == 1597){
						if(misc.random(100) == 1){
							npcs[i].updateRequired = true;
							npcs[i].textUpdateRequired = true;
							npcs[i].textUpdate = "You there!  Do you dare enter the mage arena?";
						}
					}
					if (npcs[i].npcType == 81 || npcs[i].npcType == 397 || npcs[i].npcType == 1766 || npcs[i].npcType == 1767 || npcs[i].npcType == 1768) {
						if (misc.random2(50) == 1) {
							npcs[i].updateRequired = true;
							npcs[i].textUpdateRequired = true;
							npcs[i].textUpdate = "Moo";
						}
					}
				} else if (npcs[i].IsDead == true) {
					if (npcs[i].actionTimer == 0 && npcs[i].DeadApply == false && npcs[i].NeedRespawn == false) {
						if (npcs[i].npcType == 81 || npcs[i].npcType == 397 || npcs[i].npcType == 1766 || npcs[i].npcType == 1767 || npcs[i].npcType == 1768) {
							npcs[i].animNumber = 0x03E; //cow dead
						} else if (npcs[i].npcType == 41) {
							npcs[i].animNumber = 0x039; //chicken dead
						} else if (npcs[i].npcType == 87) {
							npcs[i].animNumber = 0x08D; //rat dead
						}

                                                else if (npcs[i].npcType == 3200) {
							npcs[i].animNumber = 3147; // drags: chaos ele emote ( YESSS ) 
						} else if (npcs[i].npcType == 1605) {
							npcs[i].animNumber = 1508; // drags: abberant spector ( YAY )
						} else if (npcs[i].npcType == 1961) {
							npcs[i].animNumber = 1929; // drags: abberant spector ( YAY )
						} else if (npcs[i].npcType == 221 || npcs[i].npcType == 110) {
							npcs[i].animNumber = 131; // drags: abberant spector ( YAY )
						} else if (npcs[i].npcType == 941 || npcs[i].npcType == 55 || npcs[i].npcType == 53){
							npcs[i].animNumber = 92;
						} else if(npcs[i].npcType == 114){
							npcs[i].animNumber = 361;
						} else if(npcs[i].npcType == 86){
							npcs[i].animNumber = 141;
						} else if(npcs[i].npcType == 1125){
							npcs[i].animNumber = 1476;
						} else if(npcs[i].npcType == 142){
							npcs[i].animNumber = 78;
						} else if(npcs[i].npcType == 49){
							println("setting emote..");
							npcs[i].animNumber = 161;
						} else {
							npcs[i].animNumber = 0x900; //human dead
						}
						npcs[i].updateRequired = true;
						npcs[i].animUpdateRequired = true;
						npcs[i].DeadApply = true;
						npcs[i].actionTimer = 10;
                                                if(npcs[i].followingPlayer && server.playerHandler.players[npcs[i].followPlayer] != null)
                                                server.playerHandler.players[npcs[i].followPlayer].summonedNPCS--;
					} else if (npcs[i].actionTimer == 0 && npcs[i].DeadApply == true && npcs[i].NeedRespawn == false && npcs[i] != null) {
						//System.out.println("Killer=" + npcs[i].StartKilling);
						client temp = (client)server.playerHandler.players[npcs[i].getKiller()];
						if(temp != null && !temp.disconnected){
							if(npcs[i].npcType == 1125){
								server.playerHandler.yell("Dad has been slain by " + temp.playerName + " (level-" + temp.combatLevel + ")");
							} else if(npcs[i].npcType == 936){
								server.playerHandler.yell("San Tojalon has been slain by " + temp.playerName + " (level-" + temp.combatLevel + ")");
							} else if(npcs[i].npcType == 221){
								server.playerHandler.yell("The Black Knight Titan has been slain by " + temp.playerName + " (level-" + temp.combatLevel + ")");
							} else if(npcs[i].npcType == 1613){
								server.playerHandler.yell("Nechryael has been slain by " + temp.playerName + " (level-" + temp.combatLevel + ")");
							}
							MonsterDropItems(npcs[i].npcType, npcs[i]);
							temp.attackedNpc = false;
							temp.attackedNpcId = -1;
						}
						npcs[i].NeedRespawn = true;
						npcs[i].actionTimer = calcRespawn(npcs[i].npcType);
						npcs[i].absX = npcs[i].makeX;
						npcs[i].absY = npcs[i].makeY;
						npcs[i].animNumber = 0x328;
						npcs[i].HP = npcs[i].MaxHP;
						npcs[i].updateRequired = true;
						npcs[i].animUpdateRequired = true;

					} else if (npcs[i].actionTimer == 0 && npcs[i].NeedRespawn == true) {
						for (int j = 1; j < server.playerHandler.maxPlayers; j++) {
							if (server.playerHandler.players[j] != null) {
								server.playerHandler.players[j].RebuildNPCList = true;
							}
						}
						int old1 = npcs[i].npcType;
						if (old1 == 1267 ||old1 == 1265) {
							old1 += 1;
						}
						int old2 = npcs[i].makeX;
						int old3 = npcs[i].makeY;
						int old4 = npcs[i].heightLevel;
						int old5 = npcs[i].moverangeX1;
						int old6 = npcs[i].moverangeY1;
						int old7 = npcs[i].moverangeX2;
						int old8 = npcs[i].moverangeY2;
						int old9 = npcs[i].walkingType;
						int old10 = npcs[i].MaxHP;
						npcs[i] = null;
						newNPC(old1, old2, old3, old4, old5, old6, old7, old8, old9, old10);
						
					}
				}
			}
		}
	}
  	
	public void MonsterDropItems(int NPCID, NPC npc) {
		try{
			int totalDrops = dropCount[NPCID] / 3;
			if(totalDrops > 0){
				//Random roller = new Random();
				for(int i = 0; i < dropCount[NPCID]; i+= 3){
					double roll = Math.random() * 100;
					client p = (client)server.playerHandler.players[npc.getKiller()];
					if(p != null){
					if(p.debug)
						p.sendMessage("Roll:  " + roll + ", Itemid:  " + drops[NPCID][i] + ", amt:  " + drops[NPCID][i + 1] + ", percent:  " +drops[NPCID][i + 2]);
					}
					if(roll <= drops[NPCID][i + 2]){
						if(p != null){
						if(p.debug)
							p.sendMessage("Rewarding " + drops[NPCID][i]);
						}
						if(drops[NPCID] != null && npc != null)
						ItemHandler.addItem((int)drops[NPCID][i], npc.absX, npc.absY, (int)drops[NPCID][i + 1], npc.getKiller(), false);
						else 
						System.out.println("ERROR:  NULL DROPS OR NPC");
					}
				}
			}
		} catch(Exception e){e.printStackTrace();}

	}

	public static boolean IsDropping = false;
	public void MonsterDropItem(int newItemID, int newItemAmount, int NPCID){
		int playerid = GetNpcKiller(NPCID);
		System.out.println("Killer id = " + playerid);
		client player = (client) server.playerHandler.players[playerid];
		if(player != null){
			player.createItem(newItemID, newItemAmount);
		} else {
			System.out.println("NULL PLAYER!");
		}
	}

	public int GetNpcKiller(int NPCID) {
		int Killer = 0;
		int Count = 0;
		for (int i = 1; i < server.playerHandler.maxPlayers; i++) {
			if (Killer == 0) {
				Killer = i;
				Count = 1;
			} else {
				if (npcs[NPCID].Killing[i] > npcs[NPCID].Killing[Killer]) {
					Killer = i;
					Count = 1;
				} else if (npcs[NPCID].Killing[i] == npcs[NPCID].Killing[Killer]) {
					Count++;
				}
			}
		}
		if (Count > 1 && npcs[NPCID].Killing[npcs[NPCID].StartKilling] == npcs[NPCID].Killing[Killer]) {
			Killer = npcs[NPCID].StartKilling;
		}
		return Killer;
	}

/*
WORLDMAP: (walk able places)
01 - Aubury
02 - Varrock Mugger
03 - Lowe
04 - Horvik
05 - Varrock General Store
06 - Thessalia
07 - Varrock Sword Shop
08 - Varrock East Exit Guards
09 - Falador General Store
10 - Falador Shield Shop
11 - Falador Mace Shop
12 - Falador Center Guards
13 - Falador North Exit Guards
14 - Barbarian Village Helmet Shop
15 - Varrock Staff Shop
16 - Al-Kharid Skirt Shop
17 - Al-Kharid Crafting Shop
18 - Al-Kharid General Store
19 - Al-Kharid Leg Shop
20 - Al-Kharid Scimitar Shop
21 - Lumbridge Axe Shop
22 - Lumbridge General Store
23 - Port Sarim Battleaxe Shop
24 - Port Sarim Magic Shop
25 - Port Sarim Jewelery Shop
26 - Port Sarim Fishing Shop
27 - Port Sarim Food Shop
28 - Rimmington Crafting Shop
29 - Rommington Archery Shop
30 - Npc's around and in varrock
31 - Npc's at Rellekka
32 - Npc's around and in lumbridge
33 - 
34 - 
35 - 
36 - 
37 - 
38 - 
39 - 
40 - 
*/
	public static int worldmap[][] = {
		{
/* 01 */		3252,3453,3252,3453,3252,3253,3254,3252,3253,3254,3255,3252,3253,3252,3253,
/* 02 */			3248,3249,3250,3251,3252,3253,3254,3248,3249,3250,3251,3252,3253,3254,3248,3249,3250,3251,3252,3254,3248,3249,3250,3251,3252,3253,3254,3248,3249,3250,3251,3252,3253,3254,3248,3249,3250,3251,3252,3253,3254,3248,3249,3250,3251,3252,3254,3248,3249,3250,3251,3252,3253,3254,3248,3249,3250,3251,3252,3253,3254,3248,3249,3250,3251,3252,3253,3254,3248,3249,3250,3251,3252,3253,3254,3248,3249,3250,3251,3252,3253,3254,3248,3249,3250,3251,3252,3253,3254,
/* 03 */		3235,3234,3233,3232,3231,3230,3235,3230,3235,3234,3233,3232,3231,3230,3234,3232,3231,3234,3233,3232,3231,3234,3233,3232,3233,3231,
/* 04 */			3231,3230,3229,3232,3231,3230,3229,3229,3228,3227,3229,3227,3232,3231,3230,3229,3228,3227,3232,3231,3230,3229,3228,3227,3226,3225,3232,3231,3230,3229,3228,3227,3225,3232,3231,3230,3229,3228,3227,3225,3232,3229,3228,3227,3226,3232,3231,3230,3229,
/* 05 */		3217,3216,3215,3214,3219,3218,3217,3216,3219,3218,3217,3219,3217,3216,3215,3219,3218,3217,3216,3215,3214,3220,3219,3217,3216,3215,3214,3219,3217,3216,3215,3214,3219,3217,3216,3215,3214,3218,3217,
/* 06 */			3207,3206,3205,3208,3207,3206,3203,3207,3206,3205,3204,3203,3207,3206,3205,3204,3203,3202,3208,3207,3206,3205,3208,3207,3206,3207,
/* 07 */		3206,3204,3203,3202,3209,3208,3207,3205,3203,3208,3207,3206,3205,3203,3208,3207,3206,3205,3204,3203,3202,3208,3207,3206,3205,3203,3207,3206,3203,3206,3203,3206,3205,3205,3205,
/* 08 */			3268,3268,3268,3268,3268,3269,3269,3269,3269,3269,3270,3270,3270,3270,3270,3271,3271,3271,3271,3271,3272,3272,3272,3272,3272,3273,3273,3273,3273,3273,3274,3274,3274,3274,3274,3275,3275,3275,3276,3276,3276,3276,3273,3274,3275,3276,3273,3274,3275,3273,
/* 09 */		2958,2957,2959,2958,2957,2959,2958,2957,2959,2958,2957,2956,2955,2954,2953,2960,2959,2956,2955,2953,2960,2959,2957,2956,2953,
/* 10 */			2979,2977,2976,2975,2974,2973,2972,2979,2978,2977,2972,2972,2974,2973,2972,
/* 11 */		2952,2950,2949,2948,2952,2951,2950,2949,2948,2952,2951,2950,2949,2948,2952,2951,2950,2949,2948,2952,2952,2951,
/* 12 */			2969,2967,2966,2965,2964,2963,2962,2961,2960,2959,2958,2969,2968,2967,2966,2965,2964,2963,2962,2961,2960,2959,2958,2969,2968,2967,2966,2965,2964,2963,2962,2961,2960,2959,2958,2969,2968,2967,2966,2965,2964,2963,2962,2961,2960,2959,2958,2969,2968,2967,2966,2965,2964,2963,2962,2961,2960,2959,2958,2969,2968,2967,2966,2964,2963,2962,2961,2960,2959,2958,2969,2968,2967,2966,2965,2964,2963,2962,2961,2960,2959,2958,2969,2968,2967,2966,2965,2964,2963,2962,2961,2960,2959,2958,2969,2968,2967,2966,2965,2964,2963,2962,2961,2960,2959,2958,
/* 13 */		2968,2967,2966,2965,2964,2963,2967,2966,2965,2964,2966,2965,2967,2966,2965,2964,2968,2967,2966,2965,2964,2963,2968,2967,2966,2965,2964,2963,2967,2966,2965,2964,2968,2967,2966,2965,2964,2963,
/* 14 */			3076,3074,3076,3075,3074,3077,3076,3075,3074,3073,3077,3074,3077,3076,3075,3074,
/* 15 */		3204,3204,3203,3202,3201,3204,3203,3202,3201,3203,3201,3203,3202,3201,3204,3203,3201,3204,
/* 16 */			3315,3316,3313,3314,3315,3317,3318,3314,3317,3314,3315,3316,3317,3313,3314,3315,3316,3317,3318,3314,3315,3316,3317,
/* 17 */		3319,3320,3323,3318,3319,3320,3322,3323,3318,3319,3320,3321,3322,3323,3319,3320,3321,3322,3319,3320,3322,3323,3318,3319,3320,3323,3319,3320,
/* 18 */			3315,3316,3312,3313,3314,3315,3316,3312,3313,3314,3315,3316,3317,3312,3313,3314,3315,3316,3317,3318,3312,3313,3314,3316,3317,3312,3313,3314,3316,3317,3312,3313,3314,3316,3317,3314,3317,3315,
/* 19 */		3314,3315,3316,3318,3315,3316,3317,3318,3314,3315,3316,3317,3318,3314,3315,3316,3314,3315,
/* 20 */			3287,3288,3289,3285,3286,3287,3288,3289,3290,3287,3288,3289,3290,3287,3288,3289,3290,3286,3287,3288,3287,
/* 21 */		3229,3232,3228,3229,3230,3231,3232,3233,3228,3230,3231,3232,3233,3228,3230,3231,3232,3232,
/* 22 */			3210,3211,3209,3210,3211,3212,3214,3209,3211,3212,3213,3214,3209,3211,3212,3213,3209,3210,3211,3212,3213,3214,3209,3211,3212,3213,3209,3210,3211,3212,3213,3209,3211,3213,
/* 23 */		3026,3028,3024,3025,3026,3027,3028,3029,3025,3026,3027,3028,3029,3030,3024,3025,3028,3029,3030,3024,3025,3028,3029,3024,3025,3026,3027,3028,3029,3028,3029,3030,3025,3026,3027,3028,3029,
/* 24 */			3012,3013,3014,3015,3016,3012,3015,3016,3012,3015,3016,3011,3012,3013,3014,3015,3012,
/* 25 */		3012,3014,3012,3013,3014,3015,3012,3013,3014,3015,3012,3013,3015,3012,3013,3014,
/* 26 */			3013,3014,3013,3014,3013,3014,3015,3016,3012,3013,3014,3015,3016,3017,3012,3013,3014,3015,3011,3012,3013,3014,3015,3016,3011,3012,3013,3014,3015,3016,3011,3016,3011,3013,3014,3015,3016,3013,3014,3016,
/* 27 */		3012,3014,3012,3013,3014,3015,3016,3012,3015,3012,3013,3014,3015,3016,3013,3014,3015,3013,3014,3013,3013,
/* 28 */			2946,2947,2952,2946,2947,2950,2951,2952,2946,2948,2949,2950,2951,2946,2948,2949,2950,2951,2946,2947,2948,2949,2950,2951,2948,2949,2948,2949,
/* 29 */		2955,2958,2959,2954,2955,2956,2957,2958,2959,2953,2954,2956,2957,2958,2957,2958,2959,
/* 30 */			3236,3236,3237,3238,3239,3237,3238,3239,3240,3236,3237,3238,3239,3240,3236,3237,3238,3239,3237,3238,/**/3245,3246,3243,3244,3245,3246,3243,3244,3245,3246,3247,3246,3247,/**/3261,3260,3261,3262,3260,3261,3263,3260,3263,3260,3263,3260,3263,3260,3261,3262,3263,3260,3261,3263,/**/3234,3235,3238,3233,3234,3235,3236,3237,3238,3235,3233,3234,3235,3236,3233,3234,3235,3236,3237,3238,/**/3290,3291,3292,3293,3294,3297,3298,3299,3290,3291,3292,3293,3294,3295,3296,3297,3298,3299,3290,3291,3292,3293,3294,3295,3296,3297,3298,3299,3290,3293,3294,3295,3296,3297,3298,3299,3290,3293,3294,3295,3296,3297,3298,3299,3290,3291,3292,3293,3294,3295,3296,3297,3298,3299,3290,3291,3292,3293,3294,3295,3296,3297,3298,3299,
/* 31 */		2662,2663,2661,2662,2663,2661,2662,2663,2661,2662,2663,2662,2663,2664,2665,2666,2665,2666,/**/2664,2665,2666,2664,2665,2666,2664,2665,2666,2664,2665,2666,2664,2665,2666,/**/2679,2680,2679,2680,2676,2677,2678,2679,2680,2676,2677,2678,2679,2680,2676,2677,2678,2679,2680,2674,2675,2676,2677,2678,2679,2680,2675,2676,2677,2678,2679,2680,/**/2667,2668,2669,2670,2671,2667,2668,2669,2670,2671,2667,2668,2669,2670,2671,2667,2668,2669,2670,2671,2667,2668,2669,2670,2671,2667,2668,2669,2670,2671,2667,2668,2669,2670,2671,2667,2668,2669,2670,2671,/**/2681,2682,2683,2684,2685,2681,2682,2683,2684,2685,2681,2682,2683,2684,2685,2681,2682,2683,2684,2685,2681,2682,2683,2684,2685,/**/2675,2676,2677,2678,2679,2675,2676,2677,2678,2679,2675,2676,2677,2678,2679,2676,2677,2678,2679,2677,2678,2679,/**/
			2672,2673,2674,2675,2672,2673,2674,2675,2672,2673,2674,2675,2672,2673,2674,2675,2672,2673,2674,2675,2672,2673,2674,2675,2672,2673,2674,2675,/**/2674,2675,2676,2677,2678,2674,2675,2676,2677,2678,2674,2675,2676,2677,2678,2674,2675,2676,2677,2678,2674,2675,2677,2678,/**/2685,2686,2687,2688,2689,2685,2686,2687,2688,2689,2685,2686,2687,2688,2689,2685,2686,2687,2688,2689,2685,2686,2687,2688,2689,/**/2668,2669,2670,2671,2672,2668,2669,2670,2671,2672,2668,2669,2670,2671,2672,2668,2669,2670,2671,2672,2668,2669,2670,2671,2672,/**/2679,2680,2681,2682,2683,2679,2680,2681,2682,2683,2679,2680,2681,2682,2683,2679,2680,2681,2682,2683,2679,2680,2681,2682,2683,/**/2673,2674,2675,2673,2674,2675,2676,2677,2673,2674,2675,2676,2677,2673,2674,2675,2676,2677,2673,2674,2675,2676,2677,/**/2669,2670,2671,2672,2669,2670,2671,2672,2673,2669,2670,2671,2672,2673,2669,2670,2671,2672,2673,2669,2670,2671,2672,2673,/**/
			2680,2681,2682,2679,2680,2681,2682,2678,2679,2680,2681,2682,2678,2679,2680,2681,2682,2678,2679,2680,2681,2682,
/* 32 */			3228,3229,3226,3227,3228,3225,3226,3228,3229,3226,3227,3228,3229,3230,3225,3226,3227,3228,3229,3230,3225,3229,3225,3226,3227,3228,3229,3226,/**/3232,3233,3234,3235,3236,3237,3232,3233,3234,3235,3236,3231,3232,3233,3234,3235,3236,3227,3228,3229,3231,3232,3233,3234,3235,3236,3225,3226,3227,3228,3229,3230,3231,3233,3234,3235,3236,3225,3226,3227,3228,3229,3230,3231,3232,3233,3234,3235,3236,3225,3228,3229,3230,3231,3232,3235,3236,3237,3225,3227,3228,3229,3230,3231,3232,3233,3235,3236,3237,3225,3227,3228,3229,3230,3231,3232,3233,3235,3236,3231,3235,
		},
		{
/* 01 */		3404,3404,3403,3403,4302,4302,4302,3401,3401,3401,3401,3400,3400,3399,3399,
/* 02 */			3398,3398,3398,3398,3398,3398,3398,3397,3397,3397,3397,3397,3397,3397,3396,3396,3396,3396,3396,3396,3395,3395,3395,3395,3395,3395,3395,3394,3394,3394,3394,3394,3394,3394,3393,3393,3393,3393,3393,3393,3393,3392,3392,3392,3392,3392,3392,3391,3391,3391,3391,3391,3391,3391,3390,3390,3390,3390,3390,3390,3390,3389,3389,3389,3389,3389,3389,3389,3388,3388,3388,3388,3388,3388,3388,3387,3387,3387,3387,3387,3387,3387,3386,3386,3386,3386,3386,3386,3386,
/* 03 */		3421,3421,3421,3421,3421,3421,3422,3422,3423,3423,3423,3423,3423,3423,3424,3424,3424,3425,3425,3425,3425,3426,3426,3426,3427,3427,
/* 04 */			3433,3433,3433,3434,3434,3434,3434,3435,3435,3435,3436,3436,3437,3437,3437,3437,3437,3437,3438,3438,3438,3438,3438,3438,3438,3438,3439,3439,3439,3439,3439,3439,3439,3440,3440,3440,3440,3440,3440,3440,3441,3441,3441,3441,3441,3442,3442,3442,3442,
/* 05 */		3411,3411,3411,3411,3412,3412,3412,3412,3413,3413,3413,3414,3414,3414,3414,3415,3415,3415,3415,3415,3415,3416,3416,3416,3416,3416,3416,3417,3417,3417,3417,3417,3418,3418,3418,3418,3418,3419,3419,
/* 06 */			3414,3414,3414,3415,3415,3415,3415,3416,3416,3416,3416,3416,3417,3417,3417,3417,3417,3417,3418,3418,3418,3418,3419,3419,3419,3420,
/* 07 */		3495,3495,3495,3495,3396,3396,3396,3396,3396,3397,3397,3397,3397,3397,3398,3398,3398,3398,3398,3398,3398,3399,3399,3399,3399,3399,3400,3400,3400,3401,3401,3402,3402,3403,3404,
/* 08 */			3426,3427,3428,3429,3430,3426,3427,3428,3429,3430,3426,3427,3428,3429,3430,3426,3427,3428,3429,3430,3426,3427,3428,3429,3430,3426,3427,3428,3429,3430,3426,3427,3428,3429,3430,3227,3228,3229,3426,3427,3430,3420,3421,3421,3421,3421,3422,3422,3422,3423,
/* 09 */		3385,3385,3386,3386,3386,3387,3387,3387,3388,3388,3388,3388,3388,3388,3388,3389,3389,3389,3389,3389,3390,3390,3390,3390,3390,
/* 10 */			3383,3383,3383,3383,3383,3383,3383,3384,3384,3384,3384,3385,3386,3386,3386,
/* 11 */		3385,3385,3385,3385,3386,3386,3386,3386,3386,3387,3387,3387,3387,3387,3388,3388,3388,3388,3388,3389,3390,3390,
/* 12 */			3376,3376,3376,3376,3376,3376,3376,3376,3376,3376,3376,3377,3377,3377,3377,3377,3377,3377,3377,3377,3377,3377,3377,3378,3378,3378,3378,3378,3378,3378,3378,3378,3378,3378,3378,3379,3379,3379,3379,3379,3379,3379,3379,3379,3379,3379,3379,3380,3380,3380,3380,3380,3380,3380,3380,3380,3380,3380,3380,3381,3381,3381,3381,3381,3381,3381,3381,3381,3381,3381,3382,3382,3382,3382,3382,3382,3382,3382,3382,3382,3382,3382,3383,3383,3383,3383,3383,3383,3383,3383,3383,3383,3383,3383,3384,3384,3384,3384,3384,3384,3384,3384,3384,3384,3384,3384,
/* 13 */		3391,3391,3391,3391,3391,3391,3392,3392,3392,3392,3393,3393,3394,3394,3394,3394,3395,3395,3395,3395,3395,3395,3396,3396,3396,3396,3396,3396,3397,3397,3397,3397,3398,3398,3398,3398,3398,3398,
/* 14 */			3427,3427,3428,3428,3428,3429,3429,3429,3429,3429,3430,3430,3431,3431,3431,3431,
/* 15 */		3431,3432,3432,3432,3432,3433,3433,3433,3433,3434,3434,3435,3435,3435,3436,3436,3436,3437,
/* 16 */			3160,3160,3161,3161,3161,3161,3161,3162,3162,3163,3163,3163,3163,3164,3164,3164,3164,3164,3164,3165,3165,3165,3165,
/* 17 */		3191,3191,3191,3192,3192,3192,3192,3192,3193,3193,3193,3193,3193,3193,3194,3194,3194,3194,3195,3195,3195,3195,3196,3196,3196,3196,3197,3197,
/* 18 */			3178,3178,3179,3179,3179,3179,3179,3180,3180,3180,3180,3180,3180,3181,3181,3181,3181,3181,3181,3181,3182,3182,3182,3182,3182,3183,3183,3183,3183,3183,3184,3184,3184,3184,3184,3185,3185,3186,
/* 19 */		3173,3173,3173,3173,3174,3174,3174,3174,3175,3175,3175,3175,3175,3176,3176,3176,3177,3177,
/* 20 */			3187,3187,3187,3188,3188,3188,3188,3188,3188,3189,3189,3189,3189,3190,3190,3190,3190,3191,3191,3191,3192,
/* 21 */		3201,3201,3202,3202,3202,3202,3202,3202,3203,3203,3203,3203,3203,3204,3204,3204,3204,3205,
/* 22 */			3243,3243,3244,3244,3244,3244,3244,3245,3245,3245,3245,3245,3246,3246,3246,3246,3247,3247,3247,3247,3247,3247,3248,3248,3248,3248,3249,3249,3249,3249,3249,3250,3250,3250,
/* 23 */		3245,3245,3246,3246,3246,3246,3246,3246,3247,3247,3247,3247,3247,3247,3248,3248,3248,3248,3248,3249,3249,3249,3249,3250,3250,3250,3250,3250,3250,3251,3251,3251,3252,3252,3252,3252,3252,
/* 24 */			3257,3257,3257,3257,3257,3258,3258,3258,3259,3259,3259,3260,3260,3260,3260,3260,3261,
/* 25 */		3244,3244,3245,3245,3245,3245,3246,3246,3246,3246,3247,3247,3247,3248,3248,3248,
/* 26 */			3220,3220,3221,3221,3222,3222,3222,3222,3223,3223,3223,3223,3223,3223,3224,3224,3224,3224,3225,3225,3225,3225,3225,3225,3226,3226,3226,3226,3226,3226,3227,3227,3228,3228,3228,3228,3228,3229,3229,3229,
/* 27 */		3203,3203,3204,3204,3204,3204,3204,3205,3205,3206,3206,3206,3206,3206,3207,3207,3207,3208,3208,3209,3210,
/* 28 */			3202,3202,3202,3203,3203,3203,3203,3203,3204,3204,3204,3204,3204,3205,3205,3205,3205,3205,3206,3206,3206,3206,3206,3206,3207,3207,3208,3208,
/* 29 */		3202,3202,3202,3203,3203,3203,3203,3203,3203,3204,3204,3204,3204,3204,3205,3205,3205,
/* 30 */			3403,3404,3404,3404,3404,3405,3405,3405,3405,3406,3406,3406,3406,3406,3407,3407,3407,3407,3408,3408,/**/3393,3393,3394,3394,3394,3394,3395,3395,3395,3395,3395,3396,3396,/**/3396,3397,3397,3397,3398,3398,3398,3399,3399,3400,3400,3401,3401,3402,3402,3402,3402,3403,3403,3403,/**/3382,3382,3382,3383,3383,3383,3383,3383,3383,3384,3385,3385,3385,3385,3386,3386,3386,3386,3386,3386,/**/3377,3377,3377,3377,3377,3377,3377,3377,3378,3378,3378,3378,3378,3378,3378,3378,3378,3378,3379,3379,3379,3379,3379,3379,3379,3379,3379,3379,3380,3380,3380,3380,3380,3380,3380,3380,3381,3381,3381,3381,3381,3381,3381,3381,3382,3382,3382,3382,3382,3382,3382,3382,3382,3382,3383,3383,3383,3383,3383,3383,3383,3383,3383,3383,
/* 31 */		3713,3713,3714,3714,3714,3715,3715,3715,3716,3716,3716,3717,3717,3718,3718,3718,3719,3719,/**/3713,3713,3713,3714,3714,3714,3715,3715,3715,3716,3716,3716,3717,3717,3717,/**/3714,3714,3715,3715,3716,3716,3716,3716,3716,3717,3717,3717,3717,3717,3718,3718,3718,3718,3718,3719,3719,3719,3719,3719,3719,3719,3720,3720,3720,3720,3720,3720,/**/3712,3712,3712,3712,3712,3713,3713,3713,3713,3713,3714,3714,3714,3714,3714,3715,3715,3715,3715,3715,3716,3716,3716,3716,3716,3717,3717,3717,3717,3717,3718,3718,3718,3718,3718,3719,3719,3719,3719,3719,/**/3714,3714,3714,3714,3714,3715,3715,3715,3715,3715,3716,3716,3716,3716,3716,3717,3717,3717,3717,3717,3718,3718,3718,3718,3718,/**/3718,3718,3718,3718,3718,3719,3719,3719,3719,3719,3720,3720,3720,3720,3720,3721,3721,3721,3721,3722,3722,3722,/**/
			3712,3712,3712,3712,3713,3713,3713,3713,3714,3714,3714,3714,3715,3715,3715,3715,3716,3716,3716,3716,3717,3717,3717,3717,3718,3718,3718,3718,/**/3711,3711,3711,3711,3711,3712,3712,3712,3712,3712,3713,3713,3713,3713,3713,3714,3714,3714,3714,3714,3715,3715,3715,3715,3715,/**/3722,3722,3722,3722,3722,3723,3723,3723,3723,3723,3724,3724,3724,3724,3724,3725,3725,3725,3725,3725,3726,3726,3726,3726,3726,/**/3725,3725,3725,3725,3725,3726,3726,3726,3726,3726,3727,3727,3727,3727,3727,3728,3728,3728,3728,3728,3729,3729,3729,3729,3729,/**/3730,3730,3730,3730,3730,3731,3731,3731,3731,3731,3732,3732,3732,3732,3732,3733,3733,3733,3733,3733,3734,3734,3734,3734,3734,/**/3727,3727,3727,3728,3728,3728,3728,3728,3729,3729,3729,3729,3729,3730,3730,3730,3730,3730,3731,3731,3731,3731,3731,/**/3723,3723,3723,3723,3724,3724,3724,3724,3724,3725,3725,3725,3725,3725,3726,3726,3726,3726,3726,3727,3727,3727,3727,3727,/**/
			3726,3726,3726,3727,3727,3727,3727,3728,3728,3728,3728,3728,3729,3729,3729,3729,3729,3730,3730,3730,3730,3730,
/* 32 */			3287,3287,3288,3288,3288,3289,3289,3289,3289,3290,3290,3290,3290,3290,3291,3291,3291,3291,3291,3291,3292,3292,3293,3293,3293,3293,3293,3294,/**/3292,3292,3292,3292,3292,3292,3293,3293,3293,3293,3293,3294,3294,3294,3294,3294,3294,3295,3295,3295,3295,3295,3295,3295,3295,3295,3296,3296,3296,3296,3296,3296,3296,3296,3296,3296,3296,3297,3297,3297,3297,3297,3297,3297,3297,3297,3297,3297,3297,3298,3298,3298,3298,3298,3298,3298,3298,3298,3299,3299,3299,3299,3299,3299,3299,3299,3299,3299,3299,3300,3300,3300,3300,3300,3300,3300,3300,3300,3300,3301,3301,
		},
	};


/*
WORLDMAP 2: (not-walk able places)
01 - Lumbridge cows
*/
	public static int worldmap2[][] = {
		{
/* 01 */		3257,3258,3260,3261,3261,3262,3261,3262,3257,3258,3257,3258,3254,3255,3254,3255,3252,3252,3250,3251,3250,3251,3249,3250,3249,3250,3242,3242,3243,3243,3257,3244,3245,3244,3245,3247,3248,3250,3251,3255,3256,3255,3256,3259,3260,
		},
		{
/* 01 */		3256,3256,3256,3256,3266,3266,3267,3267,3270,3270,3271,3271,3272,3272,3273,3273,3275,3276,3277,3277,3278,3278,3279,3279,3280,3280,3285,3286,3289,3290,3289,3297,3297,3298,3298,3298,3298,3297,3297,3297,3297,3298,3298,3297,3297,
		},
	};

public int remove = 2; // 1 = removes equipment, 2 = doesn't remove - xerozcheez
public static int removeschaos[] = {1,2,2,2};

    public static int randomremoveschaos()
    {
    	return removeschaos[(int)(Math.random()*removeschaos.length)];
    }

public void gfxAll(int id, int Y, int X)
{
	//for (Player p : server.playerHandler.players) {
	for(int i = 0; i < server.playerHandler.players.length; i++){
		Player p = server.playerHandler.players[i];
		if(p != null) {
			client person = (client)p;
			if((person.playerName != null || person.playerName != "null")){
				if(person.distanceToPoint(X, Y) <= 60){
					person.stillgfx2(id, Y, X, 0, 0);
				}
			}
		}
	}
}

public int GetNPCBlockAnim(int id) 
{
switch (id) {

case 50: // dragons
case 53:
case 54:
case 55: 
return 89;
case 1961:
	return 1927;
case 221:
case 110:
	return 129;

default:
return 1834;

}
}
	public boolean AttackPlayer(int NPCID) {
		if(npcs[NPCID].getKiller() == 0){
			return false;
		}
		int Player = npcs[NPCID].getKiller();
		if (server.playerHandler.players[Player] == null) {
			ResetAttackPlayer(NPCID);
			return false;
		} else if (server.playerHandler.players[Player].DirectionCount < 2) {
			return false;
		}
                client plr = (client) server.playerHandler.players[Player];
		int EnemyX = server.playerHandler.players[Player].absX;
		int EnemyY = server.playerHandler.players[Player].absY;
                npcs[NPCID].enemyX = EnemyX;
                npcs[NPCID].enemyY = EnemyY;
		if(Math.abs(npcs[NPCID].absX - EnemyX) > 20 || Math.abs(npcs[NPCID].absY - EnemyY) > 20){
			ResetAttackPlayer(NPCID);
		}
                //if(EnemyX != npcs[NPCID].absX && EnemyY != npcs[NPCID].absY) {
                //npcs[NPCID].viewX = EnemyX;
                //npcs[NPCID].viewY = EnemyY;
                //npcs[NPCID].faceToUpdateRequired = true;
                //}
		int EnemyHP = server.playerHandler.players[Player].playerLevel[server.playerHandler.players[Player].playerHitpoints];
		int EnemyMaxHP = getLevelForXP(server.playerHandler.players[Player].playerXP[server.playerHandler.players[Player].playerHitpoints]);
                //if(EnemyX != npcs[NPCID].absX && EnemyY != npcs[NPCID].absY) // Xerozcheez: stops client crashing
                //plr.viewTo(npcs[NPCID].absX, npcs[NPCID].absY); // Xerozcheez: Player turns to npc

                if(server.playerHandler.players[Player].attacknpc == NPCID) {
                server.playerHandler.players[Player].attacknpc = NPCID; // Xerozcheez: makes it so if player runs away the player attacks back when npc follows
                server.playerHandler.players[Player].IsAttackingNPC = true; // Xerozcheez: makes it so if player runs away the player attacks back when npc follows
                }
		int hitDiff = 0;
		hitDiff = misc.random(npcs[NPCID].MaxHit);
		client player = (client)server.playerHandler.players[Player];
		if(player != null){
			int def = player.playerBonus[6];
			int rand = misc.random(def);
			if(NPCID == 1961){
				combatLevel[NPCID] = 105;
			}
			int rand_npc = misc.random(combatLevel[NPCID] * 5);
			if(npcs[NPCID].npcType == 1472){
				rand_npc = misc.random(800);
			}
			if(npcs[NPCID].npcType == 80){
				rand_npc = misc.random(500);
			}
			if(npcs[NPCID].npcType == 1913){
				rand_npc = misc.random(1500);
			}
			if(npcs[NPCID].npcType == 936){
				rand_npc = misc.random(100);
			}
			if(npcs[NPCID].npcType == 110){
				rand_npc = misc.random(20);
			}
			int blocked = (int)(def / 10);
			//println("rand_npc=" + rand_npc + ", rand=" + rand);
			if(rand_npc > rand){
				hitDiff = misc.random(npcs[NPCID].MaxHit) - blocked;
				if(hitDiff < 0){
					hitDiff = 0;
				}
			} else {
				hitDiff = 0;
			}
		}
                                          if(npcs[NPCID].npcType != 3200 && npcs[NPCID].npcType != 1645) {
                       FollowPlayerCB(NPCID, Player);
                                          }
		if (GoodDistance(npcs[NPCID].absX, npcs[NPCID].absY, EnemyX, EnemyY, 1) == true || npcs[NPCID].npcType == 3200 || npcs[NPCID].npcType == 2745) {
			if (npcs[NPCID].actionTimer == 0) {
				if (false && EnemyHP <= (int)((double)((double)EnemyMaxHP / 10.0) + 0.5)) {
				} else {
					if (server.playerHandler.players[Player].deathStage > 0) {
						ResetAttackPlayer(NPCID);
					} else {
						if (npcs[NPCID].npcType == 81 || npcs[NPCID].npcType == 397 || npcs[NPCID].npcType == 1766 || npcs[NPCID].npcType == 1767 || npcs[NPCID].npcType == 1768) {
							npcs[NPCID].animNumber = 0x03B; //cow attack
						} else if (npcs[NPCID].npcType == 41) {
							npcs[NPCID].animNumber = 0x037; //chicken attack
						} else if (npcs[NPCID].npcType == 87) {
							npcs[NPCID].animNumber = 0x08A; //rat attack
						} else if (npcs[NPCID].npcType == 3200) { // chaos elemental
							npcs[NPCID].animNumber = 0x326;
                                                        remove = randomremoveschaos();
                                                        if(remove == 1)
                                                        {
                                                        server.playerHandler.players[Player].removeequipped();
                                                        } 
						}
                                                else if(npcs[NPCID].npcType == 752) {
                                                npcs[NPCID].animNumber = 0x326;
						} else if (npcs[NPCID].npcType == 1961) {
							npcs[NPCID].animNumber = 1926;
						} else if (npcs[NPCID].npcType == 221 || npcs[NPCID].npcType == 110) 
							npcs[NPCID].animNumber = 128;
						else if(npcs[NPCID].npcType == 941 || npcs[NPCID].npcType == 55 || npcs[NPCID].npcType == 53){
							npcs[NPCID].animNumber = 80;
						} else if(npcs[NPCID].npcType == 114){
							npcs[NPCID].animNumber = 359;
						} else if(npcs[NPCID].npcType == 86){
							npcs[NPCID].animNumber = 138;
						} else if(npcs[NPCID].npcType == 1125){
							npcs[NPCID].animNumber = 1142;
						} else if(npcs[NPCID].npcType == 142){
							npcs[NPCID].animNumber = 75;
						} else if(npcs[NPCID].npcType == 49){
							println("setting emote...");
							npcs[NPCID].animNumber = 158;
						} else {
							npcs[NPCID].animNumber = 0x326; //human attack
						}
                                             
						npcs[NPCID].animUpdateRequired = true;
						npcs[NPCID].updateRequired = true;
						if ((EnemyHP - hitDiff) < 0) {
							hitDiff = EnemyHP;
							ResetAttackPlayer(NPCID);
						}
						server.playerHandler.players[Player].dealDamage(hitDiff);
						server.playerHandler.players[Player].hitDiff = hitDiff;
						server.playerHandler.players[Player].updateRequired = true;
						server.playerHandler.players[Player].hitUpdateRequired = true;
						server.playerHandler.players[Player].appearanceUpdateRequired = true;
						npcs[NPCID].actionTimer = 7;
					}
				}
				return true;
			}
		}
		return false;
	}
public boolean AttackPlayerMage(int NPCID) {
		int Player = npcs[NPCID].StartKilling;
                client p = (client) server.playerHandler.players[Player];
		if (server.playerHandler.players[Player] == null) {
			ResetAttackPlayer(NPCID);
			return false;
		} else if (server.playerHandler.players[Player].DirectionCount < 2) {
			return false;
		}
		int EnemyX = server.playerHandler.players[Player].absX;
		int EnemyY = server.playerHandler.players[Player].absY;
		int EnemyHP = server.playerHandler.players[Player].playerLevel[server.playerHandler.players[Player].playerHitpoints];
		int EnemyMaxHP = getLevelForXP(server.playerHandler.players[Player].playerXP[server.playerHandler.players[Player].playerHitpoints]);
		boolean RingOfLife = false;
		if (server.playerHandler.players[Player].playerEquipment[server.playerHandler.players[Player].playerRing] == 2570) {
			//RingOfLife = true;
		}
		int hitDiff = 0;
		//hitDiff = misc.random(npcs[NPCID].MaxHit);
			if (npcs[NPCID].actionTimer == 0) {
				if (false) {
				} else {
					if (server.playerHandler.players[Player].currentHealth > 0 == true) {
						ResetAttackPlayer(NPCID);
					} else {
                                               npcs[NPCID].animNumber = 711; // attack animation
								if(npcs[NPCID].npcType == 2591) { //TzHaar-Mej
                                               	p.animation(78, p.absY, p.absX);
                                               	hitDiff = 8 + misc.random(40);
                                               	
                                               npcs[NPCID].animNumber = 1979; // mage attack
								}
						if(npcs[NPCID].npcType == 50) { //KBD
							try{
							       p.animation(0, p.absY, p.absX); // Flames
								client player = (client)server.playerHandler.players[Player];
								int bonus = misc.random(player.playerBonus[5]);
								int dragon_bonus = misc.random(828);
								if(dragon_bonus > bonus){
									hitDiff = misc.random(56);
								}
								int random =  misc.random(5);
								if(random == 1){
									for(int i = 0; i < npcs[NPCID].Killing.length; i++){
										client player2 = (client)server.playerHandler.players[i];
										if(player2 != null && npcs[NPCID].Killing[i] > 0){
											int x = player2.absX;
											int y = player2.absY;
											//if(Math.abs(npcs[NPCID].absX - x) <= 50 || Math.abs(npcs[NPCID].absY - y) <= 50){
												player2.hitDiff = misc.random(60);
												player2.updateRequired = true;
												player2.hitUpdateRequired = true;
												player2.appearanceUpdateRequired = true;
												try{
												player2.sendMessage("The enraged dragon's flames burn you!");
												} catch(Exception e){
													e.printStackTrace();
												}
											//}
										}
									}
								}
								npcs[NPCID].animNumber = 81; // Dragon breath attack
							} catch(Exception e){
								e.printStackTrace();
							}
						}
						if(npcs[NPCID].npcType == 54) { //balck dragon
						       p.animation(0, p.absY, p.absX); // Flames
							client player = (client)server.playerHandler.players[Player];
							int bonus = misc.random(player.playerBonus[5]);
							int dragon_bonus = misc.random(681);
							if(dragon_bonus > bonus){
								hitDiff = misc.random(45);
							}
							int random =  misc.random(5);
							if(random == 1){
								for(int i = 0; i < server.playerHandler.players.length; i++){
									client player2 = (client)server.playerHandler.players[i];
									if(player2 != null){
										int x = player2.absX;
										int y = player2.absY;
										if(Math.abs(npcs[NPCID].absX - x) <= 50 || Math.abs(npcs[NPCID].absY - y) <= 50){
											bonus = misc.random(player2.playerBonus[5]);
											dragon_bonus = misc.random(681);
											if(dragon_bonus > bonus){
												player2.hitDiff = misc.random(42);
											} else 
											player2.hitDiff = 0;
											player2.updateRequired = true;
											player2.hitUpdateRequired = true;
											player2.appearanceUpdateRequired = true;
											player2.sendMessage("The enraged dragon's flames burn you!");
										}
									}
								}
							}
							npcs[NPCID].animNumber = 81; // Dragon breath attack
						}
                                               if(npcs[NPCID].npcType == 1645) { //Infernal Mage
                                               p.animation(369, p.absY, p.absX);
                                               hitDiff = 6 + misc.random(20);
								}
							npcs[NPCID].animNumber = 1979; // mage attack
								if(npcs[NPCID].npcType == 2025) { //Ahrim
                                               	p.animation(369, p.absY, p.absX);
                                               	hitDiff = 6 + misc.random(22);
                                               	}
							npcs[NPCID].animNumber = 1979; // mage attack
								if(npcs[NPCID].npcType == 1250) { //Shade
                                               	p.animation(383, p.absY, p.absX);
                                               	hitDiff = 1 + misc.random(30);
                                               	}
							npcs[NPCID].animNumber = 81; // Dragon breath attack

                                               if(npcs[NPCID].npcType == 509) {
                                               p.stillgfx(365, p.absY, p.absX);
                                               hitDiff = 8 + misc.random(51); 
                                               }
                                               if(npcs[NPCID].npcType == 1241) {
                                               p.stillgfx(363, p.absY, p.absX);
                                               hitDiff = 2 + misc.random(19); 
                                               }
                                               if(npcs[NPCID].npcType == 1246) {
                                               p.stillgfx(368, npcs[NPCID].absY, npcs[NPCID].absX);
                                               p.stillgfx(367, p.absY, p.absX);
                                               hitDiff = 4 + misc.random(35); 
                                               }
						npcs[NPCID].animUpdateRequired = true;
						npcs[NPCID].updateRequired = true;
						if ((EnemyHP - hitDiff) < 0) {
							hitDiff = EnemyHP;
						}
						server.playerHandler.players[Player].hitDiff = hitDiff;
						server.playerHandler.players[Player].updateRequired = true;
						server.playerHandler.players[Player].hitUpdateRequired = true;
						server.playerHandler.players[Player].appearanceUpdateRequired = true;
						npcs[NPCID].actionTimer = 7;
					}
				}
				return true;
		}
		return false;
	}
public boolean AttackNPCMage(int NPCID) {
		int EnemyX = server.npcHandler.npcs[npcs[NPCID].attacknpc].absX;
		int EnemyY = server.npcHandler.npcs[npcs[NPCID].attacknpc].absY;
		int EnemyHP = server.npcHandler.npcs[npcs[NPCID].attacknpc].HP;
		int hitDiff = 0;
                int Npchitdiff = 0;
                int wepdelay = 0;
		//hitDiff = misc.random(npcs[NPCID].MaxHit);
			if (npcs[NPCID].actionTimer == 0) {
                         if (server.npcHandler.npcs[npcs[NPCID].attacknpc].IsDead == true) {
					ResetAttackNPC(NPCID);
					//npcs[NPCID].textUpdate = "Oh yeah I win bitch!";
					//npcs[NPCID].textUpdateRequired = true;
                                        npcs[NPCID].animNumber = 2103;
					npcs[NPCID].animUpdateRequired = true;
					npcs[NPCID].updateRequired = true;
				} else  {
                                               npcs[NPCID].animNumber = 711; // mage attack
                                               if(npcs[NPCID].npcType == 1645) {
                                               gfxAll(369, EnemyY, EnemyX);
                                               hitDiff = 6 + misc.random(43);
                                               }
                                               if(npcs[NPCID].npcType == 1645) {
                                               gfxAll(369, EnemyY, EnemyX);
                                               hitDiff = 6 + misc.random(43);
                                               }
                                               if(npcs[NPCID].npcType == 509) {
                                               gfxAll(365, EnemyY, EnemyX);
                                               hitDiff = 8 + misc.random(51); 
                                               }
                                               if(npcs[NPCID].npcType == 1241) {
                                               gfxAll(363, EnemyY, EnemyX);
                                               hitDiff = 2 + misc.random(19); 
                                               }
                                               if(npcs[NPCID].npcType == 1246) {
                                               gfxAll(368, npcs[NPCID].absY, npcs[NPCID].absX);
                                               gfxAll(367, EnemyY, EnemyX);
                                               hitDiff = 4 + misc.random(35); 
                                               }
						npcs[NPCID].animUpdateRequired = true;
						npcs[NPCID].updateRequired = true;
						if ((EnemyHP - hitDiff) < 0) {
							hitDiff = EnemyHP;
						}
					server.npcHandler.npcs[npcs[NPCID].attacknpc].hitDiff = hitDiff;
					server.npcHandler.npcs[npcs[NPCID].attacknpc].attacknpc = NPCID;
					server.npcHandler.npcs[npcs[NPCID].attacknpc].updateRequired = true;
					server.npcHandler.npcs[npcs[NPCID].attacknpc].hitUpdateRequired = true;
					npcs[NPCID].actionTimer = 7;
				        return true;
		}
		return false;
	}
        return false;
}
public boolean AttackNPC(int NPCID) {
    if(server.npcHandler.npcs[npcs[NPCID].attacknpc] != null) {
		int EnemyX = server.npcHandler.npcs[npcs[NPCID].attacknpc].absX;
		int EnemyY = server.npcHandler.npcs[npcs[NPCID].attacknpc].absY;
		int EnemyHP = server.npcHandler.npcs[npcs[NPCID].attacknpc].HP;
		int hitDiff = 0;
                int Npchitdiff = 0;
                int wepdelay = 0;
                hitDiff = misc.random(npcs[NPCID].MaxHit);
		if (GoodDistance(EnemyX, EnemyY, npcs[NPCID].absX, npcs[NPCID].absY, 1) == true) {
				if (server.npcHandler.npcs[npcs[NPCID].attacknpc].IsDead == true) {
					ResetAttackNPC(NPCID);
					//npcs[NPCID].textUpdate = "Oh yeah I win bitch!";
					//npcs[NPCID].textUpdateRequired = true;
                                        npcs[NPCID].animNumber = 2103;
					npcs[NPCID].animUpdateRequired = true;
					npcs[NPCID].updateRequired = true;
				} else  {
					if ((EnemyHP - hitDiff) < 0) {
						hitDiff = EnemyHP;
					}
                                        if(npcs[NPCID].npcType == 9)
                                        npcs[NPCID].animNumber = 386;
                                        if(npcs[NPCID].npcType == 3200)
					npcs[NPCID].animNumber = 0x326; // drags: chaos ele emote ( YESSS ) 
                                        if(npcs[NPCID].npcType == 1605 || npcs[NPCID].npcType == 1472) {
					npcs[NPCID].animNumber = 386; // drags: abberant spector death ( YAY )
					} 
					npcs[NPCID].animUpdateRequired = true;
					npcs[NPCID].updateRequired = true;
					server.npcHandler.npcs[npcs[NPCID].attacknpc].hitDiff = hitDiff;
					server.npcHandler.npcs[npcs[NPCID].attacknpc].attacknpc = NPCID;
					server.npcHandler.npcs[npcs[NPCID].attacknpc].updateRequired = true;
					server.npcHandler.npcs[npcs[NPCID].attacknpc].hitUpdateRequired = true;
					npcs[NPCID].actionTimer = 7;
				        return true;
                                        }
				}
                }
		return false;
	}
	public boolean ResetAttackNPC(int NPCID) {
		npcs[NPCID].IsUnderAttackNpc = false;
		npcs[NPCID].IsAttackingNPC = false;
		npcs[NPCID].attacknpc = -1;
		npcs[NPCID].RandomWalk = true;
		npcs[NPCID].animNumber = 0x328;
		npcs[NPCID].animUpdateRequired = true;
		npcs[NPCID].updateRequired = true;
		return true;
	}
	public int getLevelForXP(int exp) {
		int points = 0;
		int output = 0;

		for (int lvl = 1; lvl <= 135; lvl++) {
			points += Math.floor((double)lvl + 300.0 * Math.pow(2.0, (double)lvl / 7.0));
			output = (int)Math.floor(points / 4);
			if (output >= exp)
				return lvl;
		}
		return 0;
	}
	public boolean GoodDistance(int objectX, int objectY, int playerX, int playerY, int distance) {
		for (int i = 0; i <= distance; i++) {
		  for (int j = 0; j <= distance; j++) {
			if ((objectX + i) == playerX && ((objectY + j) == playerY || (objectY - j) == playerY || objectY == playerY)) {
				return true;
			} else if ((objectX - i) == playerX && ((objectY + j) == playerY || (objectY - j) == playerY || objectY == playerY)) {
				return true;
			} else if (objectX == playerX && ((objectY + j) == playerY || (objectY - j) == playerY || objectY == playerY)) {
				return true;
			}
		  }
		}
		return false;
	}
	public boolean ResetAttackPlayer(int NPCID) {
		npcs[NPCID].IsUnderAttack = false;
		npcs[NPCID].StartKilling = 0;
		npcs[NPCID].RandomWalk = true;
		npcs[NPCID].animNumber = 0x328;
		npcs[NPCID].animUpdateRequired = true;
		npcs[NPCID].updateRequired = true;
		return true;
	}
	public void loadAutoSpawn(){
		try{
			results = statement.executeQuery("SELECT * FROM uber3_spawn WHERE live = 1");
			while(results.next()){
				newNPC(results.getInt("id"), results.getInt("x"), results.getInt("y"), results.getInt("height"), results.getInt("rx1"), results.getInt("ry1"), results.getInt("rx2"), results.getInt("ry2"), results.getInt("walk"), results.getInt("hitpoints"));
			}
		} catch(Exception e){
			e.printStackTrace();
		}
	}
			
	public boolean loadAutoSpawn(String FileName) {
		String line = "";
		String token = "";
		String token2 = "";
		String token2_2 = "";
		String[] token3 = new String[10];
		boolean EndOfFile = false;
		int ReadMode = 0;
		BufferedReader characterfile = null;
		try {
			characterfile = new BufferedReader(new FileReader("config\\"+FileName));
		} catch(FileNotFoundException fileex) {
			misc.println(FileName+": file not found.");
			return false;
		}
		try {
			line = characterfile.readLine();
		} catch(IOException ioexception) {
			misc.println(FileName+": error loading file.");
			return false;
		}
		while(EndOfFile == false && line != null) {
			line = line.trim();
			int spot = line.indexOf("=");
			if (spot > -1) {
				token = line.substring(0, spot);
				token = token.trim();
				token2 = line.substring(spot + 1);
				token2 = token2.trim();
				token2_2 = token2.replaceAll("\t\t", "\t");
				token2_2 = token2_2.replaceAll("\t\t", "\t");
				token2_2 = token2_2.replaceAll("\t\t", "\t");
				token2_2 = token2_2.replaceAll("\t\t", "\t");
				token2_2 = token2_2.replaceAll("\t\t", "\t");
				token3 = token2_2.split("\t");
				if (token.equals("spawn")) {
					newNPC(Integer.parseInt(token3[0]), Integer.parseInt(token3[1]), Integer.parseInt(token3[2]), Integer.parseInt(token3[3]), Integer.parseInt(token3[4]), Integer.parseInt(token3[5]), Integer.parseInt(token3[6]), Integer.parseInt(token3[7]), Integer.parseInt(token3[8]), GetNpcListHP(Integer.parseInt(token3[0])));
				}
			} else {
				if (line.equals("[ENDOFSPAWNLIST]")) {
					try { characterfile.close(); } catch(IOException ioexception) { }
					return true;
				}
			}
			try {
				line = characterfile.readLine();
			} catch(IOException ioexception1) { EndOfFile = true; }
		}
		try { characterfile.close(); } catch(IOException ioexception) { }
		return false;
	}

	public int GetNpcListHP(int NpcID) {
		for (int i = 0; i < maxListedNPCs; i++) {
			if (NpcList[i] != null) {
				if (NpcList[i].npcId == NpcID) {
					return NpcList[i].npcHealth;
				}
			}
		}
		return 0;
	}
	public void loadNPCList(){
		try{
			results = statement.executeQuery("SELECT * FROM uber3_npcs");
			while(results.next()){
				newNPCList(results.getInt("id"), results.getString("name"), results.getInt("combat"), results.getInt("hitpoints"));
				respawnTime[results.getInt("id")] = results.getInt("respawn");
				combatLevel[results.getInt("id")] = results.getInt("combat");
			}
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void loadNPCDrops(){
		try{
			results = mysql_query("SELECT * FROM uber3_drops");
			while(results.next()){
				drops[results.getInt("npcid")][dropCount[results.getInt("npcid")]] = results.getInt("itemid");
				drops[results.getInt("npcid")][dropCount[results.getInt("npcid")] + 1] = results.getInt("amount");
				drops[results.getInt("npcid")][dropCount[results.getInt("npcid")] + 2] = results.getDouble("percent");
				dropCount[results.getInt("npcid")] += 3;
			}
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	public int GetNPCDropArrayID(int NPCID, int DropType) {
		for (int i = 0; i < maxNPCDrops; i++) {
			if (NpcDrops[i] != null) {
				if (NpcDrops[i].npcId == NPCID && NpcDrops[i].DropType == DropType) {
					return i;
				}
			}
		}
		return -1;
	}

	public void println(String str) {
		System.out.println(str);
	}
	public int calcRespawn(int npcid){
		return respawnTime[npcid];
	}
}

public class NPC {
	// Dodain's variables
	public int respawnTime = 60, attackEmote = 0x326, deathEmote = 0x900; // in seconds
	public boolean aggressive = false;
	public int aggroRadius = 0;
	// Dodian's variables
	
	public int npcId;
	public int npcType;

        public int PoisonDelay = 999999;
        public int PoisonClear = 0;
	public int absX, absY;
	public int heightLevel;
	public int makeX, makeY, moverangeX1, moverangeY1, moverangeX2, moverangeY2, moveX, moveY, direction, walkingType, attacknpc, followPlayer;
	public int spawnX, spawnY;
        public int viewX, viewY;
	public int HP, MaxHP, hitDiff, MaxHit, animNumber, actionTimer, StartKilling, enemyX, enemyY;
	public boolean IsDead, DeadApply, NeedRespawn, IsUnderAttack, IsClose, Respawns, IsUnderAttackNpc, IsAttackingNPC, poisondmg, walkingToPlayer, followingPlayer;
	public int[] Killing = new int[server.playerHandler.maxPlayers];

	public boolean RandomWalk;
	public boolean dirUpdateRequired;
	public boolean animUpdateRequired;
	public boolean hitUpdateRequired;
	public boolean updateRequired;
	public boolean textUpdateRequired;
        public boolean faceToUpdateRequired;
	public String textUpdate;
	public boolean hit = false;
	public int[] effects = new int[10];
	public int getKiller(){
		int Killer = 0;
		int Count = 0;
		for (int i = 1; i < server.playerHandler.maxPlayers; i++) {
			if (false) {
				Killer = i;
				Count = 1;
			} else {
				if (Killing[i] > Killing[Killer]) {
					Killer = i;
					Count = 1;
				} else if (Killing[i] == Killing[Killer]) {
					Count++;
				}
			}
		}
		return Killer;
	}
	public NPC(int _npcId, int _npcType) {
		npcId = _npcId;
		npcType = _npcType;
		direction = -1;
		IsDead = false;
		DeadApply = false;
		actionTimer = 0;
		RandomWalk = true;
		StartKilling = 0;
		IsUnderAttack = false;
		IsClose = false;
		for (int i = 0; i < Killing.length; i++) {
			Killing[i] = 0;
		}
	}
	
	public void updateNPCMovement(stream str) {
		if (direction == -1) {
			// don't have to update the npc position, because the npc is just standing
			if (updateRequired) {
				// tell client there's an update block appended at the end
				str.writeBits(1, 1);
				str.writeBits(2, 0);
			} else {
				str.writeBits(1, 0);
			}
		} else {
			// send "walking packet"
			str.writeBits(1, 1);
			str.writeBits(2, 1);		// updateType
			str.writeBits(3, misc.xlateDirectionToClient[direction]);
			if (updateRequired) {
				str.writeBits(1, 1);		// tell client there's an update block appended at the end
			} else {
				str.writeBits(1, 0);
			}
		}
	}

	public void appendNPCUpdateBlock(stream str) {
		if(!updateRequired) return ;		// nothing required
		int updateMask = 0;
		if(textUpdateRequired) updateMask |= 1;
		if(animUpdateRequired) updateMask |= 0x10;
                //if(hitUpdateRequired) updateMask |= 0x8;
		if(hitUpdateRequired) updateMask |= 0x40;
		if(dirUpdateRequired) updateMask |= 0x20;
		if(faceToUpdateRequired) updateMask |= 0x20;

		/*if(updateMask >= 0x100) {
			// byte isn't sufficient
			updateMask |= 0x40;			// indication for the client that updateMask is stored in a word
			str.writeByte(updateMask & 0xFF);
			str.writeByte(updateMask >> 8);
		} else {*/
			str.writeByte(updateMask);
		//}

		// now writing the various update blocks itself - note that their order crucial
		if(textUpdateRequired) {
			str.writeString(textUpdate);
		}
		if (animUpdateRequired) appendAnimUpdate(str);
		if (hitUpdateRequired) appendHitUpdate(str);
		if (dirUpdateRequired) appendDirUpdate(str);
                if (faceToUpdateRequired) appendFaceToUpdate(str);
		// TODO: add the various other update blocks
	}

	public void clearUpdateFlags() {
		updateRequired = false;
		textUpdateRequired = false;
		hitUpdateRequired = false;
		animUpdateRequired = false;
		dirUpdateRequired = false;
		textUpdate = null;
		moveX = 0;
		moveY = 0;
		direction = -1;
	}

	// returns 0-7 for next walking direction or -1, if we're not moving
	public int getNextWalkingDirection() {
		int dir;
		dir = misc.direction(absX, absY, (absX + moveX), (absY + moveY));
		if(dir == -1) return -1;
		dir >>= 1;
		absX += moveX;
		absY += moveY;
		return dir;
	}

	public void getNextNPCMovement() {
		direction = -1;
		direction = getNextWalkingDirection();
	}

	protected void appendHitUpdate(stream str) {		
		try {
			if(hit){
				HP -= hitDiff;
				hit = false;
			}
			if (HP <= 0) {
				IsDead = true;
			}
			str.writeByteC(hitDiff); // What the perseon got 'hit' for
			if (hitDiff > 0 && !poisondmg) {
				str.writeByteS(1); // 0: red hitting - 1: blue hitting
			} else if (hitDiff > 0 && poisondmg) {
				str.writeByteS(2); // 0: red hitting - 1: blue hitting
			} else {
				str.writeByteS(0); // 0: red hitting - 1: blue hitting
			}
			str.writeByteS(HP); // Their current hp, for HP bar
			str.writeByteC(MaxHP); // Their max hp, for HP bar
                        poisondmg = false;
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	protected void appendHitUpdate2(stream str) {		
		try {
			HP -= hitDiff;
			if (HP <= 0) {
				IsDead = true;
			}
			str.writeByteS(hitDiff); // What the perseon got 'hit' for
			if (hitDiff > 0 && !poisondmg) {
				str.writeByteC(1); // 0: red hitting - 1: blue hitting
			} else if (hitDiff > 0 && poisondmg) {
				str.writeByteC(2); // 0: red hitting - 1: blue hitting
			} else {
				str.writeByteC(0); // 0: red hitting - 1: blue hitting
			}
			str.writeByteS(HP); // Their current hp, for HP bar
			str.writeByte(MaxHP); // Their max hp, for HP bar
                        poisondmg = false;
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public void appendAnimUpdate(stream str) {
		str.writeWordBigEndian(animNumber);
		str.writeByte(1);
	}

	public void appendDirUpdate(stream str){
		str.writeWord(direction);
	}
        
        public void appendFaceToUpdate(stream str) {
                str.writeWordBigEndian(viewX);
                str.writeWordBigEndian(viewY);
        }
}

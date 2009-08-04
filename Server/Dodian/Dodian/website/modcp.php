<?php
define('ROOT', './');
include ROOT.'common/common.php';

if (isset($_COOKIE['member_id'])) {
	$user_query = $db->query("SELECT * FROM uber3_players WHERE id=". $_COOKIE['member_id']);
	$user = $db->fetch_assoc($user_query);
}
$g = $user['mgroup'];
$validGroups = array(6, 7, 13, 4);
$valid = 0;
foreach($validGroups as $vg){
	if($g == $vg){
		$valid = 1;
		break;
	}
}
if($valid == 0){
	header("Location: index.php");
}
$level = 1;
if($g == 4){
	$level = 3;
}
if($g == 6){
	$level = 2;
}
$name = $user['name'];
$playername = $_GET['player'];
$action = $_GET['action'];
$id = $_GET['dbid'];
if($action == "viewTrade"){
	$tradeId = $_GET['id'];
	$trade = $db->fetch_assoc($db->query("SELECT * FROM uber3_trades WHERE id = $tradeId"));
	$p1 = $trade['p1'];
	$p2 = $trade['p2'];
	$n1f = $db->fetch_assoc($db->query("SELECT name FROM uber3_players WHERE id = ". $p1));
	$n1 = $n1f['name'];
	$n2f = $db->fetch_assoc($db->query("SELECT name FROM uber3_players WHERE id = ". $p2));
	$n2 = $n2f['name'];
	if($trade['type'] == 0){
		$type = "Trade";
		$line = "trades";
	} else if($trade['type'] == 1){
		$type = "Stake ($n1 won)";
		$line = "stakes";
	}
	echo "<center><b>Interaction Type:  $type</b></center>";
	?>
	<table align="center"><tr><td><a href="index.php?code=modcp&action=search&player=<? echo $n1;?>"><? echo $n1;?></a> <? echo $line; ?></td><td><a href="index.php?code=modcp&action=search&player=<? echo $n2;?>"><? echo $n2;?></a> <? echo $line;?></td></tr>
	<?
	$out1 = "";
	$q1 = $db->query("SELECT * FROM uber3_logs WHERE id = $tradeId AND pid = $p1") or print(mysql_error());
	while($items1 = $db->fetch_assoc($q1)){
		$out1 .= getItemName($items1['item'] + 1) . " (". $items1['amount']. ") <br>";
	}
	$out2 = "";
	$q2 = $db->query("SELECT * FROM uber3_logs WHERE id = $tradeId AND pid = $p2") or print(mysql_error());
	while($items2 = $db->fetch_assoc($q2)){
		$out2 .= getItemName($items2['item'] + 1) . "(". $items2['amount']. ") <br>";
	}
	echo "<tr><td>$out1</td><td>$out2</td></table>";
}
if($action == "changeBan"){
	$query = $db->query("SELECT * FROM uber3_players WHERE id = $id") or die(mysql_error());
	$player = $db->fetch_assoc($query);
	if($player['banned'] == 0){
		$banning = 1;
	} else {
		$banning = 0;
	}
	$durations = array("1", "2", "3", "4", "5", "7", "10", "Forever");
	echo "<center><b>". $player['name'] ."</b><br/>";
	if($banning == 1){
	?>
		<form method="get" action="index.php"><input type="hidden" name="code" value="modcp">
		<input type="hidden" name="action" value="banUser">
		<input type="hidden" name="dbid" value="<?php echo $id; ?>">
		<input type="hidden" name="msg" value="<?php echo $player['new_msg']; ?>">
		<textarea name="reason" COLS=40 ROWS=6>Ban reason</textarea><br />
		<select name="duration">
		<?php
			foreach($durations as $duration){
				?><option value="<?php echo $duration;?>"><? echo $duration; ?> days</option><?
			}
		?>
		</select>
		<input type="submit" value="Ban user"></center></form>
	<?
	} else {
		if($level < $player['ban_level']){
			echo "<center><b>You cannot unban this player</b></center>";
		} else {
			$db->query("UPDATE uber3_players SET ban_level = 0, banned = 0, ban_reason='', ban_expire = '', ban_by = '' WHERE id=$id") or die(mysql_error());
			$body = "This automated message has been sent to inform you that your ban on Uber Server has been lifted";
			newPm($id, 3017, "Notification of Status Change", $body);
			?>
			<center><b>User unbanned!</b> (<a href="index.php?code=modcp">Back to modcp</a>)</center>
			<?php
		}
	}
}
if($action =="banUser"){
	$expire = time() + (3600000 * 24 * $_GET['duration']);
	$q = $db->query("SELECT * FROM ibf_message_topics ORDER BY mt_id DESC");
	$lastMsg = $db->fetch_assoc($q);
	$mid = $lastMsg['mt_id'] + 1;
	$msgId = $lastMsg['mt_msg_id'] + 1;
	$title = "Notification of Account Suspension";
	$duration = $_GET['duration'] ." days";
	if($_GET['duration'] == "1"){
		$duration = "1 day";
	}
	if($_GET['duration'] == "Forever"){
		$duration = "forever";
	}
	$temp = 0;
	if($expire > time()){
		$temp = 1;
	}
	$body = "This automated message has been sent to notify you that your account has been suspended from Uber Server for $duration.  To appeal this ban, click on Irc from the menu on the homepage and ask to speak to a moderator.  If this ban is temporary, your account will be reinstated automatically at the appropriate time";
	newPm($id, 3017, $title, $body);
	$msgCount = $_GET['msg'] + 1;
	$db->query("UPDATE uber3_players SET ban_level = $level, tempbanned = $temp, banned = 1, ban_expire='$expire', ban_reason='". $_GET['reason']."', ban_by='". $name."' WHERE id=$id") or die(mysql_error());
	?>
	<center><b>User banned!</b> (<a href="index.php?code=modcp">Back to modcp</a>)</center>
	<?php

}
if($action == "search"){
	$query = $db->query("SELECT * FROM uber3_players WHERE name LIKE '$playername'");
	$player = $db->fetch_assoc($query);
	echo "<center><b>" . $player['name'];
	?>(<a href="index.php?showuser=<?php echo $player['id'];?>">view stats</a>)</b></center>
	<?
	if(!$player){
		$action = "";
	}
	$equipRaw = explode(" ", $player['equipment']);
	if($player['banned'] == 1){
		$banStatus = "Banned";
		$banStatus .="<br/>Banned by:  ". $player['ban_by'] . "<br />";
		$banStatus .="Ban reason:  ". $player['ban_reason'] . "<br />";
		$now = time();
		$diff = $player['ban_expire'] - $now;
		$hours = round($diff / 3600000, 2);
		$banStatus .="Ban Expires:  ". $hours . " hours";
	} else {
		$banStatus = "Not banned";
	}	
	?>
	<center><table border=0 align="center"><b><tr><td>Settings</td><td>Equipment</td><td>Inventory</td><td>Bank</td><td>Recent Trades</td></tr>
	<tr><td valign="top">Player coords: <?php echo $player['posx'] . ", " . $player['posy'];?> (<a href="index.php?code=modcp&act=resetPos&dbid=<?php echo $player['id'];?>">Reset pos</a>)<br />
	Ban Status:  <?php echo $banStatus;?> (<a href="index.php?code=modcp&action=changeBan&dbid=<?php echo $player['id'];?>">Change</a>)
	</td><td valign="top">
	<?php
	foreach ($equipRaw as $equip){
		$contents = explode("-", $equip);
		if(strlen($contents[0]) > 0 && strlen($contents[1]) > 0 && $contents[1] > 0)
		echo getItemName($contents[0] + 1) . " (" . number_format($contents[1]) . ")<br />";
	}
	?>
	</td><td valign="top">
	<?
	$itemsRaw = explode(" ", $player['inventory']);
	foreach ($itemsRaw as $inv){
		$contents = explode("-", $inv);
		if(strlen($contents[0]) > 0 && strlen($contents[1]) > 0 && $contents[1] > 0)
		echo getItemName($contents[0]) . " (" . number_format($contents[1]) . ")<br />";
	}
	?>
	</td>
	<?
	$bankRaw = explode(" ", $player['bank']);
	?>
	<td valign="top"><?php
	foreach ($bankRaw as $bank){
		$contents = explode("-", $bank);
		if(strlen($contents[0]) > 0 && strlen($contents[1]) > 0 && $contents[1] > 0)
		echo getItemName($contents[0]) . " (" . number_format($contents[1]) . ")<br />";
	}
	?>
	</td><td valign="top">
	<?
	$trade_q = $db->query("SELECT * FROM uber3_trades WHERE p1 = ". $player['id'] ." OR p2 = ". $player['id'] ." ORDER BY id DESC");
	while($trade = $db->fetch_assoc($trade_q)){
		if($trade['p1'] == $player['id']){
			$p = "p2";
		} else {
			$p = "p1";
		}
		$other = $db->fetch_assoc($db->query("SELECT name FROM uber3_players WHERE id = ". $trade[$p]));
		$otherName = $other['name'];
		if($trade['type'] == 0)
		$out = "Trade with $otherName";
		if($trade['type'] == 1){
			if($p == "p2"){
				$out = "Duel with $otherName (won)";
			} else {
				$out = "Duel with $otherName (lost)";
			}
		}
		?>
		<a href="index.php?code=modcp&action=viewTrade&id=<?php echo $trade['id'];?>"><? echo $out; ?></a><br>
		<?
	}
	?>
	</td></tr></table>
	<?
}
if($action == "restart"){
	echo "Killing old process..<br>";
	$done = 0;
	while($done < 5){
		$out = exec("killall -e java");
		sleep(1);
		$done ++;
		
	}
	echo "Launching new server...<br>";
	chdir("/var/www/bin/");
	exec("java -Xmx1200M server > /var/www/html/log.txt &");
	echo "Done!<br>";
	//fork("cd /var/www/bin && java -Xmx800M server");
	
}
if($action == "stop"){
	exec("killall -e java");
	echo "Server killed";
}
if($action == "hax"){
	$q = $db->query("SELECT * FROM uber3_trades");
	while($f = $db->fetch_assoc($q)){
		if($f['p1'] == $f['p2']){
			$pf = $db->fetch_assoc($db->query("SELECT name FROM uber3_players WHERE id = ". $f['p1']));
			$name = $pf['name'];
			echo "Player ". $name ." traded himseslf!<br>";
		}
	}
}
if($action == "ipban"){
	$host = $_GET['host'];
	chdir("/var/www/bin");
	echo exec('/bin/sh ./ban $host');
	$db->query("INSERT INTO uber3_bans SET host = '$host', time='". time() ."', banner=". $user['id'] . ", reason='Banned through modcp'") or die(mysql_error());
	?>
	<center><b><?php echo $host; ?> banned!</b> (<a href="index.php?code=modcp">Back to modcp</a>)</center>
	<?
}
if(!$action){?>
		<center><b>Find player:  </b><form type="get" action="index.php">
		<input type="hidden" name="code" value="modcp">
		<input type="hidden" name="action" value="search">
		<input type="text" name="player" value="">
		<input type="submit" value="Search">
		</form><br><br>
		<a href="index.php?code=modcp&action=restart">Start/restart Server</a><br>
		<a href="index.php?code=modcp&action=stop">Stop Server</a><br></center>
		<br><br>
		<form type="get" action="index.php">
		<input type="hidden" name="code" value="modcp">
		<input type="hidden" name="action" value="scan">
		<input type="text" name="itemid" value="">
		<input type="submit" value="Search">
		</form>
		<?php
		/*$q = $db->query("SELECT * FROM uber3_players WHERE banned = 1");
		while($f = $db->fetch_assoc($q)){
			echo $f['name'] . " (". $f['ban_reason'] .") <br>";
		}*/
		?>
<?php
}
function getItemName($id_raw){
	$id = $id_raw - 1;
	$q = $db->query("SELECT * FROM uber3_items WHERE id = $id") or die(mysql_error());
	$f = $db->fetch_assoc($q);
	return $f['name'];
}
function newPm($to, $from, $title, $text){
	$q = $db->query("SELECT * FROM ibf_message_topics ORDER BY mt_id DESC");
	$lastMsg = $db->fetch_assoc($q);
	$mid = $lastMsg['mt_id'] + 1;
	$msgId = $lastMsg['mt_msg_id'] + 1;
	$db->query("INSERT INTO ibf_message_topics SET mt_id=$mid, mt_msg_id=$msgId, mt_date=". time(). ", mt_title='$title', mt_from_id=$from, mt_to_id=". $to. ", mt_vid_folder='in', mt_read=0, mt_owner_id=$to, mt_user_read=0") or die(mysql_error());
	$db->query("INSERT INTO ibf_message_text SET msg_id=$msgId, msg_date=". time() .", msg_post='$text', msg_sent_to_count=1, msg_author_id=$from") or die(mysql_error());
	$userq = $db->query("SELECT new_msg FROM uber3_players WHERE id = $to");
	$userData = $db->fetch_assoc($userq);
	$msgCount = $userData['new_msg'] + 1;
	$db->query("UPDATE uber3_players SET new_msg=$msgCount, show_popup=1 WHERE id = $to") or die(mysql_error());
}
function fork($shellCmd) {
    exec("$shellCmd > /dev/null 2>&1 &");
}
function getName($id){
	$other = $db->fetch_assoc($db->query("SELECT name FROM uber3_players WHERE id = ". $id));
	$otherName = $other['name'];
	if(preg_match("[a-zA-Z0-9_]", $otherName)){
		$otherName.=" (valid)";
	}
	return $otherName;
}
?>
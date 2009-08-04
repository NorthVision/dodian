<?php
header("Content-type: image/png");
$name = addslashes($_GET['name']);
define('ROOT', './');
include ROOT.'common/common.php';
$q = $db->query("SELECT * FROM uber3_players WHERE name LIKE '" . $name. "'");
$f = $db->fetch_assoc($q);
if(!$f){
	include("error_dynamic.png");
	die();
}
$sT = $f['total'];
$rank = 1;
$id = $f['id'];
$tq = @mysql_query("SELECT * FROM uber3_players WHERE total > $sT AND mgroup != 4 AND id != $id AND banned = 0");
while($tR = @mysql_fetch_array($tq)){
	$rank++;
}
$skills = array("attack", "defence", "strength", "hitpoints", "range", "prayer", "magic", "cooking", "woodcutting", "fletching", "fishing", "firemaking", "crafting", "smithing", "mining", "thieving");
$posx = array("25", "25", "25", "25", "25", "62", "62", "62", "62", "62", "100", "100", "100", "100", "100", "135");
$posy = array("6", "24", "42", "59", "77", "6", "24", "42", "59", "77", "6", "24", "42", "59", "77", "6");
$img = imagecreatefrompng("./dynamic.png");
$color = imagecolorallocate($img, 255, 255, 255);
$font = 3;
imagestring($img, $font, 158, 18, $f['name'], $color);
imagestring($img, $font, 192, 40, $f['combat'], $color);
imagestring($img, $font, 183, 51, $f['total'] ." (#$rank)", $color);
for($i = 0; $i < 16; $i++){
	$level = getLevel($f[$skills[$i]]);
	if($level < 10){
		$level = "0". getLevel($f[$skills[$i]]);
	}
	imagestring($img, $font, $posx[$i], $posy[$i], $level, $color);
}
imagepng($img);
imagedestroy($img);
function getLevel($xp){
	$lvl =1;
	while($xp > $done){
	$exp = $exp + floor($lvl + 300 * pow(2, ($lvl / 7)));

	$lvl = $lvl + 1;
	$done = floor($exp / 4);
	}
	if($lvl > 100){
	$lvl = 100;
	}
	$stuff = $lvl;
	if($xp < 83){
	$stuff = 2;
	}
	return $stuff - 1;
}

?>
<?php

	function error($s) {
		global $db;
		if($db) {
			$db->close();
		}
		exit($s);
	}

	include ROOT.'conf/config.php';
	include ROOT.'lib/data_conversions.php';
	include ROOT.'lib/mysql.php';
	$db = new DBLayer($config['mysqlhost'], $config['mysqluser'], $config['mysqlpass'], $config['mysqldb'], '', false);
	
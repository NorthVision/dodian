<?php
	
	$statNames = array('attack', 'defense', 'strength', 'hits', 'ranged', 'prayer', 'magic', 'cooking', 'woodcut', 'fletching', 'fishing', 'firemaking', 'crafting', 'smithing', 'mining', 'herblaw', 'agility', 'thieving');
	
	$experienceArray = array();
	for($index = 0, $exp = 0;$index < 99;$index++) {
		$offset = intval(($index + 1) + 300 * pow(2, ($index + 1) / 7));
		$exp += $offset;
		$experienceArray[$index] = ($exp & 0xffffffc) / 4;
	}

	function usernameToHash($s) {
		$s = strtolower($s);
		$s1 = '';
		for ($i = 0;$i < strlen($s);$i++) {
			$c = $s{$i};
			if ($c >= 'a' && $c <= 'z')
				$s1 = $s1.$c;
			else if ($c >= '0' && $c <= '9')
				$s1 = $s1.$c;
			else
				$s1 = $s1.' ';
		}

		$s1 = trim($s1);
		if (strlen($s1) > 12)
			$s1 = substring($s1, 0, 12);

		$l = 0;
		for ($j = 0;$j < strlen($s1);$j++) {
			$c1 = $s1{$j};
			$l *= 37;
			if ($c1 >= 'a' && $c1 <= 'z')
				$l += (1 + ord($c1)) - 97;
			else if ($c1 >= '0' && $c1 <= '9')
				$l += (27 + ord($c1)) - 48;
		}
		return $l;
	}

	function hashToUsername($l) {
		if ($l < 0)
			return 'invalid_name';
		$s = '';
		while ($l != 0) {
			$i = floor(floatval($l % 37));
			$l = floor(floatval($l / 37));
			if ($i == 0)
				$s = ' '.$s;
			else if ($i < 27) {
				if ($l % 37 == 0)
					$s = chr(($i + 65) - 1).$s;
				else
					$s = chr(($i + 97) - 1).$s;
			}
			else {
				$s = chr(($i + 48) - 27).$s;
			}
		}
		return $s;
	}
	
	function experienceToLevel($exp) {
		global $experienceArray;
		for($level = 0;$level < 98;$level++) {
			if($exp >= $experienceArray[$level]) {
				continue;
			}
			return ($level + 1);
		}
		return 99; // This means were over the highest level there is
	}
	
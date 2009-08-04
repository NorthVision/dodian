<?php
	
	define('ROOT', './');
	include ROOT.'common/common.php';
	include ROOT.'common/header.php';

	if(isset($_POST['submit'])) {
		$errors = array();
		
		$username = trim($_POST['username']);
		$password = trim($_POST['password1']);

		if(trim($_POST['password2']) != $password) {
			$errors[] = 'The passwords provided do not match.';
		}
		
		$usernameLength = strlen($username);
		if($usernameLength < 1 || $usernameLength > 12) {
			$errors[] = 'username too short or too long... (1-12 chars)';
		}

		$passwordLength = strlen($password);
		if($passwordLength < 1 || $passwordLength > 20) {
			$errors[] = 'password too short or too long (1-20 chars) if you make it 1 char hf getting hacked.';
		}
	

		$result = $db->query('SELECT `name` FROM `uber3_players` WHERE `name` = \''.$db->escape(ucwords($username)).'\'');
		if($db->num_rows($result)) {
			$errors[] = 'That username is already taken.';
		}

		if(!empty($errors)) {
			echo "<fieldset class=\"menu main\">
				<legend>Registering Error</legend>";
				$html = print_r($errors, true);
    				$html = str_replace( " ","&nbsp;", $html);
    				$html = str_replace( "\r\n","<br>\r\n", $html);
    				$html = str_replace( "\r","<br>\r", $html);
    				$html = str_replace( "\n","<br>\n", $html);
				echo $html;
			echo "<p><em>Go <a href=\"register.php\">back</a>.</em></p><br />
			</fieldset>";		
			include ROOT.'common/footer.php';
			$db->close();
			exit;
                }
		$result = $db->query('INSERT INTO `uber3_players` (name) VALUES(\''.$db->escape(ucwords($username)).'\')') or die($db->error()); // We don't need to escape any of these, they are all messed with already
		if (!$result) {
		   echo "Unknown error putting new user in database.";
		   exit;
		}

		$row = $db->fetch_row($db->query('SELECT `id` FROM `uber3_players` WHERE `name` = \''.$db->escape(ucwords($username)).'\''));
		$id = $row[0];
		$salt = substr(uniqid(rand(), true), 0, 5);
		$passcomputed = md5(md5($salt) . md5($password));
		$db->query('INSERT INTO uber3_converge (`converge_id`,`converge_pass_salt`,`converge_pass_hash`) VALUES(\''.$id.'\',\''.$salt.'\',\''.$passcomputed.'\')') or die($db->error());
		
		echo "<fieldset class=\"menu main\">
			<legend>Registering Complete!</legend>
		        User: <b>" .htmlspecialchars($username)."</b> has been created, you may now login!
		</fieldset>";		
		include ROOT.'common/footer.php';
		$db->close();
		exit;
	}
		
?>
<fieldset class="menu main">
	<legend>Register</legend>
	<p><em>By registering you are agreeing to abide by our <a href="rules.php">rules</a>. Please make sure you've read them first.</em></p><br />
	<form action="<?php echo basename($_SERVER['PHP_SELF']); ?>" method="POST">
		<table>
			<tr>
				<td>Username:</td>
				<td><input type="text" name="username" value="" /></td>
			</tr>
			<tr>
				<td>Password:</td>
				<td><input type="password" name="password1" value="" /></td>
			</tr>
			<tr>
				<td>Confirm Password:</td>
				<td><input type="password" name="password2" value="" /></td>
			</tr>
			<tr>
				<td>Recovery secret phrase:</td>
				<td><input type="text" name="recovery" value="" /></td>
			</tr>
			<tr>
				<td colspan="2"><input type="submit" name="submit" value="Register" /></td>
			</tr>
		</table>
	</form>
</fieldset>
<?php

	include ROOT.'common/footer.php';
	$db->close();
	exit;
	
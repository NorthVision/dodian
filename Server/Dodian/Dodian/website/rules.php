<?php

	define('ROOT', './');
	include ROOT.'common/common.php';
	include ROOT.'common/header.php';
	
?>
<fieldset class="menu main">
<legend>Rules</legend>
<center>
No faggots allowed<br>
Go back to register page <a href=register.php>click here</a><br>
</center>
</fieldset>
<?php

	include ROOT.'common/footer.php';
	$db->close();
	exit;
	
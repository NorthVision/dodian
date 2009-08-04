<?php

	define('ROOT', './');
	include ROOT.'common/common.php';
	include ROOT.'common/header.php';
	
?>
<fieldset class="menu main">
	<?php include ROOT.'news.php'; ?>
</fieldset>
<?php

	include ROOT.'common/footer.php';
	$db->close();
	exit;
	
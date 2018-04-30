
<?php
	if($_SERVER['REQUEST_METHOD']=='POST'){
		
        $email = $_POST['mail'];

        require_once('DB_Connect.php');
        $db = new Db_Connect();
        $conn = $db->connect();

        $sql = "SELECT profile_image FROM users WHERE email = '$email'";

		$stmt = mysqli_prepare($conn,$sql);
		
        mysqli_stmt_execute($stmt);
        
        $user_profile_image = $stmt->get_result()->fetch_assoc();
       
        echo $user_profile_image["profile_image"];

/**
 *                                  another try 
 *https://stackoverflow.com/questions/30277570/get-blob-image-from-mysql-database-via-json?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
 *http://wiki.hashphp.org/PDO_Tutorial_for_MySQL_Developers
 *http://php.net/manual/en/mysqli.query.php

        $query = "SELECT profile_image FROM users WHERE email = '$email'"; 
        $result = mysqli_query($conn, $query, MYSQLI_USE_RESULT); 
        $photo = mysqli_fetch_array($result); 
        header('Content-Type:image/jpeg'); 
        echo $photo['profile_image']; 
        
*
*/
		mysqli_close($conn);
	}else{
		echo "Error";
      }












?>


 
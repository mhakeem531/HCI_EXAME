<?php
	if($_SERVER['REQUEST_METHOD']=='POST'){
		
        $image = $_POST['image'];
        $email = $_POST['mail'];

        require_once('DB_Connect.php');
        $db = new Db_Connect();
        $conn = $db->connect();

        $sql = "UPDATE users SET profile_image = '$image' WHERE email = '$email'";

		$stmt = mysqli_prepare($conn,$sql);
		
		mysqli_stmt_execute($stmt);
		
		$check = mysqli_stmt_affected_rows($stmt);

		if($check === 1){
            echo "Image Uploaded Successfully";
            
		}else{
            echo "Error Uploading Image";
		}
		mysqli_close($conn);
	}else{
		echo "Error";
      }
?>
<?php

// db information
$db_host = "localhost";  
$db_id = "serviceUser";
$db_password = "honbabService2User*)";
$db_dbname = "chat";

// connect db
$db_conn = mysqli_connect ( $db_host, $db_id, $db_password ) or die ( "Fail to connect database!!" );
mysqli_select_db ( $db_conn, $db_dbname );

// load report count
$uuid = $_POST['uuid'];
$cash = $_POST['cash'];

$updateQuery = "update user set cash = '".$cash."' where uuid = '".$uuid."'";

$updateResult = mysqli_query ( $db_conn, $updateQuery, MYSQLI_STORE_RESULT );

if(!updateResult){
    echo "failed";
}else{
    echo "succeeded";
}

// close db
mysqli_close ( $db_conn );

?>

<?php

error_reporting(E_ALL);
ini_set("display_errors", 1);

// db information
$db_host = "localhost";  
$db_id = "serviceUser";  
$db_password = "honbabService2User*)";
$db_dbname = "chat";

// connect db  
$db_conn = mysqli_connect ( $db_host, $db_id, $db_password ) or die ( "Fail to connect database!!" );
mysqli_select_db ( $db_conn, $db_dbname );  

// get data
$uuid = $_POST["uuid"];

//check start
$deleteQuery = "delete from queue where uuid = '".$uuid."'";
$deleteResult = mysqli_query ( $db_conn, $deleteQuery, MYSQLI_STORE_RESULT ) or die(mysqli_error($db_conn));

if(!$deleteResult){
    echo "error";
}else{
    echo "success";
}

// close db  
mysqli_close ( $db_conn );

?>
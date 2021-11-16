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
$checkQuery = "select * from user where uuid = '".$uuid."' ";

$checkResult = mysqli_query ( $db_conn, $checkQuery, MYSQLI_STORE_RESULT );

$row = mysqli_fetch_assoc ( $checkResult );

$resultArray = array (
    "cash" => $row["cash"]
    );

print_r ( urldecode ( json_encode ( $resultArray ) ) );

// close db
mysqli_close ( $db_conn );

?>

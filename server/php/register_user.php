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
$gender = $_POST['gender'];
$roomName = $_POST['roomName'];

$checkQuery = "select * from queue"

$checkResult = mysqli_query ( $db_conn, $checkQuery, MYSQLI_STORE_RESULT );

$checkTotal = mysqli_num_rows($checkResult)

if($checkTotal == 0){
    //waiting
    $insertQuery = "INSERT INTO queue(uuid, gender, room,) VALUES('".$uuid."', '".$gender."', '".$roomName."')";


}else{
    //match

}

// close db
mysqli_close ( $db_conn );

?>

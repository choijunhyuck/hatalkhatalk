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
$data = $_POST['data'];
$email = $_POST['email'];

$insertQuery = "insert into question(data, email) values('".$data."', '".$email."')";

$insertResult = mysqli_query ( $db_conn, $insertQuery, MYSQLI_STORE_RESULT );

if(!$insertResult){
    echo "error";
}else{
    echo "succeeded";
}

// close db
mysqli_close ( $db_conn );

?>

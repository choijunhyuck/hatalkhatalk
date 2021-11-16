<?php

// db information
$db_host = "localhost";  
$db_id = "root";  
$db_password = "Alomteamd11";
$db_dbname = "chat";

// connect db
$db_conn = mysqli_connect ( $db_host, $db_id, $db_password ) or die ( "Fail to connect database!!" );
mysqli_select_db ( $db_conn, $db_dbname );

// load report count
$uuid = $_POST['uuid'];
$loadquery = "select report from user where uuid = '".$uuid."'";

$load_result = mysqli_query ( $db_conn, $loadquery, MYSQLI_STORE_RESULT );

$row = mysqli_fetch_assoc ( $load_result );
$count = $row['report'];

// update report
$updated_count = $count+1;

$updatequery = "update user set report = '".$updated_count."' where uuid = '".$uuid."'";

$update_result = mysqli_query ( $db_conn, $updatequery, MYSQLI_STORE_RESULT );

if ( !$load_result || !$update_result ) {
    echo ( "failed" );
} else {
    echo ( "succeeded" );
}

// close db
mysqli_close ( $db_conn );

?>

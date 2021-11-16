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
$gender = $_POST["gender"];

$findG = 0;

if($gender == 1){
    $findG = 2;
}else{
    $findG = 1;
}

//check start
$checkQuery = "select * from queue where gender = '".$findG."'";
$checkResult = mysqli_query ( $db_conn, $checkQuery, MYSQLI_STORE_RESULT ) or die(mysqli_error($db_conn));

if(mysqli_num_rows($checkResult) == 0){
    
    print_r ( urldecode ( json_encode ( "empty" ) ) );

}else{
    
    //matching
    $row = mysqli_fetch_assoc ( $checkResult );
    
    $resultArray = array (
        "uuid" => $row["uuid"],
        "gender" => $row["gender"],
        "roomName" => $row["room"]
        );
    
    //delete queue
    $deleteQuery = "delete from queue WHERE uuid = '".$row["uuid"]."'";
    $deleteResult = mysqli_query ( $db_conn, $deleteQuery, MYSQLI_STORE_RESULT );
    
    print_r ( urldecode ( json_encode ( $resultArray ) ) );

}

// close db  
mysqli_close ( $db_conn );

?>
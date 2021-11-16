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

$cashQuery = "select * from user where uuid = '".$uuid."'";
$cashResult = mysqli_query ( $db_conn, $cashQuery, MYSQLI_STORE_RESULT ) or die(mysqli_error($db_conn));

$row1 = mysqli_fetch_assoc ( $cashResult );
$cash = (int)$row1["cash"];

$cash_temp = (int)$row1["cash"] - 50;

//if($cash >= 50)
if(true){
    
    //check start
    $checkQuery = "select * from queue where gender = '".$findG."'";
    $checkResult = mysqli_query ( $db_conn, $checkQuery, MYSQLI_STORE_RESULT ) or die(mysqli_error($db_conn));
    
    if(mysqli_num_rows($checkResult) == 0){
        
        print_r ( urldecode ( json_encode ( "empty" ) ) );
    
    }else{
        
        //matching
        $row2 = mysqli_fetch_assoc ( $checkResult );
        
        $resultArray = array (
            "uuid" => $row2["uuid"],
            "gender" => $row2["gender"],
            "roomName" => $row2["room"]
            );
        
        //delete queue
        $deleteQuery = "delete from queue WHERE uuid = '".$row2["uuid"]."'";
        $deleteResult = mysqli_query ( $db_conn, $deleteQuery, MYSQLI_STORE_RESULT );

        //update cash
        $updateQuery = "update user set cash = '".$cash_temp."' where uuid = '".$uuid."'";
        $updateResult = mysqli_query ( $db_conn, $updateQuery, MYSQLI_STORE_RESULT );
        
        print_r ( urldecode ( json_encode ( $resultArray ) ) );
    
    }
}else{
    print_r ( urldecode ( json_encode ( "not enough money" ) ) );
}

// close db  
mysqli_close ( $db_conn );

?>

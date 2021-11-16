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
$room = $_POST["roomName"];

//check start
$checkQuery = "select * from queue";
$checkResult = mysqli_query ( $db_conn, $checkQuery, MYSQLI_STORE_RESULT ) or die(mysqli_error($db_conn));

if( mysqli_num_rows($checkResult) == 0 ){

        //insert Queue
        $insertQuery = "insert into queue(uuid,gender,room) VALUES( '".$uuid."', '".$gender."', '".$room."' )";
        $insertResult = mysqli_query ( $db_conn, $insertQuery, MYSQLI_STORE_RESULT );
        
        if(!$insertResult){
            echo "error";
        }else{
            echo "waiting";
        }

}else{

    //duplicate check
    $row = mysqli_fetch_assoc ( $checkResult );

    if($row["uuid"] == $uuid ){
        
        //same uuid & update queue
        $updateQuery = "update queue set room='".$room."' where uuid='".$uuid."'";
        $updateResult = mysqli_query ( $db_conn, $updateQuery, MYSQLI_STORE_RESULT );
        
        if(!$updateResult){
            echo "error";
        }else{
            echo "waiting";
        }

    }else{

        //match
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

}

// close db  
mysqli_close ( $db_conn );

?>
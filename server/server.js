const express = require('express')
const path = require('path')
var app = express();

//Generate Http Server
var http = require('http').Server(app);

//Http Server to Socket.io Server
var io = require('socket.io')(http);

//Use 5000 port
app.set('port', (process.env.PORT || 5000));

console.log("Ready To Connect");

//Functions
io.on('connection', function(socket){
  
  //When user connected
  console.log('User Conncetion');
  
  socket.on('connect user', function(user){
    console.log("Connected user ");
    socket.join(user['roomName']);
    console.log("roomName : ",user['roomName']);
    console.log("state : ",socket.adapter.rooms);
    io.emit('connect user', user);
  });
 
  //When user sended message
  socket.on('chat message', function(msg){
    console.log("Message " + msg['script']);
    console.log("sent from : ",msg['uuid']);
    io.to(msg['roomName']).emit('chat message', msg);
  });

  //When user disconnected
  socket.on('user left', function(msg){
    console.log("uuid " + msg['uuid']);
    console.log("sent form : ",msg['uuid']);
    io.to(msg['roomName']).emit('user left', msg);
  });

});

//Print server information
http.listen(app.get('port'), function() {
  console.log('Node app is running on port', app.get('port'));
});
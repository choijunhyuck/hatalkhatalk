const express = require('express')
const path = require('path')
var app = express();
var http = require('http').Server(app);
var io = require('socket.io')(http);

app.set('port', (process.env.PORT || 5000));

app.use(express.static(__dirname + '/public'));
 
// views is directory for all template files
app.set('views', __dirname + '/views');
app.set('view engine', 'ejs');
 
//서버연결하기 직전에뜸
console.log("outside io");

io.on('connection', function(socket){

  //로그인하면 이거 밑에 두개뜸
  console.log('User Conncetion');
  
  socket.on('connect user', function(user){
    console.log("Connected user ");
    socket.join(user['roomName']);
    console.log("roomName : ",user['roomName']);
    console.log("state : ",socket.adapter.rooms);
    io.emit('connect user', user);
  });
 
  //메세지 입력하면 서버 로그에 이거뜸
  socket.on('chat message', function(msg){
    console.log("Message " + msg['script']);
    console.log("보내는 아이디 : ",msg['uuid']);
    io.to(msg['roomName']).emit('chat message', msg);
  });

  socket.on('user left', function(msg){
    console.log("uuid " + msg['uuid']);
    console.log("보내는 아이디 : ",msg['uuid']);
    io.to(msg['roomName']).emit('user left', msg);
  });

});

//맨처음에 서버 연결하면 몇번포트에 서버 연결되어있는지 ㅇㅇ
http.listen(app.get('port'), function() {
  console.log('Node app is running on port', app.get('port'));
});

Description
===========
This is a simple sample of WebSocket Server Connector of Ballerina.
This will send you back the same text that you sent.
If you send "closeMe" then server will close the connection from the server side for the given connection.

How to run the sample
=====================
bin$ ./ballerina run service ../samples/websocket/echoServer/server/websocketEchoServer.bal

You can use the simple web app client ../samples/websocket/echoServer/client/index.html to check the capability.
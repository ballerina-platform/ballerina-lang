Description
===========
This is a simple sample of WebSocket Server Connector of Ballerina.
This broadcast received text from a connection to all the connections in the endpoint.

How to run the sample
=====================
bin$ ./ballerina run ../samples/websocket/chatapp/ChatApp.bal

To check the sample,use Chrome or Firefox javascript console and run the below commands.
Run first 3 lines of the below code in two or more consoles and see how the messages are received by sending messages
$ var ws = new WebSocket("ws://localhost:9090/chat/ws");
$ ws.onmessage = function(frame) {console.log(frame.data)};
$ ws.onclose = function(frame) {console.log(frame)};

To send messages
$ ws.send("hello world");

To close the connection
$ ws.close(1000, "I want to go");

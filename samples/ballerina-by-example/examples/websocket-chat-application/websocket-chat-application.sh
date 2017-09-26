# To run the program, put the code in `websocket-basic-functions.bal`
# and use `ballerina run websocket-basic-functions.bal` command.
$ ballerina run websocket-chat-app.bal

# To check the sample,use Chrome or Firefox javascript console and run the below commands <br>
# Run first 3 lines of the below code in two or more consoles and see how the messages are received by sending messages
$ var ws = new WebSocket("ws://localhost:9090/chat/ws");
$ ws.onmessage = function(frame) {console.log(frame.data)};
$ ws.onclose = function(frame) {console.log(frame)};

# To send messages
$ ws.send("hello world");

#To close the connection
$ ws.close(1000, "I want to go");

# To close connection from server side
$ ws.send("closeMe");


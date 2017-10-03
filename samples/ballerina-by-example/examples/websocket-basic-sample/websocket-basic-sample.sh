# To run the program, put the code in `websocket-basic-functions.bal`
# and use `ballerina run websocket-basic-sample.bal` command.
$ ballerina run websocket-basic-sample.bal

# To check the sample,use Chrome or Firefox javascript console and run the below commands <br>
# change "xml" to another sub protocol to observe the behavior of WebSocket server.
$ var ws = new WebSocket("ws://localhost:9090/basic/ws", "xml", "my-protocol");

$ ws.onmessage = function(frame) {console.log(frame.data)};
$ ws.onclose = function(frame) {console.log(frame)};

# To send messages
$ ws.send("hello world");

#To close the connection
$ ws.close(1000, "I want to go");

# To close connection from server side
$ ws.send("closeMe");


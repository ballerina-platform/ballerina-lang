# To run the program, put the code in `websocket-basic-functions.bal`
# and use `ballerina run service websocket-basic-functions.bal` command.
$ ballerina run service websocket-basic-functions.bal
# Now that we can run this program using any WebSocket client with "ws://host:port/functions/ws"

# To check the sample, you can use Chrome or Firefox javascript console and run the below commands <br>
$ var ws = new WebSocket("ws://localhost:9090/functions/ws");
$ ws.onmessage = function(frame) {console.log(frame.data)};
$ ws.onclose = function(frame) {console.log(frame)};

# To send messages
$ ws.send("hello world");

# To check closeConnection()
$ ws.send("closeMe");

#To close the connection
$ ws.close();

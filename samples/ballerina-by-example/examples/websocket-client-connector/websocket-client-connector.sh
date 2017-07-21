# To run the program, put the code in `websocket-client-connector`
# and use `ballerina run websocket-client-connector` command.
$ ballerina run websocket-client-connector.bal

# To check the sample, use Chrome or Firefox javascript console and run the below commands <br>
# To check the broadcast run first 3 scripts in more than 2 consoles and check.
$ var ws = new WebSocket("ws://localhost:9090/client-connector/ws");
$ ws.onmessage = function(frame) {console.log(frame.data)};
$ ws.onclose = function(frame) {console.log(frame)};

# To send messages
$ ws.send("hello world");

# To check the closeConnection()
$ ws.send("closeMe");

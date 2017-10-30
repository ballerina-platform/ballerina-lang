# To run the program, put the code in `http-to-websocket-upgrade.bal`
# and use `ballerina run http-to-websocket-upgrade.bal` command.
$ ballerina run http-to-websocket-upgrade.bal

# To check the sample,use Chrome or Firefox javascript console and run the below commands <br>
# change "xml" to another sub protocol to observe the behavior of WebSocket server.
# There are two endpoints for this WebSocket sample since it is configured that way.
$ var ws = new WebSocket("ws://localhost:9090/hello/ws", "xml", "my-protocol");
$ var ws = new WebSocket("ws://localhost:9090/world/ws", "xml", "my-protocol");

$ ws.onmessage = function(frame) {console.log(frame.data)};
$ ws.onclose = function(frame) {console.log(frame)};

# To send messages
$ ws.send("hello world");

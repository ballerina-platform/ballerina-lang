# To run the program, put the code in `websocket-connection-store.bal`
# and use `ballerina run websocket-connection-store.bal`.
$ ballerina run websocket-connection-store.bal
# Make sure to use multiple clients for this example (at least 2 clients)
# sample json string: {"command":"send", "id":"0", "msg":"hi 0"}

# To check the sample, you can use Chrome or Firefox javascript console and run the below commands <br>
$ var ws = new WebSocket("ws://localhost:9090/store/ws");
$ ws.onmessage = function(frame) {console.log(frame.data)};
$ ws.onclose = function(frame) {console.log(frame)};

# To send messages to connection 0
$ ws.send('{"command":"send", "id":"0", "msg":"hi 0"}');

# To remove this connection 1
$ ws.send('{"command":"remove", "id":"1", "msg":"remove 1"}');

# To close connection 0
$ ws.send('{"command":"close", "id":"0", "msg":"close 0"}');

# To run the program, put the code in `websocket-basic-endpoint.bal`
# and use `ballerina run service websocket-basic-endpoint.bal` command.
$ ballerina run service websocket-basic-endpoint.bal
# Now that we can run this program using any WebSocket client with "ws://host:port/endpoint/ws"

# To check the sample, you can use Chrome or Firefox javascript console and run the below commands <br>
$ var ws = new WebSocket("ws://localhost:9090/endpoint/ws");
$ ws.onmessage = function(frame) {console.log(frame.data)};
$ ws.onclose = function(frame) {console.log(frame)};
# To send messages
$ ws.send("hello world");
#To close the connection
$ ws.close();

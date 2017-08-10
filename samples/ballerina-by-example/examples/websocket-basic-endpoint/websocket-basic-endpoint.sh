# To run the program, put the code in `websocket-basic-endpoint.bal`
# and use `ballerina run websocket-basic-endpoint.bal` command.
$ ballerina run websocket-basic-endpoint.bal

# To invoke the service, use Chrome or Firefox javascript console and run the below commands <br>
$ var ws = new WebSocket("ws://localhost:9090/endpoint/ws");
$ ws.onmessage = function(frame) {console.log(frame.data)};
$ ws.onclose = function(frame) {console.log(frame)};
# To send messages
$ ws.send("hello world");
#To close the connection
$ ws.close();

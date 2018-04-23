# To run the program, copy the code to a file named `http_to_websocket_upgrade.bal`
# and use the `ballerina run http_to_websocket_upgrade.bal` command.
$ ballerina run http_to_websocket_upgrade.bal

# To check the sample, use the Chrome or Firefox javascript console and run the below commands. <br>
# Change "xml" to another sub protocol to observe the behavior of the WebSocket server.
# There are two endpoints for this WebSocket sample as it is configured that way.
$ var ws = new WebSocket("ws://localhost:9090/hello/ws", "xml", "my-protocol");

$ ws.onmessage = function(frame) {console.log(frame.data)};
$ ws.onclose = function(frame) {console.log(frame)};

# Send messages.
$ ws.send("hello world");

#To call the http resource you can use curl command
$ curl -H "Content-Type: text/plain" -X POST -d 'Hello World!!' 'http://localhost:9090/hello/world'
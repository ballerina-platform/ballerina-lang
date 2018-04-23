#At the command line, navigate to the directory that contains the
#`websocket-basic-sample.bal` file and run the `ballerina run` command.
$ ballerina run websocket-basic-sample.bal

# To check the sample, use a Chrome or Firefox JavaScript console and run the following commands: <br>
# Change `xml` to another sub protocol to observe the behavior of the WebSocket server.
$ var ws = new WebSocket("ws://localhost:9090/basic/ws", "xml", "my-protocol");

$ ws.onmessage = function(frame) {console.log(frame.data)};
$ ws.onclose = function(frame) {console.log(frame)};

# Send a message
$ ws.send("hello world");

# Use a more advance client to check the ping pong since the browser client does not have the capability to send pings.
# Check the the behavior when the server sends a ping message to the browser client use the following command
$ ws.send("ping");

#Close the connection
$ ws.close(1000, "I want to go");

# Close the connection from the server side:
$ ws.send("closeMe");

# To check the connection closure due to connection timeout wait for 120 seconds
# or change the timeout in the configuration annotation.


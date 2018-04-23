# To run the program, put the code in `websocket-basic-application.bal`
# and use `ballerina run websocket_chat_application.bal` command.
$ ballerina run websocket_chat_application.bal

# To check the sample, use Chrome or Firefox JavaScript console and run the following commands <br>
# Run the first 3 lines of the following code in two or more different consoles and see how the messages are received
# Change the names and/or age in the URI `/chat/fistName?age` so that they are different for each client
$ var ws = new WebSocket("ws://localhost:9090/chat/bruce?age=30");
$ ws.onmessage = function(frame) {console.log(frame.data)};
$ ws.onclose = function(frame) {console.log(frame)};

# To send messages
$ ws.send("hello world");

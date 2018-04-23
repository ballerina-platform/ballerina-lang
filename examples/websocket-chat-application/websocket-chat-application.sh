# To run the program, put the code in `websocket-basic-application.bal`
# and use `ballerina run websocket-basic-application.bal` command.
$ ballerina run websocket-chat-application.bal

# To check the sample, use Chrome or Firefox JavaScript console and run the following commands <br>
# Run the first 3 lines of the below code in two or more consoles and see how the messages are received by sending messages
# To check the capability of change the names the URI such that /chat/fistName+LastName/age with multiple clients
var ws = new WebSocket("ws://localhost:9090/chat/bruce?age=30");
ws.onmessage = function(frame) {console.log(frame.data)};
ws.onclose = function(frame) {console.log(frame)};

# To send messages
ws.send("hello world");

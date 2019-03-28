import ballerina/test;
import ballerina/http;

channel<string> serviceReply = new;
string msg = "hey";

@test:Config
function testText() {
    http:WebSocketClient wsClient = new("ws://localhost:9090/basic/ws", config = {callbackService:callback, subProtocols:["xml", "my-protocol"]});
    error? result = wsClient->pushText(msg);
    if (result is error) {
        log:printError("Error occurred when pushing text", err = result);
    }
    string wsReply = <- serviceReply;
    test:assertEquals(wsReply, "You said: " + msg, msg = "Received message should be equal to the expected message");
}

service callback = @http:WebSocketServiceConfig {} service {
    resource function onText(http:WebSocketClient conn, string text, boolean finalFrame) {
        text -> serviceReply;
    }
};

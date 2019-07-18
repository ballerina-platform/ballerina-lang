import ballerina/test;
import ballerina/http;

channel<string> serviceReply = new;
string msg = "hey";

@test:Config {
    enable: false
}
function testText() {
    http:WebSocketClient wsClient = new("ws://localhost:9090/chat/bruce?age=30", {callbackService:callback});
    string wsReply = <- serviceReply;
    test:assertEquals(wsReply, "Hi bruce! You have successfully connected to the chat",
    "Received message should be equal to the expected message");
    checkpanic wsClient->pushText(msg);
    wsReply = <- serviceReply;
    test:assertEquals(wsReply, "bruce: " + msg, "Received message should be equal to the expected message");
}

service callback = @http:WebSocketServiceConfig {} service {
    resource function onText(http:WebSocketClient conn, string text, boolean finalFrame) {
        text -> serviceReply;
    }
};

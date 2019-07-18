import ballerina/test;
import ballerina/http;

channel<string> serviceReply = new;
string msg = "hey";

@test:Config {
    enable: false
}
function testText() {
    http:WebSocketClient wsClient = new("ws://localhost:9090/proxy/ws", {callbackService:callback});
    checkpanic wsClient->pushText(msg);
    string wsReply = <- serviceReply;
    test:assertEquals(wsReply, msg, "Received message should be equal to the expected message");
}

service callback = @http:WebSocketServiceConfig {} service {
    resource function onText(http:WebSocketClient conn, string text, boolean finalFrame) {
        text -> serviceReply;
    }
};

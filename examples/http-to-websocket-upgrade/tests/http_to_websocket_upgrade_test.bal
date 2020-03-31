import ballerina/http;
import ballerina/log;
import ballerina/test;

channel<string> serviceReply = new;
string msg = "hey";
@test:Config
function testWebSocket() {
    http:WebSocketClient wsClient = new("ws://localhost:9090/hello/ws", config = {callbackService:callback});
    error? result = wsClient->pushText(msg);
    if (result is error) {
        log:printError("Error in sending text", result);
    }
    string wsReply = <- serviceReply;
    test:assertEquals(wsReply, msg, msg = "Received message should be equal to the expected message");
}

@test:Config
function testHttp() returns error? {
    http:Client httpClient = new("http://localhost:9090");
    http:Response resp = check httpClient->post("/hello/world", msg);
    test:assertEquals(check resp.getTextPayload(), "HTTP POST received: " + msg, msg = "Received message should be equal to the expected message");
}
service callback = @http:WebSocketServiceConfig {} service {
    resource function onText(http:WebSocketClient conn, string text, boolean finalFrame) {
        text -> serviceReply;
    }
};

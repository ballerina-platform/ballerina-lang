import ballerina/lang.messages;
import ballerina/http;
import ballerina/net.ws;

@http:configuration {basePath:"/test"}
@ws:WebSocketUpgradePath {value:"/websocket"}
service<ws> testEndpoint {

    @ws:OnOpen {}
    resource onOpen(message m) {
        ws:broadcastText("new client connected");
    }

    @ws:OnTextMessage {}
    resource onTextMessage(message m) {
        string  text = messages:getStringPayload(m);
        if ("closeMe" == text) {
            ws:closeConnection();
        }
        ws:pushText(messages:getStringPayload(m));
    }

    @ws:OnClose {}
    resource onClose(message m) {
        ws:broadcastText("client left");
    }
}
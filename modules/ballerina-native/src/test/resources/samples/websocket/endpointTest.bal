import ballerina.lang.messages;
import ballerina.net.http;
import ballerina.net.ws;

@http:BasePath {value:"/test"}
@ws:WebSocketUpgradePath {value:"/websocket"}
service testEndpoint {

    @ws:OnOpen {}
    resource onOpen(message m) {
        ws:broadcastText("new client connected");
    }

    @ws:OnTextMessage {}
    resource onTextMessage(message m) {
        ws:pushText(messages:getStringPayload(m));
    }

    @ws:OnClose {}
    resource onClose(message m) {
        ws:broadcastText("client left");
    }
}
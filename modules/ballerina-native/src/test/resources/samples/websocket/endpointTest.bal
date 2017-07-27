import ballerina.lang.messages;
import ballerina.lang.strings;
import ballerina.net.http;
import ballerina.net.ws;

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
        } else if ("binary" == text) {
            blob b = strings:toBlob(text, "UTF-8");
            ws:pushBinary(b);
        }
        ws:pushText(messages:getStringPayload(m));
    }

    @ws:OnClose {}
    resource onClose(message m) {
        ws:broadcastText("client left");
    }
}
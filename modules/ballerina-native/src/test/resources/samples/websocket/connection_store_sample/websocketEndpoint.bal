import ballerina.lang.system;
import ballerina.net.ws;
import ballerina.net.http;
import ballerina.lang.messages;

@http:BasePath {value:"/chat-store"}
@ws:WebSocketUpgradePath {value:"/ws"}
service websocketEndpoint {

    @ws:OnOpen {}
    resource onOpen(message m) {
        string id = messages:getHeader(m, "id");
        ws:storeConnection(id);
    }

    @ws:OnClose {}
    resource onClose(message m) {
        system:println("client left the server.");
        ws:broadcastText("client left the server.");
    }
}

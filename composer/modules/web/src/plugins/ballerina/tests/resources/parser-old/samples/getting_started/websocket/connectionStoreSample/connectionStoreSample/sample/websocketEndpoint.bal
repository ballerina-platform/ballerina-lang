
import ballerina/lang.system;
import ballerina/http;
import ballerina/net.ws;

@http:configuration {basePath:"/store"}
@ws:WebSocketUpgradePath {value:"/ws"}
service<ws> websocketEndpoint {
    int i = 0;

    @ws:OnOpen {}
    resource onOpen(message m) {
        string id = "" + i;
        ws:storeConnection(id);
        i = i + 1;
    }

    @ws:OnClose {}
    resource onClose(message m) {
        system:println("client left the server.");
        ws:broadcastText("client left the server.");
    }
}

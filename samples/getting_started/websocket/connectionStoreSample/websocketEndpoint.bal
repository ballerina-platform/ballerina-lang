import ballerina.lang.system;
import ballerina.net.http;
import ballerina.net.ws;

@http:BasePath {value:"/store"}
@ws:WebSocketUpgradePath {value:"/ws"}
service websocketEndpoint {
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

import ballerina.lang.system;
import ballerina.net.ws;

@http:BasePath {value:"/chat"}
@ws:WebSocketUpgradePath {value:"/"}
service websocketEndpoint {
    int i = 1;

    // Store each connection under different id
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

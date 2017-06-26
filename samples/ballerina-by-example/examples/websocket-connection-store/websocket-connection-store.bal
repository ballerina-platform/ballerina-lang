import ballerina.lang.system;
import ballerina.lang.messages;
import ballerina.net.http;
import ballerina.net.ws;
import ballerina.lang.jsons;
import ballerina.doc;
import samples.post_m1.data_types.json;

@http:BasePath {value:"/endpoint"}
@ws:WebSocketUpgradePath {value:"/ws"}
service echoServer {

    int i = 0;

    @ws:OnOpen {}
    resource onOpen(message m) {
        string connectionID = "" + i;
        ws:storeConnection(connectionID);
        i = i + 1;
    }

    @ws:OnTextMessage {}
    resource onTextMessage(message m) {
        json payload = messages:getJsonPayload(m);
        string command = jsonPayload["command"];
        string id = jsonPayload["id"];
        string msg = jsonPayload["msg"];

        if ("send" == command) {
            // Push text to the client under the given id
            ws:pushTextToConnection(id, msg);
        } else if ("remove" == command) {
            // Remove the client from the connection store under the given id
            ws:removeStoredConnection(id);
        }
    }

    @ws:OnClose {}
    resource onClose(message m) {
        // broadcast text to all connected clients
        ws:broadcastText("Client left");
    }
}

import ballerina.lang.jsons;
import ballerina.lang.messages;
import ballerina.net.http;
import ballerina.net.ws;
import ballerina.lang.system;

@http:BasePath {value:"/store"}
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
        json jsonPayload = messages:getJsonPayload(m);
        system:println(jsonPayload);
        string command = jsons:toString(jsonPayload["command"]);
        string id = jsons:toString(jsonPayload["id"]);
        string msg = jsons:toString(jsonPayload["msg"]);

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

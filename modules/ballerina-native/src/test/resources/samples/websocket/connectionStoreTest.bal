import ballerina.lang.system;
import ballerina.lang.messages;
import ballerina.net.ws;
import ballerina.net.http;

@http:BasePath {value:"/chat"}
@ws:WebSocketUpgradePath {value:"/ws"}
service oddEvenWebSocketConnector {

    @ws:OnOpen {}
    resource onOpen(message m) {
        string connectionName = messages:getHeader(m, "connectionName");
        ws:addConnectionToStore(connectionName);
    }

    @ws:OnTextMessage {}
    resource onTextMessage(message m) {
        string connectionName = messages:getStringPayload(m);
        ws:pushTextToConnection(connectionName, connectionName + ": test");
    }

    @ws:OnClose {}
    resource onClose(message m) {
        system:println("client left the server.");
        ws:broadcastText("client left the server.");
    }

}

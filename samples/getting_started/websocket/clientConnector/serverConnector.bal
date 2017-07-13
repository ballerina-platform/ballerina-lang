import ballerina.net.http;
import ballerina.net.ws;
import ballerina.lang.messages;
import ballerina.lang.system;

@http:config {basePath:"/client-connector"}
@ws:WebSocketUpgradePath {value:"/ws"}
service<ws> serverConnector {

    ws:ClientConnector c = create ws:ClientConnector("ws://localhost:8080/websocket", "clientService");

    @ws:OnTextMessage {}
    resource ontext(message m) {
        system:println("Clinet connector sending message: " + messages:getStringPayload(m));
        ws:ClientConnector.pushText(c, messages:getStringPayload(m));
    }

    @ws:OnClose {}
    resource onClose(message m) {
        ws:ClientConnector.close(c);
    }
}

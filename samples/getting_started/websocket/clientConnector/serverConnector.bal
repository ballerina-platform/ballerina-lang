import ballerina.net.http;
import ballerina.net.ws;
import ballerina.lang.messages;
import ballerina.lang.system;

@http:config {basePath:"/mediation"}
@ws:WebSocketUpgradePath {value:"/ws"}
service<ws> serverConnector {

    ws:ClientConnector c1 = create ws:ClientConnector("ws://localhost:8080/websocket", "clientService1");
    ws:ClientConnector c2 = create ws:ClientConnector("ws://localhost:8080/websocket", "clientService2");

    @ws:OnTextMessage {}
    resource ontext(message m) {
        system:println("Clinet connector sending message: " + messages:getStringPayload(m));
        ws:ClientConnector.pushText(c1, messages:getStringPayload(m));
        ws:ClientConnector.pushText(c2, messages:getStringPayload(m));
    }

    @ws:OnClose {}
    resource onClose(message m) {
        ws:ClientConnector.close(c1);
        ws:ClientConnector.close(c2);
    }
}

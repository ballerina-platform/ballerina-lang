
import ballerina/http;
import ballerina/net.ws;
import ballerina/lang.messages;
import ballerina/lang.system;

@http:configuration {basePath:"/client-connector"}
@ws:WebSocketUpgradePath {value:"/ws"}
service<ws> serverConnector {

    ws:ClientConnector c = create ws:ClientConnector("ws://localhost:15500/websocket", "clientService");

    @ws:OnTextMessage {}
    resource onText(message m) {
        string textReceived = messages:getStringPayload(m);

        if ("closeMe" == textReceived) {
            system:println("Removing the connection...");
            ws:closeConnection();
        } else {
            system:println("Client connector sending message: " + messages:getStringPayload(m));
            c.pushText(messages:getStringPayload(m));
        }
    }
}

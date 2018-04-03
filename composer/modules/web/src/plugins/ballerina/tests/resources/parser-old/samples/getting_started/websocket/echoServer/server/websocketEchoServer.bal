import ballerina/lang.system;
import ballerina/lang.messages;
import ballerina/http;
import ballerina/net.ws;

@http:configuration {basePath:"/echo-server"}
@ws:WebSocketUpgradePath {value:"/ws"}
service<ws> websocketEchoServer {

    @ws:OnOpen {}
    resource onOpen(message m) {
        system:println("New client connected to the server.");
    }

    @ws:OnTextMessage {}
    resource onTextMessage(message m) {
        string stringPayload = messages:getStringPayload(m);
        if ("closeMe" == stringPayload) {
            ws:closeConnection(); // Close connection from server side
        } else {
            ws:pushText(stringPayload);
            system:println("client: " + messages:getStringPayload(m));
        }
    }

    @ws:OnClose {}
    resource onClose(message m) {
        system:println("client left the server.");
    }
}

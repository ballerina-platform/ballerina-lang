import ballerina.lang.system;
import ballerina.lang.messages;
import ballerina.net.http;
import ballerina.net.ws;

@http:BasePath {value:"/echo-server"}
@ws:WebSocketUpgradePath {value:"/ws"}
service websocketEchoServer {

    @ws:OnOpen {}
    resource onOpen(message m) {
        system:println("New client connected to the server.");
    }

    @ws:OnTextMessage {}
    resource onTextMessage(message m) {
        ws:pushText(messages:getStringPayload(m));
        system:println("client: " + messages:getStringPayload(m));
    }

    @ws:OnClose {}
    resource onClose(message m) {
        system:println("client left the server.");
    }
}

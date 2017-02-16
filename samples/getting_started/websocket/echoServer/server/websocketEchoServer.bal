import ballerina.lang.system;
import ballerina.lang.message;
import ballerina.net.ws;

@http:BasePath("/ws-echo-server")
@ws:WebSocketUpgradePath("/")
service websocketEchoServer {

    @ws:OnOpen
    resource onOpen(message m) {
        system:println("New client connected to the server.");
    }

    @ws:OnTextMessage
    resource onTextMessage(message m) {
        ws:pushText(m, message:getStringPayload(m));
        system:println("client : " + message:getStringPayload(m));
    }

    @ws:OnClose
    resource onClose(message m) {
        system:println("client left the server.");
    }

}

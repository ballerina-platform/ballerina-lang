import ballerina.lang.system;
import ballerina.lang.message;
import ballerina.net.ws;

@BasePath("echo")
@WebSocketUpgradePath("/")
service echoServer {

    @OnOpen
    resource onOpen(message m) {
        system:println("New client connected to the server.");
    }

    @OnTextMessage
    resource onTextMessage(message m) {
        ws:sendText(m, "you said " + message:getStringPayload(m));
        system:println("client : " + message:getStringPayload(m));
    }

    @OnClose
    resource onClose(message m) {
        system:println("client left the server.");
    }

}

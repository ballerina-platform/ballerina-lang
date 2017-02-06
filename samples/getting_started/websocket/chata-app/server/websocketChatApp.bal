import ballerina.lang.message;
import ballerina.lang.system;
import ballerina.net.http;
import ballerina.net.ws;

@BasePath ("/chat")
@WebSocketUpgradePath("/")
service helloWorld {
    @OnOpen
    resource onOpenMessage(message m) {
        system:println("New user connected to chat");
        ws:sendTextToMembers(m, "server : client connected to chat");
    }

    @OnTextMessage
    resource onTextMessage(message m) {
        ws:sendTextToMembers(m, message:getStringPayload(m));
        system:println("Received text message: " + message:getStringPayload(m));
    }

    @OnClose
    resource onCloseMessage(message m) {
        ws:sendTextToMembers(m, "server : client left the chat");
        system:println("server : client left the chat");
    }
}

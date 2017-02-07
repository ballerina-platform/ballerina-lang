import ballerina.lang.message;
import ballerina.lang.system;
import ballerina.net.ws;

@BasePath ("/chat")
@WebSocketUpgradePath("/")
service helloWorld {
    @OnOpen
    resource onOpenMessage(message m) {
        system:println("New user connected to chat");
        ws:sendTextToOthers(m, "server : client connected to chat");
        ws:sendText(m, "server : You have successfully connected to chat");
    }

    @OnTextMessage
    resource onTextMessage(message m) {
        ws:broadcastText(m, message:getStringPayload(m));
        system:println("Received text message: " + message:getStringPayload(m));
    }

    @OnClose
    resource onCloseMessage(message m) {
        ws:sendTextToOthers(m, "server : client left the chat");
        system:println("server : client left the chat");
    }
}

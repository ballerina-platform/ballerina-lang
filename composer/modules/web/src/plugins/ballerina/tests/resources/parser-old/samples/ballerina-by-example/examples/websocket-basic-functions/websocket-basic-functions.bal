import ballerina/lang.messages;
import ballerina/http;
import ballerina/net.ws;

@http:configuration{basePath:"/functions"}
@ws:WebSocketUpgradePath {value:"/ws"}
service<ws> websocketEndpoint {

    @ws:OnOpen {}
    resource onOpen(message m) {
        // Broadcast text to all connected clients.
        ws:broadcastText("New client connected");
    }

    @ws:OnTextMessage {}
    resource onTextMessage(message m) {
        // Push text to the same client who sent the message.
        string text = messages:getStringPayload(m);
        ws:pushText("You said: " + text);

        // Close client connection if requested by the text closeMe.
        if ("closeMe" == text) {
            ws:closeConnection();
        }
    }

    @ws:OnClose {}
    resource onClose(message m) {
        // Broadcast text to all connected clients.
        ws:broadcastText("Client left");
    }
}


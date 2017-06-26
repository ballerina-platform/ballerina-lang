import ballerina.lang.messages;
import ballerina.net.http;
import ballerina.net.ws;

@http:BasePath {value:"/functions"}
@ws:WebSocketUpgradePath {value:"/ws"}
service websocketEndpoint {

    @ws:OnOpen {}
    resource onOpen(message m) {
        // broadcast text to all connected clients
        ws:broadcastText("New client connected");
    }

    @ws:OnTextMessage {}
    resource onTextMessage(message m) {
        // Push text to the same client who sent the message
        ws:pushText("You said: " + messages:getStringPayload(m));
    }

    @ws:OnClose {}
    resource onClose(message m) {
        // broadcast text to all connected clients
        ws:broadcastText("Client left");
    }
}


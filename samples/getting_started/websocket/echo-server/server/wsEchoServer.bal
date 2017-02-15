import ballerina.lang.system;
import ballerina.lang.message;
import ballerina.net.ws;

@BasePath("/ws-echo-server")
@ws:WebSocketUpgradePath("/")
service testWs {

    @ws:OnOpen
    resource onOpen(message m) {
        system:println("New client connected to the server.");
    }

    @ws:OnTextMessage
    resource onTextMessage(message m) {
        ws:sendText(m, message:getStringPayload(m));
        system:println("client : " + message:getStringPayload(m));
    }

    @ws:OnClose
    resource onClose(message m) {
        system:println("client left the server.");
    }

}

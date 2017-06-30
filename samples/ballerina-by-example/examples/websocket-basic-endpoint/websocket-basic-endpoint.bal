import ballerina.lang.system;
import ballerina.lang.messages;
import ballerina.net.http;
import ballerina.net.ws;
import ballerina.doc;

@doc:Description {value:"WebSocket endpoint is defined as a composition of  BasePath + WebSocketUpgradePath."}
@http:config{basePath:"/endpoint"}
@ws:WebSocketUpgradePath {value:"/ws"}
service<ws> websocketServerEndpoint {

    @doc:Description {value:"@OnOpen annotation is triggered when a new connection is initiated."}
    @ws:OnOpen {}
    resource onOpen(message m) {
        system:println("New client connected to the server.");
    }

    @doc:Description {value:"@OnTextMessage annotation is triggered when a new message comes from the user."}
    @ws:OnTextMessage {}
    resource onTextMessage(message m) {
        system:println("client: " + messages:getStringPayload(m));
    }

    @doc:Description {value:"@OnOpen annotation is triggered when a existing connection is closed."}
    @ws:OnClose {}
    resource onClose(message m) {
        system:println("client left the server.");
    }
}

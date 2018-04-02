import ballerina/lang.system;
import ballerina/lang.messages;
import ballerina/http;
import ballerina/net.ws;
import ballerina/doc;

@doc:Description {value:"WebSocket endpoint is defined as a composition of  BasePath + WebSocketUpgradePath."}
@http:configuration {basePath:"/endpoint"}
@ws:WebSocketUpgradePath {value:"/ws"}
service<ws> websocketServerEndpoint {

    @doc:Description {value:"Annotating a resource with @OnOpen annotation, allows the resource to receive events upon websocket handshake."}
    @ws:OnOpen {}
    resource onOpen(message m) {
        system:println("New client connected to the server");
    }

    @doc:Description {value:"Annotating a resource with @OnTextMessage annotation, allows the resource to receive websocket text messages."}
    @ws:OnTextMessage {}
    resource onTextMessage(message m) {
        system:println("client: " + messages:getStringPayload(m));
    }

    @doc:Description {value:"Annotating a resource with @OnClose annotation, allows the resource to receive events upon connection close."}
    @ws:OnClose {}
    resource onClose(message m) {
        system:println("client left the server");
    }
}

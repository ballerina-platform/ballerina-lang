import ballerina/http;
import ballerina/io;
import ballerina/runtime;

@final string REMOTE_BACKEND_URL = "ws://localhost:15500/websocket";
@final string ASSOCIATED_CONNECTION = "ASSOCIATED_CONNECTION";

endpoint http:Listener ep {
    port:9090
};

service <http:Service> proxy bind ep {

    @http:ResourceConfig {
        webSocketUpgrade: {
            upgradePath: "/ws",
            upgradeService: typeof simpleProxy
        }
    }
    websocketProxy (endpoint httpEp, http:Request req) {
        endpoint http:WebSocketListener wsServiceEp;
        endpoint http:WebSocketClient wsClientEp {
            url: REMOTE_BACKEND_URL,
            callbackService: typeof clientCallbackService
        };
        wsServiceEp = httpEp -> upgradeToWebSocket({"some-header":"some-header-value"});
        io:println("connections established");
        wsServiceEp.attributes[ASSOCIATED_CONNECTION] = wsClientEp;
        wsClientEp.attributes[ASSOCIATED_CONNECTION] = wsServiceEp;
        io:println("connections saved");
    }
}

service <http:WebSocketService> simpleProxy {

    //onOpen(endpoint ep) {
    //    io:println("on open");
    //}

    onText (endpoint wsEp, string text) {
        endpoint http:WebSocketClient clientEp = getAssociatedClientEndpoint(wsEp);
        io:println("Listner service received text");
        _ = clientEp -> pushText(text);
    }

onBinary (endpoint wsEp, blob data) {
    endpoint http:WebSocketClient clientEp = getAssociatedClientEndpoint(wsEp);
    io:println("Listner service received binary");
    _ = clientEp -> pushBinary(data);
    }

onClose (endpoint wsEp, int statusCode, string reason) {
        endpoint http:WebSocketClient clientEp = getAssociatedClientEndpoint(wsEp);
        _ = clientEp -> close(statusCode, reason);
    }

}

service <http:WebSocketClientService> clientCallbackService {
onText (endpoint wsEp, string text) {
        endpoint http:WebSocketListener serviceEp = getAssociatedListener(wsEp);
        io:println("client service received text");
        _ = serviceEp -> pushText(text);
    }

onBinary (endpoint wsEp, blob data) {
    endpoint http:WebSocketListener serviceEp = getAssociatedListener(wsEp);
    io:println("client service received binary");
    _ = serviceEp -> pushBinary(data);
    }

    onClose (endpoint wsEp, int statusCode, string reason) {
        endpoint http:WebSocketListener serviceEp = getAssociatedListener(wsEp);
        _ = serviceEp -> close(statusCode, reason);
    }
}

public function getAssociatedClientEndpoint(http:WebSocketListener wsServiceEp) returns (http:WebSocketClient) {
    return check <http:WebSocketClient> wsServiceEp.attributes[ASSOCIATED_CONNECTION];
}

public function getAssociatedListener(http:WebSocketClient wsClientEp) returns (http:WebSocketListener) {
    return check <http:WebSocketListener> wsClientEp.attributes[ASSOCIATED_CONNECTION];
}
import ballerina/io;
import ballerina/http;

@final string REMOTE_BACKEND_URL = "ws://localhost:15500/websocket";
@final string ASSOCIATED_CONNECTION = "ASSOCIATED_CONNECTION";

endpoint http:Listener ep {
    port:9090
};

service <http:Service> proxy bind ep {

    @http:ResourceConfig {
        webSocketUpgrade: {
            upgradePath: "/ws",
            upgradeService: simpleProxy
        }
    }
    websocketProxy (endpoint httpEp, http:Request req) {
        endpoint http:WebSocketClient wsClientEp {
            url: REMOTE_BACKEND_URL,
            callbackService: clientCallbackService
        };
        http:WebSocketListener wsServiceEp = httpEp -> acceptWebSocketUpgrade({"some-header":"some-header-value"});
        wsServiceEp.attributes[ASSOCIATED_CONNECTION] = wsClientEp;
        wsClientEp.attributes[ASSOCIATED_CONNECTION] = wsServiceEp;
    }
}

service <http:WebSocketService> simpleProxy {

    onOpen(endpoint ep) {
        ep -> pushText("send") but {error e => io:println("server text error")};
    }

    onText (endpoint wsEp, string text) {
        endpoint http:WebSocketClient clientEp = getAssociatedClientEndpoint(wsEp);
        clientEp -> pushText(text) but {error e => io:println("server text error")};
    }

    onBinary (endpoint wsEp, blob data) {
        endpoint http:WebSocketClient clientEp = getAssociatedClientEndpoint(wsEp);
        clientEp -> pushBinary(data) but {error e => io:println("server binary error")};
    }

    onClose (endpoint wsEp, int statusCode, string reason) {
        endpoint http:WebSocketClient clientEp = getAssociatedClientEndpoint(wsEp);
        clientEp -> close(statusCode, reason) but {error e => io:println("server closing error")};
    }

}

service <http:WebSocketClientService> clientCallbackService {
    onText (endpoint wsEp, string text) {
        endpoint http:WebSocketListener serviceEp = getAssociatedListener(wsEp);
        serviceEp -> pushText(text)  but {error e => io:println("client text error")};
    }

    onBinary (endpoint wsEp, blob data) {
        endpoint http:WebSocketListener serviceEp = getAssociatedListener(wsEp);
        serviceEp -> pushBinary(data) but {error e => io:println("client binary error")};
    }

    onClose (endpoint wsEp, int statusCode, string reason) {
        endpoint http:WebSocketListener serviceEp = getAssociatedListener(wsEp);
        serviceEp -> close(statusCode, reason) but {error e => io:println("client closing error")};
    }
}

public function getAssociatedClientEndpoint(http:WebSocketListener wsServiceEp) returns (http:WebSocketClient) {
    return check <http:WebSocketClient> wsServiceEp.attributes[ASSOCIATED_CONNECTION];
}

public function getAssociatedListener(http:WebSocketClient wsClientEp) returns (http:WebSocketListener) {
    return check <http:WebSocketListener> wsClientEp.attributes[ASSOCIATED_CONNECTION];
}
import ballerina/http;
import ballerina/io;
import ballerina/log;
import ballerina/runtime;

@final string REMOTE_BACKEND_URL = "ws://localhost:15500/websocket";
@final string ASSOCIATED_CONNECTION = "ASSOCIATED_CONNECTION";
@final string data = "data";
@final blob APPLICATION_DATA = data.toBlob("UTF-8");

endpoint http:Listener ep {
    port:9090
};

service <http:Service> pingpong bind ep {

    @http:ResourceConfig {
        webSocketUpgrade: {
            upgradePath: "/ws",
            upgradeService: simplePingProxy
        }
    }
    websocketProxy (endpoint httpEp, http:Request req) {
        endpoint http:WebSocketClient wsClientEp {
            url: REMOTE_BACKEND_URL,
            callbackService: clientCallbackService
        };
        http:WebSocketListener wsServiceEp = httpEp -> acceptWebSocketUpgrade({"some-header":"some-header-value"});
        io:println("connections established");
        wsServiceEp.attributes[ASSOCIATED_CONNECTION] = wsClientEp;
        wsClientEp.attributes[ASSOCIATED_CONNECTION] = wsServiceEp;
        io:println("connections saved");
    }
}

service <http:WebSocketService> simplePingProxy {

    onOpen(endpoint wsEp) {
        wsEp -> pushText("send") but {error e => io:println("server text error")};
    }

    onText (endpoint wsEp, string text) {
        endpoint http:WebSocketClient clientEp;

        if(text == "ping-me") {
            wsEp -> ping(APPLICATION_DATA) but {error e => io:println("error sending server ping")};
        }

        if(text == "ping-remote-server") {
            clientEp = getAssociatedClientEndpoint(wsEp);
            clientEp -> ping(APPLICATION_DATA) but {error e => io:println("error sending client ping")};
        }

        if(text == "tell-remote-server-to-ping") {
            clientEp = getAssociatedClientEndpoint(wsEp);
            clientEp -> pushText("ping") but {error e => io:println("error sending client ping")};
        }
    }

    onPing (endpoint wsEp, blob data) {
        wsEp -> pong(data) but {error e => io:println("Error sending server pong")};
    }

    onPong (endpoint wsEp, blob data) {
        wsEp -> pushText("pong-from-you") but {error e => io:println("server text error")};
    }

}

service <http:WebSocketClientService> clientCallbackService {

    onPing (endpoint wsEp, blob data) {
        endpoint http:WebSocketListener serverEp = getAssociatedListener(wsEp);
        serverEp -> pushText("ping-from-remote-server-received") but {error e => io:println("error sending client text")};
    }

    onPong (endpoint wsEp, blob data) {
        endpoint http:WebSocketListener serverEp = getAssociatedListener(wsEp);
        serverEp -> pushText("pong-from-remote-server-received") but {error e => io:println("error sending client text")};
    }
}

public function getAssociatedClientEndpoint(http:WebSocketListener wsServiceEp) returns (http:WebSocketClient) {
    return check <http:WebSocketClient> wsServiceEp.attributes[ASSOCIATED_CONNECTION];
}

public function getAssociatedListener(http:WebSocketClient wsClientEp) returns (http:WebSocketListener) {
    return check <http:WebSocketListener> wsClientEp.attributes[ASSOCIATED_CONNECTION];
}
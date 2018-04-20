import ballerina/log;
import ballerina/http;

@final string ASSOCIATED_CONNECTION = "ASSOCIATED_CONNECTION";
@final string REMOTE_BACKEND = "wss://echo.websocket.org";

endpoint http:Listener serviceEndpoint {
    port:9090
};

service<http:Service> proxy bind serviceEndpoint {

    @http:ResourceConfig {
        webSocketUpgrade:{
            upgradePath:"/ws",
            upgradeService:SimpleProxyServer
        }
    }
    upgrader(endpoint ep, http:Request req, string name) {
        endpoint http:WebSocketClient wsClientEp {
            url:REMOTE_BACKEND,
            callbackService:ClientService
        };
        endpoint http:WebSocketListener wsServerEp;
        wsServerEp = ep -> acceptWebSocketUpgrade({"custom":"header"});
        wsClientEp.attributes[ASSOCIATED_CONNECTION] = wsServerEp;
        wsServerEp.attributes[ASSOCIATED_CONNECTION] = wsClientEp;
    }
}

service<http:WebSocketService> SimpleProxyServer {

    //This resource is triggered when a new text frame is received from a client.
    onText(endpoint ep, string text) {
        endpoint http:WebSocketClient clientEp = getAssociatedClientEndpoint(ep);
        clientEp -> pushText(text) but { error e => log:printErrorCause("Error occurred when sending text message", e) };
    }

    //This resource is triggered when a new binary frame is received from a client.
    onBinary(endpoint ep, blob data) {
        endpoint http:WebSocketClient clientEp = getAssociatedClientEndpoint(ep);
        clientEp -> pushBinary(data) but { error e => log:printErrorCause("Error occurred when sending binary message", e) };
    }

    //This resource is triggered when a client connection is closed from the client side.
    onClose(endpoint ep, int statusCode, string reason) {
        endpoint http:WebSocketClient clientEp = getAssociatedClientEndpoint(ep);
        clientEp -> close(statusCode, reason) but { error e => log:printErrorCause("Error occurred when closing the connection", e) };
        _ = ep.attributes.remove(ASSOCIATED_CONNECTION);
    }
}

//Client service to receive frames from the remote server.
@http:WebSocketServiceConfig {}
service<http:WebSocketClientService> ClientService {

    //This resource is triggered when a new text frame is received from the remote backend.
    onText(endpoint ep, string text) {
        endpoint http:WebSocketListener parentEp = getAssociatedServerEndpoint(ep);
        parentEp -> pushText(text) but { error e => log:printErrorCause("Error occurred when sending text message", e) };
    }

    //This resource is triggered when a new binary frame is received from the remote backend.
    onBinary(endpoint ep, blob data) {
        endpoint http:WebSocketListener parentEp = getAssociatedServerEndpoint(ep);
        parentEp -> pushBinary(data) but { error e => log:printErrorCause("Error occurred when sending binary message", e) };
    }

    //This resource is triggered when a client connection is closed by the remote backend.
    onClose(endpoint ep, int statusCode, string reason) {
        endpoint http:WebSocketListener parentEp = getAssociatedServerEndpoint(ep);
        parentEp -> close(statusCode, reason) but { error e => log:printErrorCause("Error occurred when closing the connection", e) };
        _ = ep.attributes.remove(ASSOCIATED_CONNECTION);
    }

}

function getAssociatedClientEndpoint(http:WebSocketListener ep) returns (http:WebSocketClient) {
    http:WebSocketClient client = check <http:WebSocketClient>ep.attributes[ASSOCIATED_CONNECTION];
    return client;
}

function getAssociatedServerEndpoint(http:WebSocketClient ep) returns (http:WebSocketListener) {
    http:WebSocketListener wsEndpoint = check <http:WebSocketListener>ep.attributes[ASSOCIATED_CONNECTION];
    return wsEndpoint;
}

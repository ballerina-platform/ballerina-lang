import ballerina/log;
import ballerina/http;

@final string ASSOCIATED_CONNECTION = "ASSOCIATED_CONNECTION";
@final string REMOTE_BACKEND = "wss://echo.websocket.org";

endpoint http:Listener serviceEndpoint {
    port:9090
};

service <http:Service> proxy bind serviceEndpoint {

    @Description {value:"Create a client connection to a remote server from Ballerina when a new client connects to this service endpoint."}
    @http:ResourceConfig {
        webSocketUpgrade: {
            upgradePath: "/ws",
            upgradeService: typeof SimpleProxyServer
        }
    }
    upgrader(endpoint ep, http:Request req, string name) {
        endpoint http:WebSocketClient wsClientEp {
            url:REMOTE_BACKEND,
            callbackService:typeof ClientService
        };
        endpoint http:WebSocketListener wsServerEp;
        wsServerEp = ep -> acceptWebSocketUpgrade({"custom":"header"});
        wsClientEp.attributes[ASSOCIATED_CONNECTION] = wsServerEp;
        wsServerEp.attributes[ASSOCIATED_CONNECTION] = wsClientEp;
    }
}

@http:WebSocketServiceConfig {
    basePath:"/proxy/ws"
}
service<http:WebSocketService> SimpleProxyServer bind serviceEndpoint {

    onText (endpoint ep, string text) {
        endpoint http:WebSocketClient clientEp = getAssociatedClientEndpoint(ep);
        clientEp -> pushText(text) but {error e => log:printErrorCause("Error occured when sending text message", e)};
    }

    onBinary (endpoint ep, blob data) {
        endpoint http:WebSocketClient clientEp = getAssociatedClientEndpoint(ep);
        clientEp -> pushBinary(data) but {error e => log:printErrorCause("Error occured when sending binary message", e)};
    }

    onClose (endpoint ep, int statusCode, string reason) {
        endpoint http:WebSocketClient clientEp = getAssociatedClientEndpoint(ep);
        clientEp -> close(statusCode, reason) but {error e => log:printErrorCause("Error occured when closing the connection", e)};
        _ = ep.attributes.remove(ASSOCIATED_CONNECTION);
    }
}

@Description {value:"Client service to receive frames from remote server"}
@http:WebSocketServiceConfig {}
service<http:WebSocketClientService> ClientService {

    onText (endpoint ep, string text) {
        endpoint http:WebSocketListener parentEp = getAssociatedServerEndpoint(ep);
        parentEp -> pushText(text) but {error e => log:printErrorCause("Error occured when sending text message", e)};
    }

    onBinary (endpoint ep, blob data) {
        endpoint http:WebSocketListener parentEp = getAssociatedServerEndpoint(ep);
        parentEp -> pushBinary(data) but {error e => log:printErrorCause("Error occured when sending binary message", e)};
    }

    onClose (endpoint ep, int statusCode, string reason) {
        endpoint http:WebSocketListener parentEp = getAssociatedServerEndpoint(ep);
        parentEp -> close(statusCode, reason) but {error e => log:printErrorCause("Error occured when closing the connection", e)};
        _ = ep.attributes.remove(ASSOCIATED_CONNECTION);
    }

}

function getAssociatedClientEndpoint (http:WebSocketListener ep) returns (http:WebSocketClient) {
    http:WebSocketClient client = check <http:WebSocketClient> ep.attributes[ASSOCIATED_CONNECTION];
    return client;
}

function getAssociatedServerEndpoint (http:WebSocketClient ep) returns (http:WebSocketListener) {
    http:WebSocketListener wsEndpoint = check <http:WebSocketListener> ep.attributes[ASSOCIATED_CONNECTION];
    return wsEndpoint;
}

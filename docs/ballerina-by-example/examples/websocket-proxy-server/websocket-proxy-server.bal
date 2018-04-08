import ballerina/io;
import ballerina/http;

@final string ASSOCIATED_CONNECTION = "ASSOCIATED_CONNECTION";
@final string REMOTE_BACKEND = "wss://echo.websocket.org";

endpoint http:ServiceEndpoint serviceEndpoint {
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
        endpoint http:WebSocketEndpoint wsServerEp;
        wsServerEp = ep -> upgradeToWebSocket({"custom":"header"});
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
        var val = clientEp -> pushText(text);
    }

    onBinary (endpoint ep, blob data) {
        endpoint http:WebSocketClient clientEp = getAssociatedClientEndpoint(ep);
        var val = clientEp -> pushBinary(data);
    }

    onClose (endpoint ep, int statusCode, string reason) {
        endpoint http:WebSocketClient clientEp = getAssociatedClientEndpoint(ep);
        var val = clientEp -> close(statusCode, reason);
        _ = ep.attributes.remove(ASSOCIATED_CONNECTION);
    }
}

@Description {value:"Client service to receive frames from remote server"}
@http:WebSocketServiceConfig {}
service<http:WebSocketClientService> ClientService {

    onText (endpoint ep, string text) {
        endpoint http:WebSocketEndpoint parentEp = getAssociatedServerEndpoint(ep);
        var val = parentEp -> pushText(text);
    }

    onBinary (endpoint ep, blob data) {
        endpoint http:WebSocketEndpoint parentEp = getAssociatedServerEndpoint(ep);
        var val = parentEp -> pushBinary(data);
    }

    onClose (endpoint ep, int statusCode, string reason) {
        endpoint http:WebSocketEndpoint parentEp = getAssociatedServerEndpoint(ep);
        var val = parentEp -> close(statusCode, reason);
        _ = ep.attributes.remove(ASSOCIATED_CONNECTION);
    }

}


function getAssociatedClientEndpoint (http:WebSocketEndpoint ep) returns (http:WebSocketClient) {
    http:WebSocketClient client = check <http:WebSocketClient> ep.attributes[ASSOCIATED_CONNECTION];
    return client;
}


function getAssociatedServerEndpoint (http:WebSocketClient ep) returns (http:WebSocketEndpoint) {
    http:WebSocketEndpoint wsEndpoint = check <http:WebSocketEndpoint> ep.attributes[ASSOCIATED_CONNECTION];
    return wsEndpoint;
}

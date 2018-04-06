import ballerina/io;
import ballerina/http;

const string ASSOCIATED_CONNECTION = "ASSOCIATED_CONNECTION";
const string REMOTE_BACKEND = "wss://echo.websocket.org";

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
        handleError(val);
    }

    onBinary (endpoint ep, blob data) {
        endpoint http:WebSocketClient clientEp = getAssociatedClientEndpoint(ep);
        var val = clientEp -> pushBinary(data);
        handleError(val);
    }

    onClose (endpoint ep, int statusCode, string reason) {
        endpoint http:WebSocketClient clientEp = getAssociatedClientEndpoint(ep);
        var val = clientEp -> close(statusCode, reason);
        handleError(val);
        _ = ep.attributes.remove(ASSOCIATED_CONNECTION);
    }
}

@Description {value:"Client service to receive frames from remote server"}
@http:WebSocketServiceConfig {}
service<http:WebSocketClientService> ClientService {

    onText (endpoint ep, string text) {
        endpoint http:WebSocketEndpoint parentEp = getAssociatedServerEndpoint(ep);
        var val = parentEp -> pushText(text);
        handleError(val);
    }

    onBinary (endpoint ep, blob data) {
        endpoint http:WebSocketEndpoint parentEp = getAssociatedServerEndpoint(ep);
        var val = parentEp -> pushBinary(data);
        handleError(val);
    }

    onClose (endpoint ep, int statusCode, string reason) {
        endpoint http:WebSocketEndpoint parentEp = getAssociatedServerEndpoint(ep);
        var val = parentEp -> close(statusCode, reason);
        handleError(val);
        _ = ep.attributes.remove(ASSOCIATED_CONNECTION);
    }

}


function getAssociatedClientEndpoint (http:WebSocketEndpoint ep) returns (http:WebSocketClient) {
    var param = ep.attributes[ASSOCIATED_CONNECTION];
    match param {
        http:WebSocketClient associatedEndpoint => {return associatedEndpoint;}
        any|null val => {
            error err = {message:"Associated connection is not set"};
            throw err;
        }
    }
}


function getAssociatedServerEndpoint (http:WebSocketClient ep) returns (http:WebSocketEndpoint) {
    var param = ep.attributes[ASSOCIATED_CONNECTION];
    match param {
        http:WebSocketEndpoint associatedEndpoint => {return associatedEndpoint;}
        any|null val => {
            error err = {message:"Associated connection is not set"};
            throw err;
        }
    }
}

function handleError (http:WebSocketConnectorError|null val) {
    match val {
        http:WebSocketConnectorError err => {io:println("Error: " + err.message);}
        any|null err => {//ignore x
            var x = err;
        }
    }
}
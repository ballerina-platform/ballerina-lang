import ballerina/io;
import ballerina/http;

const string ASSOCIATED_CONNECTION = "ASSOCIATED_CONNECTION";
endpoint http:ServiceEndpoint serviceEndpoint {
    port:9090
};
@http:WebSocketServiceConfig {
    basePath:"/proxy/ws"
}
service<http:WebSocketService> SimpleProxyServer bind serviceEndpoint {

    string remoteUrl = "wss://echo.websocket.org";

    @Description {value:"Create a client connection to a remote server from Ballerina when a new client connects to this service endpoint."}
    onUpgrade (endpoint ep, http:Request req) {
        endpoint http:WebSocketClient wsEndpoint {
            url:remoteUrl,
            callbackService:typeof ClientService
        };
        ep = ep -> upgradeToWebSocket({"custom":"header"});
        ep.attributes[ASSOCIATED_CONNECTION] = wsEndpoint;
        wsEndpoint.attributes[ASSOCIATED_CONNECTION] = ep;
    }

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
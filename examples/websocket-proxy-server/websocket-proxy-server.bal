import ballerina/io;
import ballerina/net.http;

const string ASSOCIATED_CONNECTION = "ASSOCIATED_CONNECTION";
endpoint http:ServiceEndpoint serviceEndpoint {
    port:9090
};
@http:WebSocketServiceConfig {
    basePath:"/proxy/ws"
}
service<http:WebSocketService> SimpleProxyServer bind serviceEndpoint {

    string remoteUrl = "wss://echo.websocket.org";

    @Description {value:"Create a client connection to remote server from ballerina when new client connects to this service endpoint."}
    onUpgrade (endpoint ep, http:Request req) {
        endpoint http:WebSocketClient wsEndpoint {
            url:remoteUrl,
            callbackService:typeof ClientService
        };
        ep -> upgradeToWebSocket({"custom":"header"});
        ep.attributes[ASSOCIATED_CONNECTION] = wsEndpoint;
        wsEndpoint.attributes[ASSOCIATED_CONNECTION] = ep;
    }

    onTextMessage (endpoint ep, http:TextFrame frame) {
        endpoint http:WebSocketClient clientEp = getAssociatedClientEndpoint(ep);
        clientEp -> pushText(frame.text);
    }

    onBinaryMessage (endpoint ep, http:BinaryFrame frame) {
        endpoint http:WebSocketClient clientEp = getAssociatedClientEndpoint(ep);
        clientEp -> pushBinary(frame.data);
    }

    onClose (endpoint ep, http:CloseFrame frame) {
        endpoint http:WebSocketClient clientEp = getAssociatedClientEndpoint(ep);
        clientEp -> closeConnection(frame.statusCode, frame.reason);
        _ = ep.attributes.remove(ASSOCIATED_CONNECTION);
    }
}

@Description {value:"Client service to receive frames from remote server"}
@http:WebSocketServiceConfig {}
service<http:WebSocketClientService> ClientService {

    onTextMessage (endpoint ep, http:TextFrame frame) {
        endpoint http:WebSocketEndpoint parentEp = getAssociatedServerEndpoint(ep);
        parentEp -> pushText(frame.text);
    }

    onBinaryMessage (endpoint ep, http:BinaryFrame frame) {
        endpoint http:WebSocketEndpoint parentEp = getAssociatedServerEndpoint(ep);
        parentEp -> pushBinary(frame.data);
    }

    onClose (endpoint ep, http:CloseFrame frame) {
        endpoint http:WebSocketEndpoint parentEp = getAssociatedServerEndpoint(ep);
        parentEp -> closeConnection(frame.statusCode, frame.reason);
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
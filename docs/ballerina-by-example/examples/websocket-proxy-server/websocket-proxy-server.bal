import ballerina/io;
import ballerina/net.http;

const string ASSOCIATED_CONNECTION = "ASSOCIATED_CONNECTION";
endpoint http:WebSocketClient wsEndpoint {
    url:"wss://echo.websocket.org",
    callbackService:typeof ClientService
};
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
        var conn = ep.getClient();
        var clientConn = wsEndpoint.getClient();
        ep -> upgradeToWebSocket({"custom":"header"});
        conn.attributes[ASSOCIATED_CONNECTION] = clientConn;
        clientConn.attributes[ASSOCIATED_CONNECTION] = conn;
    }

    onTextMessage (endpoint conn, http:TextFrame frame) {
        var clientCon = getAssociatedConnection(conn.getClient());
        clientCon.pushText(frame.text);
    }

    onBinaryMessage (endpoint conn, http:BinaryFrame frame) {
        var clientCon = getAssociatedConnection(conn.getClient());
        clientCon.pushBinary(frame.data);
    }

    onClose (endpoint ep, http:CloseFrame frame) {
        var conn = ep.getClient();
        var clientConn = getAssociatedConnection(conn);
        clientConn.closeConnection(frame.statusCode, frame.reason);
        _ = conn.attributes.remove(ASSOCIATED_CONNECTION);
    }
}

@Description {value:"Client service to receive frames from remote server"}
@http:WebSocketServiceConfig {}
service<http:WebSocketService> ClientService {

    onTextMessage (endpoint conn, http:TextFrame frame) {
        var parentConn = getAssociatedConnection(conn.getClient());
        parentConn.pushText(frame.text);
    }

    onBinaryMessage (endpoint conn, http:BinaryFrame frame) {
        var parentConn = getAssociatedConnection(conn.getClient());
        parentConn.pushBinary(frame.data);
    }

    onClose (endpoint ep, http:CloseFrame frame) {
        var conn = ep.getClient();
        var parentConn = getAssociatedConnection(conn);
        parentConn.closeConnection(frame.statusCode, frame.reason);
        _ = conn.attributes.remove(ASSOCIATED_CONNECTION);
    }

}


function getAssociatedConnection (http:WebSocketConnector conn) returns (http:WebSocketConnector) {
    var param = <http:WebSocketConnector>conn.attributes[ASSOCIATED_CONNECTION];
    match param {
        http:WebSocketConnector associatedConnection => return associatedConnection;
        any|null val => {
            error err = {message:"Associated connection is not set"};
            throw err;
        }
    }
}

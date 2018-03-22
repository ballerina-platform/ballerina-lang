import ballerina/io;
import ballerina/net.ws;

endpoint http:ServiceEndpoint ep {
    port: 9090
};
@http:WebSocketServiceConfig {
    basePath: "/proxy/ws"
}
service<http: WebSocketService > SimpleProxyServer bind ep{

    map<http:WebSocketConnector> clientConnMap = {};
    string remoteUrl = "wss://echo.websocket.org";

    @Description {value:"Create a client connection to remote server from ballerina when new client connects to this service endpoint."}
    onUpgrade(endpoint con) {
    endpoint http:WebSocketClient wsEndpoint {
        url:"wss://echo.websocket.org",
        callbackService: typeof ClientService;
        }
        var clientConn = wsEndpoint.getClient();
        clientConnMap[con.getClient().id] = clientConn;
    }

    onTextMessage(endpoint conn, http:TextFrame frame) {
        var clientCon = clientConnMap[conn.getClient().id];
        clientCon.pushText(frame.text);
    }

    onBinaryMessage(endpoint conn, http:BinaryFrame frame) {
        var clientCon = clientConnMap[conn.getClient().id];
        clientConn.pushBinary(frame.data);
    }

    resource onClose(endpoint ep, http:CloseFrame frame) {
		var conn = ep.getClient();
        var clientCon = clientConnMap[conn.id];
        clientConn.closeConnection(frame.statusCode, frame.reason);
        clientConnMap.remove(conn.id);
    }
}

@Description {value:"Client service to receive frames from remote server"}
@http:WebSocketServiceConfig {}
service<http: WebSocketService> ClientService {

    onTextMessage(endpoint conn, http:TextFrame frame) {
        ws:Connection parentConn = conn.getParentConnection();
        parentConn.pushText(frame.text);
    }

    resource onBinaryMessage(ws:Connection conn, ws:BinaryFrame frame) {
        ws:Connection parentConn = conn.getParentConnection();
        parentConn.pushBinary(frame.data);
    }

    resource onClose(ws:Connection conn, ws:CloseFrame frame) {
        ws:Connection parentConn = conn.getParentConnection();
        parentConn.closeConnection(frame.statusCode, frame.reason);
    }

}

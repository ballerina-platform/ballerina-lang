import ballerina.io;
import ballerina.net.ws;

@ws:configuration {
    basePath: "/proxy/ws",
    port:9090
}
service<ws> SimpleProxyServer {

    map clientConnMap = {};
    string remoteUrl = "wss://echo.websocket.org";
    string remoteServerCallbackService = "ClientService";

    @Description {value:"Create a client connection to remote server from ballerina when new client connects to this service endpoint."}
    resource onHandshake(ws:HandshakeConnection con) {
        endpoint<ws:WsClient> wsEndpoint {
            create ws:WsClient(remoteUrl, remoteServerCallbackService);
        }
        var clientConn, err = wsEndpoint.connect({parentConnectionID:con.connectionID});
        if (err != null) {
            io:println(err.message);
            con.cancelHandshake(1001, "Cannot connect to remote server");
        } else {
            clientConnMap[con.connectionID] = clientConn;
        }
    }

    resource onTextMessage(ws:Connection conn, ws:TextFrame frame) {
        var clientCon, _ = (ws:Connection)clientConnMap[conn.getID()];
        clientCon.pushText(frame.text);
    }

    resource onBinaryMessage(ws:Connection conn, ws:BinaryFrame frame) {
        var clientConn, _ = (ws:Connection)clientConnMap[conn.getID()];
        clientConn.pushBinary(frame.data);
    }

    resource onClose(ws:Connection conn, ws:CloseFrame frame) {
        var clientConn, _ = (ws:Connection)clientConnMap[conn.getID()];
        clientConn.closeConnection(frame.statusCode, frame.reason);
        clientConnMap.remove(conn.getID());
    }
}

@Description {value:"Client service to receive frames from remote server"}
@ws:clientService {}
service<ws> ClientService {

    resource onTextMessage(ws:Connection conn, ws:TextFrame frame) {
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

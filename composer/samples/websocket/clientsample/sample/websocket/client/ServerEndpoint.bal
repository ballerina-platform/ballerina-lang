
import ballerina/io;
import ballerina/net.ws;

@ws:configuration {
    basePath:"/pass-through/ws",
    port:9090
}
service<ws> SimpleProxyServer {

    map clientConnMap = {};

    resource onHandshake (ws:HandshakeConnection con) {
        endpoint<ws:WsClient> c {
            create ws:WsClient("wss://echo.websocket.org", "ClientService");
        }
        var clientConn, err = c.connectWithDefault();
        if (err != null) {
            con.cancelHandshake(1001, "Cannot connect to remote server");
        } else {
            clientConnMap[con.connectionID] = clientConn;
            io:println("Client connection sucessful");
        }
    }

    resource onTextMessage (ws:Connection conn, ws:TextFrame frame) {
        var clientCon, _ = (ws:Connection)clientConnMap[conn.getID()];
        clientCon.pushText(frame.text);
    }

    resource onClose (ws:Connection conn, ws:CloseFrame frame) {
        var clientConn, _ = (ws:Connection)clientConnMap[conn.getID()];
        clientConn.closeConnection(frame.statusCode, frame.reason);
        clientConnMap.remove(conn.getID());
    }
}

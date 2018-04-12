
import ballerina/io;
import ballerina/net.ws;

@ws:configuration {
    basePath:"/proxy/ws",
    port:9090
}
service<ws> SimpleProxyServer {

    map clientConnMap = {};

    resource onHandshake (ws:HandshakeConnection con) {
        endpoint<ws:WsClient> c {
            create ws:WsClient("wss://echo.websocket.org", "ClientService");
        }
        var clientConn, err = c.connect({parentConnectionID:con.connectionID});
        if (err != null) {
            io:println("Error occcurred : " + err.message);
            con.cancelHandshake(1001, "Cannot connect to remote server");
        } else {
            clientConnMap[con.connectionID] = clientConn;
        }
    }

    resource onTextMessage (ws:Connection conn, ws:TextFrame frame) {
        var clientConn, _ = (ws:Connection)clientConnMap[conn.getID()];
        clientConn.pushText(frame.text);
    }

    resource onClose (ws:Connection conn, ws:CloseFrame frame) {
        var clientConn, _ = (ws:Connection)clientConnMap[conn.getID()];
        clientConn.closeConnection(1001, "Client closing connection");
        _ = clientConnMap.remove(conn.getID());
    }
}

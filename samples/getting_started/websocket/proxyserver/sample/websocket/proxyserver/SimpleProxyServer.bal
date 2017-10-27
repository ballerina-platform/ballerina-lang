package sample.websocket.proxyserver;

import ballerina.net.ws;

@ws:configuration {
    basePath:"/proxy/ws",
    port:9090
}
service<ws> SimpleProxyServer {

    map clientConnMap = {};

    resource onHandshake (ws:HandshakeConnection con) {
        ws:ClientConnector c = create ws:ClientConnector("wss://echo.websocket.org", "ClientService");
        try {
            ws:Connection clientConn = c.connect({parentConnectionID:con.connectionID});
            clientConnMap[con.connectionID] = clientConn;
        } catch (error err) {
            println("Error occcurred : " + err.msg);
            con.cancelHandshake(1001, "Cannot connect to remote server");
        }
    }

    resource onTextMessage (ws:Connection conn, ws:TextFrame frame) {
        var clientConn, _ = (ws:Connection)clientConnMap[conn.getID()];
        clientConn.pushText(frame.text);
    }

    resource onClose (ws:Connection conn, ws:CloseFrame frame) {
        var clientConn, _ = (ws:Connection)clientConnMap[conn.getID()];
        clientConn.closeConnection(1001, "Client closing connection");
        clientConnMap.remove(conn.getID());
    }
}

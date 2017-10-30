package sample.websocket.client;

import ballerina.net.ws;

@ws:configuration {
    basePath:"/pass-through/ws",
    port:9090
}
service<ws> SimpleProxyServer {

    map clientConnMap = {};

    resource onHandshake (ws:HandshakeConnection con) {
        ws:ClientConnector c = create ws:ClientConnector("wss://echo.websocket.org", "ClientService");
        try {
            ws:Connection clientConn = c.connectWithDefault();
            clientConnMap[con.connectionID] = clientConn;
            println("Client connection sucessful");
        } catch (error err) {
            con.cancelHandshake(1001, "Cannot connect to remote server");
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

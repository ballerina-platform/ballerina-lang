import ballerina.net.ws;

@ws:configuration {
    basePath: "/proxy/ws",
    port:9090
}
service<ws> SimpleProxyServer {

    ws:ClientConnector c = create ws:ClientConnector("ws://localhost:15500/websocket", "ClientService");
    map clientConnMap = {};

    resource onHandshake(ws:HandshakeConnection con) {
        ws:ClientConnectorConfig clientConnectorConfig = {parentConnectionID:con.connectionID};
        var clientConn, e = c.connect(clientConnectorConfig);
        if (e != null) {
            println("Error occcurred : " + e.msg);
            con.cancelHandshake(1001, "Cannot connect to remote server");
        } else {
            clientConnMap[con.connectionID] = clientConn;
        }
    }

    resource onTextMessage(ws:Connection conn, ws:TextFrame frame) {
        var clientConn, _ = (ws:Connection) clientConnMap[conn.getID()];
        string text = frame.text;

        if (text == "closeMe") {
            clientConn.closeConnection(1001, "Client is going away");
            conn.closeConnection(1001, "You told to close your connection");
        } else if (text == "ping") {
            conn.ping(text.toBlob("UTF-8"));
        } else if (text == "client_ping") {
            clientConn.ping(text.toBlob("UTF-8"));
        } else if (text == "client_ping_req") {
            clientConn.pushText("ping");
        } else if (clientConn != null) {
            clientConn.pushText(text);
        }
    }

    resource onPing(ws:Connection conn, ws:PingFrame frame) {
        conn.pong(frame.data);
    }

    resource onPong(ws:Connection conn, ws:PongFrame frame) {
        conn.pushText("pong_received");
    }

    resource onClose(ws:Connection conn, ws:CloseFrame frame) {
        var clientConn, _ = (ws:Connection) clientConnMap[conn.getID()];
        clientConn.closeConnection(1001, "Client closing connection");
        clientConnMap.remove(conn.getID());
    }
}

@ws:clientService {}
service<ws> ClientService {

    resource onTextMessage(ws:Connection conn, ws:TextFrame frame) {
        ws:Connection parentCon = conn.getParentConnection();
        parentCon.pushText("client service: " + frame.text);
    }

    resource onPing(ws:Connection conn, ws:PingFrame frame) {
        ws:Connection parentConn = conn.getParentConnection();
        parentConn.pushText("remote_server_ping");
    }

    resource onPong(ws:Connection conn, ws:PongFrame frame) {
        ws:Connection parentConn = conn.getParentConnection();
        parentConn.pushText("remote_server_pong");
    }

    resource onClose(ws:Connection conn, ws:CloseFrame frame) {
        ws:Connection parentCon = conn.getParentConnection();
        parentCon.closeConnection(1001, "Server closing connection");
    }

}

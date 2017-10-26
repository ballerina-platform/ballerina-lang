import ballerina.lang.system;
import ballerina.lang.maps;
import ballerina.net.ws;
import ballerina.lang.strings;

@ws:configuration {
    basePath: "/proxy/ws",
    port:9090
}
service<ws> SimpleProxyServer {

    ws:ClientConnector c = create ws:ClientConnector("ws://localhost:15500/websocket", "ClientService");
    map clientConnMap = {};

    resource onHandshake(ws:HandshakeConnection con) {
        try {
            ws:ClientConnectorConfig clientConnectorConfig = {parentConnectionID:con.connectionID};
            ws:Connection  clientConn = c.connect(clientConnectorConfig);
            clientConnMap[con.connectionID] = clientConn;
        } catch (error err) {
            system:println("Error occcurred : " + err.msg);
            con.cancelHandshake(1001, "Cannot connect to remote server");
        }
    }

    resource onTextMessage(ws:Connection conn, ws:TextFrame frame) {
        int pingTimeout = 5;
        var clientConn, _ = (ws:Connection) clientConnMap[conn.getID()];
        string text = frame.text;

        if (text == "closeMe") {
            clientConn.closeConnection(1001, "Client is going away");
            conn.closeConnection(1001, "You told to close your connection");
        } else if (text == "ping") {
            conn.ping(strings:toBlob(text, "UTF-8"), pingTimeout);
        } else if (text == "client_ping") {
            clientConn.ping(strings:toBlob(text, "UTF-8"), pingTimeout);
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
        maps:remove(clientConnMap, conn.getID());
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

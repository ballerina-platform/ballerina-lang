import ballerina.lang.system;
import ballerina.lang.maps;
import ballerina.net.ws;

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
        var clientConn, _ = (ws:Connection) clientConnMap[conn.getID()];

        if (frame.text == "closeMe") {
            clientConn.closeConnection(1001, "Client is going away");
            conn.closeConnection(1001, "You told to close your connection");
        } else if (clientConn != null) {
            clientConn.pushText(frame.text);
        }
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

    resource onClose(ws:Connection conn, ws:CloseFrame frame) {
        ws:Connection parentCon = conn.getParentConnection();
        parentCon.closeConnection(1001, "Server closing connection");
    }

}

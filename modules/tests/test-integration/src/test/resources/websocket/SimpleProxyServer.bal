import ballerina.lang.system;
import ballerina.lang.errors;
import ballerina.lang.maps;
import ballerina.net.ws;

@ws:configuration {
    basePath: "/proxy/ws",
    port:9090
}
service<ws> SimpleProxyServer {

    map clientConnMap = {};

    resource onHandshake(ws:HandshakeConnection con) {
        ws:ClientConnector c = create ws:ClientConnector("ws://localhost:15500/websocket", "ClientService");
        ws:ClientConnectorConfig clientConnectorConfig = {parentConnectionID:con.connectionID};
        ws:Connection clientConn;
        try {
            clientConn = c.connect(clientConnectorConfig);
        } catch (errors:Error err) {
            system:println("Error occcurred : " + err.msg);
            ws:cancelHandshake(con, 1001, "Cannot connect to remote server");
        }
        clientConnMap[con.connectionID] = clientConn;
    }

    resource onTextMessage(ws:Connection conn, ws:TextFrame frame) {
        var clientConn, e = (ws:Connection) clientConnMap[ws:getID(conn)];

        if (frame.text == "closeMe") {
            ws:closeConnection(clientConn, 1001, "Client is going away");
            ws:closeConnection(conn, 1001, "You told to close your connection");
        } else if (clientConn != null) {
            ws:pushText(clientConn, frame.text);
        }
    }

    resource onClose(ws:Connection conn, ws:CloseFrame frame) {
        var clientConn, e = (ws:Connection) clientConnMap[ws:getID(conn)];
        if (clientConn != null) {
            ws:closeConnection(clientConn, 1001, "Client closing connection");
        }
        maps:remove(clientConnMap, ws:getID(conn));
    }
}

@ws:clientService {}
service<ws> ClientService {

    resource onTextMessage(ws:Connection conn, ws:TextFrame frame) {
        ws:Connection parentCon = ws:getParentConnection(conn);
        ws:pushText(parentCon, "client service: " + frame.text);
    }

    resource onClose(ws:Connection conn, ws:CloseFrame frame) {
        ws:Connection parentCon = ws:getParentConnection(conn);
        ws:closeConnection(parentCon, 1001, "Server closing connection");
    }

}

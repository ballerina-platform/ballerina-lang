package sample.websocket.client;

import ballerina.net.ws;
import ballerina.lang.maps;
import ballerina.lang.errors;
import ballerina.lang.system;

@ws:configuration {
    basePath: "/pass-through/ws",
    port:9090
}
service<ws> ServerEndpoint {

    map clientConnMap = {};

    resource onHandshake(ws:HandshakeConnection con) {
        ws:ClientConnector c = create ws:ClientConnector("wss://echo.websocket.org", "ClientService");
        ws:Connection clientConn;
        try {
            clientConn = c.connectWithDefault();
            clientConnMap[con.connectionID] = clientConn;
        } catch (errors:Error err) {
            system:println("Error occcurred : " + err.msg);
            ws:cancelHandshake(con, 1001, "Cannot connect to remote server");
        }
    }

    resource onTextMessage(ws:Connection conn, ws:TextFrame frame) {
        var x, e = (ws:Connection) clientConnMap[ws:getID(conn)];
        if (x != null) {
            ws:pushText(x, frame.text);
        }
    }

    resource onClose(ws:Connection conn, ws:CloseFrame frame) {
        var x, e = (ws:Connection) clientConnMap[ws:getID(conn)];
        if (x != null) {
            ws:closeConnection(x, 1001, "Client closing connection");
        }
        maps:remove(clientConnMap, ws:getID(conn));
    }
}

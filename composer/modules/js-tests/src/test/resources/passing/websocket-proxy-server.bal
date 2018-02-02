import ballerina.lang.maps;
import ballerina.lang.errors;
import ballerina.doc;
import ballerina.net.ws;

@ws:configuration {
    basePath: "/proxy/ws",
    port:9090
}
service<ws> SimpleProxyServer {

    map clientConnMap = {};

    @doc:Description {value:"Create a client connection to remote server from ballerina"}
    @doc:Description {value:"when new client connects to this service endpoint"}
    resource onHandshake(ws:HandshakeConnection con) {
        ws:ClientConnector c = create ws:ClientConnector("wss://echo.websocket.org", "ClientService");
        ws:ClientConnectorConfig clientConnectorConfig = {parentConnectionID:con.connectionID};
        try {
            ws:Connection clientConn = c.connect(clientConnectorConfig);
            clientConnMap[con.connectionID] = clientConn;
        } catch (errors:Error err) {
            ws:cancelHandshake(con, 1001, "Cannot connect to remote server");
        }
    }

    resource onTextMessage(ws:Connection conn, ws:TextFrame frame) {
        var clientCon, e = (ws:Connection)clientConnMap[ws:getID(conn)];
        if (clientCon != null) {
            ws:pushText(clientCon, frame.text);
        }
    }

    resource onBinaryMessage(ws:Connection conn, ws:BinaryFrame frame) {
        var clientConn, e = (ws:Connection)clientConnMap[ws:getID(conn)];
        if (clientConn != null) {
            ws:pushBinary(clientConn, frame.data);
        }
    }

    resource onClose(ws:Connection conn, ws:CloseFrame frame) {
        var clientConn, e = (ws:Connection)clientConnMap[ws:getID(conn)];
        if (clientConn != null) {
            ws:closeConnection(clientConn, frame.statusCode, frame.reason);
        }
        maps:remove(clientConnMap, ws:getID(conn));
    }
}

@doc:Description {value:"Client service to receive frames from remote server"}
@ws:clientService {}
service<ws> ClientService {

    resource onTextMessage(ws:Connection conn, ws:TextFrame frame) {
        ws:Connection parentConn = ws:getParentConnection(conn);
        ws:pushText(parentConn, frame.text);
    }

    resource onBinaryMessage(ws:Connection conn, ws:BinaryFrame frame) {
        ws:Connection parentConn = ws:getParentConnection(conn);
        ws:pushBinary(parentConn, frame.data);
    }

    resource onClose(ws:Connection conn, ws:CloseFrame frame) {
        ws:Connection parentConn = ws:getParentConnection(conn);
        ws:closeConnection(parentConn, frame.statusCode, frame.reason);
    }

}

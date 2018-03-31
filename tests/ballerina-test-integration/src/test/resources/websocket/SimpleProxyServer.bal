import ballerina/io;
import ballerina/net.ws;

@ws:configuration {
    basePath: "/proxy/ws/{name}",
    port:9090
}
service<ws> SimpleProxyServer {

    endpoint<ws:WsClient> c {
        create ws:WsClient("ws://localhost:15500/websocket", "ClientService");
    }
    map clientConnMap = {};

    resource onHandshake(ws:HandshakeConnection con, string name) {
        ws:ClientConnectorConfig clientConnectorConfig = {parentConnectionID:con.connectionID};
        var clientConn, e = c.connect(clientConnectorConfig);
        if (e != null) {
            io:println("Error occcurred : " + e.message);
            con.cancelHandshake(1001, "Cannot connect to remote server");
        } else {
            clientConn.attributes["name"] = name;
            clientConnMap[con.connectionID] = clientConn;
            map queryParams = con.getQueryParams();
            var age, err = (string)queryParams.age;
            if (err == null) {
                clientConn.attributes["age"] = age;
            }
        }
    }

    resource onTextMessage(ws:Connection conn, ws:TextFrame frame, string name) {
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
            map params = conn.getQueryParams();
            var age, err = (string)params.age;
            if (err != null) {
                clientConn.pushText(name + " " + text);
            } else {
                clientConn.pushText(name + "(" + age + ") " + text);
            }
        }
    }

    resource onPing(ws:Connection conn, ws:PingFrame frame) {
        conn.pong(frame.data);
    }

    resource onPong(ws:Connection conn, ws:PongFrame frame, string name) {
        conn.pushText(name + ": pong_received");
    }

    resource onClose(ws:Connection conn, ws:CloseFrame frame) {
        var clientConn, _ = (ws:Connection) clientConnMap[conn.getID()];
        clientConn.closeConnection(1001, "Client closing connection");
        _ = clientConnMap.remove(conn.getID());
    }
}

@ws:clientService {}
service<ws> ClientService {

    resource onTextMessage(ws:Connection conn, ws:TextFrame frame) {
        ws:Connection parentConn = conn.getParentConnection();
        var parentName, _ = (string) conn.attributes["name"];
        var age, err = (string) conn.attributes["age"];
        if(err != null) {
            parentConn.pushText(parentName + " client service: " + frame.text);
        } else {
            parentConn.pushText(parentName + "(" + age + ") client service: " + frame.text);
        }
    }

    resource onPing(ws:Connection conn, ws:PingFrame frame) {
        ws:Connection parentConn = conn.getParentConnection();
        var parentName, _ = (string) conn.attributes["name"];
        parentConn.pushText(parentName + " remote_server_ping");
    }

    resource onPong(ws:Connection conn, ws:PongFrame frame) {
        ws:Connection parentConn = conn.getParentConnection();
        var parentName, _ = (string) conn.attributes["name"];
        parentConn.pushText(parentName + " remote_server_pong");
    }

    resource onClose(ws:Connection conn, ws:CloseFrame frame) {
        ws:Connection parentCon = conn.getParentConnection();
        var parentName, _ = (string) conn.attributes["name"];
        parentCon.closeConnection(1001, parentName + " server closing connection");
    }

}

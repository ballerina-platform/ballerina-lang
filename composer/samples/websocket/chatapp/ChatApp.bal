import ballerina/io;
import ballerina/net.ws;

@ws:configuration {
    basePath:"/chat/ws",
    port:9090,
    idleTimeoutInSeconds:3600
}
service<ws> ChatApp {

    map consMap = {};

    resource onOpen (ws:Connection conn) {
        broadcast(consMap, "New client connected");
        consMap[conn.getID()] = conn;
    }

    resource onTextMessage (ws:Connection con, ws:TextFrame frame) {
        broadcast(consMap, frame.text);
    }

    resource onIdleTimeout (ws:Connection con) {
        // Connection is closed due to inactivity after 1 hour
        io:println("Idle timeout: " + con.getID());
        con.closeConnection(1000, "Closing connection due to inactivity in chat");
    }

    resource onClose (ws:Connection con, ws:CloseFrame frame) {
        consMap.remove(con.getID());
        broadcast(consMap, "User left");
    }
}

function broadcast (map consMap, string text) {
    int i = 0;
    string[] conKeys = consMap.keys();
    int len = lengthof conKeys;
    while (i < len) {
        var con, e = (ws:Connection)consMap[conKeys[i]];
        if (e == null) {
            con.pushText(text);
        }
        i = i + 1;
    }
}

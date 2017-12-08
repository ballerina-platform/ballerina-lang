import ballerina.net.ws;

@ws:configuration {
    basePath: "/chat/{lname}+{fname}/{age}",
    port:9090,
    idleTimeoutInSeconds: 3600
}
service<ws> ChatApp {

    map consMap = {};


    resource onHandshake(ws:HandshakeConnection conn, string fname, string lname, string age) {
        string msg = string`{{lname}} {{fname}} with age {{age}} trying to connect to the chat`;
        println(msg);
    }

    resource onOpen(ws:Connection conn, string fname, string lname, string age) {
        consMap[conn.getID()] = conn;
        println(fname);
        string msg = string`{{lname}} {{fname}} with age {{age}} connected to the chat`;
        broadcast(consMap, msg);
    }

    resource onTextMessage(ws:Connection con, ws:TextFrame frame) {
        println(frame.text);
        broadcast(consMap, frame.text);
    }

    resource onIdleTimeout(ws:Connection con) {
        // Connection is closed due to inactivity after 1 hour.
        println("Idle timeout: " + con.getID());
        con.closeConnection(1000, "Closing connection due to inactivity in chat");
    }

    resource onClose(ws:Connection con, ws:CloseFrame frame) {
        consMap.remove(con.getID());
        broadcast(consMap, "User left");
    }
}

function broadcast(map consMap, string text) {
    string[] conKeys = consMap.keys();
    int len = lengthof conKeys;
    int i = 0;
    while (i < len) {
        var con, _ = (ws:Connection) consMap[conKeys[i]];
        con.pushText(text);
        i = i + 1;
    }
}

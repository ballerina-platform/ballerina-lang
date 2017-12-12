import ballerina.net.ws;

@ws:configuration {
    basePath: "/chat/{fname}+{lname}/{age}",
    port:9090
}
service<ws> ChatApp {

    map consMap = {};

    resource onOpen(ws:Connection conn, string fname, string lname, string age) {
        consMap[conn.getID()] = conn;
        string msg = string `{{fname}} {{lname}} with age {{age}} connected to chat`;
        broadcast(consMap, msg);
    }

    resource onTextMessage(ws:Connection con, ws:TextFrame frame, string fname) {
        string msg = string `{{fname}}: {{frame.text}}`;
        println(msg);
        broadcast(consMap, msg);
    }

    resource onClose(ws:Connection con, ws:CloseFrame frame, string fname) {
        string msg = string `{{fname}} left the chat`;
        consMap.remove(con.getID());
        broadcast(consMap, msg);
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

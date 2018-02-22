import ballerina.io;
import ballerina.net.ws;

@ws:configuration {
    basePath: "/chat/{name}",
    port:9090
}
service<ws> ChatApp {

    map consMap = {};

    resource onOpen(ws:Connection conn, string name) {
        consMap[conn.getID()] = conn;
        map params = conn.getQueryParams();
        var age, err = (string)params.age;
        string msg;
        if (err == null) {
            msg = string `{{name}} with age {{age}} connected to chat`;
        } else {
            msg = string `{{name}} connected to chat`;
        }
        broadcast(consMap, msg);
    }

    resource onTextMessage(ws:Connection con, ws:TextFrame frame, string name) {
        string msg = string `{{name}}: {{frame.text}}`;
        io:println(msg);
        broadcast(consMap, msg);
    }

    resource onClose(ws:Connection con, ws:CloseFrame frame, string name) {
        string msg = string `{{name}} left the chat`;
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

import ballerina/io;
import ballerina/http;

const string NAME = "NAME";
const string AGE = "AGE";
endpoint http:ServiceEndpoint ep {
    port:9090
};

@http:WebSocketServiceConfig {
    basePath:"/chat/{name}"
}
service<http:WebSocketService> ChatApp bind ep {
    string msg;
    map<http:WebSocketConnector> consMap = {};
    onUpgrade (endpoint conn, http:Request req, string name) {
        var params = req.getQueryParams();
        conn.attributes[NAME] = name;
        msg = string `{{untaint name}} connected to chat`;
        string age = untaint <string>params.age;

        if (age != null) {
            conn.attributes[AGE] = age;
            msg = string `{{untaint name}} with age {{age}} connected to chat`;
        }

    }
    onOpen (endpoint conn) {
        io:println(msg);
        consMap[conn.id] = conn.getClient();
        broadcast(consMap, msg);
    }

    onTextMessage (endpoint conn, http:TextFrame frame) {
        msg = untaint string `{{untaint <string>conn.attributes[NAME]}}: {{frame.text}}`;
        io:println(msg);
        broadcast(consMap, msg);
    }

    onClose (endpoint conn, http:CloseFrame frame) {
        msg = string `{{untaint <string>conn.attributes[NAME]}} left the chat`;
        _ = consMap.remove(conn.id);
        broadcast(consMap, msg);
    }
}

function broadcast (map<http:WebSocketConnector> consMap, string text) {
    string[] conKeys = consMap.keys();
    int len = lengthof conKeys;
    int i = 0;
    while (i < len) {
        http:WebSocketConnector con = consMap[conKeys[i]];
        con.pushText(text);
        i = i + 1;
    }
}

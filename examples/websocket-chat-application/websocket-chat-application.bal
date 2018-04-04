import ballerina/io;
import ballerina/http;

const string NAME = "NAME";
const string AGE = "AGE";
endpoint http:WebSocketEndpoint ep {
    port:9090
};

@http:WebSocketServiceConfig {
    basePath:"/chat/{name}"
}
service<http:WebSocketService> ChatApp bind ep {
    string msg;
    map<http:WebSocketEndpoint> consMap = {};
    onUpgrade (endpoint conn, http:Request req, string name) {
        var params = req.getQueryParams();
        conn.attributes[NAME] = name;
        msg = string `{{untaint name}} connected to chat`;
        string age = untaint <string>params.age;

        if (age != null) {
            conn.attributes[AGE] = age;
            msg = string `{{untaint name}} with age {{age}} connected to chat`;
        }
        io:println(msg);
    }
    onOpen (endpoint conn) {
        consMap[conn.id] = conn;
        broadcast(consMap, msg);
    }

    onText (endpoint conn, string text) {
        msg = untaint string `{{untaint <string>conn.attributes[NAME]}}: {{text}}`;
        io:println(msg);
        broadcast(consMap, msg);
    }

    onClose (endpoint conn, int statusCode, string reason) {
        msg = string `{{untaint <string>conn.attributes[NAME]}} left the chat`;
        _ = consMap.remove(conn.id);
        broadcast(consMap, msg);
    }
}

function broadcast (map<http:WebSocketEndpoint> consMap, string text) {
    endpoint http:WebSocketEndpoint con;
    string[] conKeys = consMap.keys();
    int len = lengthof conKeys;
    int i = 0;
    while (i < len) {
        con = consMap[conKeys[i]];
        var val = con -> pushText(text);
        match val {
            http:WebSocketConnectorError err => {io:println("Error: " + err.message);}
            any|null err => {//ignore x
                var x = err;
            }
        }
        i = i + 1;
    }
}

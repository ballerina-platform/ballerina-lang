import ballerina/io;
import ballerina/net.http;

const string NAME = "NAME";
const string AGE = "AGE";
endpoint http:ServiceEndpoint ep {
    port:9090
};

@http:WebSocketServiceConfig {
    basePath:"/chat"
}
service<http:WebSocketService> ChatApp bind ep {
    string msg;
    map<http:WebSocketConnector> consMap = {};
    onUpgrade (endpoint ep, http:Request req) {
        var params = req.getQueryParams();
        string name = untaint <string>params.name;
        if (name != null) {
            ep.getClient().attributes[NAME] = name;
            msg = string `{{name}} connected to chat`;
        } else {
            error err = {message:"Please enter a name"};
            throw err;
        }
        string age = untaint <string>params.age;

        if (age != null) {
            ep.getClient().attributes[AGE] = age;
            msg = string `{{name}} with age {{age}} connected to chat`;
        }

    }
    onOpen (endpoint ep) {
        io:println(msg);
        var conn = ep.getClient();
        consMap[conn.id] = conn;
        broadcast(consMap, msg);
    }

    onTextMessage (endpoint ep, http:TextFrame frame) {
        msg = untaint string `{{untaint <string>ep.getClient().attributes[NAME]}}: {{frame.text}}`;
        io:println(msg);
        broadcast(consMap, msg);
    }

    onClose (endpoint ep, http:CloseFrame frame) {
        var con = ep.getClient();
        msg = string `{{untaint <string>ep.getClient().attributes[NAME]}} left the chat`;
        _ = consMap.remove(con.id);
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

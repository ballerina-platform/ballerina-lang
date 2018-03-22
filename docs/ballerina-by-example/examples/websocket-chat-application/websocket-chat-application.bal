import ballerina/io;
import ballerina/net.http;

endpoint http:ServiceEndpoint ep {
    port:9090
};

@http:WebSocketServiceConfig {
    basePath:"/chat"
}
service<http:WebSocketService> ChatApp bind ep {
    string msg;
    string name;
    string age;
    map<http:WebSocketConnector> consMap = {};
    onUpgrade (endpoint ep, http:Request req) {
        var params = req.getQueryParams();
        var param = <string>params.name;
        match param {
            string val => {
                name = untaint val;
                msg = string `{{name}} connected to chat`;
            }
            error e => {
                error err = {message:"Please enter a name"};
                throw err;
            }
        }

        param = <string>params.age;

        match param {
            string val => {
                age = untaint val;
                msg = string `{{name}} with age {{age}} connected to chat`;
            }
            error e => io:println("You can also enter an error");
        }
    }
    onOpen (endpoint ep) {
        io:println(msg);
        var conn = ep.getClient();
        consMap[conn.id] = conn;
        broadcast(consMap, msg);
    }

    onTextMessage (endpoint con, http:TextFrame frame) {
        msg = untaint string `{{name}}: {{frame.text}}`;
        io:println(msg);
        broadcast(consMap, msg);
    }

    onClose (endpoint ep, http:CloseFrame frame) {
        var con = ep.getClient();
        msg = string `{{name}} left the chat`;
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

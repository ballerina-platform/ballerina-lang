import ballerina.io;
import ballerina.net.grpc;

@grpc:serviceConfig {rpcEndpoint:"chat",
                     clientStreaming:true,
                     serverStreaming:true,
                     generateClientConnector:false}
service<grpc> chatServer {
    map consMap = {};

    resource onOpen (grpc:ServerConnection conn) {
        consMap[<string>conn.id] = conn;
    }

    resource onMessage (grpc:ServerConnection conn, ChatMessage chat) {
        string msg = string `{{chat.from}}: {{chat.message}}`;
        io:println(msg);

        string[] conKeys = consMap.keys();
        int len = lengthof conKeys;
        int i = 0;
        while (i < len) {
           var con, _ = (grpc:ServerConnection) consMap[conKeys[i]];
           _ = con.send(msg);
           i = i + 1;
        }
    }

    resource onError (grpc:ServerConnection conn, grpc:ServerError err) {
        // code to execute when error receive from client.
    }

    resource onComplete (grpc:ServerConnection conn) {
        string msg = string `{{conn.id}} left the chat`;
        io:println(msg);
        consMap.remove(<string>conn.id);

        string[] conKeys = consMap.keys();
        int len = lengthof conKeys;
        int i = 0;
        while (i < len) {
           var con, _ = (grpc:ServerConnection) consMap[conKeys[i]];
           _ = con.send(msg);
           i = i + 1;
        }
    }
}

struct ChatMessage {
    string from;
    string message;
}

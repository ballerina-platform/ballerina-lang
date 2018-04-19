// This is server implementation for bidirectional streaming scenario
import ballerina/io;
import ballerina/grpc;
import ballerina/log;

// Server endpoint configuration
endpoint grpc:Service ep {
    host:"localhost",
    port:9090
};

@grpc:serviceConfig {rpcEndpoint:"chat",
    clientStreaming:true,
    serverStreaming:true,
    generateClientConnector:false}
service<grpc:Listener> Chat bind ep {
    map consMap;
    onOpen(endpoint client) {
        consMap[<string>client.id] = client;
    }

    onMessage(endpoint client, ChatMessage chatMsg) {
        endpoint grpc:Service con;
        string msg = string `{{chatMsg.name}}: {{chatMsg.message}}`;
        io:println(msg);
        string[] conKeys = consMap.keys();
        int len = lengthof conKeys;
        int i = 0;
        while (i < len) {
            con = check <grpc:Service>consMap[conKeys[i]];
            error? err = con->send(msg);
            io:println(err.message but { () => "" });
            i = i + 1;
        }
    }

    onError(endpoint client, grpc:ServerError err) {
        if (err != ()) {
            io:println("Something unexpected happens at server : " + err.message);
        }
    }

    onComplete(endpoint client) {
        endpoint grpc:Service con;
        string msg = string `{{client.id}} left the chat`;
        io:println(msg);
        var v = consMap.remove(<string>client.id);
        string[] conKeys = consMap.keys();
        int len = lengthof conKeys;
        int i = 0;
        while (i < len) {
            con = check <grpc:Service>consMap[conKeys[i]];
            error? err = con->send(msg);
            io:println(err.message but { () => "" });
            i = i + 1;
        }
    }
}

type ChatMessage {
    string name;
    string message;
};

// This is server implementation for bidirectional streaming scenario
import ballerina/io;
import ballerina/grpc;

// Server endpoint configuration
endpoint grpc:Listener ep {
    host:"localhost",
    port:9090
};

@grpc:serviceConfig {name:"chat",
    clientStreaming:true,
    serverStreaming:true}
service<grpc:Service> Chat bind ep {
    map<grpc:Listener> consMap;

    onOpen(endpoint caller) {
        var connID = caller.id;
        consMap[<string>connID] = caller;
    }

    onMessage(endpoint caller, ChatMsg chatMsg) {
        endpoint grpc:Listener con;
        string msg = string `{{chatMsg.name}}: {{chatMsg.message}}`;
        io:println(msg);
        string[] conKeys = consMap.keys();
        int len = lengthof conKeys;
        int i = 0;
        while (i < len) {
            con = <grpc:Listener>consMap[conKeys[i]];
            error? err = caller->send(msg);
            io:println(err.message but { () => "" });
            i = i + 1;
        }
    }

    onError(endpoint caller, error err) {
        if (err != ()) {
            io:println("Something unexpected happens at server : " + err.message);
        }
    }

    onComplete(endpoint caller) {
        endpoint grpc:Listener con;
        var connID = caller.id;
        string msg = string `{{connID}} left the chat`;
        io:println(msg);
        var v = consMap.remove(<string>connID);
        string[] conKeys = consMap.keys();
        int len = lengthof conKeys;
        int i = 0;
        while (i < len) {
            con = <grpc:Listener>consMap[conKeys[i]];
            error? err = con->send(msg);
            io:println(err.message but { () => "" });
            i = i + 1;
        }
    }
}

type ChatMsg {
    string name;
    string message;
};

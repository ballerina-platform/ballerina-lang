// This is the server implementation for the bidirectional streaming scenario.
import ballerina/io;
import ballerina/grpc;

// The server endpoint configuration.
endpoint grpc:Listener listener {
    host: "localhost",
    port: 9090
};

@grpc:ServiceConfig {
    name: "chat",
    clientStreaming: true,
    serverStreaming: true
}

service<grpc:Service> Chat bind listener {
    map<grpc:Listener> consMap;

    //This resource is triggered when a new caller connection is initialized.
    onOpen(endpoint caller) {
        var connID = caller.id;
        consMap[<string>connID] = caller;
    }

    //This resource is triggered when the caller sends a request message to the service.
    onMessage(endpoint caller, ChatMessage chatMsg) {
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

    //This resource is triggered when the server receives an error message from the caller.
    onError(endpoint caller, error err) {
        if (err != ()) {
            io:println("Something unexpected happens at server : "
                    + err.message);
        }
    }

    //This resource is triggered when the caller sends a notification to the server to indicate that it has finished sending messages.
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

type ChatMessage record {
    string name;
    string message;
};

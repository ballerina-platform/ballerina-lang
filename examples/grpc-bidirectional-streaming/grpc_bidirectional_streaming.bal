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
service Chat bind listener {
    map<grpc:Listener> consMap;

    //This resource is triggered when a new caller connection is initialized.
    onOpen(endpoint caller) {
        io:println(string `{{caller.id}} connected to chat`);
        consMap[<string>caller.id] = caller;
    }

    //This resource is triggered when the caller sends a request message to the service.
    onMessage(endpoint caller, ChatMessage chatMsg) {
        endpoint grpc:Listener ep;
        string msg = string `{{chatMsg.name}}: {{chatMsg.message}}`;
        io:println(msg);
        foreach id, con in consMap {
            ep = con;
            error? err = ep->send(msg);
            io:println(err.message but { () => "" });
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
        endpoint grpc:Listener ep;
        string msg = string `{{caller.id}} left the chat`;
        io:println(msg);
        var v = consMap.remove(<string>caller.id);
        foreach id, con in consMap {
            ep = con;
            error? err = ep->send(msg);
            io:println(err.message but { () => "" });
        }
    }
}

type ChatMessage record {
    string name;
    string message;
};

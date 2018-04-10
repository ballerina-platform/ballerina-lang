// This is the server implementation for the bidirectional streaming scenario.
import ballerina/io;
import ballerina/grpc;
import ballerina/log;

// The server endpoint configuration.
endpoint grpc:Service ep {
    host:"localhost",
    port:9090
};

@grpc:serviceConfig {rpcEndpoint:"chat",
    clientStreaming:true,
    serverStreaming:true,
    generateClientConnector:false}
service<grpc:Endpoint> Chat bind ep {
    //TODO: Remove 'the temp initialization' once map issue is fixed.
    map<grpc:Service> consMap = {"_":new};

    onOpen (endpoint client) {
        var connID = client -> getID();
        consMap[<string> connID] = client;
    }

    onMessage (endpoint client, ChatMessage chatMsg) {
        endpoint grpc:Service con;
        string msg = string `{{chatMsg.name}}: {{chatMsg.message}}`;
        io:println(msg);
        string[] conKeys = consMap.keys();
        int len = lengthof conKeys;
        //TODO: set i to zero when map issue is fixed.
        int i = 1;
        while (i < len) {
            con = <grpc:Service>consMap[conKeys[i]];
            grpc:ConnectorError err = con -> send(msg);
            if (err != ()) {
                io:println("Error at onMessage : " + err.message);
            }
            i = i + 1;
        }
    }

    onError (endpoint client, grpc:ServerError err) {
        if (err != ()) {
            io:println("Something unexpected happens at server : " + err.message);
        }
    }

    onComplete (endpoint client) {
        endpoint grpc:Service con;
        var connID = client -> getID();
        string msg = string `{{connID}} left the chat`;
        io:println(msg);
        var v = consMap.remove(<string>connID);
        string[] conKeys = consMap.keys();
        int len = lengthof conKeys;
        //TODO: Set 'i' to zero once the map issue is fixed.
        int i = 1;
        while (i < len) {
            con = <grpc:Service>consMap[conKeys[i]];
            grpc:ConnectorError err = con -> send(msg);
            if (err != ()) {
                io:println("Error at onComplete send message : " + err.message);
            }
            i = i + 1;
        }
    }
}

type ChatMessage {
    string name;
    string message;
};

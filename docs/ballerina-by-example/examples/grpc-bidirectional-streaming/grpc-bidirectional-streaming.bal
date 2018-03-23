import ballerina/io;
import ballerina/net.grpc;
import ballerina/log;

endpoint grpc:Service ep {
    host:"localhost",
    port:9090
};

@grpc:serviceConfig {rpcEndpoint:"LotsOfGreetings",
                     clientStreaming:true,
                     serverStreaming:true,
                     generateClientConnector:false}
service<grpc:Endpoint> helloWorld bind ep {
    map consMap = {};
    onOpen (endpoint client) {
        consMap[<string>client.getClient ().getID()] = client;
    }

    onMessage (endpoint client,  ChatMessage chat) {
        string msg = string `{{chat.name}}: {{chat.message}}`;
        io:println(msg);
        string[] conKeys = consMap.keys();
        int len = lengthof conKeys;
        int i = 0;
        while (i < len) {
            var con =? <grpc:Service> consMap[conKeys[i]];
            grpc:ConnectorError err =  con.getClient ().send(string `{{chat.message}} {{chat.name}}!`);
            if (err != null) {
                io:println("Error at lotsOfReplies : " + err.message);
            }
            i = i + 1;
        }
    }

    onError (endpoint client, grpc:ServerError err) {
        if (err != null) {
            io:println("Something unexpected happens at server : " + err.message);
        }
    }

    onComplete (endpoint client) {
        string msg = string `{{client.getClient ().getID()}} left the chat`;
        io:println(msg);
        var v=consMap.remove(<string>client.getClient ().getID());
        string[] conKeys = consMap.keys();
        int len = lengthof conKeys;
        int i = 0;
        while (i < len) {
            var con =? <grpc:ClientResponder> consMap[conKeys[i]];
            grpc:ConnectorError err = con.send(msg);
            if (err != null) {
                io:println("Error at onComplete send message : " + err.message);
            }
            i = i + 1;
        }
    }
}

struct ChatMessage {
    string name;
    string message;
}

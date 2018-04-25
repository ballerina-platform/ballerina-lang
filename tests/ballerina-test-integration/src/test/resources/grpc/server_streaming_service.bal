// This is server implementation for server streaming scenario
import ballerina/io;
import ballerina/grpc;

// Server endpoint configuration
endpoint grpc:Listener ep {
    host:"localhost",
    port:9090
};

service HelloWorld bind ep {

    @grpc:ResourceConfig {streaming:true}
    lotsOfReplies(endpoint caller, string name) {
        io:println("Server received hello from " + name);
        string[] greets = ["Hi", "Hey", "GM"];
        foreach greet in greets {
            error? err = caller->send(greet + " " + name);
            io:println(err.message but { () => ("send reply: " + greet + " " + name) });
        }
        // Once all messages are sent, server send complete message to notify the client, I’m done.
        _ = caller->complete();
        io:println("send all responses sucessfully.");
    }

}

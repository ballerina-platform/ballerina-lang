// This is the server implementation for the server streaming scenario.
import ballerina/io;
import ballerina/grpc;

// Server endpoint configuration
endpoint grpc:Listener ep {
    host: "localhost",
    port: 9090
};

service HelloWorld bind ep {
    // The annotation indicates how the service resource operates as server streaming.
    @grpc:ResourceConfig { streaming: true }
    lotsOfReplies(endpoint caller, string name) {
        io:println("Server received hello from " + name);
        string[] greets = ["Hi", "Hey", "GM"];

        // Send multiple messages to the caller.
        foreach greet in greets {
            error? err = caller->send(greet + " " + name);
            io:println(err.message but { () => "send reply: " + greet + " " +
                    name });
        }

        // Once all the messages are sent, the server notifies the caller with a `complete` message.
        _ = caller->complete();

        io:println("send all responses sucessfully.");
    }

}

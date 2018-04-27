// This is server implementation for server streaming scenario
import ballerina/io;
import ballerina/grpc;

// Server endpoint configuration
endpoint grpc:Listener ep {
    host: "localhost",
    port: 9090
};

service HelloWorld bind ep {
    // Annotation indicates the service resource operates as server streaming.
    @grpc:ResourceConfig { streaming: true }

    lotsOfReplies(endpoint caller, string name) {
        io:println("Server received hello from " + name);
        string[] greets = ["Hi", "Hey", "GM"];

        // Sends multiple messages to the caller.
        foreach greet in greets {
            error? err = caller->send(greet + " " + name);
            io:println(err.message but { () => "send reply: " + greet + " " +
                                                                        name });
        }

        // Once all messages are sent, server send complete message to notify the caller, I’m done.
        _ = caller->complete();

        io:println("send all responses sucessfully.");
    }

}

// This is the server implementation for the server streaming scenario.
import ballerina/grpc;
import ballerina/io;

service HelloWorld on new grpc:Listener(9090) {
    // The annotation indicates how the service resource operates as server streaming.
    @grpc:ResourceConfig { streaming: true }
    resource function lotsOfReplies(grpc:Caller caller, string name) {

        io:println("Server received hello from " + name);
        string[] greets = ["Hi", "Hey", "GM"];

        // Send multiple messages to the caller.
        foreach greet in greets {
            string msg = greet + " " + name;
            error? err = caller->send(msg);
            if (err is error) {
                io:println("Error from Connector: " + err.reason() + " - "
                                                    + <string>err.detail().message);
            } else {
                io:println("send reply: " + msg);
            }
        }

        // Once all the messages are sent, the server notifies the caller with a `complete` message.
        _ = caller->complete();

        io:println("send all responses sucessfully.");
    }

}

// This is client implementation for bidirectional streaming scenario.
import ballerina/grpc;
import ballerina/io;
import ballerina/log;
import ballerina/runtime;

int total = 0;
function main(string... args) {

    //Client endpoint configuration.
    endpoint ChatClient chatEp {
        url: "http://localhost:9090"
    };

    endpoint grpc:Client ep;
    // Executes unary non-blocking call registering server message listener.
    var res = chatEp->chat(ChatMessageListener);

    match res {
        grpc:error err => {
            io:print("Unexpected error occurred.");
        }
        grpc:Client con => {
            ep = con;
        }
    }

    // Sends multiple messages to the server.
    ChatMsg mes = { name: "Sam", message: "Hi " };
    error? connErr = ep->send(mes);

    io:println(connErr.message but { () => "" });
    runtime:sleep(6000);

    // Once all messages are sent, client send complete message to notify the server, I’m done.
    _ = ep->complete();
}


service<grpc:Service> ChatMessageListener {

    // Resource registered to receive server messages.
    onMessage(string message) {
        io:println("Response received from server: " + message);
    }

    // Resource registered to receive server error messages.
    onError(error err) {
        if (err != ()) {
            io:println("Error reported from server: " + err.message);
        }
    }

    // Resource registered to receive server completed message.
    onComplete() {
        io:println("Server Complete Sending Responses.");
    }
}

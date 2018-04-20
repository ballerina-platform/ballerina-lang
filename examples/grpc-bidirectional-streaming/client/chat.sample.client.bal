// This is client implementation for bidirectional streaming scenario
import ballerina/grpc;
import ballerina/io;
import ballerina/log;
import ballerina.runtime;

int total = 0;
function main(string... args) {

    endpoint ChatClient chatEp {
        host:"localhost",
        port:9090
    };

    endpoint grpc:Client ep;
    // Executing unary non-blocking call registering server message listener.
    var res = chatEp->chat(ChatMessageListener);
    match res {
        grpc:error err => {
            io:print("error");
        }
        grpc:Client con => {
            ep = con;
        }
    }
    ChatMessage mes = {name:"Sam", message:"Hi "};
    error? connErr = ep->send(mes);
    io:println(connErr.message but { () => "" });
    //this will hold forever since this is chat application
    runtime:sleepCurrentWorker(6000);
    _ = ep->complete();
}


service<grpc:Service> ChatMessageListener {

    onMessage(string message) {
        io:println("Response received from server: " + message);
    }

    onError(error err) {
        if (err != ()) {
            io:println("Error reported from server: " + err.message);
        }
    }

    onComplete() {
        io:println("Server Complete Sending Responses.");
    }
}

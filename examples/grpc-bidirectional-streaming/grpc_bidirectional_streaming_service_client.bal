// This is client implementation for bidirectional streaming scenario.
import ballerina/grpc;
import ballerina/io;
import ballerina/runtime;

int total = 0;
public function main() {

    //Client endpoint configuration.
    ChatClient chatEp = new ("http://localhost:9090");

    grpc:StreamingClient ep;
    // Executes unary non-blocking call registering server message listener.
    var res = chatEp->chat(ChatMessageListener);

    if (res is grpc:Error) {
        io:println("Error from Connector: " + res.reason() + " - "
                                  + <string>res.detail()["message"]);
        return;
    } else {
        io:println("Initialized connection sucessfully.");
        ep = res;
    }

    // Sends multiple messages to the server.
    ChatMessage mes = {name: "Sam", message: "Hi "};
    grpc:Error? connErr = ep->send(mes);

    if (connErr is grpc:Error) {
        io:println("Error from Connector: " + connErr.reason() + " - "
                               + <string>connErr.detail()["message"]);
    }
    runtime:sleep(6000);

    // Once all messages are sent, client send complete message to notify the server, I’m done.
    grpc:Error? result = ep->complete();
    if (result is grpc:Error) {
        io:println("Error in sending complete message", result);
    }
}


service ChatMessageListener = service {

    // Resource registered to receive server messages.
    resource function onMessage(string message) {
        io:println("Response received from server: " + message);
    }

    // Resource registered to receive server error messages.
    resource function onError(error err) {
        io:println("Error reported from server: " + err.reason() + " - "
                                  + <string>err.detail()["message"]);
    }

    // Resource registered to receive server completed message.
    resource function onComplete() {
        io:println("Server Complete Sending Responses.");
    }
};

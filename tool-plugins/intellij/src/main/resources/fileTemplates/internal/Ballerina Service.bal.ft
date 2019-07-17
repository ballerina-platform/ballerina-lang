import ballerina/http;
import ballerina/log;

// By default, Ballerina exposes an HTTP service via HTTP/1.1.
service hello on new http:Listener(9090) {

    // Resource functions are invoked with the HTTP caller and the incoming request as arguments.
    resource function sayHello(http:Caller caller, http:Request req) {
        // Send a response back to the caller.
        var result = caller->respond("Hello, World!");
        if (result is error) {
            log:printError("Error sending response", result);
        }
    }
}

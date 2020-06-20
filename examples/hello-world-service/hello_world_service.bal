import ballerina/http;

// By default, Ballerina exposes an HTTP service via HTTP/1.1.
service hello on new http:Listener(9090) {

    // Resource functions are invoked with the HTTP caller and the
    // incoming request as arguments.
    resource function sayHello(http:Caller caller, http:Request req) returns error? {
        // Send a response back to the caller.
        check caller->respond("Hello, World!");
    }
}

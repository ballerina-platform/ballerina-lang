import ballerina/http;

// By default, Ballerina exposes an HTTP service via HTTP/1.1.
service / on new http:Listener(9092) {

    // The resource method is invoked by the GET request for the
    // `/greeting` path. The returned string value
    // eventually becomes the payload of the `http:Response`.
    resource function get welcome() returns string {
        return "Welcome!";
    }
}

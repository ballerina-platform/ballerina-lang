import ballerina/http;

listener http:Listener helloEp = new (9090);

// By default, Ballerina exposes an HTTP service via HTTP/1.1.
service /hello on helloEp {

    // The resource method is invoked by the GET request for the
    // `/greeting` path. The returned string value
    // eventually becomes the payload of the `http:Response`.
    resource function get satyHello() returns string {
        return "Hello!";
    }
}

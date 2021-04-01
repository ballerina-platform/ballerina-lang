import ballerina/http;

// By default, Ballerina exposes an HTTP service via HTTP/1.1.
service "process" on new http:Listener(9091) {

    resource function post connect() returns string {
        return "Connected to database!";
    }
}

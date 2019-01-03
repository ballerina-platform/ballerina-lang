import ballerina/http;
import ballerina/io;
import ballerina/log;

// Create a new client with the backend url.
http:Client clientEndpoint = new("https://postman-echo.com");

public function main() {

    http:Request req = new;
    // Send a `GET` request to the specified endpoint.
    var response = clientEndpoint->get("/get?test=123");

    if (response is http:Response) {
        io:println("GET request:");
        // Retrieve the json payload from the response.
        var msg = response.getJsonPayload();
        if (msg is json) {
            // Print the received json response.
            io:println(msg);
        } else {
            log:printError("Invalid payload received", err = msg);
        }
    } else {
        log:printError("Error when calling the backend", err = response);
    }

    // Set a string payload to the message to be sent to the endpoint.
    req.setPayload("POST: Hello World");

    // Send a `POST` request to the specified endpoint.
    response = clientEndpoint->post("/post", req);
    if (response is http:Response) {
        io:println("\nPOST request:");
        // Retrieve the json payload from the response.
        var msg = response.getJsonPayload();
        if (msg is json) {
            // Print the received json response.
            io:println(msg);
        } else {
            log:printError("Invalid payload received", err = msg);
        }
    } else {
        log:printError("Error when calling the backend", err = response);
    }

    req.setPayload("DELETE: Hello World");
    // Send a `DELETE` request to the specified endpoint.
    response = clientEndpoint->delete("/delete", req);
    if (response is http:Response) {
        io:println("\nDELETE request:");
        var msg = response.getJsonPayload();
        if (msg is json) {
            io:println(msg);
        } else {
            log:printError("Invalid payload received", err = msg);
        }
    } else {
        log:printError("Error when calling the backend", err = response);
    }

    req.setPayload("CUSTOM: Hello World");
    // Use the `execute()` remote function for custom HTTP verbs.
    response = clientEndpoint->execute("COPY", "/get", req);

    // Reinitialize the request.
    req = new;
    req.addHeader("Sample-Name", "http-client-connector");
    // The `get()`, `head()` and `options()` can have the optional `message` parameter
    // that is a request or a payload.
    response = clientEndpoint->get("/get", message = req);
    if (response is http:Response) {
        string contentType = response.getHeader("Content-Type");
        log:printInfo("Content-Type: " + contentType);

        int statusCode = response.statusCode;
        log:printInfo("Status code: " + statusCode);

    } else {
        log:printError("Error when calling the backend", err = response);
    }
}

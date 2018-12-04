import ballerina/http;
import ballerina/io;
import ballerina/log;

http:Client clientEndpoint = new("https://postman-echo.com");

public function main() {

    http:Request req = new;
    // Send a GET request to the specified endpoint.
    var response = clientEndpoint->get("/get?test=123");

    if (response is http:Response) {
        io:println("GET request:");
        var msg = response.getJsonPayload();
        if (msg is json) {
            io:println(msg);
        } else if (msg is error) {
            log:printError(<string>msg.detail().message, err = msg);
        }
    } else if (response is error) {
        log:printError(<string>response.detail().message, err = response);
    }

    // Set a string payload to the message to be sent to the endpoint.
    req.setPayload("POST: Hello World");

    response = clientEndpoint->post("/post", req);
    if (response is http:Response) {
        io:println("\nPOST request:");
        var msg = response.getJsonPayload();
        if (msg is json) {
            io:println(msg);
        } else if (msg is error) {
            log:printError(<string>msg.detail().message, err = msg);
        }
    } else if (response is error) {
        log:printError(<string>response.detail().message, err = response);
    }

    req.setPayload("DELETE: Hello World");
    response = clientEndpoint->delete("/delete", req);
    if (response is http:Response) {
        io:println("\nDELETE request:");
        var msg = response.getJsonPayload();
        if (msg is json) {
            io:println(msg);
        } else if (msg is error) {
            log:printError(<string>msg.detail().message, err = msg);
        }
    } else if (response is error) {
        log:printError(<string>response.detail().message, err = response);
    }

    req.setPayload("CUSTOM: Hello World");
    // The `execute()` remote function can be used if one needs to use custom HTTP verbs.
    response = clientEndpoint->execute("COPY", "/get", req);

    req = new;
    req.addHeader("Sample-Name", "http-client-connector");
    // The `get()`, `head()` and `options() can have optional message parameter
    // which represents request or payload.
    response = clientEndpoint->get("/get", message = req);
    if (response is http:Response) {
        string contentType = response.getHeader("Content-Type");
        log:printInfo("Content-Type: " + contentType);

        int statusCode = response.statusCode;
        log:printInfo("Status code: " + statusCode);

    } else if (response is error) {
        log:printError(<string>response.detail().message, err = response);
    }
}

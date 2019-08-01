import ballerina/http;
import ballerina/io;

// Creates a new client with the backend URL.
http:Client clientEndpoint = new("https://postman-echo.com");

public function main() {
    io:println("GET request:");
    // Sends a `GET` request to the specified endpoint.
    var response = clientEndpoint->get("/get?test=123");
    // Handles the response.
    handleResponse(response);

    io:println("\nPOST request:");
    // Sets a `string` payload to the message to be sent to the endpoint.
    http:Request req = new;
    req.setPayload("POST: Hello World");
    // Sends a `POST` request to the specified endpoint.
    response = clientEndpoint->post("/post", req);
    // Handles the response.
    handleResponse(response);

    io:println("\nDELETE request:");
    // Sets a `string` payload to the message to be sent to the endpoint.
    req.setPayload("DELETE: Hello World");
    // Sends a `DELETE` request to the specified endpoint.
    response = clientEndpoint->delete("/delete", req);
    // Handles the response.
    handleResponse(response);

    io:println("\nUse custom HTTP verbs:");
    // Sets a `string` payload to the message, which will be sent to the endpoint.
    req.setPayload("CUSTOM: Hello World");
    // Uses the `execute()` remote function for custom HTTP verbs.
    response = clientEndpoint->execute("COPY", "/get", req);

    // Reinitializes the request.
    req = new;
    req.addHeader("Sample-Name", "http-client-connector");
    // The `get()`, `head()`, and `options()` can have the optional `message` parameter,
    // which will be a request or a payload.
    response = clientEndpoint->get("/get", req);
    if (response is http:Response) {
        string contentType = response.getHeader("Content-Type");
        io:println("Content-Type: " + contentType);

        int statusCode = response.statusCode;
        io:println("Status code: " + statusCode.toString());

    } else {
        io:println("Error when calling the backend: " , response.reason());
    }
}

//The below function handles the response received from the remote HTTP endpoint.
function handleResponse(http:Response|error response) {
    if (response is http:Response) {
        var msg = response.getJsonPayload();
        if (msg is json) {
            // Prints the received `json` response.
            io:println(msg);
        } else {
            io:println("Invalid payload received:" , msg.reason());
        }
    } else {
        io:println("Error when calling the backend: ", response.reason());
    }
}

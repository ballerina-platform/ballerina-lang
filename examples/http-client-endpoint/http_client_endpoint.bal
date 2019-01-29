import ballerina/http;
import ballerina/io;

// Create a new client with the backend url.
http:Client clientEndpoint = new("https://postman-echo.com");

public function main() {
    io:println("GET request:");
    // Send a `GET` request to the specified endpoint.
    var response = clientEndpoint->get("/get?test=123");
    // Handle the response.
    handleResponse(response);

    io:println("\nPOST request:");
    // Set a `string` payload to the message to be sent to the endpoint.
    http:Request req = new;
    req.setPayload("POST: Hello World");
    // Send a `POST` request to the specified endpoint.
    response = clientEndpoint->post("/post", req);
    // Handle the response.
    handleResponse(response);

    io:println("\nDELETE request:");
    // Set a `string` payload to the message to be sent to the endpoint.
    req.setPayload("DELETE: Hello World");
    // Send a `DELETE` request to the specified endpoint.
    response = clientEndpoint->delete("/delete", req);
    // Handle the response.
    handleResponse(response);

    io:println("\nUse custom HTTP verbs:");
    // Set a `string` payload to the message, which will be sent to the endpoint.
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
        io:println("Content-Type: " + contentType);

        int statusCode = response.statusCode;
        io:println("Status code: " + statusCode);

    } else {
        io:println("Error when calling the backend: " , response.reason());
    }
}

//Function to handle the response received from the remote HTTP endpoint.
function handleResponse(http:Response|error response) {
    if (response is http:Response) {
        var msg = response.getJsonPayload();
        if (msg is json) {
            // Print the received `json` response.
            io:println(msg);
        } else {
            io:println("Invalid payload received:" , msg.reason());
        }
    } else {
        io:println("Error when calling the backend: ", response.reason());
    }
}

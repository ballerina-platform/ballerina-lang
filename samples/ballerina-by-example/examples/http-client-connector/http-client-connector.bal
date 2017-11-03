import ballerina.net.http;

function main (string[] args) {
    http:ClientConnector httpConnector;
    // Create an HTTP Client Connector
    httpConnector = create http:ClientConnector("https://postman-echo.com", {});
    http:Request req = {};

    // Send a GET request to the specified endpoint
    http:Response resp = httpConnector.get("/get?test=123", req);
    println("GET request:");
    println(resp.getJsonPayload());

    // Set a string payload to the message to be sent to the endpoint
    req.setStringPayload("POST: Hello World");
    resp = httpConnector.post("/post", req);
    println("\nPOST request:");
    println(resp.getJsonPayload());

    // Set a JSON payload to the message to be sent to the endpoint
    json jsonMsg = {method:"PUT", payload:"Hello World"};
    req.setJsonPayload(jsonMsg);
    resp = httpConnector.put("/put", req);
    println("\nPUT request:");
    println(resp.getJsonPayload());

    // Set an XML payload to the message to be sent to the endpoint.
    xml xmlMsg = xml `<request><method>PATCH</method><payload>Hello World!</payload></request>`;
    req.setXmlPayload(xmlMsg);
    json j = {};
    // Remove the json payload
    req.setJsonPayload(j);
    resp = httpConnector.patch("/patch", req);
    println("\nPATCH request:");
    println(resp.getJsonPayload());

    req.setStringPayload("DELETE: Hello World");
    resp = httpConnector.delete("/delete", req);
    println("\nDELETE request:");
    println(resp.getJsonPayload());

    req.setStringPayload("CUSTOM: Hello World");
    // The execute() action can be used if one needs to use custom HTTP verbs
    resp = httpConnector.execute("COPY", "/get", req);

    // The messages and http packages provide various utility functions which are useful when dealing with HTTP requests/responses.
    req = {};
    req.addHeader("Sample-Name", "http-client-connector");
    resp = httpConnector.get("/get", req);

    string contentType = resp.getHeader("Content-Type");
    println("\nContent-Type: " + contentType);

    int statusCode = resp.getStatusCode();
    println("Status code: " + statusCode);
}

import ballerina.lang.system;
import ballerina.net.http;
import ballerina.net.http.request;
import ballerina.net.http.response;

function main (string[] args) {
    // Create an HTTP Client Connector
    http:ClientConnector httpConnector = create http:ClientConnector("https://postman-echo.com");
    http:Request req = {};

    // Send a GET request to the specified endpoint
    http:Response resp = httpConnector.get("/get?test=123", req);
    system:println("GET request:");
    system:println(resp);

    // Set a string payload to the message to be sent to the endpoint
    request:setStringPayload(req, "POST: Hello World");
    resp = httpConnector.post("/post", req);
    system:println("\nPOST request:");
    system:println(resp);

    // Set a JSON payload to the message to be sent to the endpoint
    json jsonMsg = {method: "PUT", payload: "Hello World"};
    request:setJsonPayload(req, jsonMsg);
    resp = httpConnector.put("/put", req);
    system:println("\nPUT request:");
    system:println(resp);

    // Set an XML payload to the message to be sent to the endpoint
    xml xmlMsg = xml `<request><method>PATCH</method><payload>Hello World!</payload></request>`;
    request:setXmlPayload(req, xmlMsg);
    resp = httpConnector.patch("/patch", req);
    system:println("\nPATCH request:");
    system:println(resp);

    request:setStringPayload(req, "DELETE: Hello World");
    resp = httpConnector.delete("/delete", req);
    system:println("\nDELETE request:");
    system:println(resp);

    request:setStringPayload(req, "CUSTOM: Hello World");
    // The execute() action can be used if one needs to use custom HTTP verbs
    resp = httpConnector.execute("COPY", "/get", req);

    // The messages and http packages provide various utility functions which are useful when dealing with HTTP requests/responses.
    req = {};
    request:addHeader(req, "Sample-Name", "http-client-connector");
    resp = httpConnector.get("/get", req);

    string contentType = response:getHeader(resp, "Content-Type");
    system:println("\nContent-Type: " + contentType);

    int statusCode = response:getStatusCode(resp);
    system:println("Status code: " + statusCode);
}

import ballerina.lang.system;
import ballerina.net.http;
import ballerina.lang.messages;

function main (string[] args) {
    // Create an HTTP Client Connector
    http:ClientConnector httpConnector = create http:ClientConnector("https://postman-echo.com");
    message m = {};

    // Send a GET request to the specified endpoint
    message response = httpConnector.get("/get?test=123", m);
    system:println("GET request:");
    system:println(response);

    // Set a string payload to the message to be sent to the endpoint
    messages:setStringPayload(m, "POST: Hello World");
    response = httpConnector.post("/post", m);
    system:println("\nPOST request:");
    system:println(response);

    // Set a JSON payload to the message to be sent to the endpoint
    json jsonMsg = {method: "PUT", payload: "Hello World"};
    messages:setJsonPayload(m, jsonMsg);
    response = httpConnector.put("/put", m);
    system:println("\nPUT request:");
    system:println(response);

    // Set an XML payload to the message to be sent to the endpoint
    xml xmlMsg = xml `<request><method>PATCH</method><payload>Hello World!</payload></request>`;
    messages:setXmlPayload(m, xmlMsg);
    response = httpConnector.patch("/patch", m);
    system:println("\nPATCH request:");
    system:println(response);

    messages:setStringPayload(m, "DELETE: Hello World");
    response = httpConnector.delete("/delete", m);
    system:println("\nDELETE request:");
    system:println(response);

    messages:setStringPayload(m, "CUSTOM: Hello World");
    // The execute() action can be used if one needs to use custom HTTP verbs
    response = httpConnector.execute("COPY", "/get", m);

    // The messages and http packages provide various utility functions which are useful when dealing with HTTP requests/responses.
    m = {};
    messages:addHeader(m, "Sample-Name", "http-client-connector");
    response = httpConnector.get("/get", m);

    string contentType = messages:getHeader(response, "Content-Type");
    system:println("\nContent-Type: " + contentType);

    int statusCode = http:getStatusCode(response);
    system:println("Status code: " + statusCode);
}

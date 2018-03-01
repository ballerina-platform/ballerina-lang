import ballerina.io;
import ballerina.net.http;

function main (string[] args) {
    endpoint<http:HttpClient> httpEndpoint {
        create http:HttpClient("https://postman-echo.com", {});
    }

    http:OutRequest req = {};
    // Send a GET request to the specified endpoint
    http:InResponse resp = {};
    resp, _ = httpEndpoint.get("/get?test=123", req);
    io:println("GET request:");
    io:println(resp.getJsonPayload());

    // Set a string payload to the message to be sent to the endpoint.
    req.setStringPayload("POST: Hello World");
    resp, _ = httpEndpoint.post("/post", req);
    io:println("\nPOST request:");
    io:println(resp.getJsonPayload());

    // Set a JSON payload to the message to be sent to the endpoint.
    json jsonMsg = {method:"PUT", payload:"Hello World"};
    req.setJsonPayload(jsonMsg);
    resp, _ = httpEndpoint.put("/put", req);
    io:println("\nPUT request:");
    io:println(resp.getJsonPayload());

    // Set an XML payload to the message to be sent to the endpoint.
    xml xmlMsg = xml `<request><method>PATCH</method><payload>Hello World!</payload></request>`;
    req.setXmlPayload(xmlMsg);
    json j = {};
    // Remove the json payload.
    req.setJsonPayload(j);
    resp, _ = httpEndpoint.patch("/patch", req);
    io:println("\nPATCH request:");
    io:println(resp.getJsonPayload());

    req.setStringPayload("DELETE: Hello World");
    resp, _ = httpEndpoint.delete("/delete", req);
    io:println("\nDELETE request:");
    io:println(resp.getJsonPayload());

    req.setStringPayload("CUSTOM: Hello World");
    // The execute() action can be used if one needs to use custom HTTP verbs.
    resp, _ = httpEndpoint.execute("COPY", "/get", req);

    http:HttpClient httpConn = create http:HttpClient("https://postman-echo.com", {});

    //bind statement can be used to bind new connections to existing endpoints.
    bind httpConn with httpEndpoint;

    // The messages and http packages provide various utility functions which are useful when dealing with HTTP requests/responses.
    req = {};
    req.addHeader("Sample-Name", "http-client-connector");
    resp, _ = httpEndpoint.get("/get", req);

    string contentType = resp.getHeader("Content-Type");
    io:println("\nContent-Type: " + contentType);

    int statusCode = resp.statusCode;
    io:println("Status code: " + statusCode);
}

import ballerina/http;
import ballerina/io;
import ballerina/log;

endpoint http:Client clientEndpoint {
    url: "https://postman-echo.com"
};

function main(string... args) {

    http:Request req = new;
    // Send a GET request to the specified endpoint.
    var response = clientEndpoint->get("/get?test=123");

    match response {
        http:Response resp => {
            io:println("GET request:");
            var msg = resp.getJsonPayload();
            match msg {
                json jsonPayload => {
                    io:println(jsonPayload);
                }
                error err => {
                    log:printError(err.message, err = err);
                }
            }
        }
        error err => { log:printError(err.message, err = err); }
    }
    // Set a string payload to the message to be sent to the endpoint.
    req.setPayload("POST: Hello World");

    response = clientEndpoint->post("/post", req);
    match response {
        http:Response resp => {
            io:println("\nPOST request:");
            var msg = resp.getJsonPayload();
            match msg {
                json jsonPayload => {
                    io:println(jsonPayload);
                }
                error err => {
                    log:printError(err.message, err = err);
                }
            }
        }
        error err => { log:printError(err.message, err = err); }

    }

    // Set a JSON payload to the message to be sent to the endpoint.
    json jsonMsg = { method: "PUT", payload: "Hello World" };
    req.setJsonPayload(jsonMsg);

    response = clientEndpoint->put("/put", req);
    match response {
        http:Response resp => {
            io:println("\nPUT request:");
            var msg = resp.getJsonPayload();
            match msg {
                json jsonPayload => {
                    io:println(jsonPayload);
                }
                error err => {
                    log:printError(err.message, err = err);
                }
            }
        }
        error err => { log:printError(err.message, err = err); }
    }

    // Set an XML payload to the message to be sent to the endpoint.
    xml xmlMsg = xml `<request>
                        <method>PATCH</method>
                        <payload>Hello World!</payload>
                      </request>`;
    req.setXmlPayload(xmlMsg);

    response = clientEndpoint->patch("/patch", req);
    match response {
        http:Response resp => {
            io:println("\nPATCH request:");
            var msg = resp.getJsonPayload();
            match msg {
                json jsonPayload => {
                    io:println(jsonPayload);
                }
                error err => {
                    log:printError(err.message, err = err);
                }
            }
        }
        error err => { log:printError(err.message, err = err); }
    }

    req.setPayload("DELETE: Hello World");
    response = clientEndpoint->delete("/delete", req);
    match response {
        http:Response resp => {
            io:println("\nDELETE request:");
            var msg = resp.getJsonPayload();
            match msg {
                json jsonPayload => {
                    io:println(jsonPayload);
                }
                error err => {
                    log:printError(err.message, err = err);
                }
            }
        }
        error err => { log:printError(err.message, err = err); }
    }

    req.setPayload("CUSTOM: Hello World");
    // The `execute()` action can be used if one needs to use custom HTTP verbs.
    response = clientEndpoint->execute("COPY", "/get", req);

    req = new;
    req.addHeader("Sample-Name", "http-client-connector");
    response = clientEndpoint->get("/get", message = req);
    match response {
        http:Response resp => {
            string contentType = resp.getHeader("Content-Type");
            log:printInfo("\nContent-Type: " + contentType);

            int statusCode = resp.statusCode;
            log:printInfo("Status code: " + statusCode);

        }
        error err => { log:printError(err.message, err = err); }
    }
}

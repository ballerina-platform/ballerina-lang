import ballerina/http;
import ballerina/mime;
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
            log:printInfo("GET request:");
            var msg = resp.getJsonPayload();
            match msg {
                json jsonPayload => {
                    log:printInfo(jsonPayload);
                }
                error err => {
                    log:printError(err.message);
                }
            }
        }
        error err => {log:printError(err.message);}
    }
    // Set a string payload to the message to be sent to the endpoint.
    req.setPayload("POST: Hello World");

    response = clientEndpoint->post("/post", request = req);
    match response {
        http:Response resp => {
            log:printInfo("\nPOST request:");
            var msg = resp.getJsonPayload();
            match msg {
                json jsonPayload => {
                    log:printInfo(jsonPayload);
                }
                error err => {
                    log:printError(err.message);
                }
            }
        }
        error err => {log:printError(err.message);}

    }

    // Set a JSON payload to the message to be sent to the endpoint.
    json jsonMsg = { method: "PUT", payload: "Hello World" };
    req.setJsonPayload(jsonMsg);

    response = clientEndpoint->put("/put", request = req);
    match response {
        http:Response resp => {
            log:printInfo("\nPUT request:");
            var msg = resp.getJsonPayload();
            match msg {
                json jsonPayload => {
                    log:printInfo(jsonPayload);
                }
                error err => {
                    log:printError(err.message);
                }
            }
        }
        error err => {log:printError(err.message);}
    }

    // Set an XML payload to the message to be sent to the endpoint.
    xml xmlMsg = xml `<request><method>PATCH</method><payload>Hello World!</payload></request>`;
    req.setXmlPayload(xmlMsg);

    response = clientEndpoint->patch("/patch", request = req);
    match response {
        http:Response resp => {
            log:printInfo("\nPATCH request:");
            var msg = resp.getJsonPayload();
            match msg {
                json jsonPayload => {
                    log:printInfo(jsonPayload);
                }
                error err => {
                    log:printError(err.message);
                }
            }
        }
        error err => {log:printError(err.message);}
    }

    req.setPayload("DELETE: Hello World");
    response = clientEndpoint->delete("/delete", request = req);
    match response {
        http:Response resp => {
            log:printInfo("\nDELETE request:");
            var msg = resp.getJsonPayload();
            match msg {
                json jsonPayload => {
                    log:printInfo(jsonPayload);
                }
                error err => {
                    log:printError(err.message);
                }
            }
        }
        error err => {log:printError(err.message);}
    }

    req.setPayload("CUSTOM: Hello World");
    // The `execute()` action can be used if one needs to use custom HTTP verbs.
    response = clientEndpoint->execute("COPY", "/get", req);

    req = new;
    req.addHeader("Sample-Name", "http-client-connector");
    response = clientEndpoint->get("/get", request = req);
    match response {
        http:Response resp => {
            string contentType = resp.getHeader("Content-Type");
            log:printInfo("\nContent-Type: " + contentType);

            int statusCode = resp.statusCode;
            log:printInfo("Status code: " + statusCode);

        }
        error err => {log:printError(err.message);}
    }
}

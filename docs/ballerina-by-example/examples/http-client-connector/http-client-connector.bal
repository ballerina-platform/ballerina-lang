import ballerina/io;
import ballerina/http;
import ballerina/mime;
endpoint http:Client clientEndpoint {
    targets: [{
                  url: "https://postman-echo.com"
              }]
    };

function main (string[] args) {

    http:Request req = new;
    // Send a GET request to the specified endpoint
    var response = clientEndpoint -> get("/get?test=123", req);
    match response {
        http:Response resp => {
            io:println("GET request:");
            var msg = resp.getJsonPayload();
            match msg {
                json jsonPayload => {
                    io:println(jsonPayload);
                }
                http:PayloadError payloadError => {
                    io:println(payloadError.message);
                }
            }
        }
        http:HttpConnectorError err =>{io:println(err.message);}
    }
    // Set a string payload to the message to be sent to the endpoint.
    req.setStringPayload("POST: Hello World");
    response = clientEndpoint -> post("/post", req);
    match response {
        http:Response resp => {
            io:println("\nPOST request:");
            var msg = resp.getJsonPayload();
            match msg {
                json jsonPayload => {
                    io:println(jsonPayload);
                }
                http:PayloadError payloadError => {
                    io:println(payloadError.message);
                }
            }
        }
        http:HttpConnectorError err =>{io:println(err.message);}

    }

    // Set a JSON payload to the message to be sent to the endpoint.
    json jsonMsg = {method:"PUT", payload:"Hello World"};
    req.setJsonPayload(jsonMsg);
    response = clientEndpoint -> put("/put", req);
    match response {
        http:Response resp => {
            io:println("\nPUT request:");
            var msg = resp.getJsonPayload();
            match msg {
                json jsonPayload => {
                    io:println(jsonPayload);
                }
                http:PayloadError payloadError => {
                    io:println(payloadError.message);
                }
            }
        }
        http:HttpConnectorError err =>{io:println(err.message);}
    }

    // Set an XML payload to the message to be sent to the endpoint.
    xml xmlMsg = xml `<request><method>PATCH</method><payload>Hello World!</payload></request>`;
    req.setXmlPayload(xmlMsg);
    json j = {};
    // Remove the json payload.
    req.setJsonPayload(j);
    response = clientEndpoint -> patch("/patch", req);
    match response {
        http:Response resp => {
            io:println("\nPATCH request:");
            var msg = resp.getJsonPayload();
            match msg {
                json jsonPayload => {
                    io:println(jsonPayload);
                }
                http:PayloadError payloadError => {
                    io:println(payloadError.message);
                }
            }
        }
        http:HttpConnectorError err =>{io:println(err.message);}
    }

    req.setStringPayload("DELETE: Hello World");
    response = clientEndpoint -> delete("/delete", req);
    match response {
        http:Response resp => {
            io:println("\nDELETE request:");
            var msg = resp.getJsonPayload();
            match msg {
                json jsonPayload => {
                    io:println(jsonPayload);
                }
                http:PayloadError payloadError => {
                    io:println(payloadError.message);
                }
            }
        }
        http:HttpConnectorError err =>{io:println(err.message);}

    }

    req.setStringPayload("CUSTOM: Hello World");
    // The execute() action can be used if one needs to use custom HTTP verbs.
    response = clientEndpoint -> execute("COPY", "/get", req);

    req = new;
    req.addHeader("Sample-Name", "http-client-connector");
    response = clientEndpoint -> get("/get", req);
    match response {
        http:Response resp => {
            string contentType = resp.getHeader("Content-Type");
            io:println("\nContent-Type: " + contentType);

            int statusCode = resp.statusCode;
            io:println("Status code: " + statusCode);

        }
        http:HttpConnectorError err =>{io:println(err.message);}

    }
}

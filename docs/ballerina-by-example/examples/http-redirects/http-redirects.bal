import ballerina.io;
import ballerina.net.http;

endpoint<http:Client> clientEndoint {
    serviceUri: "http://www.mocky.io",
    followRedirects : { enabled : true, maxCount : 5 }
}

function main (string[] args) {
    http:Request req = {};

    //Send a GET request to the specified endpoint
    http:Response resp;
    resp, _ = clientEndoint -> get("/v2/59d590762700000a049cd694", req);
    var payload, payloadError = resp.getStringPayload();
    if (payloadError == null) {
        io:println("Response received for the GET request is : " + payload);
    } else {
        io:println("Error occurred : " + payloadError.message);
    }
}

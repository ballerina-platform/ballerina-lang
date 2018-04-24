import ballerina/io;
import ballerina/http;
import ballerina/log;
import ballerina/mime;

endpoint http:Client clientEP {
    url: "http://www.mocky.io",
    followRedirects: {enabled: true, maxCount: 5}
};

function main(string... args) {

    // Send a GET request to the specified endpoint
    var returnResult = clientEP->get("/v2/59d590762700000a049cd694");
    match returnResult {
        error connectionErr => log:printError("Error in connection", err = connectionErr);
        http:Response resp => {
            match resp.getTextPayload() {
                error payloadError => log:printError("Error in payload", err = payloadError);
                string payload => io:println("Response received for the GET request is : " + payload);
            }
        }
    }
}

import ballerina/http;
import ballerina/log;

// Create an endpoint for the client.
endpoint http:Client clientEP {
    url:"http://www.mocky.io"
};

function main(string... args) {
    // Send a get request to the server.
    var resp = clientEP->get("/v2/5ae082123200006b00510c3d/");

    match resp {
        http:Response response => {
            // Retrieve the text message from the response.
            match (response.getTextPayload()) {
                // Log the retrieved text paylod.
                string res => log:printInfo(res);
                // If an error occurs when retrieving the text payload, log the error.
                error err => log:printError(err.message);
            }
        }
        // If an error occurs when getting the response, log the error.
        error err => log:printError(err.message);
    }
}

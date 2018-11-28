import ballerina/http;
import ballerina/log;

public function main() {
    // Create an endpoint for the client.
    http:Client clientEP = new("http://www.mocky.io");
    // Send a get request to the server.
    var resp = clientEP->get("/v2/5ae082123200006b00510c3d/");

    if (resp is http:Response) {
        // Retrieve the text message from the response.
        var payload = resp.getTextPayload();
        if (payload is string) {
            // Log the retrieved text paylod.
            log:printInfo(payload);
        } else if (payload is error) {
            // If an error occurs when retrieving the text payload, log the error.
            log:printError(string.create(payload.detail().message));
        }
    } else if (resp is error) {
            // If an error occurs when getting the response, log the error.
        log:printError(string.create(resp.detail().message));
    }
}

import ballerina/http;
import ballerina/log;

public function main() {
    // Create an HTTP client to interact with a remote endpoint.
    http:Client clientEP = new("http://www.mocky.io");
    // Send a get request to the server.
    var resp = clientEP->get("/v2/5ae082123200006b00510c3d/");

    if (resp is http:Response) {
        // If the request is successful, retrieve the text payload from the
        // response.
        var payload = resp.getTextPayload();
        if (payload is string) {
            // Log the retrieved text paylod.
            log:printInfo(payload);
        } else {
            // If an error occurs while retrieving the text payload, log
            // the error.
            log:printError(<string> payload.detail().message);
        }
    } else {
        // If an error occurs when getting the response, log the error.
        log:printError(<string> resp.detail().message);
    }
}

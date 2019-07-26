import ballerina/http;
import ballerina/io;

public function main() {
    // Creates an HTTP client to interact with a remote endpoint.
    http:Client clientEP = new("http://www.mocky.io");
    // Sends a GET request to the server.
    var resp = clientEP->get("/v2/5ae082123200006b00510c3d/");

    if (resp is http:Response) {
        // If the request is successful, retrieves the text payload from the
        // response.
        var payload = resp.getTextPayload();
        if (payload is string) {
            // Logs the retrieved text payload.
            io:println(payload);
        } else {
            // If an error occurs while retrieving the text payload, prints
            // the error.
            io:println(payload.detail()?.message);
        }
    } else {
        // If an error occurs when getting the response, prints the error.
        io:println(resp.detail()?.message);
    }
}

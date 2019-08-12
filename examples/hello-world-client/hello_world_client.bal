import ballerina/http;
import ballerina/io;

public function main() {
    // Create an HTTP client to interact with a remote endpoint.
    http:Client clientEP = new("http://www.mocky.io");
    // Send a GET request to the server.
    var resp = clientEP->get("/v2/5ae082123200006b00510c3d/");

    if (resp is http:Response) {
        // If the request is successful, retrieve the text payload from the
        // response.
        var payload = resp.getTextPayload();
        if (payload is string) {
            // Log the retrieved text payload.
            io:println(payload);
        } else {
            // If an error occurs while retrieving the text payload, print
            // the detail mapping of the error.
            io:println(payload.detail());
        }
    } else {
        // If an error occurs when getting the response, print the detail
        // mapping of the error.
        io:println(resp.detail());
    }
}

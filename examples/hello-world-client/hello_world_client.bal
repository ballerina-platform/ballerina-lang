import ballerina/http;
import ballerina/io;

public function main() returns @tainted error? {
    // Create an HTTP client to interact with a remote endpoint.
    http:Client clientEP = new ("http://www.mocky.io");
    // Send a GET request to the server.
    http:Response resp = check clientEP->get("/v2/5ae082123200006b00510c3d/");
    // Retrieve the text payload from the response.
    string payload = check resp.getTextPayload();
    // Log the retrieved text payload.
    io:println(payload);
}

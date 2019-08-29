import ballerina/io;
import ballerina/http;

public function main() {
    http:Client clientEP = new("http://localhost:9241");
    http:Request req = new();
    req.addHeader("content-type", "text/plain");
    req.addHeader("Expect", "100-continue");
    req.setPayload("Hello World!");
    var response = clientEP->post("/continue", req);
    if (response is http:Response) {
        var payload = response.getTextPayload();
        if (payload is string) {
            io:print(payload);
            io:print(response.statusCode);
        } else {
            io:println(<string>payload.detail()["message"]);
        }
    } else {
        io:println(<string>response.detail()["message"]);
    }
}
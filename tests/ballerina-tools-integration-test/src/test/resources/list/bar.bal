import ballerina/http;
import ballerina/io;
import ballerina/log;

http:Client clientEndpoint = new("https://postman-echo.com");

public function main() {

    http:Request req = new;
    // Send a GET request to the specified endpoint.
    var response = clientEndpoint->get("/get?test=123");

    if (response is http:Response) {
        io:println("GET request:");
        var msg = response.getJsonPayload();
        if (msg is json) {
            io:println(msg);
        } else if (msg is error) {
            log:printError(<string>msg.detail().message, err = msg);
        }
    } else if (response is error) {
        log:printError(<string>response.detail().message, err = response);
    }
}

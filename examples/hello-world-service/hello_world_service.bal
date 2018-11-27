import ballerina/http;
import ballerina/log;

// By default, Ballerina exposes a service via HTTP/1.1.
service hello on new http:Server(9090) {

    // Invoke all resources with arguments of server connector and request.
    resource function hello (http:Caller ep, http:Request req) {
        // Send the response back to the caller.

        var result = caller->respond(res);
        if (result is error) {
            log:printError("Error sending response", err = result);
        }
    }
}

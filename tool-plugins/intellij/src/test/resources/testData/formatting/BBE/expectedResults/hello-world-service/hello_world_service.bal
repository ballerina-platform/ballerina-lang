import ballerina/http;
import ballerina/log;

// By default, Ballerina exposes a service via HTTP/1.1.
service<http:Service> hello bind { port: 9090 } {

    // Invoke all resources with arguments of server connector and request.
    sayHello(endpoint caller, http:Request req) {
        http:Response res = new;
        // Use a util method to set a string payload.
        res.setPayload("Hello, World!");

        // Send the response back to the caller.
        caller->respond(res) but { error e => log:printError(
                                                  "Error sending response", err = e) };
    }
}

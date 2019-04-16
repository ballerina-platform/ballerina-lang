import ballerina/http;
import ballerina/log;
import ballerina/runtime;

http:Client backendClientEP = new("http://localhost:8080", config = {
    // Timeout configuration.
    timeoutMillis: 10000

});

// Create an HTTP service bound to the listener endpoint.
@http:ServiceConfig {
    basePath: "/timeout"
}
service timeoutService on new http:Listener(9090) {
    // Create a REST resource within the API.
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    // The parameters include a reference to the caller
    // endpoint and an object of the request data.
    resource function invokeEndpoint(http:Caller caller, http:Request request) {

        var backendResponse = backendClientEP->forward("/hello", request);
        // The `is` operator is used to separate out union-type returns.
        // The type of `backendResponse` variable is the union of `http:Response` and an `error`.
        // If a response is returned, `backendResponse` is treated as an `http:Response`
        // within the if-block and the normal process runs.
        // If the service returns an `error`, `backendResponse` is implicitly
        // converted to an `error` within the else block.
        if (backendResponse is http:Response) {

            var responseToCaller = caller->respond(backendResponse);
            if (responseToCaller is error) {
                log:printError("Error sending response", err = responseToCaller);
            }
        } else {
            http:Response response = new;
            response.statusCode = http:INTERNAL_SERVER_ERROR_500;
            string errorMessage = <string> backendResponse.detail().message;
            if (errorMessage ==
                  "Idle timeout triggered before initiating inbound response") {
                response.setPayload(
                            "Request timed out. Please try again in sometime."
                );
            } else {
                response.setPayload(errorMessage);
            }
            var responseToCaller = caller->respond(response);
            if (responseToCaller is error) {
                log:printError("Error sending response", err = responseToCaller);
            }
        }
    }
}

// This sample service is used to mock connection timeouts.
@http:ServiceConfig {
    basePath: "/hello"
}
service helloWorld on new http:Listener(8080) {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function sayHello(http:Caller caller, http:Request req) {
        // Delay the response by 15000 milliseconds to
        // mimic the network level delays.
        runtime:sleep(15000);

        var result = caller->respond("Hello World!!!");
        if (result is error) {
           log:printError("Error sending response from mock service",
                           err = result);
        }
    }
}

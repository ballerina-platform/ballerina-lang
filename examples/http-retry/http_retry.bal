import ballerina/http;
import ballerina/log;
import ballerina/runtime;

// Define the endpoint to the call the `mockHelloService`.
http:Client backendClientEP = new("http://localhost:8080", config = {
        // Retry configuration options.
        retryConfig: {

            // Initial retry interval in milliseconds.
            interval: 3000,

            // Number of retry attempts before giving up.
            count: 3,

            // Multiplier of the retry interval to exponentailly
            // increase; retry interval.
            backOffFactor: 2.0,

            // Upper limit of the retry interval in milliseconds.
            // If `interval` into `backOffFactor` value exceeded
            // `maxWaitInterval` interval value. `maxWaitInterval`
            // will be considered as the retry interval.
            maxWaitInterval: 20000

        },
        timeoutMillis: 2000
    });

@http:ServiceConfig {
    basePath: "/retry"
}
service retryDemoService on new http:Listener(9090) {
    // Create a REST resource within the API.
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    // Parameters include a reference to the caller and an object of
    // the request data.
    resource function invokeEndpoint(http:Caller caller, http:Request request) {

        var backendResponse = backendClientEP->forward("/hello", request);

        // The `is` operator is used to separate out union-type returns.
        // The type of `backendResponse` variable is the union of `http:Response` and `error`.
        // If a response is returned, `backendResponse` is treated as an `http:Response`
        // within the if-block and the normal process runs.
        // If the service returns an `error`, `backendResponse` is implicitly
        // converted to an `error` within the else block.
        if (backendResponse is http:Response) {

            var responseToCaller = caller->respond(backendResponse);
            if (responseToCaller is error) {
                log:printError("Error sending response",
                                err = responseToCaller);
            }

        } else {
            http:Response response = new;
            response.statusCode = http:INTERNAL_SERVER_ERROR_500;
            string errCause = <string> backendResponse.detail().message;
            response.setPayload(errCause);
            var responseToCaller = caller->respond(response);
            if (responseToCaller is error) {
                log:printError("Error sending response",
                                err = responseToCaller);
            }
        }
    }
}

public int counter = 0;

// This sample service is used to mock connection timeouts and service outages.
// The service outage is mocked by stopping/starting this service.
// This should run separately from the `retryDemoService` service.
@http:ServiceConfig {
    basePath: "/hello"
}
service mockHelloService on new http:Listener(8080) {

    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/"
    }
    resource function sayHello(http:Caller caller, http:Request req) {
        counter = counter + 1;
        if (counter % 4 != 0) {
            log:printInfo(
                "Request received from the client to delayed service.");
            // Delay the response by 5000 milliseconds to
            // mimic network level delays.
            runtime:sleep(5000);
            var responseToCaller = caller->respond("Hello World!!!");
            if (responseToCaller is error) {
                log:printError("Error sending response from mock service",
                        err = responseToCaller);
            }
        } else {
            log:printInfo("Request received from the client to healthy service.");
            var responseToCaller = caller->respond("Hello World!!!");
            if (responseToCaller is error) {
                log:printError("Error sending response from mock service",
                        err = responseToCaller);
            }
        }
    }
}

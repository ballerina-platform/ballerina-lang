import ballerina/http;
import ballerina/log;
import ballerina/runtime;

// Define the end point to the call the `mockHelloService`.
endpoint http:Client backendClientEP {
    url: "http://localhost:8080",
    // Retry configuration options.
    retryConfig: {
        interval: 3000,
        count: 3,
        backOffFactor: 0.5
    },

    timeoutMillis: 2000
};

@http:ServiceConfig {
    basePath: "/retry"
}
service<http:Service> retryDemoService bind { port: 9090 } {
    // Create a REST resource within the API.
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    // Parameters include a reference to the caller endpoint and an object of
    // the request data.
    invokeEndpoint(endpoint caller, http:Request request) {
        var backendResponse = backendClientEP->get("/hello", message = untaint request);
        // `match` is used to handle union-type returns.
        // If a response is returned, the normal process runs.
        // If the service does not get the expected response,
        // the error-handling logic is executed.
        match backendResponse {

            http:Response response => {
                // '->' signifies remote call.
                caller->respond(response) but {
                    error e => log:printError("Error sending response", err = e)
                };

            }
            error responseError => {
                // Create a new HTTP response by looking at the error message.
                http:Response errorResponse = new;
                errorResponse.statusCode = 500;
                errorResponse.setPayload(responseError.message);

                caller->respond(errorResponse) but {
                    error e => log:printError("Error sending response", err = e)
                };
            }
        }
    }
}

public int counter = 0;

// This sample service is used to mock connection timeouts and service outages.
// The service outage is mocked by stopping/starting this service.
// This should run separately from the `retryDemoService` service.

@http:ServiceConfig { basePath: "/hello" }
service<http:Service> mockHelloService bind { port: 8080 } {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    sayHello(endpoint caller, http:Request req) {
        counter = counter + 1;
        if (counter % 4 != 0) {
            log:printInfo(
                "Request received from the client to delayed service.");
            // Delay the response by 5000 milliseconds to
            // mimic network level delays.
            runtime:sleep(5000);

            http:Response res = new;
            res.setPayload("Hello World!!!");
            caller->respond(res) but {
                error e => log:printError(
                    "Error sending response from mock service", err = e)
            };
        } else {
            log:printInfo(
                "Request received from the client to healthy service.");
            http:Response res = new;
            res.setPayload("Hello World!!!");
            caller->respond(res) but {
                error e => log:printError(
                    "Error sending response from mock service", err = e) };
        }
    }
}

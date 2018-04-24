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

// Create an HTTP service bound to the endpoint (retryEP).
@http:ServiceConfig {
    basePath: "/retry"
}
service<http:Service> retryDemoService bind {port: 9090} {
    // Create a REST resource within the API.
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    // Parameters include a reference to the caller endpoint and a object with the request data.
    invokeEndpoint(endpoint caller, http:Request request) {
        var backendResponse = backendClientEP->get("/hello", request = request);
        // "match" command in the code is used to handle a union-type return:
        // if the return value is a Response - normal processing happens. If our service did not get the Response
        // it expected - we use error-handling logic instead.
        match backendResponse {
            http:Response response => {
                // Return response, '->' signifies remote call.
                // '_' means ignore the function return value.
                caller->respond(response) but { error e => log:printError("Error sending response", err = e) };
            }
            error responseError => {
                // Create new HTTP response by looking at the error message.
                http:Response errorResponse = new;
                errorResponse.statusCode = 500;
                errorResponse.setPayload(responseError.message);
                caller->respond(errorResponse) but { error e => log:printError("Error sending response", err = e) };
            }
        }
    }
}

public int counter = 0;

// This sample service can be used to mock connection timeouts and service outages.
// Service outage can be mocked by stopping/starting this service.
// This should be run separately from the `retryDemoService` service.
@http:ServiceConfig {basePath: "/hello"}
service<http:Service> mockHelloService bind {port: 8080} {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    sayHello(endpoint caller, http:Request req) {
        counter = counter + 1;
        if (counter % 4 != 0) {
            log:printInfo("Request received from the client to delayed service.");
            // Delaying the response for 5000 to mimic network level delays.
            runtime:sleep(5000);
            http:Response res = new;
            res.setPayload("Hello World!!!");
            caller->respond(res) but { error e => log:printError("Error sending response from mock service", err = e) };
        } else {
            log:printInfo("Request received from the client to healthy service.");
            http:Response res = new;
            res.setPayload("Hello World!!!");
            caller->respond(res) but { error e => log:printError("Error sending response from mock service", err = e) };
        }
    }
}

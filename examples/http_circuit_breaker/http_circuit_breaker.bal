import ballerina/http;
import ballerina/log;
import ballerina/runtime;

// Circuit Breakers are a way of protecting against distributed failure in Ballerina.
// The circuit breaker is looking for errors across a rolling time window.
// After breaking the circuit, the circuit breaker will not send any more requests to the backend until the resetTime.
endpoint http:Client backendClientEP {
    url: "http://localhost:8080",
    circuitBreaker: {
        rollingWindow: {
            timeWindowMillis: 10000,
            bucketSizeMillis: 2000
        },
        failureThreshold: 0.2,
        resetTimeMillis: 10000,
        statusCodes: [400, 404, 500]
    },
    timeoutMillis: 2000
};

// Create an HTTP service bound to the endpoint (circuitBreakerEP).
@http:ServiceConfig {
    basePath: "/cb"
}
service<http:Service> circuitbreaker bind {port: 9090} {
    // Create a REST resource within the API
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    // Parameters include a reference to the caller endpoint and a object with the request data
    invokeEndpoint(endpoint caller, http:Request request) {
        var backendRes = backendClientEP->forward("/hello", request);
        // "match" command in the code is used to handle a union-type return:
        // if the return value is a Response - normal processing happens. If our service did not get the Response
        // it expected - we use error-handling logic instead.
        match backendRes {
            http:Response res => {
                caller->respond(res) but { error e => log:printError("Error sending response", err = e) };
            }
            error responseError => {
                http:Response response = new;
                response.statusCode = 500;
                response.setPayload(responseError.message);
                caller->respond(response) but { error e => log:printError("Error sending response", err = e) };
            }
        }
    }
}

public int counter = 1;

// This sample service can be used to mock connection timeouts and service outages. 
// Service outage can be mocked by stopping/starting this service.
// This should be run separately from the `circuitBreakerDemo` service.
@http:ServiceConfig {basePath: "/hello"}
service<http:Service> helloWorld bind {port: 8080} {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    sayHello(endpoint caller, http:Request req) {
        if (counter % 5 == 0) {
            // Delaying the response for 5000 to mimic network level delays.
            runtime:sleep(5000);
            counter = counter + 1;
            http:Response res = new;
            res.setPayload("Hello World!!!");
            caller->respond(res) but { error e => log:printError("Error sending response from mock service", err = e) };
        } else if (counter % 5 == 3) {
            counter = counter + 1;
            http:Response res = new;
            res.statusCode = 500;
            res.setPayload("Internal error occurred while processing the request.");
            caller->respond(res) but { error e => log:printError("Error sending response from mock service", err = e) };
        } else {
            counter = counter + 1;
            http:Response res = new;
            res.setPayload("Hello World!!!");
            caller->respond(res) but {
                error e => log:printError("Error sending response from mock service", err = e) };
        }
    }
}

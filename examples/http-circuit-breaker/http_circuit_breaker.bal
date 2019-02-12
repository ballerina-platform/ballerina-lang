import ballerina/http;
import ballerina/log;
import ballerina/runtime;

// Circuit Breakers are used to protect against distributed failure.
// The circuit breaker looks for errors across a rolling time window.
// After the circuit is broken, it does not send requests to
// the backend until the `resetTime`.
http:Client backendClientEP = new("http://localhost:8080", config = {
        // Circuit breaker configuration options that control the
        // behavior of the Ballerina circuit breaker.
        circuitBreaker: {
            // Failure calculation window. This is how long Ballerina
            // circuit breaker keeps the statistics for the operations.
            rollingWindow: {

                // Time period in milliseconds for which the failure threshold
                // is calculated.
                timeWindowMillis: 10000,

                // The granularity at which the time window slides.
                // This is measured in milliseconds.
                // The `RollingWindow` is divided into buckets
                //  and slides by these increments.
                // For example, if this `timeWindowMillis` is set to
                // 10000 milliseconds and `bucketSizeMillis` 2000.
                // Then `RollingWindow` breaks into sub windows with
                // 2-second buckets and stats are collected with
                // respect to the buckets. As time rolls a new bucket
                // will be appended to the end of the window and the
                // old bucket will be removed.
                bucketSizeMillis: 2000,

                // Minimum number of requests in a `RollingWindow`
                // that will trip the circuit.
                requestVolumeThreshold: 0

            },
            // The threshold for request failures.
            // When this threshold exceeds, the circuit trips.
            // This is the ratio between failures and total requests
            //  and the ratio is considered only within the configured
            // `RollingWindow`.
            failureThreshold: 0.2,

            // The time period (in milliseconds) to wait before
            // attempting to make another request to the upstream service.
            // When the failure threshold exceeds, the circuit trips to
            // `OPEN` state. Once the circuit is in `OPEN` state
            // circuit breaker waits for the time configured in `resetTimeMillis`
            // and switch the circuit to the `HALF_OPEN` state.
            resetTimeMillis: 10000,

            // HTTP response status codes that are considered as failures
            statusCodes: [400, 404, 500]

        },
        timeoutMillis: 2000
    });

// Create an HTTP service bound to the endpoint (circuitBreakerEP).
@http:ServiceConfig {
    basePath: "/cb"
}
service circuitbreaker on new http:Listener(9090) {
    // Create a REST resource within the API.
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    // The parameters include a reference to the caller
    // and an object of the request data.
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
            response.setPayload(<string> backendResponse.detail().message);
            var responseToCaller = caller->respond(response);
            if (responseToCaller is error) {
                log:printError("Error sending response",
                                err = responseToCaller);
            }
        }
    }
}

public int counter = 1;

// This sample service is used to mock connection timeouts and service outages.
// Mock a service outage by stopping/starting this service.
// This should run separately from the `circuitBreakerDemo` service.

@http:ServiceConfig {
    basePath: "/hello"
}
service helloWorld on new http:Listener(8080) {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function sayHello(http:Caller caller, http:Request req) {
        if (counter % 5 == 0) {
            counter = counter + 1;
            // Delay the response by 5000 milliseconds to
            // mimic the network level delays.
            runtime:sleep(5000);

            var result = caller->respond("Hello World!!!");
            if (result is error) {
               log:printError("Error sending response from mock service",
                                err = result);
            }
        } else if (counter % 5 == 3) {
            counter = counter + 1;
            http:Response res = new;
            res.statusCode = 500;
            res.setPayload(
                    "Internal error occurred while processing the request.");
            var result = caller->respond(res);
            if (result is error) {
               log:printError("Error sending response from mock service",
                               err = result);
            }
        } else {
            counter = counter + 1;
            var result = caller->respond("Hello World!!!");
            if (result is error) {
               log:printError("Error sending response from mock service",
                               err = result);
            }
        }
    }
}

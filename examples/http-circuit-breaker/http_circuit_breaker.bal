import ballerina/http;
import ballerina/log;
import ballerina/runtime;

// The circuit breaker looks for errors across a rolling time window.
// After the circuit is broken, it does not send requests to
// the backend until the `resetTime`.
http:Client backendClientEP = new ("http://localhost:8080", {
        // Configuration options that control the behavior of the circuit
        // breaker.
        circuitBreaker: {
            // Failure calculation window. This is how long the circuit breaker
            // keeps the statistics for the operations.
            rollingWindow: {

                // Time period in milliseconds for which the failure threshold
                // is calculated.
                timeWindowInMillis: 10000,

                // The granularity (in milliseconds) at which the time window
                // slides.
                // The `RollingWindow` is divided into buckets and slides by
                // these increments.
                // For an example, if this `timeWindowInMillis` is set to 10000
                // milliseconds and `bucketSizeInMillis` 2000, then
                // `RollingWindow` breaks into sub windows with 2-second
                // buckets. Stats are collected with respect to the buckets. As
                // time rolls, a new bucket will be appended to the end of the
                // window and the old bucket will be removed.
                bucketSizeInMillis: 2000,

                // Minimum number of requests in a `RollingWindow` that will
                // trip the circuit.
                requestVolumeThreshold: 0

            },
            // The threshold for request failures.
            // When this threshold exceeds, the circuit trips.
            // This is the ratio between failures and total requests and the
            // ratio is considered only within the configured `RollingWindow`.
            failureThreshold: 0.2,

            // The time period (in milliseconds) to wait before attempting to
            // make another request to the upstream service.
            // When the failure threshold exceeds, the circuit trips to `OPEN`
            // state. Once the circuit is in `OPEN` state circuit breaker waits
            // for the time configured in `resetTimeInMillis` and switch the
            // circuit to the `HALF_OPEN` state.
            resetTimeInMillis: 10000,

            // HTTP response status codes that are considered as failures
            statusCodes: [400, 404, 500]

        },
        timeoutInMillis: 2000
    }
);

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
        // If the `backendResponse` is an `http:Response`, it is sent back to
        // the client. If `backendResponse` is an `http:ClientError`, an
        // internal server error is returned to the client.
        if (backendResponse is http:Response) {
            var responseToCaller = caller->respond(backendResponse);
            if (responseToCaller is http:ListenerError) {
                log:printError("Error sending response", responseToCaller);
            }
        } else {
            http:Response response = new;
            response.statusCode = http:STATUS_INTERNAL_SERVER_ERROR;
            response.setPayload(<string>backendResponse.detail()?.message);
            var responseToCaller = caller->respond(response);
            if (responseToCaller is http:ListenerError) {
                log:printError("Error sending response", responseToCaller);
            }
        }
    }
}

int counter = 1;

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
            // Delay the response by 5000 milliseconds to
            // mimic the network level delays.
            runtime:sleep(5000);

            var result = caller->respond("Hello World!!!");
            handleRespondResult(result);
        } else if (counter % 5 == 3) {
            http:Response res = new;
            res.statusCode = 500;
            res.setPayload(
                    "Internal error occurred while processing the request.");
            var result = caller->respond(res);
            handleRespondResult(result);
        } else {
            var result = caller->respond("Hello World!!!");
            handleRespondResult(result);
        }
        counter = counter + 1;
    }
}

function handleRespondResult(error? result) {
    if (result is http:ListenerError) {
        log:printError("Error sending response from mock service", result);
    }
}

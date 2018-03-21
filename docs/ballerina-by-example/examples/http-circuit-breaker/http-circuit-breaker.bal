import ballerina/io;
import ballerina/net.http;
import ballerina/net.http.resiliency;
import ballerina/runtime;

@http:configuration {basePath:"/cb"}
service<http> circuitBreakerDemo {

    int[] httpStatusCodes = [400, 404, 500];
    // Holds the Circuit Breaker configurations, the failure threshold (should be between 0 and 1,
    // inclusive), the reset timeout (in milli seconds) and array of http status codes which needs to
    // threated as fault response.
    resiliency:CircuitBreakerConfig circuitBreakerConfig = {
                                                               failureThreshold:0.2,
                                                               resetTimeout:10000,
                                                               httpStatusCodes:httpStatusCodes
                                                           };

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource sayHello (http:Connection conn, http:Request req) {

        // The Circuit Breaker accepts an HTTP client connector as its first argument.
        // - When the percentage of failed requests passes the specified threshold, the circuit trips and subsequent requests to the backend fails immediately.<br>
        // - When the time specified in the reset timeout elapses, the circuit goes to 'half-open' state.<br>
        // - If a request comes when it is in half-open state, it is sent to the backend and if it does not result in an error, the circuit state goes to 'close'.<br>
        // - If it fails though, the circuit goes back to 'open' state, and the above process repeats.<br>
        endpoint<resiliency:CircuitBreaker> circuitBreakerEP {
            create resiliency:CircuitBreaker(create http:HttpClient("http://localhost:8080",
                                                                    {endpointTimeout:2000}), circuitBreakerConfig);
        }
        http:Response clientRes;
        http:HttpConnectorError err;
        clientRes, err = circuitBreakerEP.forward("/hello", req);
        if (err != null) {
            io:println(err);
            if (clientRes == null) {
                http:Response res = {};
                res.statusCode = 500;
                res.setStringPayload(err.message);
                _ = conn.respond(res);
            }
        } else {
            var payload, payloadError = clientRes.getStringPayload();
            if (payloadError == null) {
                io:println(payload);
                _ = conn.forward(clientRes);
            } else {
                http:Response res = {};
                res.statusCode = 500;
                res.setStringPayload(payloadError.message);
                _ = conn.respond(res);
            }
        }
    }
}

// This sample service can be used to mock connection timeouts and service outages. Service outage can be mocked by stopping/starting this service.
// This should be run separately from the circuitBreakerDemo service.
@http:configuration {basePath:"/hello", port:8080}
service<http> helloWorld {
    int counter = 1;
    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource sayHello (http:Connection conn, http:Request req) {
        if (counter % 5 == 0) {
            runtime:sleepCurrentWorker(5000);
            counter = counter + 1;
            http:Response res = {};
            res.setStringPayload("Hello World!!!");
            _ = conn.respond(res);
        } else if (counter % 5 == 3) {
            counter = counter + 1;
            http:Response res = {};
            res.statusCode = 500;
            res.setStringPayload("Internal error occurred while processing the request.");
            _ = conn.respond(res);
        } else {
            counter = counter + 1;
            http:Response res = {};
            res.setStringPayload("Hello World!!!");
            _ = conn.respond(res);
        }
    }
}

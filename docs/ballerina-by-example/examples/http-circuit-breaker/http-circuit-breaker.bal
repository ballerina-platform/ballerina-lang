import ballerina.io;
import ballerina.net.http;
import ballerina.net.http.resiliency;
import ballerina.runtime;

@http:configuration {basePath:"/cb"}
service<http> circuitBreakerDemo {

    // The Circuit Breaker accepts an HTTP client connector as its first argument.
    // The second and third arguments to the Circuit Breaker are the failure threshold (should be between 0 and 1, inclusive) and the reset timeout (in milli seconds) respectively.<br>
    // * When the percentage of failed requests passes the specified threshold, the circuit trips and subsequent requests to the backend fails immediately.<br>
    // * When the time specified in the reset timeout elapses, the circuit goes to 'half-open' state.<br>
    // * If a request comes when it is in half-open state, it is sent to the backend and if it does not result in an error, the circuit state goes to 'close'.<br>
    // * If it fails though, the circuit goes back to 'open' state, and the above process repeats.<br>
    endpoint<resiliency:CircuitBreaker> circuitBreakerEP {
        create resiliency:CircuitBreaker(create http:HttpClient("http://localhost:8080", {endpointTimeout:2000}), 0.3, 20000);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource sayHello (http:Connection conn, http:InRequest req) {
        http:InResponse clientRes;
        http:HttpConnectorError err;
        clientRes, err = circuitBreakerEP.forward("/hello", req);

        if (err != null) {
            io:println(err);
            if (clientRes == null) {
                http:OutResponse res = {};
                res.statusCode = 500;
                res.setStringPayload(err.message);
                _ = conn.respond(res);
            }
        } else {
            io:println(clientRes.getStringPayload());
            _ = conn.forward(clientRes);
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
    resource sayHello (http:Connection conn, http:InRequest req) {
        if (counter % 3 == 0) {
            runtime:sleepCurrentWorker(5000);
        }
        counter = counter + 1;
        http:OutResponse res = {};
        res.setStringPayload("Hello World!!!");
        _ = conn.respond(res);
    }
}

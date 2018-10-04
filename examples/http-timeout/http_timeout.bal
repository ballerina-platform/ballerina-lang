import ballerina/http;
import ballerina/log;
import ballerina/runtime;

endpoint http:Client backendClientEP {
    url: "http://localhost:8080",
    // Timeout configuration
    timeoutMillis: 10000

};

// Create an HTTP service bound to the listener endpoint.
@http:ServiceConfig {
    basePath: "/timeout"
}
service<http:Service> timeoutService bind { port: 9090 } {
    // Create a REST resource within the API.
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    // The parameters include a reference to the caller
    // endpoint and an object of the request data.
    invokeEndpoint(endpoint caller, http:Request request) {
        var backendRes = backendClientEP->forward("/hello", request);
        // `match` is used to handle union-type returns.
        // If a response is returned, the normal process runs. If the service
        // does not get the expected response, the error-handling logic is executed.
        match backendRes {

            http:Response res => {
                caller->respond(res) but {
                    error e => log:printError("Error sending response", err = e)
                };
            }
            error responseError => {
                http:Response response = new;
                response.statusCode = 500;
                if (responseError.message ==
                "Idle timeout triggered before initiating inbound response") {
                    response.setPayload(
                                "Request timed out. Please try again in sometime."
                    );
                } else {
                    response.setPayload(responseError.message);
                }
                caller->respond(response) but {
                    error e => log:printError("Error sending response", err = e)
                };
            }
        }
    }
}

// This sample service is used to mock connection timeouts.
@http:ServiceConfig { basePath: "/hello" }
service<http:Service> helloWorld bind { port: 8080 } {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    sayHello(endpoint caller, http:Request req) {
        // Delay the response by 15000 milliseconds to
        // mimic the network level delays.
        runtime:sleep(15000);
        http:Response res = new;
        res.setPayload("Hello World!!!");
        caller->respond(res) but {
            error e => log:printError(
                           "Error sending response from mock service", err = e)
        };
    }
}

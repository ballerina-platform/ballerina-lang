import ballerina/http;
import ballerina/log;
import ballerina/runtime;

// Create an endpoint with port 8080 for the mock backend services.
listener http:Listener backendEP = new(8080);

// Define the failover client endpoint to call the backend services.
http:FailoverClient foBackendEP = new({
        timeoutMillis: 5000,
        failoverCodes: [501, 502, 503],
        intervalMillis: 5000,
        // Define a set of HTTP Clients that are targeted for failover.
        targets: [
            { url: "http://nonexistentEP/mock1" },
            { url: "http://localhost:8080/echo" },
            { url: "http://localhost:8080/mock" }
        ]

    });

@http:ServiceConfig {
    basePath: "/fo"
}
service failoverDemoService on new http:Listener(9090) {
    // Create a REST resource within the API.
    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/"
    }
    // Parameters include a reference to the caller and an object with the request data.
    resource function invokeEndpoint(http:Caller caller, http:Request request) {

        var backendResponse = foBackendEP->get("/", message = request);

        // The `is` operator is used to separate out union-type returns.
        // The type of `backendResponse` variable is the union of `http:Response` and `error`.
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
            response.setPayload(<string>backendResponse.detail().message);
            var responseToCaller = caller->respond(response);
            if (responseToCaller is error) {
                log:printError("Error sending response", err = responseToCaller);
            }
        }
    }
}

// Define the sample service to mock connection timeouts and service outages.
@http:ServiceConfig {
    basePath: "/echo"
}
service echo on backendEP {
    @http:ResourceConfig {
        methods: ["POST", "PUT", "GET"],
        path: "/"
    }
    resource function echoResource(http:Caller caller, http:Request req) {
        // Delay the response for 30000 milliseconds to mimic network level delays.
        runtime:sleep(30000);

        var result = caller->respond("echo Resource is invoked");
        if (result is error) {
           log:printError("Error sending response from mock service",
                          err = result);
        }
    }
}

// Define the sample service to mock a healthy service.
@http:ServiceConfig {
    basePath: "/mock"
}
service mock on backendEP {
    @http:ResourceConfig {
        methods: ["POST", "PUT", "GET"],
        path: "/"
    }
    resource function mockResource(http:Caller caller, http:Request req) {
        var result = caller->respond("Mock Resource is Invoked.");
        if (result is error) {
           log:printError("Error sending response from mock service",
                          err = result);
        }
    }
}

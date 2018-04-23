import ballerina/http;
import ballerina/log;
import ballerina/runtime;

// Create an endpoint with port 8080 for the mock backend services.
endpoint http:Listener backendEP {
    port: 8080
};

// Define the failover client end point to the call the backend services.
endpoint http:FailoverClient foBackendEP {
    timeoutMillis: 5000,
    failoverCodes: [501, 502, 503],
    intervalMillis: 5000,
    // Define set of HTTP Clients that needs to be Failover.
    targets: [
        {url: "http://localhost:3000/mock1"},
        {url: "http://localhost:8080/echo"},
        {url: "http://localhost:8080/mock"}
    ]
};

// Create an HTTP service bound to the endpoint (failoverEP).
@http:ServiceConfig {
    basePath: "/fo"
}
service<http:Service> failoverDemoService bind {port: 9090} {
    // Create a REST resource within the API.
    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/"
    }
    // Parameters include a reference to the caller endpoint and a object with the request data.
    invokeEndpoint(endpoint caller, http:Request request) {
        var backendRes = foBackendEP->get("/", request = request);
        // "match" command in the code is used to handle a union-type return:
        // if the return value is a Response - normal processing happens. If our service did not get the Response
        // it expected - we use error-handling logic instead.
        match backendRes {
            http:Response response => {
                // Return response, '->' signifies remote call.
                // '_' means ignore the function return value.
                caller->respond(response) but { error e => log:printError("Error sending response", err = e) };
            }
            error responseError => {
                // Create new HTTP response by looking at the error message.
                http:Response response = new;
                response.statusCode = 500;
                response.setPayload(responseError.message);
                caller->respond(response) but { error e => log:printError("Error sending response", err = e) };
            }
        }
    }
}

// This sample service can be used to mock connection timeouts and service outages.
@http:ServiceConfig {
    basePath: "/echo"
}
service echo bind backendEP {
    @http:ResourceConfig {
        methods: ["POST", "PUT", "GET"],
        path: "/"
    }
    echoResource(endpoint caller, http:Request req) {
        http:Response outResponse = new;
        // Delaying the response for 30000 to mimic network level delays.
        runtime:sleep(30000);
        outResponse.setPayload("echo Resource is invoked");
        caller->respond(outResponse) but {
            error e => log:printError("Error sending response from mock service", err = e) };
    }
}

// This sample service can be used to mock healthy service.
@http:ServiceConfig {
    basePath: "/mock"
}
service mock bind backendEP {
    @http:ResourceConfig {
        methods: ["POST", "PUT", "GET"],
        path: "/"
    }
    mockResource(endpoint caller, http:Request req) {
        http:Response outResponse = new;
        outResponse.setPayload("Mock Resource is Invoked.");
        caller->respond(outResponse) but {
            error e => log:printError("Error sending response from mock service", err = e)
        };
    }
}

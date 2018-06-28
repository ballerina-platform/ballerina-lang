import ballerina/http;
import ballerina/log;
import ballerina/runtime;

// Create an endpoint with port 8080 for the mock backend services.
endpoint http:Listener backendEP {
    port: 8080
};

// Define the failover client end point to call the backend services.
endpoint http:FailoverClient foBackendEP {
    timeoutMillis: 5000,
    failoverCodes: [501, 502, 503],
    intervalMillis: 5000,
    // Define set of HTTP Clients that needs to be Failover.
    targets: [
        { url: "http://localhost:3000/mock1" },
        { url: "http://localhost:8080/echo" },
        { url: "http://localhost:8080/mock" }
    ]

};

@http:ServiceConfig {
    basePath: "/fo"
}
service<http:Service> failoverDemoService bind { port: 9090 } {
    // Create a REST resource within the API.
    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/"
    }
    // Parameters include a reference to the caller endpoint and a object with the request data.
    invokeEndpoint(endpoint caller, http:Request request) {
        var backendRes = foBackendEP->get("/", message = request);
        // `match` is used to handle union-type returns.
        // If a response is returned, the normal process runs.
        // If the service does not get the expected response,
        // the error-handling logic is executed.
        match backendRes {
            http:Response response => {
                // `->` signifies remote call.
                caller->respond(response) but {
                    error e => log:printError("Error sending response", err = e)
                };
            }
            error responseError => {
                // Create a new HTTP response by looking at the error message.
                http:Response response = new;
                response.statusCode = 500;
                response.setPayload(responseError.message);
                caller->respond(response) but {
                    error e => log:printError("Error sending response", err = e)
                };
            }
        }
    }
}

// Define the sample service to mock connection timeouts and service outages.
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
        // Delay the response for 30000 milliseconds to mimic network level delays.
        runtime:sleep(30000);

        outResponse.setPayload("echo Resource is invoked");
        caller->respond(outResponse) but {
            error e => log:printError(
                           "Error sending response from mock service", err = e)
        };
    }
}

// Define the sample service to mock a healthy service.
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
            error e => log:printError(
                           "Error sending response from mock service", err = e)
        };
    }
}

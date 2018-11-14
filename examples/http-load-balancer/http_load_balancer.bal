import ballerina/http;
import ballerina/log;

// Create an endpoint with port 8080 for the mock backend services.
endpoint http:Listener backendEP {
    port: 8080
};

// Define the load balance client endpoint to call the backend services.
endpoint http:LoadBalanceClient lbBackendEP {
    // Define the set of HTTP clients that need to be load balanced.
    targets: [
        { url: "http://localhost:8080/mock1" },
        { url: "http://localhost:8080/mock2" },
        { url: "http://localhost:8080/mock3" }
    ],
    // The algorithm used for load balancing.
    algorithm: http:ROUND_ROBIN,
    timeoutMillis: 5000
};

// Create an HTTP service bound to the endpoint (loadBlancerEP).
@http:ServiceConfig {
    basePath: "/lb"
}
service<http:Service> loadBalancerDemoService bind { port: 9090 } {
    // Create a REST resource within the API.
    @http:ResourceConfig {
        path: "/"
    }
    // Parameters include a reference to the
    // caller endpoint and an object of the request data.
    invokeEndpoint(endpoint caller, http:Request req) {
        http:Request outRequest = new;
        json requestPayload = { "name": "Ballerina" };
        outRequest.setPayload(requestPayload);
        var response = lbBackendEP->post("/", outRequest);
        // `match` is used to handle union-type returns.
        // If a response is returned, the normal process runs.
        // If the service does not get the expected response,
        // the error-handling logic is executed.
        match response {
            http:Response resp => {
                caller->respond(resp) but {
                    error e => log:printError("Error sending response", err = e)
                };
            }
            error responseError => {
                http:Response outResponse = new;
                outResponse.statusCode = 500;
                outResponse.setPayload(responseError.message);
                caller->respond(outResponse) but {
                    error e => log:printError("Error sending response", err = e)
                };
            }
        }
    }
}

// Define the mock backend services, which are called by the load balancer.
@http:ServiceConfig { basePath: "/mock1" }
service mock1 bind backendEP {
    @http:ResourceConfig {
        path: "/"
    }
    mock1Resource(endpoint caller, http:Request req) {
        http:Response outResponse = new;
        outResponse.setPayload("Mock1 Resource is invoked.");
        caller->respond(outResponse) but {
                        error e => log:printError(
                           "Error sending response from mock service", err = e)
                        };
    }
}

@http:ServiceConfig { basePath: "/mock2" }
service mock2 bind backendEP {
    @http:ResourceConfig {
        path: "/"
    }
    mock2Resource(endpoint caller, http:Request req) {
        http:Response outResponse = new;
        outResponse.setPayload("Mock2 Resource is Invoked.");
        caller->respond(outResponse) but {
                        error e => log:printError(
                           "Error sending response from mock service", err = e)
                        };
    }
}

@http:ServiceConfig { basePath: "/mock3" }
service mock3 bind backendEP {
    @http:ResourceConfig {
        path: "/"
    }
    mock3Resource(endpoint caller, http:Request req) {
        http:Response outResponse = new;
        outResponse.setPayload("Mock3 Resource is Invoked.");
        caller->respond(outResponse) but {
                        error e => log:printError(
                           "Error sending response from mock service", err = e)
                        };
    }
}

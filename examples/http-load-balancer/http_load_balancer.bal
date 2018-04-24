import ballerina/http;
import ballerina/log;

// Create an endpoint with port 8080 for the mock backend services.
endpoint http:Listener backendEP {
    port: 8080
};

// Define the load balance client end point to the call the backend services.
endpoint http:LoadBalanceClient lbBackendEP {
    // Define set of HTTP Clients that needs to be load balanced.
    targets: [
        {url: "http://localhost:8080/mock1"},
        {url: "http://localhost:8080/mock2"},
        {url: "http://localhost:8080/mock3"}
    ],
    // The algorithm to be used for load balancing.
    algorithm: http:ROUND_ROBIN,
    timeoutMillis: 5000
};

// Create an HTTP service bound to the endpoint (loadBlancerEP).
@http:ServiceConfig {
    basePath: "/lb"
}
service<http:Service> loadBalancerDemoService bind {port: 9090} {
    // Create a REST resource within the API.
    @http:ResourceConfig {
        path: "/"
    }
    // Parameters include a reference to the caller endpoint and a object with the request data.
    invokeEndpoint(endpoint caller, http:Request req) {
        http:Request outRequest = new;
        json requestPayload = {"name": "Ballerina"};
        outRequest.setPayload(requestPayload);
        var response = lbBackendEP->post("/", request = outRequest);
        // "match" command in the code is used to handle a union-type return:
        // if the return value is a Response - normal processing happens. If our service did not get the Response
        // it expected - we use error-handling logic instead.
        match response {
            http:Response resp => {
                caller->respond(resp) but { error e => log:printError("Error sending response", err = e) };
            }
            error responseError => {
                http:Response outResponse = new;
                outResponse.statusCode = 500;
                outResponse.setPayload(responseError.message);
                caller->respond(outResponse) but { error e => log:printError("Error sending response", err = e) };
            }
        }
    }
}

// Mock backend services which are called by load balancer.
@http:ServiceConfig {basePath: "/mock1"}
service mock1 bind backendEP {
    @http:ResourceConfig {
        path: "/"
    }
    mock1Resource(endpoint caller, http:Request req) {
        http:Response outResponse = new;
        outResponse.setPayload("Mock1 Resource is invoked.");
        caller->respond(outResponse) but {
            error e => log:printError("Error sending response from mock service", err = e)
        };
    }
}

@http:ServiceConfig {basePath: "/mock2"}
service mock2 bind backendEP {
    @http:ResourceConfig {
        path: "/"
    }
    mock2Resource(endpoint caller, http:Request req) {
        http:Response outResponse = new;
        outResponse.setPayload("Mock2 Resource is Invoked.");
        caller->respond(outResponse) but {
            error e => log:printError("Error sending response from mock service", err = e)
        };
    }
}

@http:ServiceConfig {basePath: "/mock3"}
service mock3 bind backendEP {
    @http:ResourceConfig {
        path: "/"
    }
    mock3Resource(endpoint caller, http:Request req) {
        http:Response outResponse = new;
        outResponse.setPayload("Mock3 Resource is Invoked.");
        caller->respond(outResponse) but {
            error e => log:printError("Error sending response from mock service", err = e)
        };
    }
}

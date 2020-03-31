import ballerina/http;
import ballerina/log;

// Create an endpoint with port 8080 for the mock backend services.
listener http:Listener backendEP = new (8080);

// Define the load balance client endpoint to call the backend services.
http:LoadBalanceClient lbBackendEP = new ({
        // Define the set of HTTP clients that need to be load balanced.
        targets: [
            {url: "http://localhost:8080/mock1"},
            {url: "http://localhost:8080/mock2"},
            {url: "http://localhost:8080/mock3"}
        ],
        timeoutInMillis: 5000
});


// Create an HTTP service bound to the endpoint (`loadBalancerEP`).
@http:ServiceConfig {
    basePath: "/lb"
}
service loadBalancerDemoService on new http:Listener(9090) {
    // Create a REST resource within the API.
    @http:ResourceConfig {
        path: "/"
    }
    resource function roundRobin(http:Caller caller, http:Request req) {
        json requestPayload = {"name": "Ballerina"};
        var response = lbBackendEP->post("/", requestPayload);
        // If a response is returned, the normal process runs. If the service
        // does not get the expected response, the error-handling logic is
        // executed.
        if (response is http:Response) {
            var responseToCaller = caller->respond(response);
            if (responseToCaller is http:ListenerError) {
                log:printError("Error sending response", responseToCaller);
            }
        } else {
            http:Response outResponse = new;
            outResponse.statusCode = 500;
            outResponse.setPayload(<string>response.detail()?.message);
            var responseToCaller = caller->respond(outResponse);
            if (responseToCaller is http:ListenerError) {
                log:printError("Error sending response", responseToCaller);
            }
        }
    }
}

// Define the mock backend services, which are called by the load balancer.
@http:ServiceConfig {
    basePath: "/mock1"
}
service mock1 on backendEP {
    @http:ResourceConfig {
        path: "/"
    }
    resource function mock1Resource(http:Caller caller, http:Request req) {
        var responseToCaller = caller->respond("Mock1 resource was invoked.");
        if (responseToCaller is http:ListenerError) {
            handleRespondResult(responseToCaller);
        }
    }
}

@http:ServiceConfig {
    basePath: "/mock2"
}
service mock2 on backendEP {
    @http:ResourceConfig {
        path: "/"
    }
    resource function mock2Resource(http:Caller caller, http:Request req) {
        var responseToCaller = caller->respond("Mock2 resource was invoked.");
        if (responseToCaller is http:ListenerError) {
            handleRespondResult(responseToCaller);
        }
    }
}

@http:ServiceConfig {
    basePath: "/mock3"
}
service mock3 on backendEP {
    @http:ResourceConfig {
        path: "/"
    }
    resource function mock3Resource(http:Caller caller, http:Request req) {
        var responseToCaller = caller->respond("Mock3 resource was invoked.");
        if (responseToCaller is http:ListenerError) {
            handleRespondResult(responseToCaller);
        }
    }
}

// Function to handle respond results
function handleRespondResult(http:ListenerError? result) {
    if (result is http:ListenerError) {
        log:printError("Error sending response from mock service", result);
    }
}

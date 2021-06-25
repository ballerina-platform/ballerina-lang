import ballerina/http;
import ballerina/log;
import ballerina/runtime;

// Create an endpoint with port 8080 for the mock backend services.
listener http:Listener backendEP = new (8080);

// Define the failover client endpoint to call the backend services.
http:FailoverClient foBackendEP = new ({

    timeoutInMillis: 5000,
    failoverCodes: [501, 502, 503],
    intervalInMillis: 5000,
    // Define a set of HTTP Clients that are targeted for failover.
    targets: [
            {url: "http://nonexistentEP/mock1"},
            {url: "http://localhost:8080/echo"},
            {url: "http://localhost:8080/mock"}
        ]
});

@http:ServiceConfig {
    basePath: "/fo"
}
service failoverDemoService on new http:Listener(9090) {
    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/"
    }
    resource function invokeEndpoint(http:Caller caller, http:Request request) {
        var backendResponse = foBackendEP->get("/", <@untainted>request);

        // If `backendResponse` is an `http:Response`, it is sent back to the
        // client. If `backendResponse` is an `http:ClientError`, an internal
        // server error is returned to the client.
        if (backendResponse is http:Response) {
            var responseToCaller = caller->respond(<@untainted>backendResponse);
            if (responseToCaller is error) {
                log:printError("Error sending response", responseToCaller);
            }
        } else {
            http:Response response = new;
            response.statusCode = http:STATUS_INTERNAL_SERVER_ERROR;
            response.setPayload(<@untainted>(<error>backendResponse).message());
            var responseToCaller = caller->respond(response);
            if (responseToCaller is error) {
                log:printError("Error sending response", responseToCaller);
            }
        }

    }
}

@http:ServiceConfig {
    basePath: "/echo"
}
// Define the sample service to mock connection timeouts and service outages.
service echo on backendEP {

    @http:ResourceConfig {
        methods: ["POST", "PUT", "GET"],
        path: "/"
    }
    resource function echoResource(http:Caller caller, http:Request req) {
        // Delay the response for 30000 milliseconds to mimic network level
        // delays.
        runtime:sleep(30000);

        var result = caller->respond("echo Resource is invoked");
        if (result is error) {
            log:printError("Error sending response from mock service", result);
        }
    }
}

@http:ServiceConfig {
    basePath: "/mock"
}
// Define the sample service to mock a healthy service.
service mock on backendEP {

    @http:ResourceConfig {
        methods: ["POST", "PUT", "GET"],
        path: "/"
    }
    resource function mockResource(http:Caller caller, http:Request req) {
        var result = caller->respond("Mock Resource is Invoked.");
        if (result is error) {
            log:printError("Error sending response from mock service", result);
        }
    }
}

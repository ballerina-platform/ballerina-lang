import ballerina/http;
import ballerina/log;
import ballerina/mime;
import ballerina/runtime;
import ballerina/io;

endpoint http:Listener serviceEndpoint1 {
    port: 9105
};

// Define the end point to the call the `mockHelloService`.
endpoint http:Client backendClientEP {
    url: "http://localhost:9105",
    // Retry configuration options.
    retryConfig: {
        interval: 3000,
        count: 3,
        backOffFactor: 0.5
    },
    timeoutMillis: 2000
};

@http:ServiceConfig {
    basePath: "/retry"
}
service<http:Service> retryDemoService bind serviceEndpoint1 {
    // Create a REST resource within the API.
    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/"
    }
    // Parameters include a reference to the caller endpoint and an object of
    // the request data.
    invokeEndpoint(endpoint caller, http:Request request) {
        var backendResponse = backendClientEP->forward("/hello", request);
        // `match` is used to handle union-type returns.
        // If a response is returned, the normal process runs.
        // If the service does not get the expected response,
        // the error-handling logic is executed.
        match backendResponse {
            http:Response response => {
                // '->' signifies remote call.
                caller->respond(response) but {
                    error e => log:printError("Error sending response", err = e)
                };
            }
            error responseError => {
                // Create a new HTTP response by looking at the error message.
                http:Response errorResponse = new;
                errorResponse.statusCode = 500;
                errorResponse.setPayload(responseError.message);
                caller->respond(errorResponse) but {
                    error e => log:printError("Error sending response", err = e)
                };
            }
        }
    }
}

public int counter = 0;

// This sample service is used to mock connection timeouts and service outages.
// The service outage is mocked by stopping/starting this service.
// This should run separately from the `retryDemoService` service.
@http:ServiceConfig { basePath: "/hello" }
service<http:Service> mockHelloService bind serviceEndpoint1 {
    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/"
    }
    sayHello(endpoint caller, http:Request req) {
        counter = counter + 1;
        if (counter % 4 != 0) {
            log:printInfo(
                "Request received from the client to delayed service.");
            // Delay the response by 5000 milliseconds to
            // mimic network level delays.
            runtime:sleep(5000);
            http:Response res = new;
            res.setPayload("Hello World!!!");
            caller->respond(res) but {
                error e => log:printError(
                    "Error sending response from mock service", err = e)
            };
        } else {
            log:printInfo("Request received from the client to healthy service.");
            http:Response response = new;
            if (req.hasHeader(mime:CONTENT_TYPE)
                && req.getHeader(mime:CONTENT_TYPE).hasPrefix(http:MULTIPART_AS_PRIMARY_TYPE)) {
                match req.getBodyParts() {
                    // Setting the error response in case of an error
                    error err => {
                        log:printError(err.message);
                        response.setPayload("Error in decoding multiparts!");
                        response.statusCode = 500;
                    }
                    // Iterate through the body parts.
                    mime:Entity[] bodyParts => {
                        foreach bodyPart in bodyParts {
                            if (bodyPart.hasHeader(mime:CONTENT_TYPE)
                                && bodyPart.getHeader(mime:CONTENT_TYPE).hasPrefix(http:MULTIPART_AS_PRIMARY_TYPE)) {
                                mime:Entity[] childParts = check bodyPart.getBodyParts();
                                foreach childPart in childParts {
                                    // When performing passthrough scenarios, message needs to be built before
                                    // invoking the endpoint to create a message datasource.
                                    var childBlobContent = childPart.getByteArray();
                                }
                                io:println(bodyPart.getContentType());
                                bodyPart.setBodyParts(untaint childParts, contentType = untaint bodyPart.getContentType());
                            } else {
                                var bodyPartBlobContent = bodyPart.getByteArray();
                            }
                        }
                        response.setBodyParts(untaint bodyParts, contentType = untaint req.getContentType());
                    }
                }
            } else {
                response.setPayload("Hello World!!!");
            }
            caller->respond(response) but {
                error e => log:printError("Error sending response from mock service", err = e) };
        }
    }
}

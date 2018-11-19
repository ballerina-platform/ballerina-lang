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
        if (backendResponse is http:Response) {
            var responseToCaller = caller->respond(backendResponse);
            if (responseToCaller is error) {
                log:printError("Error sending response", err = responseToCaller);
            }
        } else if (backendResponse is error) {
            http:Response response = new;
            response.statusCode = http:INTERNAL_SERVER_ERROR_500;
            string errCause = <string> backendResponse.detail().message;
            response.setPayload(errCause);
            var responseToCaller = caller->respond(response);
            if (responseToCaller is error) {
                log:printError("Error sending response", err = responseToCaller);
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
                var bodyParts = req.getBodyParts();
                if (bodyParts is mime:Entity[]) {
                    foreach bodyPart in bodyParts {
                        if (bodyPart.hasHeader(mime:CONTENT_TYPE)
                            && bodyPart.getHeader(mime:CONTENT_TYPE).hasPrefix(http:MULTIPART_AS_PRIMARY_TYPE)) {
                            var nestedParts = bodyPart.getBodyParts();
                            if (nestedParts is error) {
                                log:printError(<string> nestedParts.detail().message);
                                response.setPayload("Error in decoding nested multiparts!");
                                response.statusCode = 500;
                            } else {
                                mime:Entity[] childParts = nestedParts;
                                foreach childPart in childParts {
                                    // When performing passthrough scenarios, message needs to be built before
                                    // invoking the endpoint to create a message datasource.
                                    var childBlobContent = childPart.getByteArray();
                                }
                                io:println(bodyPart.getContentType());
                                bodyPart.setBodyParts(untaint childParts, contentType = untaint bodyPart.getContentType());
                            }
                        } else {
                            var bodyPartBlobContent = bodyPart.getByteArray();
                        }
                    }
                    response.setBodyParts(untaint bodyParts, contentType = untaint req.getContentType());
                } else if (bodyParts is error) {
                    log:printError(bodyParts.reason());
                    response.setPayload("Error in decoding multiparts!");
                    response.statusCode = 500;
                }
            } else {
                response.setPayload("Hello World!!!");
            }
            var responseToCaller = caller->respond(response);
            if (responseToCaller is error) {
                log:printError("Error sending response from mock service", err = responseToCaller);
            }
        }
    }
}

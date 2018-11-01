// Copyright (c) 2018 WSO2 Inc. (//www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// //www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
//

import ballerina/http;
import ballerina/log;
import ballerina/io;
import ballerina/mime;
import ballerina/runtime;

endpoint http:Listener failoverEP00 {
    port:9300
};

// Create an endpoint with port 8080 for the mock backend services.
endpoint http:Listener backendEP00 {
    port: 8080
};

// Define the failover client end point to call the backend services.
endpoint http:FailoverClient foBackendEP00 {
    timeoutMillis: 5000,
    failoverCodes: [501, 502, 503],
    intervalMillis: 5000,
    // Define set of HTTP Clients that needs to be Failover.
    targets: [
        { url: "http://localhost:3000/inavalidEP" },
        { url: "http://localhost:8080/echo" },
        { url: "http://localhost:8080/mock" },
        { url: "http://localhost:8080/mock" }
    ]

};

endpoint http:FailoverClient foBackendFailureEP00 {
    timeoutMillis: 5000,
    failoverCodes: [501, 502, 503],
    intervalMillis: 5000,
    // Define set of HTTP Clients that needs to be Failover.
    targets: [
        { url: "http://localhost:3000/inavalidEP" },
        { url: "http://localhost:8080/echo" },
        { url: "http://localhost:8080/echo" }
    ]

};

endpoint http:FailoverClient foStatusCodesEP00 {
    timeoutMillis: 5000,
    failoverCodes: [501, 502, 503],
    intervalMillis: 5000,
    // Define set of HTTP Clients that needs to be Failover.
    targets: [
        { url: "http://localhost:8080/statuscodes" },
        { url: "http://localhost:8080/statuscodes" },
        { url: "http://localhost:8080/statuscodes" }
    ]

};

@http:ServiceConfig {
    basePath: "/fo"
}
service<http:Service> failoverDemoService00 bind failoverEP00 {
    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/typical"
    }
    invokeEndpoint(endpoint caller, http:Request request) {
        var backendRes = foBackendEP00->forward("/", request);
        match backendRes {
            http:Response response => {
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

    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/failures"
    }
    invokeAllFailureEndpoint(endpoint caller, http:Request request) {
        var backendRes = foBackendFailureEP00->forward("/", request);
        match backendRes {
            http:Response response => {
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

    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/failurecodes"
    }
    invokeAllFailureStatusCodesEndpoint(endpoint caller, http:Request request) {
        var backendRes = foStatusCodesEP00->forward("/", request);
        match backendRes {
            http:Response response => {
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

    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/index"
    }
    failoverStartIndex(endpoint caller, http:Request request) {
        http:FailoverActions foClient = foBackendEP00.getCallerActions();
        string startIndex = <string> foClient.succeededEndpointIndex;
        var backendRes = foBackendEP00->forward("/", request);
        match backendRes {
            http:Response response => {
                string responseMessage = "Failover start index is : " + startIndex;
                response.setPayload(responseMessage);
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
service echo00 bind backendEP00 {
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

public int counter00 = 1;
// Define the sample service to mock a healthy service.
@http:ServiceConfig {
    basePath: "/mock"
}
service mock00 bind backendEP00 {
    @http:ResourceConfig {
        methods: ["POST", "PUT", "GET"],
        path: "/"
    }
    mockResource(endpoint caller, http:Request req) {
        counter00 += 1;
        if (counter00 % 5 == 0) {
            runtime:sleep(30000);
        }
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
            response.setPayload("Mock Resource is Invoked.");
        }
        caller->respond(response) but {
            error e => log:printError(
                           "Error sending response from mock service", err = e)
        };

    }
}

// Define the sample service to mock connection timeouts and service outages.
@http:ServiceConfig {
    basePath: "/statuscodes"
}
service failureStatusCodeService00 bind backendEP00 {
    @http:ResourceConfig {
        methods: ["POST", "PUT", "GET"],
        path: "/"
    }
    errorStatusResource(endpoint caller, http:Request req) {
        http:Response outResponse = new;
        outResponse.statusCode = 503;
        outResponse.setPayload("Failure status code scenario");
        caller->respond(outResponse) but {
            error e => log:printError(
                           "Error sending response from mock service", err = e)
        };
    }
}

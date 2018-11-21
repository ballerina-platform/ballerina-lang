// Copyright (c) 2028 WSO2 Inc. (//www.wso2.org) All Rights Reserved.
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

endpoint http:Listener failoverEP02 {
    port:9302
};

// Create an endpoint with port 8082 for the mock backend services.
endpoint http:Listener backendEP02 {
    port: 8082
};

// Define the failover client end point to call the backend services.
endpoint http:FailoverClient foBackendEP02 {
    timeoutMillis: 5000,
    failoverCodes: [501, 502, 503],
    intervalMillis: 5000,
    // Define set of HTTP Clients that needs to be Failover.
    targets: [
        { url: "http://localhost:3000/inavalidEP" },
        { url: "http://localhost:8082/echo" },
        { url: "http://localhost:8082/mock" },
        { url: "http://localhost:8082/mock" }
    ]

};

endpoint http:FailoverClient foBackendFailureEP02 {
    timeoutMillis: 5000,
    failoverCodes: [501, 502, 503],
    intervalMillis: 5000,
    // Define set of HTTP Clients that needs to be Failover.
    targets: [
        { url: "http://localhost:3000/inavalidEP" },
        { url: "http://localhost:8082/echo" },
        { url: "http://localhost:8082/echo" }
    ]

};

endpoint http:FailoverClient foStatusCodesEP02 {
    timeoutMillis: 5000,
    failoverCodes: [501, 502, 503],
    intervalMillis: 5000,
    // Define set of HTTP Clients that needs to be Failover.
    targets: [
        { url: "http://localhost:8082/statuscodes" },
        { url: "http://localhost:8082/statuscodes" },
        { url: "http://localhost:8082/statuscodes" }
    ]

};

@http:ServiceConfig {
    basePath: "/fo"
}
service<http:Service> failoverDemoService02 bind failoverEP02 {
    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/typical"
    }
    invokeEndpoint(endpoint caller, http:Request request) {
        var backendRes = foBackendEP02->forward("/", request);
        if (backendRes is http:Response) {
            var responseToCaller = caller->respond(backendRes);
            if (responseToCaller is error) {
                log:printError("Error sending response", err = responseToCaller);
            }
        } else if (backendRes is error) {
            http:Response response = new;
            response.statusCode = 500;
            response.setPayload(<string> backendRes.detail().message);
            var responseToCaller = caller->respond(response);
            if (responseToCaller is error) {
                log:printError("Error sending response", err = responseToCaller);
            }
        }
    }

    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/failures"
    }
    invokeAllFailureEndpoint(endpoint caller, http:Request request) {
        var backendRes = foBackendFailureEP02->forward("/", request);
        if (backendRes is http:Response) {
            var responseToCaller = caller->respond(backendRes);
            if (responseToCaller is error) {
                log:printError("Error sending response", err = responseToCaller);
            }
        } else if (backendRes is error) {
            http:Response response = new;
            response.statusCode = 500;
            response.setPayload(<string> backendRes.detail().message);
            var responseToCaller = caller->respond(response);
            if (responseToCaller is error) {
                log:printError("Error sending response", err = responseToCaller);
            }
        }
    }

    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/failurecodes"
    }
    invokeAllFailureStatusCodesEndpoint(endpoint caller, http:Request request) {
        var backendRes = foStatusCodesEP02->forward("/", request);
        if (backendRes is http:Response) {
            var responseToCaller = caller->respond(backendRes);
            if (responseToCaller is error) {
                log:printError("Error sending response", err = responseToCaller);
            }
        } else if (backendRes is error) {
            http:Response response = new;
            response.statusCode = 500;
            response.setPayload(<string> backendRes.detail().message);
            var responseToCaller = caller->respond(response);
            if (responseToCaller is error) {
                log:printError("Error sending response", err = responseToCaller);
            }
        }
    }

    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/index"
    }
    failoverStartIndex(endpoint caller, http:Request request) {
        http:FailoverActions foClient = foBackendEP02.getCallerActions();
        string startIndex = <string> foClient.succeededEndpointIndex;
        var backendRes = foBackendEP02->forward("/", request);
        if (backendRes is http:Response) {
            string responseMessage = "Failover start index is : " + startIndex;
            var responseToCaller = caller->respond(responseMessage);
            if (responseToCaller is error) {
                log:printError("Error sending response", err = responseToCaller);
            }
        } else if (backendRes is error) {
            http:Response response = new;
            response.statusCode = 500;
            response.setPayload(<string> backendRes.detail().message);
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
service echo02 bind backendEP02 {
    @http:ResourceConfig {
        methods: ["POST", "PUT", "GET"],
        path: "/"
    }
    echoResource(endpoint caller, http:Request req) {
        http:Response outResponse = new;
        // Delay the response for 30000 milliseconds to mimic network level delays.
        runtime:sleep(30000);
        var responseToCaller = caller->respond("echo Resource is invoked");
        if (responseToCaller is error) {
            log:printError("Error sending response from mock service", err = responseToCaller);
        }
    }
}

public int counter02 = 1;
// Define the sample service to mock a healthy service.
@http:ServiceConfig {
    basePath: "/mock"
}
service mock02 bind backendEP02 {
    @http:ResourceConfig {
        methods: ["POST", "PUT", "GET"],
        path: "/"
    }
    mockResource(endpoint caller, http:Request req) {
        counter02 += 1;
        if (counter02 % 5 == 0) {
            runtime:sleep(30000);
        }
        http:Response response = new;
        if (req.hasHeader(mime:CONTENT_TYPE)
            && req.getHeader(mime:CONTENT_TYPE).hasPrefix(http:MULTIPART_AS_PRIMARY_TYPE)) {
            var mimeEntity = req.getBodyParts();
            if (mimeEntity is error) {
                log:printError(<string> mimeEntity.detail().message);
                response.setPayload("Error in decoding multiparts!");
                response.statusCode = 500;
            } else if (mimeEntity is mime:Entity[]) {
                foreach bodyPart in mimeEntity {
                    if (bodyPart.hasHeader(mime:CONTENT_TYPE)
                        && bodyPart.getHeader(mime:CONTENT_TYPE).hasPrefix(http:MULTIPART_AS_PRIMARY_TYPE)) {
                        var nestedMimeEntity = bodyPart.getBodyParts();
                        if (nestedMimeEntity is error) {
                            log:printError(<string> nestedMimeEntity.detail().message);
                            response.setPayload("Error in decoding nested multiparts!");
                            response.statusCode = 500;
                        } else {
                            mime:Entity[] childParts = nestedMimeEntity;
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
                response.setBodyParts(untaint mimeEntity, contentType = untaint req.getContentType());
            }
        } else {
            response.setPayload("Mock Resource is Invoked.");
        }
        var responseToCaller = caller->respond(response);
        if (responseToCaller is error) {
            log:printError("Error sending response from mock service", err = responseToCaller);
        }
    }
}

// Define the sample service to mock connection timeouts and service outages.
@http:ServiceConfig {
    basePath: "/statuscodes"
}
service failureStatusCodeService02 bind backendEP02 {
    @http:ResourceConfig {
        methods: ["POST", "PUT", "GET"],
        path: "/"
    }
    errorStatusResource(endpoint caller, http:Request req) {
        http:Response outResponse = new;
        outResponse.statusCode = 503;
        outResponse.setPayload("Failure status code scenario");
        var responseToCaller = caller->respond(outResponse);
        if (responseToCaller is error) {
            log:printError("Error sending response from mock service", err = responseToCaller);
        }
    }
}

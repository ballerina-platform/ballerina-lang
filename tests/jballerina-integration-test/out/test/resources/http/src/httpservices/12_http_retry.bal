// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/http;
import ballerina/log;
import ballerina/mime;
import ballerina/runtime;
import ballerina/io;

listener http:Listener serviceEndpoint1 = new(9105);

// Define the end point to the call the `mockHelloService`.
http:Client backendClientEP = new ("http://localhost:9105", {
    // Retry configuration options.
    retryConfig: {
        intervalInMillis: 3000,
        count: 3,
        backOffFactor: 0.5
    },
    timeoutInMillis: 2000
});

@http:ServiceConfig {
    basePath: "/retry"
}
service retryDemoService on serviceEndpoint1 {
    // Create a REST resource within the API.
    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/"
    }
    // Parameters include a reference to the caller endpoint and an object of
    // the request data.
    resource function invokeEndpoint(http:Caller caller, http:Request request) {
        var backendResponse = backendClientEP->forward("/hello", request);
        if (backendResponse is http:Response) {
            var responseToCaller = caller->respond(backendResponse);
            if (responseToCaller is error) {
                log:printError("Error sending response", responseToCaller);
            }
        } else {
            error err = backendResponse;
            http:Response response = new;
            response.statusCode = http:STATUS_INTERNAL_SERVER_ERROR;
            string? errCause = err.detail()?.message;
            response.setPayload(errCause is string ? errCause : "Internal server error");
            var responseToCaller = caller->respond(response);
            if (responseToCaller is error) {
                log:printError("Error sending response", responseToCaller);
            }
        }
    }
}

int counter = 0;

// This sample service is used to mock connection timeouts and service outages.
// The service outage is mocked by stopping/starting this service.
// This should run separately from the `retryDemoService` service.
@http:ServiceConfig { basePath: "/hello" }
service mockHelloService on serviceEndpoint1 {
    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/"
    }
    resource function sayHello(http:Caller caller, http:Request req) {
        counter = counter + 1;
        if (counter % 4 != 0) {
            log:printInfo(
                "Request received from the client to delayed service.");
            // Delay the response by 5000 milliseconds to
            // mimic network level delays.
            runtime:sleep(5000);
            http:Response res = new;
            res.setPayload("Hello World!!!");
            var result = caller->respond(res);

            if (result is error) {
                log:printError("Error sending response from mock service", result);
            }
        } else {
            log:printInfo("Request received from the client to healthy service.");
            http:Response response = new;
            if (req.hasHeader(mime:CONTENT_TYPE)
                && req.getHeader(mime:CONTENT_TYPE).startsWith(http:MULTIPART_AS_PRIMARY_TYPE)) {
                var bodyParts = req.getBodyParts();
                if (bodyParts is mime:Entity[]) {
                    foreach var bodyPart in bodyParts {
                        if (bodyPart.hasHeader(mime:CONTENT_TYPE)
                            && bodyPart.getHeader(mime:CONTENT_TYPE).startsWith(http:MULTIPART_AS_PRIMARY_TYPE)) {
                            var nestedParts = bodyPart.getBodyParts();
                            if (nestedParts is error) {
                                log:printError(<string> nestedParts.detail().message);
                                response.setPayload("Error in decoding nested multiparts!");
                                response.statusCode = 500;
                            } else {
                                mime:Entity[] childParts = nestedParts;
                                foreach var childPart in childParts {
                                    // When performing passthrough scenarios, message needs to be built before
                                    // invoking the endpoint to create a message datasource.
                                    var childBlobContent = childPart.getByteArray();
                                }
                                io:println(bodyPart.getContentType());
                                bodyPart.setBodyParts(<@untainted> childParts, <@untainted> bodyPart.getContentType());
                            }
                        } else {
                            var bodyPartBlobContent = bodyPart.getByteArray();
                        }
                    }
                    response.setBodyParts(<@untainted> bodyParts, <@untainted> req.getContentType());
                } else {
                    error err = bodyParts;
                    log:printError(err.reason());
                    response.setPayload("Error in decoding multiparts!");
                    response.statusCode = 500;
                }
            } else {
                response.setPayload("Hello World!!!");
            }
            var responseToCaller = caller->respond(response);
            if (responseToCaller is error) {
                log:printError("Error sending response from mock service", responseToCaller);
            }
        }
    }
}

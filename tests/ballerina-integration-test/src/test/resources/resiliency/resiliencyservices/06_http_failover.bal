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
import ballerina/io;
import ballerina/log;
import ballerina/mime;
import ballerina/runtime;

endpoint http:Listener failoverEP05 {
    port: 9305
};

// Create an endpoint with port 8085 for the mock backend services.
endpoint http:Listener backendEP05 {
    port: 8085
};

// Define the failover client end point to call the backend services.
endpoint http:FailoverClient foBackendEP05 {
    timeoutMillis: 5000,
    failoverCodes: [501, 502, 503],
    intervalMillis: 5000,
    // Define set of HTTP Clients that needs to be Failover.
    targets: [
        { url: "http://localhost:3000/inavalidEP" },
        { url: "http://localhost:8085/delay" },
        { url: "http://localhost:8085/mock" }
    ]
};

@http:ServiceConfig {
    basePath: "/fo"
}
service<http:Service> failoverDemoService05 bind failoverEP05 {
    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/index"
    }
    failoverStartIndex(endpoint caller, http:Request request) {
        http:FailoverActions foClient = foBackendEP05.getCallerActions();
        string startIndex = <string>foClient.succeededEndpointIndex;
        var backendRes = foBackendEP05->forward("/", request);
        match backendRes {
            http:Response response => {
                string responseMessage = "Failover start index is : " + startIndex;
                caller->ok(responseMessage) but {
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
    basePath: "/delay"
}
service echo05 bind backendEP05 {
    @http:ResourceConfig {
        methods: ["POST", "PUT", "GET"],
        path: "/"
    }
    delayResource(endpoint caller, http:Request req) {
        // Delay the response for 30000 milliseconds to mimic network level delays.
        runtime:sleep(30000);
        caller->ok("Delayed resource is invoked") but {
            error e => log:printError("Error sending response from mock service", err = e)
        };
    }
}

// Define the sample service to mock a healthy service.
@http:ServiceConfig {
    basePath: "/mock"
}
service mock05 bind backendEP05 {
    @http:ResourceConfig {
        methods: ["POST", "PUT", "GET"],
        path: "/"
    }
    mockResource(endpoint caller, http:Request req) {
        caller->ok("Mock Resource is Invoked.") but {
            error e => log:printError("Error sending response from mock service", err = e)
        };
    }
}

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

listener http:Listener failoverEP05 = new(9305);

// Create an endpoint with port 8085 for the mock backend services.
listener http:Listener backendEP05 = new(8085);

// Define the failover client end point to call the backend services.
http:FailoverClient foBackendEP05 = new({
    timeoutMillis: 5000,
    failoverCodes: [501, 502, 503],
    intervalMillis: 5000,
    // Define set of HTTP Clients that needs to be Failover.
    targets: [
        { url: "http://localhost:3467/inavalidEP" },
        { url: "http://localhost:8085/delay" },
        { url: "http://localhost:8085/mock" }
    ]
});

@http:ServiceConfig {
    basePath: "/fo"
}
service failoverDemoService05 on failoverEP05 {
    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/index"
    }
    resource function failoverStartIndex(http:Caller caller, http:Request request) {
        string startIndex = <string>foBackendEP05.succeededEndpointIndex;
        var backendRes = foBackendEP05->forward("/", request);
        if (backendRes is http:Response) {
            string responseMessage = "Failover start index is : " + startIndex;
            var responseToCaller = caller->respond(responseMessage);
            if (responseToCaller is error) {
                log:printError("Error sending response", err = responseToCaller);
            }
        } else {
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
    basePath: "/delay"
}
service echo05 on backendEP05 {
    @http:ResourceConfig {
        methods: ["POST", "PUT", "GET"],
        path: "/"
    }
    resource function delayResource(http:Caller caller, http:Request req) {
        // Delay the response for 30000 milliseconds to mimic network level delays.
        runtime:sleep(30000);
        var responseToCaller = caller->respond("Delayed resource is invoked");
        if (responseToCaller is error) {
            log:printError("Error sending response from mock service", err = responseToCaller);
        }
    }
}

// Define the sample service to mock a healthy service.
@http:ServiceConfig {
    basePath: "/mock"
}
service mock05 on backendEP05 {
    @http:ResourceConfig {
        methods: ["POST", "PUT", "GET"],
        path: "/"
    }
    resource function mockResource(http:Caller caller, http:Request req) {
        var responseToCaller = caller->respond("Mock Resource is Invoked.");
        if (responseToCaller is error) {
            log:printError("Error sending response from mock service", err = responseToCaller);
        }
    }
}

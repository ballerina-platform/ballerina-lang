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
import ballerina/runtime;

public int forceOpenStateCount = 0;

endpoint http:Listener circuitBreakerEP01 {
    port:9307
};

endpoint http:Client healthyClientEP {
    url: "http://localhost:8087",
    circuitBreaker: {
        rollingWindow: {
            timeWindowMillis: 60000,
            bucketSizeMillis: 20000,
            requestVolumeThreshold: 0
        },
        failureThreshold: 0.2,
        resetTimeMillis: 1000,
        statusCodes: [501, 502, 503]
    },

    timeoutMillis: 2000
};

@http:ServiceConfig {
    basePath: "/cb"
}
service<http:Service> circuitbreaker01 bind circuitBreakerEP01 {

    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/forceopen"
    }
    invokeForceOpen(endpoint caller, http:Request request) {
        http:CircuitBreakerClient cbClient = check <http:CircuitBreakerClient>healthyClientEP.getCallerActions();
        forceOpenStateCount += 1;
        if (forceOpenStateCount == 2) {
            cbClient.forceOpen();
        }
        var backendRes = healthyClientEP->forward("/healthy", request);
        match backendRes {
            http:Response res => {
                caller->respond(res) but {
                    error e => log:printError("Error sending response", err = e)
                };
            }
            error responseError => {
                http:Response response = new;
                response.statusCode = http:INTERNAL_SERVER_ERROR_500;
                response.setPayload(responseError.message);
                caller->respond(response) but {
                    error e => log:printError("Error sending response", err = e)
                };
            }
        }
    }
}

@http:ServiceConfig { basePath: "/healthy" }
service<http:Service> healthyService bind { port: 8087 } {
    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/"
    }
    sayHello(endpoint caller, http:Request req) {
        http:Response res = new;
        res.setPayload("Hello World!!!");
        caller->respond(res) but {
            error e => log:printError("Error sending response from mock service", err = e)
        };
    }
}

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

int forceOpenStateCount = 0;

listener http:Listener circuitBreakerEP01 = new(9307);

http:ClientConfiguration conf01 = {
    circuitBreaker: {
        rollingWindow: {
            timeWindowInMillis: 60000,
            bucketSizeInMillis: 20000,
            requestVolumeThreshold: 0
        },
        failureThreshold: 0.2,
        resetTimeInMillis: 1000,
        statusCodes: [501, 502, 503]
    },
    timeoutInMillis: 2000
};

http:Client healthyClientEP = new("http://localhost:8087", conf01);

@http:ServiceConfig {
    basePath: "/cb"
}
service circuitbreaker01 on circuitBreakerEP01 {

    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/forceopen"
    }
    resource function invokeForceOpen(http:Caller caller, http:Request request) {
        http:CircuitBreakerClient cbClient = <http:CircuitBreakerClient>healthyClientEP.httpClient;
        forceOpenStateCount += 1;
        if (forceOpenStateCount == 2) {
            cbClient.forceOpen();
        }
        var backendRes = healthyClientEP->forward("/healthy", request);
        if (backendRes is http:Response) {
            var responseToCaller = caller->respond(backendRes);
            if (responseToCaller is error) {
                log:printError("Error sending response", responseToCaller);
            }
        } else {
            error err = backendRes;
            http:Response response = new;
            response.statusCode = http:STATUS_INTERNAL_SERVER_ERROR;
            string errCause = <string> err.detail()?.message;
            response.setPayload(errCause);
            var responseToCaller = caller->respond(response);
            if (responseToCaller is error) {
                log:printError("Error sending response", responseToCaller);
            }
        }
    }
}

@http:ServiceConfig { basePath: "/healthy" }
service healthyService on new http:Listener(8087) {
    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/"
    }
    resource function sayHello(http:Caller caller, http:Request req) {
        var responseToCaller = caller->respond("Hello World!!!");
        if (responseToCaller is error) {
            log:printError("Error sending response from mock service", responseToCaller);
        }
    }
}

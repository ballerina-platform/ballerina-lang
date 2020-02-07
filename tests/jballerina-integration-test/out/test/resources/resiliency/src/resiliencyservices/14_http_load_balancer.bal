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

listener http:Listener backendEP = new(8093);

http:LoadBalanceClient lbBackendEP = new({
    targets: [
        { url: "http://localhost:8093/mock1" },
        { url: "http://localhost:8093/mock2" },
        { url: "http://localhost:8093/mock3" }
    ],
    timeoutInMillis: 5000
});

http:LoadBalanceClient lbFailoverBackendEP = new({
    targets: [
        { url: "http://localhost:8093/mock4" },
        { url: "http://localhost:8093/mock2" },
        { url: "http://localhost:8093/mock3" }
    ],
    failover: true,
    timeoutInMillis: 2000
});

http:LoadBalanceClient delayedBackendEP = new({
    targets: [
        { url: "http://localhost:8093/mock4" },
        { url: "http://localhost:8093/mock5" }
    ],
    failover: true,
    timeoutInMillis: 2000
});

CustomLoadBalancerRule customLbRule = new CustomLoadBalancerRule(2);

http:LoadBalanceClient customLbBackendEP = new ({
    targets: [
        { url: "http://localhost:8093/mock1" },
        { url: "http://localhost:8093/mock2" },
        { url: "http://localhost:8093/mock3" }
    ],
    lbRule: customLbRule,
    timeoutInMillis: 5000
});

@http:ServiceConfig {
    basePath: "/lb"
}
service loadBalancerDemoService on new http:Listener(9313) {
    @http:ResourceConfig {
        path: "/roundRobin"
    }
    resource function roundRobin(http:Caller caller, http:Request req) {
        json requestPayload = { "name": "Ballerina" };
        var response = lbBackendEP->post("/", requestPayload);
        if (response is http:Response) {
            var responseToCaller = caller->respond(response);
            if (responseToCaller is error) {
                log:printError("Error sending response", responseToCaller);
            }
        } else {
            error err = response;
            http:Response outResponse = new;
            outResponse.statusCode = 500;
            outResponse.setPayload(<string> err.detail()?.message);
            var responseToCaller = caller->respond(outResponse);
            if (responseToCaller is error) {
                log:printError("Error sending response", responseToCaller);
            }
        }
    }

    @http:ResourceConfig {
        path: "/failover"
    }
    resource function lbFailover(http:Caller caller, http:Request req) {
        json requestPayload = { "name": "Ballerina" };
        var response = lbFailoverBackendEP->post("/", requestPayload);
        if (response is http:Response) {
            var responseToCaller = caller->respond(response);
            if (responseToCaller is error) {
                log:printError("Error sending response", responseToCaller);
            }
        } else {
            error err = response;
            http:Response outResponse = new;
            outResponse.statusCode = 500;
            outResponse.setPayload(<string> err.detail()?.message);
            var responseToCaller = caller->respond(outResponse);
            if (responseToCaller is error) {
                log:printError("Error sending response", responseToCaller);
            }
        }
    }

    @http:ResourceConfig {
        path: "/delay"
    }
    resource function delayResource(http:Caller caller, http:Request req) {
        json requestPayload = { "name": "Ballerina" };
        var response = delayedBackendEP->post("/", requestPayload);
        if (response is http:Response) {
            var responseToCaller = caller->respond(response);
            if (responseToCaller is error) {
                log:printError("Error sending response", responseToCaller);
            }
        } else {
            error err = response;
            http:Response outResponse = new;
            outResponse.statusCode = 500;
            outResponse.setPayload(<string> err.detail()?.message);
            var responseToCaller = caller->respond(outResponse);
            if (responseToCaller is error) {
                log:printError("Error sending response", responseToCaller);
            }
        }
    }

    @http:ResourceConfig {
        path: "/custom"
    }
    resource function customResource(http:Caller caller, http:Request req) {
        json requestPayload = { "name": "Ballerina" };
        var response = customLbBackendEP->post("/", requestPayload);
        if (response is http:Response) {
            var responseToCaller = caller->respond(response);
            if (responseToCaller is error) {
                log:printError("Error sending response", responseToCaller);
            }
        } else {
            error err = response;
            http:Response outResponse = new;
            outResponse.statusCode = 500;
            outResponse.setPayload(<string> err.detail()?.message);
            var responseToCaller = caller->respond(outResponse);
            if (responseToCaller is error) {
                log:printError("Error sending response", responseToCaller);
            }
        }
    }
}

@http:ServiceConfig { basePath: "/mock1" }
service mock1 on backendEP {
    @http:ResourceConfig {
        path: "/"
    }
    resource function mock1Resource(http:Caller caller, http:Request req) {
        var responseToCaller = caller->respond("Mock1 Resource is Invoked.");
        if (responseToCaller is error) {
            log:printError("Error sending response from mock service", responseToCaller);
        }
    }
}

@http:ServiceConfig { basePath: "/mock2" }
service mock2 on backendEP {
    @http:ResourceConfig {
        path: "/"
    }
    resource function mock2Resource(http:Caller caller, http:Request req) {
        var responseToCaller = caller->respond("Mock2 Resource is Invoked.");
        if (responseToCaller is error) {
            log:printError("Error sending response from mock service", responseToCaller);
        }
    }
}

@http:ServiceConfig { basePath: "/mock3" }
service mock3 on backendEP {
    @http:ResourceConfig {
        path: "/"
    }
    resource function mock3Resource(http:Caller caller, http:Request req) {
        var responseToCaller = caller->respond("Mock3 Resource is Invoked.");
        if (responseToCaller is error) {
            log:printError("Error sending response from mock service", responseToCaller);
        }
    }
}

@http:ServiceConfig { basePath: "/mock4" }
service mock4 on backendEP {
    @http:ResourceConfig {
        path: "/"
    }
    resource function mock4Resource(http:Caller caller, http:Request req) {
        runtime:sleep(5000);
        var responseToCaller = caller->respond("Mock4 Resource is Invoked.");
        if (responseToCaller is error) {
            log:printError("Error sending response from mock service", responseToCaller);
        }
    }
}

@http:ServiceConfig { basePath: "/mock5" }
service mock5 on backendEP {
    @http:ResourceConfig {
        path: "/"
    }
    resource function mock5Resource(http:Caller caller, http:Request req) {
        runtime:sleep(5000);
        var responseToCaller = caller->respond("Mock5 Resource is Invoked.");
        if (responseToCaller is error) {
            log:printError("Error sending response from mock service", responseToCaller);
        }
    }
}

# Implementation of custom load balancing strategy.
#
# + index - Keep tracks the current point of the CallerActions[]
public type CustomLoadBalancerRule object {

    public int index;

    public function __init(int index) {
        self.index = index;
    }

    # Provides an HTTP client which is choosen according to the custom algorithm.
    #
    # + loadBalanceClientsArray - Array of HTTP clients which needs to be load balanced
    # + return - Choosen `CallerActions` from the algorithm or an `error` for a failure in
    #            the algorithm implementation
    public function getNextClient(http:Client?[] loadBalanceClientsArray) returns http:Client|http:ClientError {
        http:Client httpClient = <http:Client>loadBalanceClientsArray[self.index];
        if (self.index >= loadBalanceClientsArray.length()) {
            http:AllLoadBalanceEndpointsFailedError err = error(http:ALL_LOAD_BALANCE_ENDPOINTS_FAILED,
                                                        message = "Provided index is doesn't match with the targets.");
            return err;
        }
        lock {
            if (self.index == (loadBalanceClientsArray.length() - 1)) {
                httpClient = <http:Client>loadBalanceClientsArray[self.index];
                self.index = 0;
            } else {
                httpClient = <http:Client>loadBalanceClientsArray[self.index];
                self.index += 1;
            }
        }
        return httpClient;
    }
};

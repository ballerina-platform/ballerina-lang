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

endpoint http:Listener backendEP {
    port: 8093
};

endpoint http:LoadBalanceClient lbBackendEP {
    targets: [
        { url: "http://localhost:8093/mock1" },
        { url: "http://localhost:8093/mock2" },
        { url: "http://localhost:8093/mock3" }
    ],
    timeoutMillis: 5000
};

endpoint http:LoadBalanceClient lbFailoverBackendEP {
    targets: [
        { url: "http://localhost:8093/mock4" },
        { url: "http://localhost:8093/mock2" },
        { url: "http://localhost:8093/mock3" }
    ],
    failover: true,
    timeoutMillis: 2000
};

endpoint http:LoadBalanceClient delayedBackendEP {
    targets: [
        { url: "http://localhost:8093/mock4" },
        { url: "http://localhost:8093/mock5" }
    ],
    failover: true,
    timeoutMillis: 2000
};

CustomLoadBalancerRule customLbRule = new CustomLoadBalancerRule(2);
endpoint http:LoadBalanceClient customLbBackendEP {
    targets: [
        { url: "http://localhost:8093/mock1" },
        { url: "http://localhost:8093/mock2" },
        { url: "http://localhost:8093/mock3" }
    ],
    lbRule: customLbRule,
    timeoutMillis: 5000
};

@http:ServiceConfig {
    basePath: "/lb"
}
service<http:Service> loadBalancerDemoService bind { port: 9313 } {
    @http:ResourceConfig {
        path: "/roundRobin"
    }
    roundRobin(endpoint caller, http:Request req) {
        json requestPayload = { "name": "Ballerina" };
        var response = lbBackendEP->post("/", requestPayload);
        if (response is http:Response) {
            var responseToCaller = caller->respond(response);
            if (responseToCaller is error) {
                log:printError("Error sending response", err = responseToCaller);
            }
        } else if (response is error) {
            http:Response outResponse = new;
            outResponse.statusCode = 500;
            outResponse.setPayload(<string> response.detail().message);
            var responseToCaller = caller->respond(outResponse);
            if (responseToCaller is error) {
                log:printError("Error sending response", err = responseToCaller);
            }
        }
    }

    @http:ResourceConfig {
        path: "/failover"
    }
    lbFailover(endpoint caller, http:Request req) {
        json requestPayload = { "name": "Ballerina" };
        var response = lbFailoverBackendEP->post("/", requestPayload);
        if (response is http:Response) {
            var responseToCaller = caller->respond(response);
            if (responseToCaller is error) {
                log:printError("Error sending response", err = responseToCaller);
            }
        } else if (response is error) {
            http:Response outResponse = new;
            outResponse.statusCode = 500;
            outResponse.setPayload(<string> response.detail().message);
            var responseToCaller = caller->respond(outResponse);
            if (responseToCaller is error) {
                log:printError("Error sending response", err = responseToCaller);
            }
        }
    }

    @http:ResourceConfig {
        path: "/delay"
    }
    delayResource(endpoint caller, http:Request req) {
        json requestPayload = { "name": "Ballerina" };
        var response = delayedBackendEP->post("/", requestPayload);
        if (response is http:Response) {
            var responseToCaller = caller->respond(response);
            if (responseToCaller is error) {
                log:printError("Error sending response", err = responseToCaller);
            }
        } else if (response is error) {
            http:Response outResponse = new;
            outResponse.statusCode = 500;
            outResponse.setPayload(<string> response.detail().message);
            var responseToCaller = caller->respond(outResponse);
            if (responseToCaller is error) {
                log:printError("Error sending response", err = responseToCaller);
            }
        }
    }

    @http:ResourceConfig {
        path: "/custom"
    }
    customResource(endpoint caller, http:Request req) {
        json requestPayload = { "name": "Ballerina" };
        var response = customLbBackendEP->post("/", requestPayload);
        if (response is http:Response) {
            var responseToCaller = caller->respond(response);
            if (responseToCaller is error) {
                log:printError("Error sending response", err = responseToCaller);
            }
        } else if (response is error) {
            http:Response outResponse = new;
            outResponse.statusCode = 500;
            outResponse.setPayload(<string> response.detail().message);
            var responseToCaller = caller->respond(outResponse);
            if (responseToCaller is error) {
                log:printError("Error sending response", err = responseToCaller);
            }
        }
    }
}

@http:ServiceConfig { basePath: "/mock1" }
service mock1 bind backendEP {
    @http:ResourceConfig {
        path: "/"
    }
    mock1Resource(endpoint caller, http:Request req) {
        var responseToCaller = caller->respond("Mock1 Resource is Invoked.");
        if (responseToCaller is error) {
            log:printError("Error sending response from mock service", err = responseToCaller);
        }
    }
}

@http:ServiceConfig { basePath: "/mock2" }
service mock2 bind backendEP {
    @http:ResourceConfig {
        path: "/"
    }
    mock2Resource(endpoint caller, http:Request req) {
        var responseToCaller = caller->respond("Mock2 Resource is Invoked.");
        if (responseToCaller is error) {
            log:printError("Error sending response from mock service", err = responseToCaller);
        }
    }
}

@http:ServiceConfig { basePath: "/mock3" }
service mock3 bind backendEP {
    @http:ResourceConfig {
        path: "/"
    }
    mock3Resource(endpoint caller, http:Request req) {
        var responseToCaller = caller->respond("Mock3 Resource is Invoked.");
        if (responseToCaller is error) {
            log:printError("Error sending response from mock service", err = responseToCaller);
        }
    }
}

@http:ServiceConfig { basePath: "/mock4" }
service mock4 bind backendEP {
    @http:ResourceConfig {
        path: "/"
    }
    mock4Resource(endpoint caller, http:Request req) {
        runtime:sleep(5000);
        var responseToCaller = caller->respond("Mock4 Resource is Invoked.");
        if (responseToCaller is error) {
            log:printError("Error sending response from mock service", err = responseToCaller);
        }
    }
}

@http:ServiceConfig { basePath: "/mock5" }
service mock5 bind backendEP {
    @http:ResourceConfig {
        path: "/"
    }
    mock5Resource(endpoint caller, http:Request req) {
        runtime:sleep(5000);
        var responseToCaller = caller->respond("Mock5 Resource is Invoked.");
        if (responseToCaller is error) {
            log:printError("Error sending response from mock service", err = responseToCaller);
        }
    }
}

# Implementation of custom load balancing strategy.
#
# + index - Keep tracks the current point of the CallerActions[]
public type CustomLoadBalancerRule object {

    public int index;

    public new (index) {}

    # Provides an HTTP client which is choosen according to the custom algorithm.
    #
    # + loadBalanceClientsArray - Array of HTTP clients which needs to be load balanced
    # + return - Choosen `CallerActions` from the algorithm or an `error` for a failure in
    #            the algorithm implementation
    public function getNextCallerActions(http:CallerActions[] loadBalanceClientsArray) returns http:CallerActions|error;
};

function CustomLoadBalancerRule.getNextCallerActions(http:CallerActions[] loadBalanceClientsArray)
                                          returns http:CallerActions|error {
    http:CallerActions httpClient = new;
    if (self.index >= loadBalanceClientsArray.length()) {
        error err = error("Provided index is doesn't match with the targets.");
        return err;
    }
    lock {
        if (self.index == (loadBalanceClientsArray.length() - 1)) {
            httpClient = loadBalanceClientsArray[self.index];
            self.index = 0;
        } else {
            httpClient = loadBalanceClientsArray[self.index];
            self.index += 1;
        }
    }
    return httpClient;
}

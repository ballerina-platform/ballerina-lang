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
import ballerina/runtime;

@final string TEST_SCENARIO_HEADER = "test-scenario";

@final string SCENARIO_TYPICAL = "typical-scenario";
@final string SCENARIO_TRIAL_RUN_FAILURE = "trial-run-failure";
@final string SCENARIO_HTTP_SC_FAILURE = "http-status-code-failure";

function testTypicalScenario () returns (http:Response[] , http:HttpConnectorError[]) {

    endpoint http:Client backendClientEP {
        url: "http://localhost:8080",
        circuitBreaker: {
            rollingWindow: {
                timeWindowMillis:10000,
                bucketSizeMillis:2000
            },
            failureThreshold:0.3,
            resetTimeMillis:1000,
            statusCodes:[400, 404, 500, 502]
        },
        timeoutMillis:2000
    };

    http:Response[] responses = [];
    http:HttpConnectorError[] errs = [];
    int counter = 0;
    http:CircuitBreakerClient cbClient = check <http:CircuitBreakerClient>backendClientEP.getCallerActions();
    MockClient mockClient = new;
    cbClient.httpClient = <http:HttpClient> mockClient;

    while (counter < 8) {
       http:Request request = new;
       request.setHeader(TEST_SCENARIO_HEADER, SCENARIO_TYPICAL);
       match cbClient.get("/hello", request = request) {
            http:Response res => {
                responses[counter] = res;
            }
            http:HttpConnectorError httpConnectorError => {
                httpConnectorError.statusCode = http:SERVICE_UNAVAILABLE_503;
                errs[counter] = httpConnectorError; 
            }
        }
       counter = counter + 1;
       // To ensure the reset timeout period expires
       if (counter == 5) {
           runtime:sleep(5000);
       }
    }
    return (responses, errs);
}

function testTrialRunFailure () returns (http:Response[] , http:HttpConnectorError[]) {
    
    endpoint http:Client backendClientEP {
        url: "http://localhost:8080",
        circuitBreaker: {
            rollingWindow: {
                timeWindowMillis:10000,
                bucketSizeMillis:2000
            },
            failureThreshold:0.3,
            resetTimeMillis:1000,
            statusCodes:[400, 404, 500, 502]
        },
        timeoutMillis:2000
    };

    http:Response[] responses = [];
    http:HttpConnectorError[] errs = [];
    int counter = 0;
    http:CircuitBreakerClient cbClient = check <http:CircuitBreakerClient>backendClientEP.getCallerActions();
    MockClient mockClient = new;
    cbClient.httpClient = <http:HttpClient> mockClient;

    while (counter < 8) {
        http:Request request = new;
       request.setHeader(TEST_SCENARIO_HEADER, SCENARIO_TRIAL_RUN_FAILURE);
       match cbClient.get("/hello", request = request) {
            http:Response res => {
                responses[counter] = res;
            }
            http:HttpConnectorError httpConnectorError => {
                httpConnectorError.statusCode = http:SERVICE_UNAVAILABLE_503;
                errs[counter] = httpConnectorError; 
            }
        }
       counter = counter + 1;
       // To ensure the reset timeout period expires
       if (counter == 5) {
           runtime:sleep(5000);
       }
    }
    return (responses, errs);
}

function testHttpStatusCodeFailure () returns (http:Response[] , http:HttpConnectorError[]) {
    
    endpoint http:Client backendClientEP {
        url: "http://localhost:8080",
        circuitBreaker: {
            rollingWindow: {
                timeWindowMillis:10000,
                bucketSizeMillis:2000
            },
            failureThreshold:0.3,
            resetTimeMillis:1000,
            statusCodes:[400, 404, 500, 502]
        },
        timeoutMillis:2000
    };

    http:Response[] responses = [];
    http:HttpConnectorError[] errs = [];
    int counter = 0;
    http:CircuitBreakerClient cbClient = check <http:CircuitBreakerClient>backendClientEP.getCallerActions();
    MockClient mockClient = new;
    cbClient.httpClient = <http:HttpClient> mockClient;

    while (counter < 8) {
        http:Request request = new;
       request.setHeader(TEST_SCENARIO_HEADER, SCENARIO_HTTP_SC_FAILURE);
       match cbClient.get("/hello", request = request) {
            http:Response res => {
                responses[counter] = res;
            }
            http:HttpConnectorError httpConnectorError => {
                httpConnectorError.statusCode = http:SERVICE_UNAVAILABLE_503;
                errs[counter] = httpConnectorError; 
            }
        }
       counter = counter + 1;
    }
    return (responses, errs);
}

int actualRequestNumber = 0;

public type MockClient object {
    public {
        string serviceUri;
        http:ClientEndpointConfig config;
    }

    public function post (string path, http:Request req) returns (http:Response | http:HttpConnectorError) {
        http:HttpConnectorError httpConnectorError = {message:"Unsupported fuction for MockClient"};
        return httpConnectorError;
    }

    public function head (string path, http:Request req) returns (http:Response | http:HttpConnectorError) {
        http:HttpConnectorError httpConnectorError = {message:"Unsupported fuction for MockClient"};
        return httpConnectorError;
    }

    public function put (string path, http:Request req) returns (http:Response | http:HttpConnectorError) {
        http:HttpConnectorError httpConnectorError = {message:"Unsupported fuction for MockClient"};
        return httpConnectorError;
    }

    public function execute (string httpVerb, string path, http:Request req) returns (http:Response | http:HttpConnectorError) {
        http:HttpConnectorError httpConnectorError = {message:"Unsupported fuction for MockClient"};
        return httpConnectorError;
    }

    public function patch (string path, http:Request req) returns (http:Response | http:HttpConnectorError) {
        http:HttpConnectorError httpConnectorError = {message:"Unsupported fuction for MockClient"};
        return httpConnectorError;
    }

    public function delete (string path, http:Request req) returns (http:Response | http:HttpConnectorError) {
        http:HttpConnectorError httpConnectorError = {message:"Unsupported fuction for MockClient"};
        return httpConnectorError;
    }

    public function get (string path, http:Request req) returns (http:Response | http:HttpConnectorError){
        http:Response response = new;
        actualRequestNumber = actualRequestNumber + 1;
        string scenario = req.getHeader(TEST_SCENARIO_HEADER);

        if (scenario == SCENARIO_TYPICAL) {
            match handleBackendFailureScenario(actualRequestNumber) {
                http:Response res => {
                    response = res;
                }
                http:HttpConnectorError httpConnectorError => {
                    string message = httpConnectorError.message;
                    response.statusCode = httpConnectorError.statusCode;
                    response.setStringPayload(message);
                }
            }
        } else if (scenario == SCENARIO_TRIAL_RUN_FAILURE) {
            match  handleTrialRunFailureScenario(actualRequestNumber) {
                http:Response res => {
                    response = res;
                }
                http:HttpConnectorError httpConnectorError => {
                    string message = httpConnectorError.message;
                    response.statusCode = httpConnectorError.statusCode;
                    response.setStringPayload(message);
                }
            }
        }   else if (scenario == SCENARIO_HTTP_SC_FAILURE) {
            match  handleHTTPStatusCodeErrorScenario(actualRequestNumber) {
                http:Response res => {
                    response = res;
                }
                http:HttpConnectorError httpConnectorError => {
                    string message = httpConnectorError.message;
                    response.statusCode = httpConnectorError.statusCode;
                    response.setStringPayload(message);
                }
            }
        }
        return response;
    }

    public function options (string path, http:Request req) returns (http:Response | http:HttpConnectorError) {
        http:HttpConnectorError httpConnectorError = {message:"Unsupported fuction for MockClient"};
        return httpConnectorError;
    }

    public function forward (string path, http:Request req) returns (http:Response | http:HttpConnectorError) {
        http:HttpConnectorError httpConnectorError = {message:"Unsupported fuction for MockClient"};
        return httpConnectorError;
    }

    public function submit (string httpVerb, string path, http:Request req) returns (http:HttpFuture | http:HttpConnectorError) {
        http:HttpConnectorError httpConnectorError = {message:"Unsupported fuction for MockClient"};
        return httpConnectorError;
    }

    public function getResponse (http:HttpFuture httpFuture)  returns (http:Response | http:HttpConnectorError) {
        http:HttpConnectorError httpConnectorError = {message:"Unsupported fuction for MockClient"};
        return httpConnectorError;
    }

    public function hasPromise (http:HttpFuture httpFuture) returns (boolean) {
        return false;
    }

    public function getNextPromise (http:HttpFuture httpFuture) returns (http:PushPromise | http:HttpConnectorError) {
        http:HttpConnectorError httpConnectorError = {message:"Unsupported fuction for MockClient"};
        return httpConnectorError;
    }

    public function getPromisedResponse (http:PushPromise promise) returns (http:Response | http:HttpConnectorError) {
        http:HttpConnectorError httpConnectorError = {message:"Unsupported fuction for MockClient"};
        return httpConnectorError;
    }

    public function rejectPromise (http:PushPromise promise) {
    }
};

function handleBackendFailureScenario (int requesetNo) returns (http:Response | http:HttpConnectorError) {
    // Deliberately fail a request
    if (requesetNo == 3) {
        http:HttpConnectorError err = getErrorStruct();
        return err;
    }

    http:Response response = getResponse();
    return response;
}

function handleTrialRunFailureScenario (int counter) returns (http:Response | http:HttpConnectorError) {
    // Fail a request. Then, fail the trial request sent while in the HALF_OPEN state as well.
    if (counter == 2 || counter == 3) {
        http:HttpConnectorError err = getErrorStruct();
        return err;
    }

    http:Response response = getResponse();
    return response;
}

function handleHTTPStatusCodeErrorScenario (int counter) returns (http:Response | http:HttpConnectorError) {
    // Fail a request. Then, fail the trial request sent while in the HALF_OPEN state as well.
    if (counter == 2 || counter == 3) {
        http:HttpConnectorError err = getMockErrorStruct();
        return err;
    }

    http:Response response = getResponse();
    return response;
}

function getErrorStruct () returns (http:HttpConnectorError) {
    http:HttpConnectorError err = {message:"Connection refused", statusCode:http:BAD_GATEWAY_502};
    return err;
}

function getResponse () returns (http:Response) {
    // TODO: The way the status code is set may need to be changed once struct fields can be made read-only
    http:Response response = new;
    response.statusCode = http:OK_200;
    return response;
}

function getMockErrorStruct () returns (http:HttpConnectorError) {
    http:HttpConnectorError err = {message:"Internal Server Error", statusCode:http:INTERNAL_SERVER_ERROR_500};
    return err;
}

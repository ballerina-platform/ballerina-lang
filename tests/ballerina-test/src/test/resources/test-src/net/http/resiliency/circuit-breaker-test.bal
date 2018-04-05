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

    endpoint http:ClientEndpoint backendClientEP {
        circuitBreaker: {
            rollingWindow: {
                timeWindow:10000,
                bucketSize:2000
            },
            failureThreshold:0.3,
            resetTimeout:1000,
            statusCodes:[400, 404, 500, 502]
        },
        targets:[
            {
                uri: "http://localhost:8080"
            }
        ],
        endpointTimeout:2000
    };

    http:Request request = {};
    http:Response[] responses = [];
    http:HttpConnectorError[] errs = [];
    int counter = 0;
    http:CircuitBreakerClient cbClient = check <http:CircuitBreakerClient>backendClientEP.httpClient;
    MockClient mockClient = {serviceUri: "http://localhost:8080", config:{endpointTimeout:1000}};
    cbClient.httpClient = mockClient;

    while (counter < 8) {
       request = {};
       request.setHeader(TEST_SCENARIO_HEADER, SCENARIO_TYPICAL);
       match cbClient.get("/hello", request) {
            http:Response res => {
                responses[counter] = res;
            }
            http:HttpConnectorError httpConnectorError => {
                httpConnectorError.statusCode = 503;
                errs[counter] = httpConnectorError; 
            }
        }
       counter = counter + 1;
       // To ensure the reset timeout period expires
       if (counter == 5) {
           runtime:sleepCurrentWorker(5000);
       }
    }
    return (responses, errs);
}

function testTrialRunFailure () returns (http:Response[] , http:HttpConnectorError[]) {
    
    endpoint http:ClientEndpoint backendClientEP {
        circuitBreaker: {
            rollingWindow: {
                timeWindow:10000,
                bucketSize:2000
            },
            failureThreshold:0.3,
            resetTimeout:1000,
            statusCodes:[400, 404, 500, 502]
        },
        targets:[
            {
                uri: "http://localhost:8080"
            }
        ],
        endpointTimeout:2000
    };

    http:Request request = {};
    http:Response[] responses = [];
    http:HttpConnectorError[] errs = [];
    int counter = 0;
    http:CircuitBreakerClient cbClient = check <http:CircuitBreakerClient>backendClientEP.httpClient;
    MockClient mockClient = {serviceUri: "http://localhost:8080", config:{endpointTimeout:1000}};
    cbClient.httpClient = mockClient;

    while (counter < 8) {
       request = {};
       request.setHeader(TEST_SCENARIO_HEADER, SCENARIO_TRIAL_RUN_FAILURE);
       match cbClient.get("/hello", request) {
            http:Response res => {
                responses[counter] = res;
            }
            http:HttpConnectorError httpConnectorError => {
                httpConnectorError.statusCode = 503;
                errs[counter] = httpConnectorError; 
            }
        }
       counter = counter + 1;
       // To ensure the reset timeout period expires
       if (counter == 5) {
           runtime:sleepCurrentWorker(5000);
       }
    }
    return (responses, errs);
}

function testHttpStatusCodeFailure () returns (http:Response[] , http:HttpConnectorError[]) {
    
    endpoint http:ClientEndpoint backendClientEP {
        circuitBreaker: {
            rollingWindow: {
                timeWindow:10000,
                bucketSize:2000
            },
            failureThreshold:0.3,
            resetTimeout:1000,
            statusCodes:[400, 404, 500, 502]
        },
        targets:[
            {
                uri: "http://localhost:8080"
            }
        ],
        endpointTimeout:2000
    };

    http:Request request = {};
    http:Response[] responses = [];
    http:HttpConnectorError[] errs = [];
    int counter = 0;
    http:CircuitBreakerClient cbClient = check <http:CircuitBreakerClient>backendClientEP.httpClient;
    MockClient mockClient = {serviceUri: "http://localhost:8080", config:{endpointTimeout:1000}};
    cbClient.httpClient = mockClient;

    while (counter < 8) {
       request = {};
       request.setHeader(TEST_SCENARIO_HEADER, SCENARIO_HTTP_SC_FAILURE);
       match cbClient.get("/hello", request) {
            http:Response res => {
                responses[counter] = res;
            }
            http:HttpConnectorError httpConnectorError => {
                httpConnectorError.statusCode = 503;
                errs[counter] = httpConnectorError; 
            }
        }
       counter = counter + 1;
    }
    return (responses, errs);
}

int actualRequestNumber = 0;

public type MockClient {
    string serviceUri;
    http:ClientEndpointConfiguration config;
}

public function <MockClient client> post (string path, http:Request req) returns (http:Response | http:HttpConnectorError) {
    http:HttpConnectorError httpConnectorError;
    httpConnectorError.message = "Unsupported fuction for MockClient";
    return httpConnectorError;
}

public function <MockClient client> head (string path, http:Request req) returns (http:Response | http:HttpConnectorError) {
    http:HttpConnectorError httpConnectorError;
    httpConnectorError.message = "Unsupported fuction for MockClient";
    return httpConnectorError;
}

public function <MockClient client> put (string path, http:Request req) returns (http:Response | http:HttpConnectorError) {
    http:HttpConnectorError httpConnectorError;
    httpConnectorError.message = "Unsupported fuction for MockClient";
    return httpConnectorError;
}

public function <MockClient client> execute (string httpVerb, string path, http:Request req) returns (http:Response | http:HttpConnectorError) {
    http:HttpConnectorError httpConnectorError;
    httpConnectorError.message = "Unsupported fuction for MockClient";
    return httpConnectorError;
}

public function <MockClient client> patch (string path, http:Request req) returns (http:Response | http:HttpConnectorError) {
    http:HttpConnectorError httpConnectorError;
    httpConnectorError.message = "Unsupported fuction for MockClient";
    return httpConnectorError;
}

public function <MockClient client> delete (string path, http:Request req) returns (http:Response | http:HttpConnectorError) {
    http:HttpConnectorError httpConnectorError;
    httpConnectorError.message = "Unsupported fuction for MockClient";
    return httpConnectorError;
}

public function <MockClient client> get (string path, http:Request req) returns (http:Response | http:HttpConnectorError){
    http:Response response;
    http:HttpConnectorError err;
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

public function <MockClient client> options (string path, http:Request req) returns (http:Response | http:HttpConnectorError) {
    http:HttpConnectorError httpConnectorError = {};
    httpConnectorError.message = "Unsupported fuction for MockClient";
    return httpConnectorError;
}

public function <MockClient client> forward (string path, http:Request req) returns (http:Response | http:HttpConnectorError) {
    http:HttpConnectorError httpConnectorError = {};
    httpConnectorError.message = "Unsupported fuction for MockClient";
    return httpConnectorError;
}

public function <MockClient client> submit (string httpVerb, string path, http:Request req) returns (http:Response | http:HttpConnectorError) {
    http:HttpConnectorError httpConnectorError = {};
    httpConnectorError.message = "Unsupported fuction for MockClient";
    return httpConnectorError;
}

public function <MockClient client> getResponse (http:HttpHandle handle)  returns (http:Response | http:HttpConnectorError) {
    http:HttpConnectorError httpConnectorError = {};
    httpConnectorError.message = "Unsupported fuction for MockClient";
    return httpConnectorError;
}

public function <MockClient client> hasPromise (http:HttpHandle handle) returns (boolean) {
    return false;
}

public function <MockClient client> getNextPromise (http:HttpHandle handle) returns (http:PushPromise | http:HttpConnectorError) {
    http:HttpConnectorError httpConnectorError = {};
    httpConnectorError.message = "Unsupported fuction for MockClient";
    return httpConnectorError;
}

public function <MockClient client> getPromisedResponse (http:PushPromise promise) returns (http:Response | http:HttpConnectorError) {
    http:HttpConnectorError httpConnectorError = {};
    httpConnectorError.message = "Unsupported fuction for MockClient";
    return httpConnectorError;
}

public function <MockClient client> rejectPromise (http:PushPromise promise) returns (boolean) {
    return false;
}

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
    http:HttpConnectorError err = {};
    err.message = "Connection refused";
    err.statusCode = 502;
    return err;
}

function getResponse () returns (http:Response) {
    // TODO: The way the status code is set may need to be changed once struct fields can be made read-only
    http:Response response = {};
  //  MockInResponse response = {};
    response.statusCode = 200;
    return response;
}

public type MockInResponse {
    int statusCode;
    string reasonPhrase;
    string server;
}

function getMockErrorStruct () returns (http:HttpConnectorError) {
    http:HttpConnectorError err;
    err.message = "Internal Server Error.";
    err.statusCode = 500;
    return err;
}

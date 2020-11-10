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
import ballerina/io;
import ballerina/mime;
import ballerina/runtime;

const string TEST_SCENARIO_HEADER = "test-scenario";

const string SCENARIO_TYPICAL = "typical-scenario";
const string SCENARIO_TRIAL_RUN_FAILURE = "trial-run-failure";
const string SCENARIO_HTTP_SC_FAILURE = "http-status-code-failure";
const string SCENARIO_CB_FORCE_OPEN = "cb-force-open-scenario";
const string SCENARIO_CB_FORCE_CLOSE = "cb-force-close-scenario";
const string SCENARIO_REQUEST_VOLUME_THRESHOLD_SUCCESS = "request-volume-threshold-success-scenario";
const string SCENARIO_REQUEST_VOLUME_THRESHOLD_FAILURE = "request-volume-threshold-failure-scenario";

function testTypicalScenario() returns [http:Response[], error?[]] {
    actualRequestNumber = 0;
    MockClient mockClient = new("http://localhost:8080");
    http:Client backendClientEP = new("http://localhost:8080", {
        circuitBreaker: {
            rollingWindow: {
                timeWindowInMillis:10000,
                bucketSizeInMillis:2000,
                requestVolumeThreshold: 0
            },
            failureThreshold:0.3,
            resetTimeInMillis:1000,
            statusCodes:[400, 404, 500, 502, 503]
        },
        timeoutInMillis:2000
    });

    http:Response[] responses = [];
    error?[] errs = [];
    int counter = 0;
        while (counter < 8) {
            http:Request request = new;
            request.setHeader(TEST_SCENARIO_HEADER, SCENARIO_TYPICAL);
            http:CircuitBreakerClient tempClient = <http:CircuitBreakerClient>backendClientEP.httpClient;
            tempClient.httpClient = mockClient;
            var serviceResponse = backendClientEP->get("/hello", request);
            if (serviceResponse is http:Response) {
                responses[counter] = serviceResponse;
            } else {
                errs[counter] = serviceResponse;
            }
            counter = counter + 1;
            // To ensure the reset timeout period expires
            if (counter == 5) {
                runtime:sleep(5000);
            }
        }
    return [responses, errs];
}

function testTrialRunFailure() returns [http:Response[], error?[]] {
    actualRequestNumber = 0;
    MockClient mockClient = new("http://localhost:8080");
    http:Client backendClientEP = new("http://localhost:8080", {
        circuitBreaker: {
            rollingWindow: {
                timeWindowInMillis:10000,
                bucketSizeInMillis:2000,
                requestVolumeThreshold: 0
            },
            failureThreshold:0.3,
            resetTimeInMillis:1000,
            statusCodes:[400, 404, 500, 502, 503]
        },
        timeoutInMillis:2000
    });

    http:Response[] responses = [];
    error?[] errs = [];
    int counter = 0;

        while (counter < 8) {
            http:Request request = new;
            request.setHeader(TEST_SCENARIO_HEADER, SCENARIO_TRIAL_RUN_FAILURE);
            http:CircuitBreakerClient tempClient = <http:CircuitBreakerClient>backendClientEP.httpClient;
            tempClient.httpClient = mockClient;
            var serviceResponse = backendClientEP->get("/hello", request);
            if (serviceResponse is http:Response) {
                responses[counter] = serviceResponse;
            } else {
                errs[counter] = serviceResponse;
            }
            counter = counter + 1;
            // To ensure the reset timeout period expires
            if (counter == 5) {
                runtime:sleep(5000);
            }
        }
    return [responses, errs];
}

function testHttpStatusCodeFailure() returns [http:Response[], error?[]] {
    actualRequestNumber = 0;
    MockClient mockClient = new("http://localhost:8080");
    http:Client backendClientEP = new("http://localhost:8080", {
        circuitBreaker: {
            rollingWindow: {
                timeWindowInMillis:10000,
                bucketSizeInMillis:2000,
                requestVolumeThreshold: 0
            },
            failureThreshold:0.3,
            resetTimeInMillis:1000,
            statusCodes:[400, 404, 500, 502, 503]
        },
        timeoutInMillis:2000
    });

    http:Response[] responses = [];
    error?[] errs = [];
    int counter = 0;
        while (counter < 8) {
            http:Request request = new;
            request.setHeader(TEST_SCENARIO_HEADER, SCENARIO_HTTP_SC_FAILURE);
            http:CircuitBreakerClient tempClient = <http:CircuitBreakerClient>backendClientEP.httpClient;
            tempClient.httpClient = mockClient;
            var serviceResponse = backendClientEP->get("/hello", request);
            if (serviceResponse is http:Response) {
                responses[counter] = serviceResponse;
            } else {
                errs[counter] = serviceResponse;
            }
            counter = counter + 1;
        }
    return [responses, errs];
}

function testForceOpenScenario() returns [http:Response[], error?[]] {
    actualRequestNumber = 0;
    MockClient mockClient = new("http://localhost:8080");
    http:Client backendClientEP = new("http://localhost:8080", {
        circuitBreaker: {
            rollingWindow: {
                timeWindowInMillis:10000,
                bucketSizeInMillis:2000,
                requestVolumeThreshold: 0
            },
            failureThreshold:0.3,
            resetTimeInMillis:1000,
            statusCodes:[500, 502, 503]
        },
        timeoutInMillis:2000
    });

    http:Response[] responses = [];
    error?[] errs = [];
    int counter = 0;
    while (counter < 8) {
        http:Request request = new;
        request.setHeader(TEST_SCENARIO_HEADER, SCENARIO_CB_FORCE_OPEN);
        if (counter > 3) {
            backendClientEP.httpClient = getForcedOpenCircuitBreakerClient(backendClientEP.httpClient);
        }
        http:CircuitBreakerClient tempClient = <http:CircuitBreakerClient>backendClientEP.httpClient;
        tempClient.httpClient = mockClient;
        var serviceResponse = backendClientEP->get("/hello", request);
        if (serviceResponse is http:Response) {
            responses[counter] = serviceResponse;
        } else {
            errs[counter] = serviceResponse;
        }
        counter = counter + 1;
    }
    return [responses, errs];
}

function testForceCloseScenario() returns [http:Response[], error?[]] {
    actualRequestNumber = 0;
    MockClient mockClient = new("http://localhost:8080");
    http:Client backendClientEP = new("http://localhost:8080", {
        circuitBreaker: {
            rollingWindow: {
                timeWindowInMillis:10000,
                bucketSizeInMillis:2000,
                requestVolumeThreshold: 0
            },
            failureThreshold:0.3,
            resetTimeInMillis:1000,
            statusCodes:[500, 502, 503]
        },
        timeoutInMillis:2000
    });

    http:Response[] responses = [];
    error?[] errs = [];
    int counter = 0;

    while (counter < 8) {
        http:Request request = new;
        request.setHeader(TEST_SCENARIO_HEADER, SCENARIO_CB_FORCE_CLOSE);
        if (counter > 2) {
            backendClientEP.httpClient = getForcedCloseCircuitBreakerClient(backendClientEP.httpClient);
        }
        http:CircuitBreakerClient tempClient = <http:CircuitBreakerClient>backendClientEP.httpClient;
        tempClient.httpClient = mockClient;
        var serviceResponse = backendClientEP->get("/hello", request);
        if (serviceResponse is http:Response) {
            responses[counter] = serviceResponse;
        } else {
            errs[counter] = serviceResponse;
        }
        counter = counter + 1;
    }
    return [responses, errs];
}

function testRequestVolumeThresholdSuccessResponseScenario() returns [http:Response[], error?[]] {
    actualRequestNumber = 0;
    MockClient mockClient = new("http://localhost:8080");
    http:Client backendClientEP = new("http://localhost:8080", {
        circuitBreaker: {
            rollingWindow: {
                timeWindowInMillis:10000,
                bucketSizeInMillis:2000,
                requestVolumeThreshold: 6
            },
            failureThreshold:0.3,
            resetTimeInMillis:1000,
            statusCodes:[500, 502, 503]
        },
        timeoutInMillis:2000
    });

    http:Response[] responses = [];
    error?[] errs = [];
    int counter = 0;

    while (counter < 6) {
        http:Request request = new;
        request.setHeader(TEST_SCENARIO_HEADER, SCENARIO_REQUEST_VOLUME_THRESHOLD_SUCCESS);
        http:CircuitBreakerClient tempClient = <http:CircuitBreakerClient>backendClientEP.httpClient;
        tempClient.httpClient = mockClient;
        var serviceResponse = backendClientEP->get("/hello", request);
        if (serviceResponse is http:Response) {
            responses[counter] = serviceResponse;
        } else {
            errs[counter] = serviceResponse;
        }
        counter = counter + 1;
    }
    return [responses, errs];
}

function testRequestVolumeThresholdFailureResponseScenario() returns [http:Response[], error?[]] {
    actualRequestNumber = 0;
    MockClient mockClient = new("http://localhost:8080");
    http:Client backendClientEP = new("http://localhost:8080", {
        circuitBreaker: {
            rollingWindow: {
                timeWindowInMillis:10000,
                bucketSizeInMillis:2000,
                requestVolumeThreshold: 6
            },
            failureThreshold:0.3,
            resetTimeInMillis:1000,
            statusCodes:[500, 502, 503]
        },
        timeoutInMillis:2000
    });

    http:Response[] responses = [];
    error?[] errs = [];
    int counter = 0;

    while (counter < 6) {
        http:Request request = new;
        request.setHeader(TEST_SCENARIO_HEADER, SCENARIO_REQUEST_VOLUME_THRESHOLD_FAILURE);
        http:CircuitBreakerClient tempClient = <http:CircuitBreakerClient>backendClientEP.httpClient;
        tempClient.httpClient = mockClient;
        var serviceResponse = backendClientEP->get("/hello", request);
        if (serviceResponse is http:Response) {
            responses[counter] = serviceResponse;
        } else {
            errs[counter] = serviceResponse;
        }
        counter = counter + 1;
    }
    return [responses, errs];
}

function testInvalidRollingWindowConfiguration() returns error? {
    var backendClientEP = trap new http:Client("http://localhost:8080", {
        circuitBreaker: {
            rollingWindow: {
                timeWindowInMillis: 2000,
                bucketSizeInMillis: 3000,
                requestVolumeThreshold: 0
            },
            failureThreshold:0.3,
            resetTimeInMillis:1000,
            statusCodes:[400, 404, 500, 502, 503]
        },
        timeoutInMillis:2000
    });
    if (backendClientEP is error) {
        return backendClientEP;
    }
}

int actualRequestNumber = 0;

public client class MockClient {
    public string url = "";
    public http:ClientConfiguration config = {};
    public http:HttpClient httpClient;

    public function init(string url, http:ClientConfiguration? config = ()) {
        http:HttpClient simpleClient = new(url);
        self.url = url;
        self.config = config ?: {};
        self.httpClient = simpleClient;
    }

    public remote function post(string path,
                           http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|() message)
                                                                                returns http:Response|http:ClientError {
        return getUnsupportedError();
    }

    public remote function head(string path,
                           http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|() message = ())
                                                                                returns http:Response|http:ClientError {
        return getUnsupportedError();
    }

    public remote function put(string path,
                               http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|() message)
                                                                                returns http:Response|http:ClientError {
        return getUnsupportedError();
    }

    public remote function execute(string httpVerb, string path,
                                   http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                        message) returns http:Response|http:ClientError {
        return getUnsupportedError();
    }

    public remote function patch(string path,
                           http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|() message)
                                                                                returns http:Response|http:ClientError {
        return getUnsupportedError();
    }

    public remote function delete(string path,
                           http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|() message = ())
                                                                                returns http:Response|http:ClientError {
        return getUnsupportedError();
    }

    public remote function get(string path,
                           http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|() message = ())
                                                                                returns http:Response|http:ClientError {
        http:Request req = buildRequest(message);
        http:Response response = new;
        actualRequestNumber = actualRequestNumber + 1;
        string scenario = req.getHeader(TEST_SCENARIO_HEADER);

        if (scenario == SCENARIO_TYPICAL) {
            var result = handleBackendFailureScenario(actualRequestNumber);
            if (result is http:Response) {
                response = result;
            } else {
                string errMessage = result.message();
                response.statusCode = http:STATUS_INTERNAL_SERVER_ERROR;
                response.setTextPayload(errMessage);
            }
        } else if (scenario == SCENARIO_TRIAL_RUN_FAILURE) {
            var result = handleTrialRunFailureScenario(actualRequestNumber);
            if (result is http:Response) {
                response = result;
            } else {
                string errMessage = result.message();
                response.statusCode = http:STATUS_INTERNAL_SERVER_ERROR;
                response.setTextPayload(errMessage);
            }
        } else if (scenario == SCENARIO_HTTP_SC_FAILURE) {
            var result = handleHTTPStatusCodeErrorScenario(actualRequestNumber);
            if (result is http:Response) {
                response = result;
            } else {
                string errMessage = result.message();
                response.statusCode = http:STATUS_INTERNAL_SERVER_ERROR;
                response.setTextPayload(errMessage);
            }
        } else if (scenario == SCENARIO_CB_FORCE_OPEN) {
            response = handleCBForceOpenScenario();
        } else if (scenario == SCENARIO_CB_FORCE_CLOSE) {
            var result = handleCBForceCloseScenario(actualRequestNumber);
            if (result is http:Response) {
                response = result;
            } else {
                string errMessage = result.message();
                response.statusCode = http:STATUS_INTERNAL_SERVER_ERROR;
                response.setTextPayload(errMessage);
            }
        } else if (scenario == SCENARIO_REQUEST_VOLUME_THRESHOLD_SUCCESS) {
            response = handleRequestVolumeThresholdSuccessResponseScenario();
        } else if (scenario == SCENARIO_REQUEST_VOLUME_THRESHOLD_FAILURE) {
            response = handleRequestVolumeThresholdFailureResponseScenario();
        }
        return response;
    }

    public remote function options(string path,
           http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|() message = ())
                                                                                returns http:Response|http:ClientError {
        return getUnsupportedError();
    }

    public remote function forward(string path, http:Request req) returns http:Response|http:ClientError {
        return getUnsupportedError();
    }

    public remote function submit(string httpVerb, string path,
                           http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|() message)
                                                                            returns http:HttpFuture|http:ClientError {
        return getUnsupportedError();
    }

    public remote function getResponse(http:HttpFuture httpFuture)  returns http:Response|http:ClientError {
        return getUnsupportedError();
    }

    public remote function hasPromise(http:HttpFuture httpFuture) returns boolean {
        return false;
    }

    public remote function getNextPromise(http:HttpFuture httpFuture) returns http:PushPromise|http:ClientError {
        return getUnsupportedError();
    }

    public remote function getPromisedResponse(http:PushPromise promise) returns http:Response|http:ClientError {
        return getUnsupportedError();
    }

    public remote function rejectPromise(http:PushPromise promise) {
    }
}

function handleBackendFailureScenario(int requesetNo) returns http:Response|http:ClientError {
    // Deliberately fail a request
    if (requesetNo == 3) {
        return getErrorStruct();
    }
    http:Response response = getResponse();
    return response;
}

function handleTrialRunFailureScenario(int counter) returns http:Response|http:ClientError {
    // Fail a request. Then, fail the trial request sent while in the HALF_OPEN state as well.
    if (counter == 2 || counter == 3) {
        return getErrorStruct();
    }

    http:Response response = getResponse();
    return response;
}

function handleHTTPStatusCodeErrorScenario(int counter) returns http:Response|http:ClientError {
    // Fail a request. Then, fail the trial request sent while in the HALF_OPEN state as well.
    if (counter == 2 || counter == 3) {
        return getMockErrorStruct();
    }

    http:Response response = getResponse();
    return response;
}

function handleCBForceOpenScenario() returns http:Response {
    return getResponse();
}

function handleCBForceCloseScenario(int requestNo) returns http:Response|http:ClientError {
    // Deliberately fail a request
    if (requestNo == 3) {
        return getErrorStruct();
    }
    http:Response response = getResponse();
    return response;
}

function handleRequestVolumeThresholdSuccessResponseScenario() returns http:Response {
    return getResponse();
}

function handleRequestVolumeThresholdFailureResponseScenario() returns http:Response {
    http:Response response = new;
    response.statusCode = http:STATUS_INTERNAL_SERVER_ERROR;
    return response;
}

function getErrorStruct() returns http:ClientError {
    return http:GenericClientError("Connection refused");
}

function getResponse() returns http:Response {
    // TODO: The way the status code is set may need to be changed once struct fields can be made read-only
    http:Response response = new;
    response.statusCode = http:STATUS_OK;
    return response;
}

function getMockErrorStruct() returns http:ClientError {
    return http:GenericClientError("Internal Server Error");
}

function getUnsupportedError() returns http:ClientError {
    return http:GenericClientError("Unsupported fucntion for MockClient");
}

function buildRequest(http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|() message) returns
http:Request {
    http:Request request = new;
    if (message is ()) {
        return request;
    } else if (message is http:Request) {
        request = message;
    } else if (message is string) {
        request.setTextPayload(message);
    } else if (message is xml) {
        request.setXmlPayload(message);
    } else if (message is byte[]) {
        request.setBinaryPayload(message);
    } else if (message is json) {
        request.setJsonPayload(message);
    } else if (message is io:ReadableByteChannel) {
        request.setByteChannel(message);
    } else {
        request.setBodyParts(message);
    }
    return request;
}

listener http:MockListener mockEP = new(9090);

http:Client clientEP = new("http://localhost:8080", {
    circuitBreaker: {
        rollingWindow: {
            timeWindowInMillis: 10000,
            bucketSizeInMillis: 2000
        },
        failureThreshold: 0.3,
        resetTimeInMillis: 1000,
        statusCodes: [500, 502, 503]
    },
    timeoutInMillis: 2000
});

@http:ServiceConfig { basePath: "/cb" }
service circuitBreakerService on mockEP {

    @http:ResourceConfig {
        path: "/getState"
    }
    resource function getState(http:Caller caller, http:Request req) {
        http:CircuitBreakerClient cbClient = <http:CircuitBreakerClient>clientEP.httpClient;
        http:CircuitState currentState = cbClient.getCurrentState();
        http:Response res = new;
        if (currentState == http:CB_CLOSED_STATE) {
            res.setPayload(<@untainted string> "Circuit Breaker is in CLOSED state");
            checkpanic caller->respond(res);
        }
    }
}

function getForcedOpenCircuitBreakerClient(http:HttpClient httpClient) returns http:CircuitBreakerClient {
    http:CircuitBreakerClient cbClient = <http:CircuitBreakerClient>httpClient;
    cbClient.forceOpen();
    return cbClient;
}

function getForcedCloseCircuitBreakerClient(http:HttpClient httpClient) returns http:CircuitBreakerClient {
    http:CircuitBreakerClient cbClient = <http:CircuitBreakerClient>httpClient;
    cbClient.forceClose();
    return cbClient;
}

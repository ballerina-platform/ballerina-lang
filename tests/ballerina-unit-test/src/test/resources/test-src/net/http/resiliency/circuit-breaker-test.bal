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

@final string TEST_SCENARIO_HEADER = "test-scenario";

@final string SCENARIO_TYPICAL = "typical-scenario";
@final string SCENARIO_TRIAL_RUN_FAILURE = "trial-run-failure";
@final string SCENARIO_HTTP_SC_FAILURE = "http-status-code-failure";
@final string SCENARIO_CB_FORCE_OPEN = "cb-force-open-scenario";
@final string SCENARIO_CB_FORCE_CLOSE = "cb-force-close-scenario";
@final string SCENARIO_REQUEST_VOLUME_THRESHOLD_SUCCESS = "request-volume-threshold-success-scenario";
@final string SCENARIO_REQUEST_VOLUME_THRESHOLD_FAILURE = "request-volume-threshold-failure-scenario";

function testTypicalScenario() returns (http:Response[], error[]) {
    endpoint http:Client backendClientEP {
        url: "http://localhost:8080",
        circuitBreaker: {
            rollingWindow: {
                timeWindowMillis:10000,
                bucketSizeMillis:2000,
                requestVolumeThreshold: 0
            },
            failureThreshold:0.3,
            resetTimeMillis:1000,
            statusCodes:[400, 404, 500, 502, 503]
        },
        timeoutMillis:2000
    };

    http:Response[] responses = [];
    error[] errs = [];
    int counter = 0;
    var cbClient = <http:CircuitBreakerClient>backendClientEP.getCallerActions();

    if (cbClient is http:CircuitBreakerClient) {
        MockClient mockClient = new;
        cbClient.httpClient = <http:CallerActions> mockClient;
        while (counter < 8) {
            http:Request request = new;
            request.setHeader(TEST_SCENARIO_HEADER, SCENARIO_TYPICAL);
            var serviceResponse = cbClient.get("/hello", message = request);
            if (serviceResponse is http:Response) {
                responses[counter] = serviceResponse;
            } else if (serviceResponse is error) {
                errs[counter] = serviceResponse;
            }
            counter = counter + 1;
            // To ensure the reset timeout period expires
            if (counter == 5) {
                runtime:sleep(5000);
            }
        }
    } else {
        panic cbClient;
    }
    return (responses, errs);
}

function testTrialRunFailure() returns (http:Response[], error[]) {
    endpoint http:Client backendClientEP {
        url: "http://localhost:8080",
        circuitBreaker: {
            rollingWindow: {
                timeWindowMillis:10000,
                bucketSizeMillis:2000,
                requestVolumeThreshold: 0
            },
            failureThreshold:0.3,
            resetTimeMillis:1000,
            statusCodes:[400, 404, 500, 502, 503]
        },
        timeoutMillis:2000
    };

    http:Response[] responses = [];
    error[] errs = [];
    int counter = 0;
    var cbClient = <http:CircuitBreakerClient>backendClientEP.getCallerActions();

    if (cbClient is http:CircuitBreakerClient) {
        MockClient mockClient = new;
        cbClient.httpClient = <http:CallerActions> mockClient;
        while (counter < 8) {
            http:Request request = new;
            request.setHeader(TEST_SCENARIO_HEADER, SCENARIO_TRIAL_RUN_FAILURE);
            var serviceResponse = cbClient.get("/hello", message = request);
            if (serviceResponse is http:Response) {
                responses[counter] = serviceResponse;
            } else if (serviceResponse is error) {
                errs[counter] = serviceResponse;
            }
            counter = counter + 1;
            // To ensure the reset timeout period expires
            if (counter == 5) {
                runtime:sleep(5000);
            }
        }
    } else {
        panic cbClient;
    }
    return (responses, errs);
}

function testHttpStatusCodeFailure() returns (http:Response[], error[]) {
    endpoint http:Client backendClientEP {
        url: "http://localhost:8080",
        circuitBreaker: {
            rollingWindow: {
                timeWindowMillis:10000,
                bucketSizeMillis:2000,
                requestVolumeThreshold: 0
            },
            failureThreshold:0.3,
            resetTimeMillis:1000,
            statusCodes:[400, 404, 500, 502, 503]
        },
        timeoutMillis:2000
    };

    http:Response[] responses = [];
    error[] errs = [];
    int counter = 0;
    var cbClient = <http:CircuitBreakerClient>backendClientEP.getCallerActions();

    if (cbClient is http:CircuitBreakerClient) {
        MockClient mockClient = new;
        cbClient.httpClient = <http:CallerActions> mockClient;
        while (counter < 8) {
            http:Request request = new;
            request.setHeader(TEST_SCENARIO_HEADER, SCENARIO_HTTP_SC_FAILURE);
            var serviceResponse = cbClient.get("/hello", message = request);
            if (serviceResponse is http:Response) {
                responses[counter] = serviceResponse;
            } else if (serviceResponse is error) {
                errs[counter] = serviceResponse;
            }
            counter = counter + 1;
        }
    } else {
        panic cbClient;
    }
    return (responses, errs);
}

function testForceOpenScenario() returns (http:Response[], error[]) {
    endpoint http:Client backendClientEP {
        url: "http://localhost:8080",
        circuitBreaker: {
            rollingWindow: {
                timeWindowMillis:10000,
                bucketSizeMillis:2000,
                requestVolumeThreshold: 0
            },
            failureThreshold:0.3,
            resetTimeMillis:1000,
            statusCodes:[500, 502, 503]
        },
        timeoutMillis:2000
    };

    http:Response[] responses = [];
    error[] errs = [];
    int counter = 0;
    var cbClient = <http:CircuitBreakerClient>backendClientEP.getCallerActions();

    if (cbClient is http:CircuitBreakerClient) {
        MockClient mockClient = new;
        cbClient.httpClient = <http:CallerActions> mockClient;
        while (counter < 8) {
            http:Request request = new;
            request.setHeader(TEST_SCENARIO_HEADER, SCENARIO_CB_FORCE_OPEN);
            if (counter > 3) {
                cbClient.forceOpen();
            }
            var serviceResponse = cbClient.get("/hello", message = request);
            if (serviceResponse is http:Response) {
                responses[counter] = serviceResponse;
            } else if (serviceResponse is error) {
                errs[counter] = serviceResponse;
            }
            counter = counter + 1;
        }
    } else {
        panic cbClient;
    }
    return (responses, errs);
}

function testForceCloseScenario() returns (http:Response[], error[]) {
    endpoint http:Client backendClientEP {
        url: "http://localhost:8080",
        circuitBreaker: {
            rollingWindow: {
                timeWindowMillis:10000,
                bucketSizeMillis:2000,
                requestVolumeThreshold: 0
            },
            failureThreshold:0.3,
            resetTimeMillis:1000,
            statusCodes:[500, 502, 503]
        },
        timeoutMillis:2000
    };

    http:Response[] responses = [];
    error[] errs = [];
    int counter = 0;
    var cbClient = <http:CircuitBreakerClient>backendClientEP.getCallerActions();

    if (cbClient is http:CircuitBreakerClient) {
        MockClient mockClient = new;
        cbClient.httpClient = <http:CallerActions> mockClient;
        while (counter < 8) {
            http:Request request = new;
            request.setHeader(TEST_SCENARIO_HEADER, SCENARIO_CB_FORCE_CLOSE);
            if (counter > 2) {
                cbClient.forceClose();
            }
            var serviceResponse = cbClient.get("/hello", message = request);
            if (serviceResponse is http:Response) {
                responses[counter] = serviceResponse;
            } else if (serviceResponse is error) {
                errs[counter] = serviceResponse;
            }
            counter = counter + 1;
        }
    } else {
        panic cbClient;
    }
    return (responses, errs);
}

function testRequestVolumeThresholdSuccessResponseScenario() returns (http:Response[], error[]) {
    endpoint http:Client backendClientEP {
        url: "http://localhost:8080",
        circuitBreaker: {
            rollingWindow: {
                timeWindowMillis:10000,
                bucketSizeMillis:2000,
                requestVolumeThreshold: 6
            },
            failureThreshold:0.3,
            resetTimeMillis:1000,
            statusCodes:[500, 502, 503]
        },
        timeoutMillis:2000
    };

    http:Response[] responses = [];
    error[] errs = [];
    int counter = 0;
    var cbClient = <http:CircuitBreakerClient>backendClientEP.getCallerActions();

    if (cbClient is http:CircuitBreakerClient) {
        MockClient mockClient = new;
        cbClient.httpClient = <http:CallerActions> mockClient;
        while (counter < 6) {
            http:Request request = new;
            request.setHeader(TEST_SCENARIO_HEADER, SCENARIO_REQUEST_VOLUME_THRESHOLD_SUCCESS);
            var serviceResponse = cbClient.get("/hello", message = request);
            if (serviceResponse is http:Response) {
                responses[counter] = serviceResponse;
            } else if (serviceResponse is error) {
                errs[counter] = serviceResponse;
            }
            counter = counter + 1;
        }
    } else {
        panic cbClient;
    }
    return (responses, errs);
}

function testRequestVolumeThresholdFailureResponseScenario() returns (http:Response[], error[]) {
    endpoint http:Client backendClientEP {
        url: "http://localhost:8080",
        circuitBreaker: {
            rollingWindow: {
                timeWindowMillis:10000,
                bucketSizeMillis:2000,
                requestVolumeThreshold: 6
            },
            failureThreshold:0.3,
            resetTimeMillis:1000,
            statusCodes:[500, 502, 503]
        },
        timeoutMillis:2000
    };

    http:Response[] responses = [];
    error[] errs = [];
    int counter = 0;
    var cbClient = <http:CircuitBreakerClient>backendClientEP.getCallerActions();

    if (cbClient is http:CircuitBreakerClient) {
        MockClient mockClient = new;
        cbClient.httpClient = <http:CallerActions> mockClient;
        while (counter < 6) {
            http:Request request = new;
            request.setHeader(TEST_SCENARIO_HEADER, SCENARIO_REQUEST_VOLUME_THRESHOLD_FAILURE);
            var serviceResponse = cbClient.get("/hello", message = request);
            if (serviceResponse is http:Response) {
                responses[counter] = serviceResponse;
            } else if (serviceResponse is error) {
                errs[counter] = serviceResponse;
            }
            counter = counter + 1;
        }
    } else {
        panic cbClient;
    }
    return (responses, errs);
}

int actualRequestNumber = 0;

public type MockClient object {
    public string serviceUri = "";
    public http:ClientEndpointConfig config = {};

    public function post(string path, http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                        message) returns http:Response|error {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public function head(string path, http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                        message = ()) returns http:Response|error {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public function put(string path, http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                        message) returns http:Response|error {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public function execute(string httpVerb, string path, http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                        message) returns http:Response|error {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public function patch(string path, http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                            message) returns http:Response|error {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public function delete(string path, http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                            message) returns http:Response|error {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public function get(string path, http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                            message = ()) returns http:Response|error {
        http:Request req = buildRequest(message);
        http:Response response = new;
        actualRequestNumber = actualRequestNumber + 1;
        string scenario = req.getHeader(TEST_SCENARIO_HEADER);

        if (scenario == SCENARIO_TYPICAL) {
            match handleBackendFailureScenario(actualRequestNumber) {
                http:Response res => {
                    response = res;
                }
                error httpConnectorError => {
                    string errMessage = httpConnectorError.reason();
                    response.statusCode = http:INTERNAL_SERVER_ERROR_500;
                    response.setTextPayload(errMessage);
                }
            }
        } else if (scenario == SCENARIO_TRIAL_RUN_FAILURE) {
            match  handleTrialRunFailureScenario(actualRequestNumber) {
                http:Response res => {
                    response = res;
                }
                error httpConnectorError => {
                    string errMessage = httpConnectorError.reason();
                    response.statusCode = http:INTERNAL_SERVER_ERROR_500;
                    response.setTextPayload(errMessage);
                }
            }
        } else if (scenario == SCENARIO_HTTP_SC_FAILURE) {
            match  handleHTTPStatusCodeErrorScenario(actualRequestNumber) {
                http:Response res => {
                    response = res;
                }
                error httpConnectorError => {
                    string errMessage = httpConnectorError.reason();
                    response.statusCode = http:INTERNAL_SERVER_ERROR_500;
                    response.setTextPayload(errMessage);
                }
            }
        } else if (scenario == SCENARIO_CB_FORCE_OPEN) {
            response = handleCBForceOpenScenario();
        } else if (scenario == SCENARIO_CB_FORCE_CLOSE) {
            match  handleCBForceCloseScenario(actualRequestNumber) {
                http:Response res => {
                    response = res;
                }
                error httpConnectorError => {
                    string errMessage = httpConnectorError.reason();
                    response.statusCode = http:INTERNAL_SERVER_ERROR_500;
                    response.setTextPayload(errMessage);
                }
            }
        } else if (scenario == SCENARIO_REQUEST_VOLUME_THRESHOLD_SUCCESS) {
            response = handleRequestVolumeThresholdSuccessResponseScenario();
        } else if (scenario == SCENARIO_REQUEST_VOLUME_THRESHOLD_FAILURE) {
            response = handleRequestVolumeThresholdFailureResponseScenario();
        }
        return response;
    }

    public function options(string path, http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                            message = ()) returns http:Response|error {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public function forward(string path, http:Request req) returns http:Response|error {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public function submit(string httpVerb, string path, http:Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                                            message) returns http:HttpFuture|error {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public function getResponse(http:HttpFuture httpFuture)  returns http:Response|error {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public function hasPromise(http:HttpFuture httpFuture) returns boolean {
        return false;
    }

    public function getNextPromise(http:HttpFuture httpFuture) returns http:PushPromise|error {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public function getPromisedResponse(http:PushPromise promise) returns http:Response|error {
        error httpConnectorError = error("Unsupported fuction for MockClient");
        return httpConnectorError;
    }

    public function rejectPromise(http:PushPromise promise) {
    }
};

function handleBackendFailureScenario(int requesetNo) returns http:Response|error {
    // Deliberately fail a request
    if (requesetNo == 3) {
        error err = getErrorStruct();
        return err;
    }
    http:Response response = getResponse();
    return response;
}

function handleTrialRunFailureScenario(int counter) returns http:Response|error {
    // Fail a request. Then, fail the trial request sent while in the HALF_OPEN state as well.
    if (counter == 2 || counter == 3) {
        error err = getErrorStruct();
        return err;
    }

    http:Response response = getResponse();
    return response;
}

function handleHTTPStatusCodeErrorScenario(int counter) returns http:Response|error {
    // Fail a request. Then, fail the trial request sent while in the HALF_OPEN state as well.
    if (counter == 2 || counter == 3) {
        error err = getMockErrorStruct();
        return err;
    }

    http:Response response = getResponse();
    return response;
}

function handleCBForceOpenScenario() returns http:Response {
    return getResponse();
}

function handleCBForceCloseScenario(int requestNo) returns http:Response|error {
    // Deliberately fail a request
    if (requestNo == 3) {
        error err = getErrorStruct();
        return err;
    }
    http:Response response = getResponse();
    return response;
}

function handleRequestVolumeThresholdSuccessResponseScenario() returns http:Response {
    return getResponse();
}

function handleRequestVolumeThresholdFailureResponseScenario() returns http:Response {
    http:Response response = new;
    response.statusCode = http:INTERNAL_SERVER_ERROR_500;
    return response;
}

function getErrorStruct() returns error {
    error err = error("Connection refused");
    return err;
}

function getResponse() returns http:Response {
    // TODO: The way the status code is set may need to be changed once struct fields can be made read-only
    http:Response response = new;
    response.statusCode = http:OK_200;
    return response;
}

function getMockErrorStruct() returns error {
    error err = error("Internal Server Error");
    return err;
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
    } else if (message is json) {
        request.setJsonPayload(message);
    } else if (message is byte[]) {
        request.setBinaryPayload(message);
    } else if (message is io:ReadableByteChannel) {
        request.setByteChannel(message);
    } else if (message is mime:Entity[]) {
        request.setBodyParts(message);
    }
    return request;
}

endpoint http:NonListener mockEP {
    port: 9090
};

endpoint http:Client clientEP {
    url: "http://localhost:8080",
    circuitBreaker: {
        rollingWindow: {
            timeWindowMillis: 10000,
            bucketSizeMillis: 2000
        },
        failureThreshold: 0.3,
        resetTimeMillis: 1000,
        statusCodes: [500, 502, 503]
    },
    timeoutMillis: 2000
};

@http:ServiceConfig { basePath: "/cb" }
service<http:Service> circuitBreakerService bind mockEP {

    @http:ResourceConfig {
        path: "/getState"
    }
    getState(endpoint caller, http:Request req) {
        var cbClient = <http:CircuitBreakerClient>clientEP.getCallerActions();
        if (cbClient is http:CircuitBreakerClient) {
            http:CircuitState currentState = cbClient.getCurrentState();
            http:Response res = new;
            if (currentState == http:CB_CLOSED_STATE) {
                res.setPayload("Circuit Breaker is in CLOSED state");
                _ = caller->respond(res);
            }
        } else if (cbClient is error) {
            panic cbClient;
        }
    }
}

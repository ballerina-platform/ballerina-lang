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

import ballerina.net.http;
import ballerina.net.http.resiliency;
import ballerina.runtime;

const string TEST_SCENARIO_HEADER = "test-scenario";

const string SCENARIO_TYPICAL = "typical-scenario";
const string SCENARIO_TRIAL_RUN_FAILURE = "trial-run-failure";
const string SCENARIO_HTTP_SC_FAILURE = "http-status-code-failure";

int[] errorCodes = [400, 500, 404];
resiliency:CircuitBreakerConfig circuitBreakerConfig = {failureThreshold:0.1, resetTimeout:20000, httpStatusCodes:errorCodes};

function testTypicalScenario () (http:Response[], http:HttpConnectorError[]) {

    endpoint<resiliency:CircuitBreaker> circuitBreakerEP {
        create resiliency:CircuitBreaker((http:HttpClient)create MockHttpClient("http://localhost:8080", {}), circuitBreakerConfig);
    }

    http:Request request;
    http:Response[] responses = [];
    http:HttpConnectorError[] errs = [];
    int counter = 0;

    while (counter < 8) {
        request = {};
        request.setHeader(TEST_SCENARIO_HEADER, SCENARIO_TYPICAL);
        responses[counter], errs[counter] = circuitBreakerEP.get("/hello", request);
        counter = counter + 1;

        // To ensure the reset timeout period expires
        if (counter == 5) {
            runtime:sleepCurrentWorker(5000);
        }
    }

    return responses, errs;
}

function testTrialRunFailure () (http:Response[], http:HttpConnectorError[]) {

    endpoint<resiliency:CircuitBreaker> circuitBreakerEP {
        create resiliency:CircuitBreaker((http:HttpClient)create MockHttpClient("http://localhost:8080", {}), circuitBreakerConfig);
    }

    http:Request request;
    http:Response[] responses = [];
    http:HttpConnectorError[] errs = [];
    int counter = 0;

    while (counter < 6) {
        request = {};
        request.setHeader(TEST_SCENARIO_HEADER, SCENARIO_TRIAL_RUN_FAILURE);
        responses[counter], errs[counter] = circuitBreakerEP.get("/hello", request);
        counter = counter + 1;

        if (counter == 3) {
            runtime:sleepCurrentWorker(5000);
        }
    }

    return responses, errs;
}

function testHttpStatusCodeFailure () (http:Response[], http:HttpConnectorError[]) {

    endpoint<resiliency:CircuitBreaker> circuitBreakerEP {
        create resiliency:CircuitBreaker((http:HttpClient)create MockHttpClient("http://localhost:8080", {}), circuitBreakerConfig);
    }

    http:Request request;
    http:Response[] responses = [];
    http:HttpConnectorError[] errs = [];
    int counter = 0;

    while (counter < 6) {
        request = {};
        request.setHeader(TEST_SCENARIO_HEADER, SCENARIO_HTTP_SC_FAILURE);
        responses[counter], errs[counter] = circuitBreakerEP.get("/hello", request);
        counter = counter + 1;

        if (counter == 1) {
            runtime:sleepCurrentWorker(5000);
        }
    }

    return responses, errs;
}

connector MockHttpClient (string serviceUri, http:Options connectorOptions) {

    int actualRequestNumber = 0;

    action post (string path, http:Request req) (http:Response, http:HttpConnectorError) {
        return null, null;
    }

    action head (string path, http:Request req) (http:Response, http:HttpConnectorError) {
        return null, null;
    }

    action put (string path, http:Request req) (http:Response, http:HttpConnectorError) {
        return null, null;
    }

    action execute (string httpVerb, string path, http:Request req) (http:Response, http:HttpConnectorError) {
        return null, null;
    }

    action patch (string path, http:Request req) (http:Response, http:HttpConnectorError) {
        return null, null;
    }

    action delete (string path, http:Request req) (http:Response, http:HttpConnectorError) {
        return null, null;
    }

    action get (string path, http:Request req) (http:Response, http:HttpConnectorError) {
        http:Response response;
        http:HttpConnectorError err;
        actualRequestNumber = actualRequestNumber + 1;

        string scenario = req.getHeader(TEST_SCENARIO_HEADER);

        if (scenario == SCENARIO_TYPICAL) {
            response, err = handleBackendFailureScenario(actualRequestNumber);
        } else if (scenario == SCENARIO_TRIAL_RUN_FAILURE) {
            response, err = handleTrialRunFailureScenario(actualRequestNumber);
        } else if (scenario == SCENARIO_HTTP_SC_FAILURE) {
            response, err = handleHTTPStatusCodeErrorScenario(actualRequestNumber);
        }

        return response, err;
    }

    action options (string path, http:Request req) (http:Response, http:HttpConnectorError) {
        return null, null;
    }

    action forward (string path, http:Request req) (http:Response, http:HttpConnectorError) {
        return null, null;
    }

    action submit (string httpVerb, string path, http:Request req) (http:HttpHandle, http:HttpConnectorError) {
        return null, null;
    }

    action getResponse (http:HttpHandle handle) (http:Response, http:HttpConnectorError) {
        return null, null;
    }

    action hasPromise (http:HttpHandle handle) (boolean) {
        return false;
    }

    action getNextPromise (http:HttpHandle handle) (http:PushPromise, http:HttpConnectorError) {
        return null, null;
    }

    action getPromisedResponse (http:PushPromise promise) (http:Response, http:HttpConnectorError) {
        return null, null;
    }

    action rejectPromise (http:PushPromise promise) (boolean) {
        return false;
    }
}

function handleBackendFailureScenario (int requesetNo) (http:Response, http:HttpConnectorError) {
    // Deliberately fail a request
    if (requesetNo == 3) {
        http:HttpConnectorError err = getErrorStruct();
        return null, err;
    }

    http:Response response = getResponse();
    return response, null;
}

function handleTrialRunFailureScenario (int counter) (http:Response, http:HttpConnectorError) {
    // Fail a request. Then, fail the trial request sent while in the HALF_OPEN state as well.
    if (counter == 2 || counter == 3) {
        http:HttpConnectorError err = getErrorStruct();
        return null, err;
    }

    http:Response response = getResponse();
    return response, null;
}

function handleHTTPStatusCodeErrorScenario (int counter) (http:Response, http:HttpConnectorError) {
    // Fail a request. Then, fail the trial request sent while in the HALF_OPEN state as well.
    if (counter == 2 || counter == 3) {
        http:HttpConnectorError err = getMockErrorStruct();
        return null, err;
    }

    http:Response response = getResponse();
    return response, null;
}

function getErrorStruct () (http:HttpConnectorError) {
    http:HttpConnectorError err = {};
    err.message = "Connection refused";
    err.statusCode = 502;
    return err;
}

function getResponse () (http:Response) {
    // TODO: The way the status code is set may need to be changed once struct fields can be made read-only
    http:Response response = {};
  //  MockInResponse response = {};
    response.statusCode = 200;
    return response;
}

public struct MockInResponse {
    int statusCode;
    string reasonPhrase;
    string server;
}

function getMockErrorStruct () (http:HttpConnectorError) {
    http:HttpConnectorError err = {};
    err.message = "Internal Server Error.";
    err.statusCode = 500;
    return err;
}
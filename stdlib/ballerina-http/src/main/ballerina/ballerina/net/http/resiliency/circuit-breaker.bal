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

package ballerina.net.http.resiliency;

import ballerina.log;
import ballerina.net.http;
import ballerina.time;

enum CircuitState {
    OPEN, CLOSED, HALF_OPEN
}

struct CircuitHealth {
    int requestCount;
    int errorCount;
    time:Time lastErrorTime;
}

@Description {value:"Represents Circuit Breaker connector configuration."}
@Field {value:"failureThreshold: The threshold for request failures. When this threshold is crossed, the circuit will trip. The threshold should be a value between 0 and 1."}
@Field {value:"resetTimeout: The time period to wait before attempting to make another request to the upstream service."}
@Field {value:"httpStatusCodes: Array of http response status codes which considered as failure responses."}
public struct CircuitBreakerConfig {
    float failureThreshold;
    int resetTimeout;
    int [] httpStatusCodes;
}

struct CircuitBreakerInferredConfig {
    float failureThreshold;
    int resetTimeout;
    boolean [] httpStatusCodes;
}

CircuitHealth circuitHealth = {};
CircuitState currentCircuitState;

@Description {value:"Circuit Breaker implementation for to be used with the HTTP client connector to gracefully handle network errors."}
@Param {value:"circuitBreakerConfig: Circuit Breaker configuration struct which contains the circuit handling options."}
public connector CircuitBreaker (http:HttpClient httpClient, CircuitBreakerConfig circuitBreakerConfig) {

    endpoint<http:HttpClient> httpEP {
        httpClient;
    }

    boolean [] httpStatusCodes = populateErrorCodeIndex(circuitBreakerConfig.httpStatusCodes);

    CircuitBreakerInferredConfig circuitBreakerInferredConfig = {   failureThreshold:circuitBreakerConfig.failureThreshold,
                                                                    resetTimeout:circuitBreakerConfig.resetTimeout,
                                                                    httpStatusCodes:httpStatusCodes
                                                                };

    //TODO: do the arguments validation inside connector init
    boolean isValidThreshold = validateCircuitBreakerConfiguration(circuitBreakerConfig);

    @Description {value:"The POST action implementation of the Circuit Breaker. Protects the invocation of the POST action of the underlying HTTP client connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action post (string path, http:Request request) (http:Response, http:HttpConnectorError) {
        currentCircuitState = updateCircuitState(circuitHealth, currentCircuitState, circuitBreakerInferredConfig);
        http:Response response;
        http:HttpConnectorError httpConnectorError;

        if (currentCircuitState == CircuitState.OPEN) {
            // TODO: Allow the user to handle this scenario. Maybe through a user provided function
            httpConnectorError = handleOpenCircuit(circuitHealth, circuitBreakerInferredConfig);
        } else {
            response, httpConnectorError = httpEP.post(path, request);
            updateCircuitHealth(circuitHealth, response, httpConnectorError, circuitBreakerInferredConfig);
        }
        return response, httpConnectorError;
    }

    @Description {value:"The HEAD action implementation of the Circuit Breaker. Protects the invocation of the HEAD action of the underlying HTTP client connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action head (string path, http:Request request) (http:Response, http:HttpConnectorError) {
        currentCircuitState = updateCircuitState(circuitHealth, currentCircuitState, circuitBreakerInferredConfig);
        http:Response response;
        http:HttpConnectorError httpConnectorError;

        if (currentCircuitState == CircuitState.OPEN) {
            // TODO: Allow the user to handle this scenario. Maybe through a user provided function
            httpConnectorError = handleOpenCircuit(circuitHealth, circuitBreakerInferredConfig);
        } else {
            response, httpConnectorError = httpEP.head(path, request);
            updateCircuitHealth(circuitHealth, response, httpConnectorError, circuitBreakerInferredConfig);
        }

        return response, httpConnectorError;
    }

    @Description {value:"The PUT action implementation of the Circuit Breaker. Protects the invocation of the PUT action of the underlying HTTP client connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action put (string path, http:Request request) (http:Response, http:HttpConnectorError) {
        currentCircuitState = updateCircuitState(circuitHealth, currentCircuitState, circuitBreakerInferredConfig);
        http:Response response;
        http:HttpConnectorError httpConnectorError;

        if (currentCircuitState == CircuitState.OPEN) {
            // TODO: Allow the user to handle this scenario. Maybe through a user provided function
            httpConnectorError = handleOpenCircuit(circuitHealth, circuitBreakerInferredConfig);
        } else {
            response, httpConnectorError = httpEP.put(path, request);
            updateCircuitHealth(circuitHealth, response, httpConnectorError, circuitBreakerInferredConfig);
        }

        return response, httpConnectorError;
    }

    @Description {value:"Protects the invocation of the Execute action of the underlying HTTP client connector. The Execute action can be used to invoke an HTTP call with the given HTTP verb."}
    @Param {value:"httpVerb: HTTP verb to be used for the request"}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action execute (string httpVerb, string path, http:Request request) (http:Response, http:HttpConnectorError) {
        currentCircuitState = updateCircuitState(circuitHealth, currentCircuitState, circuitBreakerInferredConfig);
        http:Response response;
        http:HttpConnectorError httpConnectorError;

        if (currentCircuitState == CircuitState.OPEN) {
            // TODO: Allow the user to handle this scenario. Maybe through a user provided function
            httpConnectorError = handleOpenCircuit(circuitHealth, circuitBreakerInferredConfig);
        } else {
            response, httpConnectorError = httpEP.execute(httpVerb, path, request);
            updateCircuitHealth(circuitHealth, response, httpConnectorError, circuitBreakerInferredConfig);
        }

        return response, httpConnectorError;
    }

    @Description {value:"The PATCH action implementation of the Circuit Breaker. Protects the invocation of the PATCH action of the underlying HTTP client connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action patch (string path, http:Request request) (http:Response, http:HttpConnectorError) {
        currentCircuitState = updateCircuitState(circuitHealth, currentCircuitState, circuitBreakerInferredConfig);
        http:Response response;
        http:HttpConnectorError httpConnectorError;

        if (currentCircuitState == CircuitState.OPEN) {
            // TODO: Allow the user to handle this scenario. Maybe through a user provided function
            httpConnectorError = handleOpenCircuit(circuitHealth, circuitBreakerInferredConfig);
        } else {
            response, httpConnectorError = httpEP.patch(path, request);
            updateCircuitHealth(circuitHealth, response, httpConnectorError, circuitBreakerInferredConfig);
        }

        return response, httpConnectorError;
    }

    @Description {value:"The DELETE action implementation of the Circuit Breaker. Protects the invocation of the DELETE action of the underlying HTTP client connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action delete (string path, http:Request request) (http:Response, http:HttpConnectorError) {
        currentCircuitState = updateCircuitState(circuitHealth, currentCircuitState, circuitBreakerInferredConfig);
        http:Response response;
        http:HttpConnectorError httpConnectorError;

        if (currentCircuitState == CircuitState.OPEN) {
            // TODO: Allow the user to handle this scenario. Maybe through a user provided function
            httpConnectorError = handleOpenCircuit(circuitHealth, circuitBreakerInferredConfig);
        } else {
            response, httpConnectorError = httpEP.delete(path, request);
            updateCircuitHealth(circuitHealth, response, httpConnectorError, circuitBreakerInferredConfig);
        }

        return response, httpConnectorError;
    }

    @Description {value:"The OPTIONS action implementation of the Circuit Breaker. Protects the invocation of the OPTIONS action of the underlying HTTP client connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action options (string path, http:Request request) (http:Response, http:HttpConnectorError) {
        currentCircuitState = updateCircuitState(circuitHealth, currentCircuitState, circuitBreakerInferredConfig);
        http:Response response;
        http:HttpConnectorError httpConnectorError;

        if (currentCircuitState == CircuitState.OPEN) {
            // TODO: Allow the user to handle this scenario. Maybe through a user provided function
            httpConnectorError = handleOpenCircuit(circuitHealth, circuitBreakerInferredConfig);
        } else {
            response, httpConnectorError = httpEP.options(path, request);
            updateCircuitHealth(circuitHealth, response, httpConnectorError, circuitBreakerInferredConfig);
        }

        return response, httpConnectorError;
    }

    @Description {value:"Protects the invocation of the Forward action of the underlying HTTP client connector. The Forward action can be used to forward an incoming request to an upstream service as it is."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action forward (string path, http:Request request) (http:Response, http:HttpConnectorError) {
        currentCircuitState = updateCircuitState(circuitHealth, currentCircuitState, circuitBreakerInferredConfig);
        http:Response response;
        http:HttpConnectorError httpConnectorError;

        if (currentCircuitState == CircuitState.OPEN) {
            // TODO: Allow the user to handle this scenario. Maybe through a user provided function
            httpConnectorError = handleOpenCircuit(circuitHealth, circuitBreakerInferredConfig);
        } else {
            response, httpConnectorError = httpEP.forward(path, request);
            updateCircuitHealth(circuitHealth, response, httpConnectorError, circuitBreakerInferredConfig);
        }

        return response, httpConnectorError;
    }

    @Description {value:"The GET action implementation of the Circuit Breaker. Protects the invocation of the GET action of the underlying HTTP client connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action get (string path, http:Request request) (http:Response, http:HttpConnectorError) {
        currentCircuitState = updateCircuitState(circuitHealth, currentCircuitState, circuitBreakerInferredConfig);
        http:Response response;
        http:HttpConnectorError httpConnectorError;

        if (currentCircuitState == CircuitState.OPEN) {
            // TODO: Allow the user to handle this scenario. Maybe through a user provided function
            httpConnectorError = handleOpenCircuit(circuitHealth, circuitBreakerInferredConfig);
        } else {
            response, httpConnectorError = httpEP.get(path, request);
            updateCircuitHealth(circuitHealth, response, httpConnectorError, circuitBreakerInferredConfig);
        }

        return response, httpConnectorError;
    }

    @Description { value:"The submit implementation of Circuit Breaker."}
    @Param { value:"httpVerb: The HTTP verb value" }
    @Param { value:"path: The Resource path " }
    @Param { value:"req: An HTTP outbound request message" }
    @Return { value:"The Handle for further interactions" }
    @Return { value:"The Error occured during HTTP client invocation" }
    action submit (string httpVerb, string path, http:Request req) (http:HttpHandle, http:HttpConnectorError) {
        http:HttpConnectorError httpConnectorError = {};
        httpConnectorError.message = "Unsupported action for Circuit breaker";
        return null, httpConnectorError;
    }

    @Description { value:"The getResponse implementation of Circuit Breaker."}
    @Param { value:"handle: The Handle which relates to previous async invocation" }
    @Return { value:"The HTTP response message" }
    @Return { value:"The Error occured during HTTP client invocation" }
    action getResponse (http:HttpHandle handle) (http:Response, http:HttpConnectorError) {
        http:HttpConnectorError httpConnectorError = {};
        httpConnectorError.message = "Unsupported action for Circuit breaker";
        return null, httpConnectorError;
    }

    @Description { value:"The hasPromise implementation of Circuit Breaker."}
    @Param { value:"handle: The Handle which relates to previous async invocation" }
    @Return { value:"Whether push promise exists" }
    action hasPromise (http:HttpHandle handle) (boolean) {
        return false;
    }

    @Description { value:"The getNextPromise implementation of Circuit Breaker."}
    @Param { value:"handle: The Handle which relates to previous async invocation" }
    @Return { value:"The HTTP Push Promise message" }
    @Return { value:"The Error occured during HTTP client invocation" }
    action getNextPromise (http:HttpHandle handle) (http:PushPromise, http:HttpConnectorError) {
        http:HttpConnectorError httpConnectorError = {};
        httpConnectorError.message = "Unsupported action for Circuit breaker";
        return null, httpConnectorError;
    }

    @Description { value:"The getPromisedResponse implementation of Circuit Breaker."}
    @Param { value:"promise: The related Push Promise message" }
    @Return { value:"HTTP The Push Response message" }
    @Return { value:"The Error occured during HTTP client invocation" }
    action getPromisedResponse (http:PushPromise promise) (http:Response, http:HttpConnectorError) {
        http:HttpConnectorError httpConnectorError = {};
        httpConnectorError.message = "Unsupported action for Circuit breaker";
        return null, httpConnectorError;
    }

    @Description { value:"The rejectPromise implementation of Circuit Breaker."}
    @Param { value:"promise: The Push Promise need to be rejected" }
    @Return { value:"Whether operation is successful" }
    action rejectPromise (http:PushPromise promise) (boolean) {
        return false;
    }
}

function updateCircuitState (CircuitHealth circuitHealth, CircuitState currentState, CircuitBreakerInferredConfig circuitBreakerInferredConfig) (CircuitState) {

    lock {
        if (currentState == CircuitState.OPEN) {
            time:Time currentT = time:currentTime();
            int elapsedTime = currentT.time - circuitHealth.lastErrorTime.time;

            if (elapsedTime > circuitBreakerInferredConfig.resetTimeout) {
                circuitHealth.errorCount = 0;
                circuitHealth.requestCount = 0;
                currentState = CircuitState.HALF_OPEN;
                log:printInfo("CircuitBreaker reset timeout reached. Circuit switched from OPEN to HALF_OPEN state.");
            }

        } else if (currentState == CircuitState.HALF_OPEN) {
            if (circuitHealth.errorCount > 0) {
                // If the trial run has failed, trip the circuit again
                currentState = CircuitState.OPEN;
                log:printInfo("CircuitBreaker trial run has failed. Circuit switched from HALF_OPEN to OPEN state.");
            } else {
                // If the trial run was successful reset the circuit
                currentState = CircuitState.CLOSED;
                log:printInfo("CircuitBreaker trial run  was successful. Circuit switched from HALF_OPEN to CLOSE state.");
            }
        } else {
            float currentFailureRate = 0;

            if (circuitHealth.requestCount > 0) {
                currentFailureRate = ((float)circuitHealth.errorCount / circuitHealth.requestCount);
            }

            if (currentFailureRate > circuitBreakerInferredConfig.failureThreshold) {
                currentState = CircuitState.OPEN;
                log:printInfo("CircuitBreaker failure threshold exceeded. Circuit tripped from CLOSE to OPEN state.");
            }
        }
    }
    return currentState;
}

function updateCircuitHealth(CircuitHealth circuitHealth, http:Response inResponse,
               http:HttpConnectorError httpConnectorError, CircuitBreakerInferredConfig circuitBreakerInferredConfig) {
    lock {
        circuitHealth.requestCount = circuitHealth.requestCount + 1;
        if ((inResponse != null && circuitBreakerInferredConfig.httpStatusCodes[inResponse.statusCode] == true) || httpConnectorError != null) {
            circuitHealth.errorCount = circuitHealth.errorCount + 1;
            circuitHealth.lastErrorTime = time:currentTime();
        }
    }
}

// Handles open circuit state.
function handleOpenCircuit (CircuitHealth circuitHealth, CircuitBreakerInferredConfig circuitBreakerInferredConfig) (http:HttpConnectorError) {
    time:Time currentT = time:currentTime();
    int timeDif = currentT.time - circuitHealth.lastErrorTime.time;
    int timeRemaining = circuitBreakerInferredConfig.resetTimeout - timeDif;
    http:HttpConnectorError httpConnectorError = {};
    httpConnectorError.message = "Upstream service unavailable. Requests to upstream service will be suspended for "
              + timeRemaining + " milliseconds.";
    return httpConnectorError;
}

// Validates the struct configurations passed to create circuit breaker.
function validateCircuitBreakerConfiguration (CircuitBreakerConfig circuitBreakerConfig) (boolean isValidThreshold){
    float failureThreshold = circuitBreakerConfig.failureThreshold;
     if (failureThreshold < 0 || failureThreshold > 1) {
         string errorMessage = "Invalid failure threshold. Failure threshold value"
                                                +  " should between 0 to 1, found "+ failureThreshold;
         error circuitBreakerConfigError = {message:errorMessage};
         throw circuitBreakerConfigError;
     }
    return true;
}

// CircuitHealth struct initializer. Sets the initial circuit state.
function <CircuitHealth circuitHealth> CircuitHealth() {
    currentCircuitState = CircuitState.CLOSED;
}

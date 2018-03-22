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

package ballerina.net.http;

import ballerina/log;
import ballerina/time;
import ballerina/io;

documentation {
    Represents Circuit Breaker circuit state.

    F{{OPEN}}  - circuit state OPEN.
    F{{CLOSED}} - circuit state CLOSED.
    F{{HALF_OPEN}} - circuit state HALF_OPEN.
}
public enum CircuitState {
   OPEN, CLOSED, HALF_OPEN
}

public struct CircuitHealth {
   int requestCount;
   int errorCount;
   time:Time lastErrorTime;
}

documentation {
    Represents Circuit Breaker configuration.

    F{{failureThreshold}}  - The threshold for request failures. When this threshold is crossed, the circuit will trip.
                             The threshold should be a value between 0 and 1.
    F{{resetTimeout}} - The time period to wait before attempting to make another request to the upstream service.
    F{{httpStatusCodes}} - Array of http response status codes which considered as failure responses.
}
public struct CircuitBreakerConfig {
    float failureThreshold;
    int resetTimeout;
    int [] httpStatusCodes;
}


public struct CircuitBreakerInferredConfig {
   float failureThreshold;
   int resetTimeout;
   boolean [] httpStatusCodes;
}

documentation {
    Represents an HTTP Circuit Breaker client to be used with the HTTP client connector to gracefully handle network errors.

    F{{serviceUri}} - Target service url.
    F{{config}}  - Circuit Breaker configuration.
    F{{circuitBreakerInferredConfig}} - Dirived configuration from circuit Breaker configuration.
    F{{httpClient}}  - HTTP client for outbound HTTP requests.
    F{{circuitHealth}} - Struct which maintain the circuit status.
    F{{currentCircuitState}}  - Current state of the circuit.
}
public struct CircuitBreakerClient {
   string serviceUri;
   ClientEndpointConfiguration config;
   CircuitBreakerInferredConfig circuitBreakerInferredConfig;
   HttpClient httpClient;
   CircuitHealth circuitHealth;
   CircuitState currentCircuitState;
}

   @Description {value:"The POST action implementation of the Circuit Breaker. Protects the invocation of the POST action of the underlying HTTP client connector."}
   @Param {value:"path: Resource path"}
   @Param {value:"request: A Request struct"}
   @Return {value:"The Response struct"}
   @Return {value:"Error occurred during the action invocation, if any"}
   public function <CircuitBreakerClient client> post (string path, Request request) returns (Response | HttpConnectorError) {
       HttpClient httpClient = client.httpClient;
       CircuitBreakerInferredConfig cbic = client.circuitBreakerInferredConfig;
       client.currentCircuitState = updateCircuitState(client.circuitHealth, client.currentCircuitState, cbic);
       Response response = {};
       HttpConnectorError httpConnectorError = {};

       if (client.currentCircuitState == CircuitState.OPEN) {
           // TODO: Allow the user to handle this scenario. Maybe through a user provided function
           httpConnectorError = handleOpenCircuit(client.circuitHealth, client.circuitBreakerInferredConfig);
           return httpConnectorError;
       } else {
           match httpClient.post(path, request) {
                Response service_response => {
                                        updateCircuitHealthSuccess(client.circuitHealth, service_response, client.circuitBreakerInferredConfig);
                                        return service_response;
                                    }
                HttpConnectorError serviceError => {
                                        updateCircuitHealthFailure(client.circuitHealth, serviceError, client.circuitBreakerInferredConfig);
                                        return httpConnectorError;
                                    }
            }
        }
   }


   @Description {value:"The HEAD action implementation of the Circuit Breaker. Protects the invocation of the HEAD action of the underlying HTTP client connector."}
   @Param {value:"path: Resource path"}
   @Param {value:"request: A Request struct"}
   @Return {value:"The Response struct"}
   @Return {value:"Error occurred during the action invocation, if any"}
   public function <CircuitBreakerClient client> head (string path, Request request) returns (Response | HttpConnectorError) {
       HttpClient httpClient = client.httpClient;
       CircuitBreakerInferredConfig cbic = client.circuitBreakerInferredConfig;
       client.currentCircuitState = updateCircuitState(client.circuitHealth, client.currentCircuitState, cbic);
       Response response = {};
       HttpConnectorError httpConnectorError = {};

       if (client.currentCircuitState == CircuitState.OPEN) {
           // TODO: Allow the user to handle this scenario. Maybe through a user provided function
           httpConnectorError = handleOpenCircuit(client.circuitHealth, client.circuitBreakerInferredConfig);
           return httpConnectorError;
       } else {
           match httpClient.head(path, request) {
                Response service_response => {
                                        updateCircuitHealthSuccess(client.circuitHealth, service_response, client.circuitBreakerInferredConfig);
                                        return service_response;
                                    }
                HttpConnectorError serviceError => {
                                        updateCircuitHealthFailure(client.circuitHealth, serviceError, client.circuitBreakerInferredConfig);
                                        return httpConnectorError;
                                    }
            }
         }
   }

   @Description {value:"The PUT action implementation of the Circuit Breaker. Protects the invocation of the PUT action of the underlying HTTP client connector."}
   @Param {value:"path: Resource path"}
   @Param {value:"request: A Request struct"}
   @Return {value:"The Response struct"}
   @Return {value:"Error occurred during the action invocation, if any"}
   public function <CircuitBreakerClient client> put (string path, Request request) returns (Response | HttpConnectorError) {
       HttpClient httpClient = client.httpClient;
       CircuitBreakerInferredConfig cbic = client.circuitBreakerInferredConfig;
       client.currentCircuitState = updateCircuitState(client.circuitHealth, client.currentCircuitState, cbic);
       Response response = {};
       HttpConnectorError httpConnectorError = {};

       if (client.currentCircuitState == CircuitState.OPEN) {
           // TODO: Allow the user to handle this scenario. Maybe through a user provided function
           httpConnectorError = handleOpenCircuit(client.circuitHealth, client.circuitBreakerInferredConfig);
           return httpConnectorError;
       } else {
           match httpClient.put(path, request) {
                Response service_response => {
                                        updateCircuitHealthSuccess(client.circuitHealth, service_response, client.circuitBreakerInferredConfig);
                                        return service_response;
                                    }
                HttpConnectorError serviceError => {
                                        updateCircuitHealthFailure(client.circuitHealth, serviceError, client.circuitBreakerInferredConfig);
                                        return httpConnectorError;
                                    }
            }
         }
   }

   @Description {value:"Protects the invocation of the Execute action of the underlying HTTP client connector. The Execute action can be used to invoke an HTTP call with the given HTTP verb."}
   @Param {value:"httpVerb: HTTP verb to be used for the request"}
   @Param {value:"path: Resource path"}
   @Param {value:"request: A Request struct"}
   @Return {value:"The Response struct"}
   @Return {value:"Error occurred during the action invocation, if any"}
   public function <CircuitBreakerClient client> execute (string httpVerb, string path, Request request) returns (Response | HttpConnectorError) {
       HttpClient httpClient = client.httpClient;
       CircuitBreakerInferredConfig cbic = client.circuitBreakerInferredConfig;
       client.currentCircuitState = updateCircuitState(client.circuitHealth, client.currentCircuitState, cbic);
       Response response = {};
       HttpConnectorError httpConnectorError = {};

       if (client.currentCircuitState == CircuitState.OPEN) {
           // TODO: Allow the user to handle this scenario. Maybe through a user provided function
           httpConnectorError = handleOpenCircuit(client.circuitHealth, client.circuitBreakerInferredConfig);
           return httpConnectorError;
       } else {
           match httpClient.execute(httpVerb, path, request) {
                Response service_response => {
                                        updateCircuitHealthSuccess(client.circuitHealth, service_response, client.circuitBreakerInferredConfig);
                                        return service_response;
                                    }
                HttpConnectorError serviceError => {
                                        updateCircuitHealthFailure(client.circuitHealth, serviceError, client.circuitBreakerInferredConfig);
                                        return httpConnectorError;
                                    }
            }
        }
   }

   @Description {value:"The PATCH action implementation of the Circuit Breaker. Protects the invocation of the PATCH action of the underlying HTTP client connector."}
   @Param {value:"path: Resource path"}
   @Param {value:"request: A Request struct"}
   @Return {value:"The Response struct"}
   @Return {value:"Error occurred during the action invocation, if any"}
   public function <CircuitBreakerClient client> patch (string path, Request request) returns (Response | HttpConnectorError) {
       HttpClient httpClient = client.httpClient;
       CircuitBreakerInferredConfig cbic = client.circuitBreakerInferredConfig;
       client.currentCircuitState = updateCircuitState(client.circuitHealth, client.currentCircuitState, cbic);
       Response response = {};
       HttpConnectorError httpConnectorError = {};

       if (client.currentCircuitState == CircuitState.OPEN) {
           // TODO: Allow the user to handle this scenario. Maybe through a user provided function
           httpConnectorError = handleOpenCircuit(client.circuitHealth, client.circuitBreakerInferredConfig);
           return httpConnectorError;
       } else {
           match httpClient.patch(path, request) {
                Response service_response => {
                                        updateCircuitHealthSuccess(client.circuitHealth, service_response, client.circuitBreakerInferredConfig);
                                        return service_response;
                                    }
                HttpConnectorError serviceError => {
                                        updateCircuitHealthFailure(client.circuitHealth, serviceError, client.circuitBreakerInferredConfig);
                                        return httpConnectorError;
                                    }
            }
        }
   }

   @Description {value:"The DELETE action implementation of the Circuit Breaker. Protects the invocation of the DELETE action of the underlying HTTP client connector."}
   @Param {value:"path: Resource path"}
   @Param {value:"request: A Request struct"}
   @Return {value:"The Response struct"}
   @Return {value:"Error occurred during the action invocation, if any"}
   public function <CircuitBreakerClient client> delete (string path, Request request) returns (Response | HttpConnectorError) {
       HttpClient httpClient = client.httpClient;
       CircuitBreakerInferredConfig cbic = client.circuitBreakerInferredConfig;
       client.currentCircuitState = updateCircuitState(client.circuitHealth, client.currentCircuitState, cbic);
       Response response = {};
       HttpConnectorError httpConnectorError = {};

       if (client.currentCircuitState == CircuitState.OPEN) {
           // TODO: Allow the user to handle this scenario. Maybe through a user provided function
           httpConnectorError = handleOpenCircuit(client.circuitHealth, client.circuitBreakerInferredConfig);
           return httpConnectorError;
       } else {
           match httpClient.delete(path, request) {
                Response service_response => {
                                        updateCircuitHealthSuccess(client.circuitHealth, service_response, client.circuitBreakerInferredConfig);
                                        return service_response;
                                    }
                HttpConnectorError serviceError => {
                                        updateCircuitHealthFailure(client.circuitHealth, serviceError, client.circuitBreakerInferredConfig);
                                        return httpConnectorError;
                                    }
            }
        }
   }

    @Description {value:"The GET action implementation of the Circuit Breaker. Protects the invocation of the GET action of the underlying HTTP client connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    public function <CircuitBreakerClient client> get (string path, Request request) returns (Response | HttpConnectorError) {
        HttpClient httpClient = client.httpClient;
        CircuitBreakerInferredConfig cbic = client.circuitBreakerInferredConfig;
        client.currentCircuitState = updateCircuitState(client.circuitHealth, client.currentCircuitState, cbic);
        Response response = {};
        HttpConnectorError httpConnectorError = {};

        if (client.currentCircuitState == CircuitState.OPEN) {
            // TODO: Allow the user to handle this scenario. Maybe through a user provided function
            httpConnectorError = handleOpenCircuit(client.circuitHealth, client.circuitBreakerInferredConfig);
            return httpConnectorError;
        } else {
           match httpClient.get(path, request) {
                Response service_response => {
                                        updateCircuitHealthSuccess(client.circuitHealth, service_response, client.circuitBreakerInferredConfig);
                                        return service_response;
                                    }
                HttpConnectorError serviceError => {
                                        updateCircuitHealthFailure(client.circuitHealth, serviceError, client.circuitBreakerInferredConfig);
                                        return httpConnectorError;
                                    }
            }
        }
    }

   @Description {value:"The OPTIONS action implementation of the Circuit Breaker. Protects the invocation of the OPTIONS action of the underlying HTTP client connector."}
   @Param {value:"path: Resource path"}
   @Param {value:"request: A Request struct"}
   @Return {value:"The Response struct"}
   @Return {value:"Error occurred during the action invocation, if any"}
   public function <CircuitBreakerClient client> options (string path, Request request) returns (Response | HttpConnectorError) {
       HttpClient httpClient = client.httpClient;
       CircuitBreakerInferredConfig cbic = client.circuitBreakerInferredConfig;
       client.currentCircuitState = updateCircuitState(client.circuitHealth, client.currentCircuitState, cbic);
       Response response = {};
       HttpConnectorError httpConnectorError = {};

       if (client.currentCircuitState == CircuitState.OPEN) {
           // TODO: Allow the user to handle this scenario. Maybe through a user provided function
           httpConnectorError = handleOpenCircuit(client.circuitHealth, client.circuitBreakerInferredConfig);
           return httpConnectorError;
       } else {
           match httpClient.options(path, request) {
                Response service_response => {
                                        updateCircuitHealthSuccess(client.circuitHealth, service_response, client.circuitBreakerInferredConfig);
                                        return service_response;
                                    }
                HttpConnectorError serviceError => {
                                        updateCircuitHealthFailure(client.circuitHealth, serviceError, client.circuitBreakerInferredConfig);
                                        return httpConnectorError;
                                    }
            }
        }
   }

   @Description {value:"Protects the invocation of the Forward action of the underlying HTTP client connector. The Forward action can be used to forward an incoming request to an upstream service as it is."}
   @Param {value:"path: Resource path"}
   @Param {value:"request: A Request struct"}
   @Return {value:"The Response struct"}
   @Return {value:"Error occurred during the action invocation, if any"}
   public function <CircuitBreakerClient client> forward (string path, Request request) returns (Response | HttpConnectorError) {
       HttpClient httpClient = client.httpClient;
       CircuitBreakerInferredConfig cbic = client.circuitBreakerInferredConfig;
       client.currentCircuitState = updateCircuitState(client.circuitHealth, client.currentCircuitState, cbic);
       Response response = {};
       HttpConnectorError httpConnectorError = {};

       if (client.currentCircuitState == CircuitState.OPEN) {
           // TODO: Allow the user to handle this scenario. Maybe through a user provided function
           httpConnectorError = handleOpenCircuit(client.circuitHealth, client.circuitBreakerInferredConfig);
           return httpConnectorError;
       } else {
           match httpClient.forward(path, request) {
                Response service_response => {
                                        updateCircuitHealthSuccess(client.circuitHealth, service_response, client.circuitBreakerInferredConfig);
                                        return service_response;
                                    }
                HttpConnectorError serviceError => {
                                        updateCircuitHealthFailure(client.circuitHealth, serviceError, client.circuitBreakerInferredConfig);
                                        return httpConnectorError;
                                    }
            }
       }
   }

   @Description { value:"The submit implementation of Circuit Breaker."}
   @Param { value:"httpVerb: The HTTP verb value" }
   @Param { value:"path: The Resource path " }
   @Param { value:"req: An HTTP outbound request message" }
   @Return { value:"The Handle for further interactions" }
   @Return { value:"The Error occured during HTTP client invocation" }
   public function <CircuitBreakerClient client> submit (string httpVerb, string path, Request req) returns (HttpHandle | HttpConnectorError) {
       HttpConnectorError httpConnectorError = {};
       httpConnectorError.message = "Unsupported action for Circuit breaker";
       return httpConnectorError;
   }

   @Description { value:"The getResponse implementation of Circuit Breaker."}
   @Param { value:"handle: The Handle which relates to previous async invocation" }
   @Return { value:"The HTTP response message" }
   @Return { value:"The Error occured during HTTP client invocation" }
   public function <CircuitBreakerClient client> getResponse (HttpHandle handle) returns (Response | HttpConnectorError) {
       HttpConnectorError httpConnectorError = {};
       httpConnectorError.message = "Unsupported action for Circuit breaker";
       return httpConnectorError;
   }

   @Description { value:"The hasPromise implementation of Circuit Breaker."}
   @Param { value:"handle: The Handle which relates to previous async invocation" }
   @Return { value:"Whether push promise exists" }
   public function <CircuitBreakerClient client> hasPromise (HttpHandle handle) returns (boolean) {
       return false;
   }

   @Description { value:"The getNextPromise implementation of Circuit Breaker."}
   @Param { value:"handle: The Handle which relates to previous async invocation" }
   @Return { value:"The HTTP Push Promise message" }
   @Return { value:"The Error occured during HTTP client invocation" }
   public function <CircuitBreakerClient client> getNextPromise (HttpHandle handle) returns (PushPromise | HttpConnectorError) {
       HttpConnectorError httpConnectorError = {};
       httpConnectorError.message = "Unsupported action for Circuit breaker";
       return httpConnectorError;
   }

   @Description { value:"The getPromisedResponse implementation of Circuit Breaker."}
   @Param { value:"promise: The related Push Promise message" }
   @Return { value:"HTTP The Push Response message" }
   @Return { value:"The Error occured during HTTP client invocation" }
   public function <CircuitBreakerClient client> getPromisedResponse (PushPromise promise) returns (Response | HttpConnectorError) {
       HttpConnectorError httpConnectorError = {};
       httpConnectorError.message = "Unsupported action for Circuit breaker";
       return httpConnectorError;
   }

   @Description { value:"The rejectPromise implementation of Circuit Breaker."}
   @Param { value:"promise: The Push Promise need to be rejected" }
   @Return { value:"Whether operation is successful" }
   public function <CircuitBreakerClient client> rejectPromise (PushPromise promise) returns (boolean) {
       return false;
   }

public function updateCircuitState (CircuitHealth circuitHealth, CircuitState currentState, CircuitBreakerInferredConfig circuitBreakerInferredConfig) returns (CircuitState) {

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
               currentFailureRate = (<float>circuitHealth.errorCount / circuitHealth.requestCount);
           }

           if (currentFailureRate > circuitBreakerInferredConfig.failureThreshold) {
               currentState = CircuitState.OPEN;
               log:printInfo("CircuitBreaker failure threshold exceeded. Circuit tripped from CLOSE to OPEN state.");
           }
       }
   }
   return currentState;
}

function updateCircuitHealthFailure(CircuitHealth circuitHealth,
                             HttpConnectorError httpConnectorError, CircuitBreakerInferredConfig circuitBreakerInferredConfig) {
    lock {
        circuitHealth.requestCount = circuitHealth.requestCount + 1;
        circuitHealth.errorCount = circuitHealth.errorCount + 1;
        circuitHealth.lastErrorTime = time:currentTime();
    }
}

function updateCircuitHealthSuccess(CircuitHealth circuitHealth, Response inResponse, CircuitBreakerInferredConfig circuitBreakerInferredConfig) {
    lock {
    circuitHealth.requestCount = circuitHealth.requestCount + 1;
    if (circuitBreakerInferredConfig.httpStatusCodes[inResponse.statusCode] == true) {
        circuitHealth.errorCount = circuitHealth.errorCount + 1;
        circuitHealth.lastErrorTime = time:currentTime();
    }
    }
}

// Handles open circuit state.
function handleOpenCircuit (CircuitHealth circuitHealth, CircuitBreakerInferredConfig circuitBreakerInferredConfig) returns (HttpConnectorError) {
   time:Time currentT = time:currentTime();
   int timeDif = currentT.time - circuitHealth.lastErrorTime.time;
   int timeRemaining = circuitBreakerInferredConfig.resetTimeout - timeDif;
   HttpConnectorError httpConnectorError = {};
   httpConnectorError.message = "Upstream service unavailable. Requests to upstream service will be suspended for "
             + timeRemaining + " milliseconds.";
   return httpConnectorError;
}

// Validates the struct configurations passed to create circuit breaker.
function validateCircuitBreakerConfiguration (CircuitBreakerConfig circuitBreakerConfig){
   float failureThreshold = circuitBreakerConfig.failureThreshold;
    if (failureThreshold < 0 || failureThreshold > 1) {
        string errorMessage = "Invalid failure threshold. Failure threshold value"
                                               +  " should between 0 to 1, found "+ failureThreshold;
        error circuitBreakerConfigError = {message:errorMessage};
        throw circuitBreakerConfigError;
    }
}

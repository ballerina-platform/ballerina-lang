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


import ballerina/log;
import ballerina/time;
import ballerina/io;

documentation {
    Represents Circuit Breaker circuit state.
}
public type CircuitState "OPEN" | "HALF_OPEN" | "CLOSED";

@final public CircuitState CB_OPEN_STATE = "OPEN";
@final public CircuitState CB_HALF_OPEN_STATE = "HALF_OPEN";
@final public CircuitState CB_CLOSED_STATE = "CLOSED";

documentation {
    Represents Circuit health of the Circuit Breaker.

    F{{startTime}}  - Circuit Breaker start time.
                             The threshold should be a value between 0 and 1.
    F{{requestCount}} - Total request count from the start.
    F{{errorCount}} - Total error count.
    F{{lastErrorTime}}  - Time that the last error occurred.
    F{{totalBuckets}} - Number of buckets fits to the time window.
    F{{lastUsedBucketId}} - Id of the last bucket used in Circuit Breaker calculations.
}
public type CircuitHealth {
   time:Time startTime,
   int requestCount,
   int errorCount,
   time:Time lastErrorTime,
   Bucket[] totalBuckets,
   int lastUsedBucketId,
};

documentation {
    Represents Circuit Breaker configuration.

    F{{failureThreshold}}  - The threshold for request failures. When this threshold is crossed, the circuit will trip.
                             The threshold should be a value between 0 and 1.
    F{{resetTimeMillis}} - The time period(in milliseconds) to wait before attempting to make another
                               request to the upstream service.
    F{{statusCodes}} - Array of http response status codes which considered as failure responses.
}
public type CircuitBreakerConfig {
    RollingWindow rollingWindow,
    float failureThreshold,
    int resetTimeMillis,
    int[] statusCodes,
};

documentation {
    Represents Circuit Breaker rolling window configuration.

    F{{timeWindowMillis}}  - Time period in milliseconds which the failure threshold is calculated.
    F{{bucketSizeMillis}} - The size of a sub unit in milliseconds that the timeWindow should be divided.
}
public type RollingWindow {
    int timeWindowMillis = 60000,
    int bucketSizeMillis = 10000,
};

documentation {
    Represents Circuit Breaker sub window (Bucket).

    F{{successCount}}  - Number of successes during sub window time frame.
    F{{failureCount}} - Number of faiures during sub window time frame.
}
public type Bucket {
    int successCount,
    int failureCount,
};

public type CircuitBreakerInferredConfig {
   float failureThreshold,
   int resetTimeMillis,
   boolean[] statusCodes,
   int noOfBuckets,
   RollingWindow rollingWindow,
};

documentation {
    Represents an HTTP Circuit Breaker client to be used with the HTTP client connector to gracefully handle network errors.

    F{{serviceUri}} - Target service url.
    F{{config}}  - Circuit Breaker configuration.
    F{{circuitBreakerInferredConfig}} - Dirived configuration from circuit Breaker configuration.
    F{{httpClient}}  - HTTP client for outbound HTTP requests.
    F{{circuitHealth}} - Struct which maintain the circuit status.
    F{{currentCircuitState}}  - Current state of the circuit.
}
public type CircuitBreakerClient object {

    public {
        string serviceUri;
        ClientEndpointConfig config;
        CircuitBreakerInferredConfig circuitBreakerInferredConfig;
        CallerActions httpClient;
        CircuitHealth circuitHealth;
        CircuitState currentCircuitState = CB_CLOSED_STATE;
    }

    public new (string serviceUri, ClientEndpointConfig config, CircuitBreakerInferredConfig circuitBreakerInferredConfig,
                                                                            CallerActions httpClient, CircuitHealth circuitHealth) {
        self.serviceUri = serviceUri;
        self.config = config;
        self.circuitBreakerInferredConfig = circuitBreakerInferredConfig;
        self.httpClient = httpClient;
        self.circuitHealth = circuitHealth;
    }

    @Description {value:"The POST action implementation of the Circuit Breaker. Protects the invocation of the POST action of the underlying HTTP client connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    public function post (string path, Request? request = ()) returns (Response | HttpConnectorError);

    @Description {value:"The HEAD action implementation of the Circuit Breaker. Protects the invocation of the HEAD action of the underlying HTTP client connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    public function head (string path, Request? request = ()) returns (Response | HttpConnectorError);

    @Description {value:"The PUT action implementation of the Circuit Breaker. Protects the invocation of the PUT action of the underlying HTTP client connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    public function put (string path, Request? request = ()) returns (Response | HttpConnectorError);

    @Description {value:"Protects the invocation of the Execute action of the underlying HTTP client connector. The Execute action can be used to invoke an HTTP call with the given HTTP verb."}
    @Param {value:"httpVerb: HTTP verb to be used for the request"}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    public function execute (string httpVerb, string path, Request request) returns (Response | HttpConnectorError);

    @Description {value:"The PATCH action implementation of the Circuit Breaker. Protects the invocation of the PATCH action of the underlying HTTP client connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    public function patch (string path, Request? request = ()) returns (Response | HttpConnectorError);

    @Description {value:"The DELETE action implementation of the Circuit Breaker. Protects the invocation of the DELETE action of the underlying HTTP client connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    public function delete (string path, Request? request = ()) returns (Response | HttpConnectorError);

    @Description {value:"The GET action implementation of the Circuit Breaker. Protects the invocation of the GET action of the underlying HTTP client connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    public function get (string path, Request? request = ()) returns (Response | HttpConnectorError);

    @Description {value:"The OPTIONS action implementation of the Circuit Breaker. Protects the invocation of the OPTIONS action of the underlying HTTP client connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    public function options (string path, Request? request = ()) returns (Response | HttpConnectorError);

    @Description {value:"Protects the invocation of the Forward action of the underlying HTTP client connector. The Forward action can be used to forward an incoming request to an upstream service as it is."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    public function forward (string path, Request request) returns (Response | HttpConnectorError);

    @Description { value:"The submit implementation of Circuit Breaker."}
    @Param { value:"httpVerb: The HTTP verb value" }
    @Param { value:"path: The Resource path " }
    @Param { value:"req: An HTTP outbound request message" }
    @Return { value:"The Future for further interactions" }
    @Return { value:"The Error occured during HTTP client invocation" }
    public function submit (string httpVerb, string path, Request request) returns (HttpFuture | HttpConnectorError);

    @Description { value:"The getResponse implementation of Circuit Breaker."}
    @Param { value:"httpFuture: The Future which relates to previous async invocation" }
    @Return { value:"The HTTP response message" }
    @Return { value:"The Error occured during HTTP client invocation" }
    public function getResponse (HttpFuture httpFuture) returns (Response | HttpConnectorError);

    @Description { value:"The hasPromise implementation of Circuit Breaker."}
    @Param { value:"httpFuture: The Future which relates to previous async invocation" }
    @Return { value:"Whether push promise exists" }
    public function hasPromise (HttpFuture httpFuture) returns (boolean);

    @Description { value:"The getNextPromise implementation of Circuit Breaker."}
    @Param { value:"httpFuture: The Future which relates to previous async invocation" }
    @Return { value:"The HTTP Push Promise message" }
    @Return { value:"The Error occured during HTTP client invocation" }
    public function getNextPromise (HttpFuture httpFuture) returns (PushPromise | HttpConnectorError);

    @Description { value:"The getPromisedResponse implementation of Circuit Breaker."}
    @Param { value:"promise: The related Push Promise message" }
    @Return { value:"HTTP The Push Response message" }
    @Return { value:"The Error occured during HTTP client invocation" }
    public function getPromisedResponse (PushPromise promise) returns (Response | HttpConnectorError);

    @Description { value:"The rejectPromise implementation of Circuit Breaker."}
    @Param { value:"promise: The Push Promise need to be rejected" }
    public function rejectPromise (PushPromise promise);
};

public function CircuitBreakerClient::post (string path, Request? request = ()) returns (Response | HttpConnectorError) {
   CallerActions httpClient = self.httpClient;
   CircuitBreakerInferredConfig cbic = self.circuitBreakerInferredConfig;
   self.currentCircuitState = updateCircuitState(self.circuitHealth, self.currentCircuitState, cbic);

   if (self.currentCircuitState == CB_OPEN_STATE) {
       // TODO: Allow the user to handle this scenario. Maybe through a user provided function
       return handleOpenCircuit(self.circuitHealth, self.circuitBreakerInferredConfig);
   } else {
       match httpClient.post(path, request = request) {
            Response service_response => {
                                    updateCircuitHealthSuccess(self.circuitHealth, service_response, self.circuitBreakerInferredConfig);
                                    return service_response;
                                }
            HttpConnectorError serviceError => {
                                    updateCircuitHealthFailure(self.circuitHealth, serviceError, self.circuitBreakerInferredConfig);
                                    return serviceError;
                                }
        }
    }
}

public function CircuitBreakerClient::head (string path, Request? request = ()) returns (Response | HttpConnectorError) {
   CallerActions httpClient = self.httpClient;
   CircuitBreakerInferredConfig cbic = self.circuitBreakerInferredConfig;
   self.currentCircuitState = updateCircuitState(self.circuitHealth, self.currentCircuitState, cbic);

   if (self.currentCircuitState == CB_OPEN_STATE) {
       // TODO: Allow the user to handle this scenario. Maybe through a user provided function
       return handleOpenCircuit(self.circuitHealth, self.circuitBreakerInferredConfig);
   } else {
       match httpClient.head(path, request = request) {
            Response service_response => {
                                    updateCircuitHealthSuccess(self.circuitHealth, service_response, self.circuitBreakerInferredConfig);
                                    return service_response;
                                }
            HttpConnectorError serviceError => {
                                    updateCircuitHealthFailure(self.circuitHealth, serviceError, self.circuitBreakerInferredConfig);
                                    return serviceError;
                                }
        }
     }
}

public function CircuitBreakerClient::put (string path, Request? request = ()) returns (Response | HttpConnectorError) {
   CallerActions httpClient = self.httpClient;
   CircuitBreakerInferredConfig cbic = self.circuitBreakerInferredConfig;
   self.currentCircuitState = updateCircuitState(self.circuitHealth, self.currentCircuitState, cbic);

   if (self.currentCircuitState == CB_OPEN_STATE) {
       // TODO: Allow the user to handle this scenario. Maybe through a user provided function
       return handleOpenCircuit(self.circuitHealth, self.circuitBreakerInferredConfig);
   } else {
       match httpClient.put(path, request = request) {
            Response service_response => {
                                    updateCircuitHealthSuccess(self.circuitHealth, service_response, self.circuitBreakerInferredConfig);
                                    return service_response;
                                }
            HttpConnectorError serviceError => {
                                    updateCircuitHealthFailure(self.circuitHealth, serviceError, self.circuitBreakerInferredConfig);
                                    return serviceError;
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
public function CircuitBreakerClient::execute (string httpVerb, string path, Request request) returns (Response | HttpConnectorError) {
   CallerActions httpClient = self.httpClient;
   CircuitBreakerInferredConfig cbic = self.circuitBreakerInferredConfig;
   self.currentCircuitState = updateCircuitState(self.circuitHealth, self.currentCircuitState, cbic);

   if (self.currentCircuitState == CB_OPEN_STATE) {
       // TODO: Allow the user to handle this scenario. Maybe through a user provided function
       return handleOpenCircuit(self.circuitHealth, self.circuitBreakerInferredConfig);
   } else {
       match httpClient.execute(httpVerb, path, request) {
            Response service_response => {
                                    updateCircuitHealthSuccess(self.circuitHealth, service_response, self.circuitBreakerInferredConfig);
                                    return service_response;
                                }
            HttpConnectorError serviceError => {
                                    updateCircuitHealthFailure(self.circuitHealth, serviceError, self.circuitBreakerInferredConfig);
                                    return serviceError;
                                }
        }
    }
}

@Description {value:"The PATCH action implementation of the Circuit Breaker. Protects the invocation of the PATCH action of the underlying HTTP client connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the action invocation, if any"}
public function CircuitBreakerClient::patch (string path, Request? request = ()) returns (Response | HttpConnectorError) {
   CallerActions httpClient = self.httpClient;
   CircuitBreakerInferredConfig cbic = self.circuitBreakerInferredConfig;
   self.currentCircuitState = updateCircuitState(self.circuitHealth, self.currentCircuitState, cbic);

   if (self.currentCircuitState == CB_OPEN_STATE) {
       // TODO: Allow the user to handle this scenario. Maybe through a user provided function
       return handleOpenCircuit(self.circuitHealth, self.circuitBreakerInferredConfig);
   } else {
       match httpClient.patch(path, request = request) {
            Response service_response => {
                                    updateCircuitHealthSuccess(self.circuitHealth, service_response, self.circuitBreakerInferredConfig);
                                    return service_response;
                                }
            HttpConnectorError serviceError => {
                                    updateCircuitHealthFailure(self.circuitHealth, serviceError, self.circuitBreakerInferredConfig);
                                    return serviceError;
                                }
        }
    }
}

@Description {value:"The DELETE action implementation of the Circuit Breaker. Protects the invocation of the DELETE action of the underlying HTTP client connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the action invocation, if any"}
public function CircuitBreakerClient::delete (string path, Request? request = ()) returns (Response | HttpConnectorError) {
   CallerActions httpClient = self.httpClient;
   CircuitBreakerInferredConfig cbic = self.circuitBreakerInferredConfig;
   self.currentCircuitState = updateCircuitState(self.circuitHealth, self.currentCircuitState, cbic);

   if (self.currentCircuitState == CB_OPEN_STATE) {
       // TODO: Allow the user to handle this scenario. Maybe through a user provided function
       return handleOpenCircuit(self.circuitHealth, self.circuitBreakerInferredConfig);
   } else {
       match httpClient.delete(path, request = request) {
            Response service_response => {
                                    updateCircuitHealthSuccess(self.circuitHealth, service_response, self.circuitBreakerInferredConfig);
                                    return service_response;
                                }
            HttpConnectorError serviceError => {
                                    updateCircuitHealthFailure(self.circuitHealth, serviceError, self.circuitBreakerInferredConfig);
                                    return serviceError;
                                }
        }
    }
}

@Description {value:"The GET action implementation of the Circuit Breaker. Protects the invocation of the GET action of the underlying HTTP client connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the action invocation, if any"}
public function CircuitBreakerClient::get (string path, Request? request = ()) returns (Response | HttpConnectorError) {
    CallerActions httpClient = self.httpClient;
    CircuitBreakerInferredConfig cbic = self.circuitBreakerInferredConfig;
    self.currentCircuitState = updateCircuitState(self.circuitHealth, self.currentCircuitState, cbic);

    if (self.currentCircuitState == CB_OPEN_STATE) {
        // TODO: Allow the user to handle this scenario. Maybe through a user provided function
        return handleOpenCircuit(self.circuitHealth, self.circuitBreakerInferredConfig);
    } else {
       match httpClient.get(path, request = request) {
            Response service_response => {
                                    updateCircuitHealthSuccess(self.circuitHealth, service_response, self.circuitBreakerInferredConfig);
                                    return service_response;
                                }
            HttpConnectorError serviceError => {
                                    updateCircuitHealthFailure(self.circuitHealth, serviceError, self.circuitBreakerInferredConfig);
                                    return serviceError;
                                }
        }
    }
}

@Description {value:"The OPTIONS action implementation of the Circuit Breaker. Protects the invocation of the OPTIONS action of the underlying HTTP client connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the action invocation, if any"}
public function CircuitBreakerClient::options (string path, Request? request = ()) returns (Response | HttpConnectorError) {
   CallerActions httpClient = self.httpClient;
   CircuitBreakerInferredConfig cbic = self.circuitBreakerInferredConfig;
   self.currentCircuitState = updateCircuitState(self.circuitHealth, self.currentCircuitState, cbic);

   if (self.currentCircuitState == CB_OPEN_STATE) {
       // TODO: Allow the user to handle this scenario. Maybe through a user provided function
       return handleOpenCircuit(self.circuitHealth, self.circuitBreakerInferredConfig);
   } else {
       match httpClient.options(path, request = request) {
            Response service_response => {
                                    updateCircuitHealthSuccess(self.circuitHealth, service_response, self.circuitBreakerInferredConfig);
                                    return service_response;
                                }
            HttpConnectorError serviceError => {
                                    updateCircuitHealthFailure(self.circuitHealth, serviceError, self.circuitBreakerInferredConfig);
                                    return serviceError;
                                }
        }
    }
}

@Description {value:"Protects the invocation of the Forward action of the underlying HTTP client connector. The Forward action can be used to forward an incoming request to an upstream service as it is."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the action invocation, if any"}
public function CircuitBreakerClient::forward (string path, Request request) returns (Response | HttpConnectorError) {
   CallerActions httpClient = self.httpClient;
   CircuitBreakerInferredConfig cbic = self.circuitBreakerInferredConfig;
   self.currentCircuitState = updateCircuitState(self.circuitHealth, self.currentCircuitState, cbic);

   if (self.currentCircuitState == CB_OPEN_STATE) {
       // TODO: Allow the user to handle this scenario. Maybe through a user provided function
       return handleOpenCircuit(self.circuitHealth, self.circuitBreakerInferredConfig);
   } else {
       match httpClient.forward(path, request) {
            Response service_response => {
                                    updateCircuitHealthSuccess(self.circuitHealth, service_response, self.circuitBreakerInferredConfig);
                                    return service_response;
                                }
            HttpConnectorError serviceError => {
                                    updateCircuitHealthFailure(self.circuitHealth, serviceError, self.circuitBreakerInferredConfig);
                                    return serviceError;
                                }
        }
   }
}

@Description { value:"The submit implementation of Circuit Breaker."}
@Param { value:"httpVerb: The HTTP verb value" }
@Param { value:"path: The Resource path " }
@Param { value:"req: An HTTP outbound request message" }
@Return { value:"The Future for further interactions" }
@Return { value:"The Error occured during HTTP client invocation" }
public function CircuitBreakerClient::submit (string httpVerb, string path, Request request) returns (HttpFuture | HttpConnectorError) {
   HttpConnectorError httpConnectorError = {message:"Unsupported action for Circuit breaker"};
   return httpConnectorError;
}

@Description { value:"The getResponse implementation of Circuit Breaker."}
@Param { value:"httpFuture: The Future which relates to previous async invocation" }
@Return { value:"The HTTP response message" }
@Return { value:"The Error occured during HTTP client invocation" }
public function CircuitBreakerClient::getResponse (HttpFuture httpFuture) returns (Response | HttpConnectorError) {
   HttpConnectorError httpConnectorError = {message:"Unsupported action for Circuit breaker"};
   return httpConnectorError;
}

@Description { value:"The hasPromise implementation of Circuit Breaker."}
@Param { value:"httpFuture: The Future which relates to previous async invocation" }
@Return { value:"Whether push promise exists" }
public function CircuitBreakerClient::hasPromise (HttpFuture httpFuture) returns (boolean) {
   return false;
}

@Description { value:"The getNextPromise implementation of Circuit Breaker."}
@Param { value:"httpFuture: The Future which relates to previous async invocation" }
@Return { value:"The HTTP Push Promise message" }
@Return { value:"The Error occured during HTTP client invocation" }
public function CircuitBreakerClient::getNextPromise (HttpFuture httpFuture) returns (PushPromise | HttpConnectorError) {
   HttpConnectorError httpConnectorError = {message:"Unsupported action for Circuit breaker"};
   return httpConnectorError;
}

@Description { value:"The getPromisedResponse implementation of Circuit Breaker."}
@Param { value:"promise: The related Push Promise message" }
@Return { value:"HTTP The Push Response message" }
@Return { value:"The Error occured during HTTP client invocation" }
public function CircuitBreakerClient::getPromisedResponse (PushPromise promise) returns (Response | HttpConnectorError) {
   HttpConnectorError httpConnectorError = {message:"Unsupported action for Circuit breaker"};
   return httpConnectorError;
}

@Description { value:"The rejectPromise implementation of Circuit Breaker."}
@Param { value:"promise: The Push Promise need to be rejected" }
public function CircuitBreakerClient::rejectPromise (PushPromise promise) {
}

public function updateCircuitState (CircuitHealth circuitHealth, CircuitState currentStateValue,
                                    CircuitBreakerInferredConfig circuitBreakerInferredConfig) returns (CircuitState) {
    CircuitState currentState = currentStateValue;
    lock {
       if (currentState == CB_OPEN_STATE) {
           time:Time currentT = time:currentTime();
           int elapsedTime = currentT.time - circuitHealth.lastErrorTime.time;

           if (elapsedTime > circuitBreakerInferredConfig.resetTimeMillis) {
               circuitHealth.errorCount = 0;
               circuitHealth.requestCount = 0;
               currentState = CB_HALF_OPEN_STATE;
               log:printInfo("CircuitBreaker reset timeout reached. Circuit switched from OPEN to HALF_OPEN state.");
           }

       } else if (currentState == CB_HALF_OPEN_STATE) {
           if (circuitHealth.errorCount > 0) {
               // If the trial run has failed, trip the circuit again
               currentState = CB_OPEN_STATE;
               log:printInfo("CircuitBreaker trial run has failed. Circuit switched from HALF_OPEN to OPEN state.");
           } else {
               // If the trial run was successful reset the circuit
               currentState = CB_CLOSED_STATE;
               log:printInfo("CircuitBreaker trial run  was successful. Circuit switched from HALF_OPEN to CLOSE state.");
           }
       } else {
           float currentFailureRate = 0;

           if (circuitHealth.requestCount > 0 && circuitHealth.errorCount > 0) {
               currentFailureRate = getcurrentFailureRatio(circuitHealth);
           }

           if (currentFailureRate > circuitBreakerInferredConfig.failureThreshold) {
               currentState = CB_OPEN_STATE;
               log:printInfo("CircuitBreaker failure threshold exceeded. Circuit tripped from CLOSE to OPEN state.");
           }
       }
   }
   return currentState;
}

function updateCircuitHealthFailure(CircuitHealth circuitHealth,
                             HttpConnectorError httpConnectorError, CircuitBreakerInferredConfig circuitBreakerInferredConfig) {
    lock {
        time:Time startTime = circuitHealth.startTime;
        time:Time currentTime = time:currentTime();
        int elapsedTime = (currentTime.time - startTime.time) % circuitBreakerInferredConfig.rollingWindow.timeWindowMillis;
        int currentBucketId = ((elapsedTime/circuitBreakerInferredConfig.rollingWindow.bucketSizeMillis) + 1 )
                              % circuitBreakerInferredConfig.noOfBuckets;
        if (currentBucketId != circuitHealth.lastUsedBucketId) {
            resetBucketStats(circuitHealth, currentBucketId);
            circuitHealth.lastUsedBucketId = currentBucketId;
        }
        circuitHealth.totalBuckets[currentBucketId].failureCount = circuitHealth.totalBuckets[currentBucketId].failureCount + 1;
        int lastBucketId = currentBucketId;
        circuitHealth.requestCount = circuitHealth.requestCount + 1;
        circuitHealth.errorCount = circuitHealth.errorCount + 1;
        circuitHealth.lastErrorTime = time:currentTime();
    }
}

function updateCircuitHealthSuccess(CircuitHealth circuitHealth, Response inResponse,
                                            CircuitBreakerInferredConfig circuitBreakerInferredConfig) {
    lock {
        time:Time startTime = circuitHealth.startTime;
        time:Time currentTime = time:currentTime();
        int elapsedTime = (currentTime.time - startTime.time) % circuitBreakerInferredConfig.rollingWindow.timeWindowMillis;
        int currentBucketId = ((elapsedTime/circuitBreakerInferredConfig.rollingWindow.bucketSizeMillis) + 1 )
                              % circuitBreakerInferredConfig.noOfBuckets;
        if (currentBucketId != circuitHealth.lastUsedBucketId) {
            resetBucketStats(circuitHealth, currentBucketId);
            circuitHealth.lastUsedBucketId = currentBucketId;
        }
        circuitHealth.requestCount = circuitHealth.requestCount + 1;
        if (circuitBreakerInferredConfig.statusCodes[inResponse.statusCode] == true) {
            circuitHealth.totalBuckets[currentBucketId].failureCount = circuitHealth.totalBuckets[currentBucketId].failureCount + 1;
            circuitHealth.errorCount = circuitHealth.errorCount + 1;
            circuitHealth.lastErrorTime = time:currentTime();
        } else {
            circuitHealth.totalBuckets[currentBucketId].successCount = circuitHealth.totalBuckets[currentBucketId].successCount + 1;
        }
    }
}

// Handles open circuit state.
function handleOpenCircuit (CircuitHealth circuitHealth, CircuitBreakerInferredConfig circuitBreakerInferredConfig) returns (HttpConnectorError) {
   time:Time currentT = time:currentTime();
   int timeDif = currentT.time - circuitHealth.lastErrorTime.time;
   int timeRemaining = circuitBreakerInferredConfig.resetTimeMillis - timeDif;
   string errorMessage = "Upstream service unavailable. Requests to upstream service will be suspended for "
             + timeRemaining + " milliseconds.";
   HttpConnectorError httpConnectorError = {message:errorMessage};
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

documentation {
    Calculate Failure at a given point.

    P{{circuitHealth}}  - Circuit Breaker health status.
}
public function getcurrentFailureRatio(CircuitHealth circuitHealth) returns float {
    int totalSuccess;
    int totalFailures;

    foreach bucket in circuitHealth.totalBuckets {
        totalSuccess  =  totalSuccess + bucket.successCount;
        totalFailures = totalFailures + bucket.failureCount;
    }
    float ratio = 0;
    if (totalFailures > 0) {
        ratio = <float> totalFailures / (totalSuccess + totalFailures);
    }
    return ratio;
}

documentation {
    Reset the bucket values to default ones.

    P{{circuitHealth}}  - Circuit Breaker health status.
    P{{bucketId}}  - Id of the bucket should reset.
}
public function resetBucketStats (CircuitHealth circuitHealth, int bucketId) {
    circuitHealth.totalBuckets[bucketId].successCount = 0;
    circuitHealth.totalBuckets[bucketId].failureCount = 0;
}

// documentation {
//     Initializes the RollingWindow struct with default values.

//     T{{rollingWindowConfig}}  - The RollingWindow struct to be initialized.
// }
// public function <RollingWindow rollingWindowConfig> RollingWindow() {
//     rollingWindowConfig.timeWindow = 60000;
//     rollingWindowConfig.bucketSize = 10000;
// }

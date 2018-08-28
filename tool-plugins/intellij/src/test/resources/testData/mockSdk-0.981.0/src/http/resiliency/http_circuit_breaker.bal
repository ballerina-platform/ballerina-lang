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
    A finite type for modeling the states of the Circuit Breaker. The Circuit Breaker starts in the `CLOSED` state.
    If any failure thresholds are exceeded during execution, the circuit trips and goes to the `OPEN` state. After
    the specified timeout period expires, the circuit goes to the `HALF_OPEN` state. If the trial request sent while
    in the `HALF_OPEN` state succeeds, the circuit goes back to the `CLOSED` state.

}
public type CircuitState "OPEN" | "HALF_OPEN" | "CLOSED";

documentation {
    Represents the open state of the circuit. When the Circuit Breaker is in `OPEN` state, requests will fail
    immediately.
}
@final public CircuitState CB_OPEN_STATE = "OPEN";

documentation {
    Represents the half-open state of the circuit. When the Circuit Breaker is in `HALF_OPEN` state, a trial request
    will be sent to the upstream service. If it fails, the circuit will trip again and move to the `OPEN` state. If not,
    it will move to the `CLOSED` state.
}
@final public CircuitState CB_HALF_OPEN_STATE = "HALF_OPEN";

documentation {
    Represents the closed state of the circuit. When the Circuit Breaker is in `CLOSED` state, all requests will be
    allowed to go through to the upstream service. If the failures exceed the configured threhold values, the circuit
    will trip and move to the `OPEN` state.
}
@final public CircuitState CB_CLOSED_STATE = "CLOSED";

documentation {
    Maintains the health of the Circuit Breaker.

    F{{lastRequestSuccess}} Whether last request is success or not
    F{{totalRequestCount}} Total request count received within the `RollingWindow`
    F{{lastUsedBucketId}} ID of the last bucket used in Circuit Breaker calculations
    F{{startTime}} Circuit Breaker start time
    F{{lastRequestTime}} The time that the last request received
    F{{lastErrorTime}} The time that the last error occurred
    F{{lastForcedOpenTime}} The time that circuit forcefully opened at last
    F{{totalBuckets}} The discrete time buckets into which the time window is divided
}
public type CircuitHealth record {
    boolean lastRequestSuccess,
    int totalRequestCount,
    int lastUsedBucketId,
    time:Time startTime,
    time:Time lastRequestTime,
    time:Time lastErrorTime,
    time:Time lastForcedOpenTime,
    Bucket[] totalBuckets,
};

documentation {
    Provides a set of configurations for controlling the behaviour of the Circuit Breaker.

    F{{rollingWindow}} `RollingWindow` options of the `CircuitBreaker`
    F{{failureThreshold}} The threshold for request failures. When this threshold exceeds, the circuit trips.
                          The threshold should be a value between 0 and 1.
    F{{resetTimeMillis}} The time period(in milliseconds) to wait before attempting to make another request to
                         the upstream service
    F{{statusCodes}} Array of HTTP response status codes which are considered as failures
}
public type CircuitBreakerConfig record {
    RollingWindow rollingWindow,
    float failureThreshold,
    int resetTimeMillis,
    int[] statusCodes,
};

documentation {
    Represents a rolling window in the Circuit Breaker.

    F{{requestVolumeThreshold}} Minimum number of requests in a `RollingWindow` that will trip the circuit.
    F{{timeWindowMillis}} Time period in milliseconds for which the failure threshold is calculated
    F{{bucketSizeMillis}} The granularity at which the time window slides. This is measured in milliseconds.
}
public type RollingWindow record {
    int requestVolumeThreshold = 10,
    int timeWindowMillis = 60000,
    int bucketSizeMillis = 10000,
};

documentation {
    Represents a discrete sub-part of the time window (Bucket).

    F{{totalCount}} Total number of requests received during the sub-window time frame
    F{{failureCount}} Number of failed requests during the sub-window time frame
    F{{rejectedCount}} Number of rejected requests during the sub-window time frame
    F{{lastUpdatedTime}} The time that the `Bucket` is last updated.
}
public type Bucket record {
    int totalCount,
    int failureCount,
    int rejectedCount,
    time:Time lastUpdatedTime,
};

documentation {
    Derived set of configurations from the `CircuitBreakerConfig`.

    F{{failureThreshold}} The threshold for request failures. When this threshold exceeds, the circuit trips.
                          The threshold should be a value between 0 and 1
    F{{resetTimeMillis}} The time period(in milliseconds) to wait before attempting to make another request to
                         the upstream service
    F{{statusCodes}} Array of HTTP response status codes which are considered as failures
    F{{noOfBuckets}} Number of buckets derived from the `RollingWindow`
    F{{rollingWindow}} `RollingWindow` options provided in the `CircuitBreakerConfig`
}
public type CircuitBreakerInferredConfig record {
    float failureThreshold,
    int resetTimeMillis,
    boolean[] statusCodes,
    int noOfBuckets,
    RollingWindow rollingWindow,
};

documentation {
    A Circuit Breaker implementation which can be used to gracefully handle network failures.

    F{{serviceUri}} The URL of the target service
    F{{config}} The configurations of the client endpoint associated with this `CircuitBreaker` instance
    F{{circuitBreakerInferredConfig}} Configurations derived from `CircuitBreakerConfig`
    F{{httpClient}}  The underlying `HttpActions` instance which will be making the actual network calls
    F{{circuitHealth}} The circuit health monitor
    F{{currentCircuitState}} The current state the cicuit is in
}
public type CircuitBreakerClient object {

    public string serviceUri;
    public ClientEndpointConfig config;
    public CircuitBreakerInferredConfig circuitBreakerInferredConfig;
    public CallerActions httpClient;
    public CircuitHealth circuitHealth;
    public CircuitState currentCircuitState = CB_CLOSED_STATE;

    documentation {
        A Circuit Breaker implementation which can be used to gracefully handle network failures.

        P{{serviceUri}} The URL of the target service
        P{{config}} The configurations of the client endpoint associated with this `CircuitBreaker` instance
        P{{circuitBreakerInferredConfig}} Configurations derived from `CircuitBreakerConfig`
        P{{httpClient}}  The underlying `HttpActions` instance which will be making the actual network calls
        P{{circuitHealth}} The circuit health monitor
    }
    public new (serviceUri, config, circuitBreakerInferredConfig, httpClient, circuitHealth) {

    }

    documentation {
        The POST action implementation of the Circuit Breaker. This wraps the `post()` function of the underlying
        HTTP actions provider.

        P{{path}} Resource path
        P{{message}} A Request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel` or `mime:Entity[]`
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public function post(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
        message) returns Response|error;

    documentation {
        The HEAD action implementation of the Circuit Breaker. This wraps the `head()` function of the underlying
        HTTP actions provider.

        P{{path}} Resource path
        P{{message}} A Request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel` or `mime:Entity[]`
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
     }
    public function head(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
        message = ()) returns Response|error;

    documentation {
        The PUT action implementation of the Circuit Breaker. This wraps the `put()` function of the underlying
        HTTP actions provider.

        P{{path}} Resource path
        P{{message}} A Request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel` or `mime:Entity[]`
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public function put(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
        message) returns Response|error;

    documentation {
        This wraps the `post()` function of the underlying HTTP actions provider. The `execute()` function can be used
        to invoke an HTTP call with the given HTTP verb.

        P{{httpVerb}} HTTP verb to be used for the request
        P{{path}} Resource path
        P{{message}} A Request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel` or `mime:Entity[]`
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public function execute(string httpVerb, string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
        message) returns Response|error;

    documentation {
        The PATCH action implementation of the Circuit Breaker. This wraps the `patch()` function of the underlying
        HTTP actions provider.

        P{{path}} Resource path
        P{{message}} A Request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel` or `mime:Entity[]`
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public function patch(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
        message) returns Response|error;

    documentation {
        The DELETE action implementation of the Circuit Breaker. This wraps the `delete()` function of the underlying
        HTTP actions provider.

        P{{path}} Resource path
        P{{message}} A Request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel` or `mime:Entity[]`
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public function delete(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
        message) returns Response|error;

    documentation {
        The GET action implementation of the Circuit Breaker. This wraps the `get()` function of the underlying
        HTTP actions provider.

        P{{path}} Resource path
        P{{message}} An optional HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
                     or `mime:Entity[]`
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public function get(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
        message = ()) returns Response|error;

    documentation {
        The OPTIONS action implementation of the Circuit Breaker. This wraps the `options()` function of the underlying
        HTTP actions provider.

        P{{path}} Resource path
        P{{message}} An optional HTTP Request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
                     or `mime:Entity[]`
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public function options(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
        message = ()) returns Response|error;

    documentation {
        This wraps the `forward()` function of the underlying HTTP actions provider. The Forward action can be used to
        forward an incoming request to an upstream service as it is.

        P{{path}} Resource path
        P{{request}} A Request struct
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public function forward(string path, Request request) returns Response|error;

    documentation {
        Circuit breaking not supported. Defaults to the `submit()` function of the underlying HTTP actions provider.

        P{{httpVerb}} The HTTP verb value
        P{{path}} The resource path
        P{{message}} An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
                     `io:ByteChannel` or `mime:Entity[]`
        R{{}} An `HttpFuture` that represents an asynchronous service invocation, or an `error` if the submission fails
    }
    public function submit(string httpVerb, string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
        message) returns HttpFuture|error;

    documentation {
        Circuit breaking not supported. Defaults to the `getResponse()` function of the underlying HTTP
        actions provider.

        P{{httpFuture}} The `HttpFuture` related to a previous asynchronous invocation
        R{{}} An HTTP response message, or an `error` if the invocation fails
    }
    public function getResponse(HttpFuture httpFuture) returns Response|error;

    documentation {
        Circuit breaking not supported. Defaults to the `hasPromise()` function of the underlying HTTP actions provider.

        P{{httpFuture}} The `HttpFuture` relates to a previous asynchronous invocation
        R{{}} A `boolean` that represents whether a `PushPromise` exists
    }
    public function hasPromise(HttpFuture httpFuture) returns (boolean);

    documentation {
        Circuit breaking not supported. Defaults to the `getNextPromise()` function of the underlying HTTP
        actions provider.

        P{{httpFuture}} The `HttpFuture` relates to a previous asynchronous invocation
        R{{}} An HTTP `PushPromise` message, or an `error` if the invocation fails
    }
    public function getNextPromise(HttpFuture httpFuture) returns PushPromise|error;

    documentation {
        Circuit breaking not supported. Defaults to the `getPromisedResponse()` function of the underlying HTTP
        actions provider.

        P{{promise}} The related `PushPromise`
        R{{}} A promised HTTP `Response` message, or an `error` if the invocation fails
    }
    public function getPromisedResponse(PushPromise promise) returns Response|error;

    documentation {
        Circuit breaking not supported. Defaults to the `rejectPromise()` function of the underlying HTTP
        actions provider.

        P{{promise}} The `PushPromise` to be rejected
    }
    public function rejectPromise(PushPromise promise);

    documentation {
        Force the circuit into a closed state in which it will allow requests regardless of the error percentage
        until the failure threshold exceeds.
    }
    public function forceClose();

    documentation {
        Force the circuit into a open state in which it will suspend all requests
        until `resetTimeMillis` interval exceeds.
    }
    public function forceOpen();

    documentation {
        Provides `CircuitState` of the circuit breaker.

        R{{}} The current `CircuitState` of circuit breaker
    }
    public function getCurrentState() returns CircuitState;
};

function CircuitBreakerClient::post(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
    message) returns Response|error {
    Request req = buildRequest(message);
    CallerActions httpClient = self.httpClient;
    CircuitBreakerInferredConfig cbic = self.circuitBreakerInferredConfig;
    self.currentCircuitState = updateCircuitState(self.circuitHealth, self.currentCircuitState, cbic);

    if (self.currentCircuitState == CB_OPEN_STATE) {
        // TODO: Allow the user to handle this scenario. Maybe through a user provided function
        return handleOpenCircuit(self.circuitHealth, self.circuitBreakerInferredConfig);
    } else {
        match httpClient.post(path, req) {
            Response service_response => {
                updateCircuitHealthSuccess(self.circuitHealth, service_response, self.circuitBreakerInferredConfig);
                return service_response;
            }
            error serviceError => {
                updateCircuitHealthFailure(self.circuitHealth, serviceError, self.circuitBreakerInferredConfig);
                return serviceError;
            }
        }
    }
}

function CircuitBreakerClient::head(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
    message = ()) returns Response|error {
    Request request = buildRequest(message);
    CallerActions httpClient = self.httpClient;
    CircuitBreakerInferredConfig cbic = self.circuitBreakerInferredConfig;
    self.currentCircuitState = updateCircuitState(self.circuitHealth, self.currentCircuitState, cbic);

    if (self.currentCircuitState == CB_OPEN_STATE) {
        // TODO: Allow the user to handle this scenario. Maybe through a user provided function
        return handleOpenCircuit(self.circuitHealth, self.circuitBreakerInferredConfig);
    } else {
        match httpClient.head(path, message = request) {
            Response service_response => {
                updateCircuitHealthSuccess(self.circuitHealth, service_response, self.circuitBreakerInferredConfig);
                return service_response;
            }
            error serviceError => {
                updateCircuitHealthFailure(self.circuitHealth, serviceError, self.circuitBreakerInferredConfig);
                return serviceError;
            }
        }
    }
}

function CircuitBreakerClient::put(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
    message) returns Response|error {
    Request request = buildRequest(message);
    CallerActions httpClient = self.httpClient;
    CircuitBreakerInferredConfig cbic = self.circuitBreakerInferredConfig;
    self.currentCircuitState = updateCircuitState(self.circuitHealth, self.currentCircuitState, cbic);

    if (self.currentCircuitState == CB_OPEN_STATE) {
        // TODO: Allow the user to handle this scenario. Maybe through a user provided function
        return handleOpenCircuit(self.circuitHealth, self.circuitBreakerInferredConfig);
    } else {
        match httpClient.put(path, request) {
            Response service_response => {
                updateCircuitHealthSuccess(self.circuitHealth, service_response, self.circuitBreakerInferredConfig);
                return service_response;
            }
            error serviceError => {
                updateCircuitHealthFailure(self.circuitHealth, serviceError, self.circuitBreakerInferredConfig);
                return serviceError;
            }
        }
    }
}

function CircuitBreakerClient::execute(string httpVerb, string path, Request|string|xml|json|byte[]|
    io:ByteChannel|mime:Entity[]|() message) returns Response|error {
    Request request = buildRequest(message);
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
            error serviceError => {
                updateCircuitHealthFailure(self.circuitHealth, serviceError, self.circuitBreakerInferredConfig);
                return serviceError;
            }
        }
    }
}

function CircuitBreakerClient::patch(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
    message) returns Response|error {
    Request request = buildRequest(message);
    CallerActions httpClient = self.httpClient;
    CircuitBreakerInferredConfig cbic = self.circuitBreakerInferredConfig;
    self.currentCircuitState = updateCircuitState(self.circuitHealth, self.currentCircuitState, cbic);

    if (self.currentCircuitState == CB_OPEN_STATE) {
        // TODO: Allow the user to handle this scenario. Maybe through a user provided function
        return handleOpenCircuit(self.circuitHealth, self.circuitBreakerInferredConfig);
    } else {
        match httpClient.patch(path, request) {
            Response service_response => {
                updateCircuitHealthSuccess(self.circuitHealth, service_response, self.circuitBreakerInferredConfig);
                return service_response;
            }
            error serviceError => {
                updateCircuitHealthFailure(self.circuitHealth, serviceError, self.circuitBreakerInferredConfig);
                return serviceError;
            }
        }
    }
}

function CircuitBreakerClient::delete(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
    message) returns Response|error {
    Request request = buildRequest(message);
    CallerActions httpClient = self.httpClient;
    CircuitBreakerInferredConfig cbic = self.circuitBreakerInferredConfig;
    self.currentCircuitState = updateCircuitState(self.circuitHealth, self.currentCircuitState, cbic);

    if (self.currentCircuitState == CB_OPEN_STATE) {
        // TODO: Allow the user to handle this scenario. Maybe through a user provided function
        return handleOpenCircuit(self.circuitHealth, self.circuitBreakerInferredConfig);
    } else {
        match httpClient.delete(path, request) {
            Response service_response => {
                updateCircuitHealthSuccess(self.circuitHealth, service_response, self.circuitBreakerInferredConfig);
                return service_response;
            }
            error serviceError => {
                updateCircuitHealthFailure(self.circuitHealth, serviceError, self.circuitBreakerInferredConfig);
                return serviceError;
            }
        }
    }
}

function CircuitBreakerClient::get(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
    message = ()) returns Response|error {
    Request request = buildRequest(message);
    CallerActions httpClient = self.httpClient;
    CircuitBreakerInferredConfig cbic = self.circuitBreakerInferredConfig;
    self.currentCircuitState = updateCircuitState(self.circuitHealth, self.currentCircuitState, cbic);

    if (self.currentCircuitState == CB_OPEN_STATE) {
        // TODO: Allow the user to handle this scenario. Maybe through a user provided function
        return handleOpenCircuit(self.circuitHealth, self.circuitBreakerInferredConfig);
    } else {
        match httpClient.get(path, message = request) {
            Response service_response => {
                updateCircuitHealthSuccess(self.circuitHealth, service_response, self.circuitBreakerInferredConfig);
                return service_response;
            }
            error serviceError => {
                updateCircuitHealthFailure(self.circuitHealth, serviceError, self.circuitBreakerInferredConfig);
                return serviceError;
            }
        }
    }
}

function CircuitBreakerClient::options(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
    message = ()) returns Response|error {
    Request request = buildRequest(message);
    CallerActions httpClient = self.httpClient;
    CircuitBreakerInferredConfig cbic = self.circuitBreakerInferredConfig;
    self.currentCircuitState = updateCircuitState(self.circuitHealth, self.currentCircuitState, cbic);

    if (self.currentCircuitState == CB_OPEN_STATE) {
        // TODO: Allow the user to handle this scenario. Maybe through a user provided function
        return handleOpenCircuit(self.circuitHealth, self.circuitBreakerInferredConfig);
    } else {
        match httpClient.options(path, message = request) {
            Response service_response => {
                updateCircuitHealthSuccess(self.circuitHealth, service_response, self.circuitBreakerInferredConfig);
                return service_response;
            }
            error serviceError => {
                updateCircuitHealthFailure(self.circuitHealth, serviceError, self.circuitBreakerInferredConfig);
                return serviceError;
            }
        }
    }
}

function CircuitBreakerClient::forward(string path, Request request) returns Response|error {
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
            error serviceError => {
                updateCircuitHealthFailure(self.circuitHealth, serviceError, self.circuitBreakerInferredConfig);
                return serviceError;
            }
        }
    }
}

function CircuitBreakerClient::submit(string httpVerb, string path, Request|string|xml|json|byte[]|
    io:ByteChannel|mime:Entity[]|() message) returns HttpFuture|error {
    Request request = buildRequest(message);
    return self.httpClient.submit(httpVerb, path, request);
}

function CircuitBreakerClient::getResponse(HttpFuture httpFuture) returns Response|error {
    return self.httpClient.getResponse(httpFuture);
}

function CircuitBreakerClient::hasPromise(HttpFuture httpFuture) returns boolean {
    return self.httpClient.hasPromise(httpFuture);
}

function CircuitBreakerClient::getNextPromise(HttpFuture httpFuture) returns PushPromise|error {
    return self.httpClient.getNextPromise(httpFuture);
}

function CircuitBreakerClient::getPromisedResponse(PushPromise promise) returns Response|error {
    return self.httpClient.getPromisedResponse(promise);
}

function CircuitBreakerClient::rejectPromise(PushPromise promise) {
    return self.httpClient.rejectPromise(promise);
}

function CircuitBreakerClient::forceClose() {
    self.currentCircuitState = CB_CLOSED_STATE;
}

function CircuitBreakerClient::forceOpen() {
    self.currentCircuitState = CB_OPEN_STATE;
    self.circuitHealth.lastForcedOpenTime = time:currentTime();
}

function CircuitBreakerClient::getCurrentState() returns CircuitState {
    return self.currentCircuitState;
}

documentation {
    Update circuit state.

    P{{circuitHealth}}  Circuit Breaker health status
    P{{currentStateValue}}  Circuit Breaker current state value
    P{{circuitBreakerInferredConfig}}   Configurations derived from `CircuitBreakerConfig`
    R{{}} State of the circuit
}
function updateCircuitState(CircuitHealth circuitHealth, CircuitState currentStateValue,
                            CircuitBreakerInferredConfig circuitBreakerInferredConfig) returns (CircuitState) {
    lock {
        CircuitState currentState = currentStateValue;
        prepareRollingWindow(circuitHealth, circuitBreakerInferredConfig);
        int currentBucketId = getCurrentBucketId(circuitHealth, circuitBreakerInferredConfig);
        updateLastUsedBucketId(currentBucketId, circuitHealth);
        circuitHealth.lastRequestTime = time:currentTime();
        int totalRequestsCount = getTotalRequestsCount(circuitHealth);
        circuitHealth.totalRequestCount = totalRequestsCount;
        if (totalRequestsCount >= circuitBreakerInferredConfig.rollingWindow.requestVolumeThreshold) {
            if (currentState == CB_OPEN_STATE) {
                currentState = switchCircuitStateOpenToHalfOpenOnResetTime(circuitBreakerInferredConfig,
                                                                                    circuitHealth, currentState);
            } else if (currentState == CB_HALF_OPEN_STATE) {
                if (!circuitHealth.lastRequestSuccess) {
                    // If the trial run has failed, trip the circuit again
                    currentState = CB_OPEN_STATE;
                    log:printInfo("CircuitBreaker trial run has failed. Circuit switched from HALF_OPEN to OPEN state.")
                    ;
                } else {
                    // If the trial run was successful reset the circuit
                    currentState = CB_CLOSED_STATE;
                    log:printInfo(
                        "CircuitBreaker trial run  was successful. Circuit switched from HALF_OPEN to CLOSE state.");
                }
            } else {
                float currentFailureRate = getCurrentFailureRatio(circuitHealth);

                if (currentFailureRate > circuitBreakerInferredConfig.failureThreshold) {
                    currentState = CB_OPEN_STATE;
                    log:printInfo("CircuitBreaker failure threshold exceeded. Circuit tripped from CLOSE to OPEN state."
                    );
                }
            }
        } else {
            currentState = switchCircuitStateOpenToHalfOpenOnResetTime(circuitBreakerInferredConfig,
                                                                                    circuitHealth, currentState);
        }
        circuitHealth.totalBuckets[currentBucketId].totalCount++;
        return currentState;
    }
}

function updateCircuitHealthFailure(CircuitHealth circuitHealth,
                                    error httpConnectorErr, CircuitBreakerInferredConfig circuitBreakerInferredConfig) {
    lock {
        int currentBucketId = getCurrentBucketId(circuitHealth, circuitBreakerInferredConfig);
        circuitHealth.lastRequestSuccess = false;
        updateLastUsedBucketId(currentBucketId, circuitHealth);
        circuitHealth.totalBuckets[currentBucketId].failureCount++;
        time:Time lastUpdated = time:currentTime();
        circuitHealth.lastErrorTime = lastUpdated;
        circuitHealth.totalBuckets[currentBucketId].lastUpdatedTime = lastUpdated;
    }
}

function updateCircuitHealthSuccess(CircuitHealth circuitHealth, Response inResponse,
                                    CircuitBreakerInferredConfig circuitBreakerInferredConfig) {
    lock {
        int currentBucketId = getCurrentBucketId(circuitHealth, circuitBreakerInferredConfig);
        time:Time lastUpdated = time:currentTime();
        updateLastUsedBucketId(currentBucketId, circuitHealth);
        if (circuitBreakerInferredConfig.statusCodes[inResponse.statusCode] == true) {
            circuitHealth.totalBuckets[currentBucketId].failureCount++;
            circuitHealth.lastRequestSuccess = false;
            circuitHealth.lastErrorTime = lastUpdated;
            circuitHealth.totalBuckets[currentBucketId].lastUpdatedTime = lastUpdated;
        } else {
            circuitHealth.lastRequestSuccess = true;
            circuitHealth.totalBuckets[currentBucketId].lastUpdatedTime = lastUpdated;
        }
    }
}

// Handles open circuit state.
function handleOpenCircuit(CircuitHealth circuitHealth, CircuitBreakerInferredConfig circuitBreakerInferredConfig)
             returns (error) {
    updateRejectedRequestCount(circuitHealth, circuitBreakerInferredConfig);
    time:Time effectiveErrorTime = getEffectiveErrorTime(circuitHealth);
    int timeDif = time:currentTime().time - effectiveErrorTime.time;
    int timeRemaining = circuitBreakerInferredConfig.resetTimeMillis - timeDif;
    string errorMessage = "Upstream service unavailable. Requests to upstream service will be suspended for "
        + timeRemaining + " milliseconds.";
    error httpConnectorErr = {message:errorMessage};
    return httpConnectorErr;
}

// Validates the struct configurations passed to create circuit breaker.
function validateCircuitBreakerConfiguration(CircuitBreakerConfig circuitBreakerConfig) {
    float failureThreshold = circuitBreakerConfig.failureThreshold;
    if (failureThreshold < 0 || failureThreshold > 1) {
        string errorMessage = "Invalid failure threshold. Failure threshold value"
            + " should between 0 to 1, found " + failureThreshold;
        error circuitBreakerConfigError = { message: errorMessage };
        throw circuitBreakerConfigError;
    }
}

documentation {
    Calculate Failure at a given point.

    P{{circuitHealth}}  Circuit Breaker health status
    R{{}} Current failure ratio
}
function getCurrentFailureRatio(CircuitHealth circuitHealth) returns float {
    int totalCount;
    int totalFailures;

    foreach bucket in circuitHealth.totalBuckets {
        totalCount =  totalCount + bucket.failureCount + (bucket.totalCount - (bucket.failureCount + bucket.rejectedCount));
        totalFailures = totalFailures + bucket.failureCount;
    }
    float ratio = 0;
    if (totalCount > 0) {
        ratio = <float> totalFailures / totalCount;
    }
    return ratio;
}

documentation {
    Calculate total requests count within `RollingWindow`.

    P{{circuitHealth}}  Circuit Breaker health status
    R{{}} Total requests count
}
function getTotalRequestsCount(CircuitHealth circuitHealth) returns int {
    int totalCount;

    foreach bucket in circuitHealth.totalBuckets {
        totalCount  =  totalCount + bucket.totalCount;
    }
    return totalCount;
}

documentation {
    Calculate the current bucket Id.

    P{{circuitHealth}}  Circuit Breaker health status
    P{{circuitBreakerInferredConfig}}   Configurations derived from `CircuitBreakerConfig`
    R{{}} Current bucket id
}
function getCurrentBucketId(CircuitHealth circuitHealth, CircuitBreakerInferredConfig circuitBreakerInferredConfig)
             returns int {
    int elapsedTime = (time:currentTime().time - circuitHealth.startTime.time) % circuitBreakerInferredConfig.
        rollingWindow.timeWindowMillis;
    int currentBucketId = ((elapsedTime / circuitBreakerInferredConfig.rollingWindow.bucketSizeMillis) + 1)
        % circuitBreakerInferredConfig.noOfBuckets;
    return currentBucketId;
}

documentation {
    Update rejected requests count.

    P{{circuitHealth}}  Circuit Breaker health status
    P{{circuitBreakerInferredConfig}}   Configurations derived from `CircuitBreakerConfig`
}
function updateRejectedRequestCount(CircuitHealth circuitHealth, CircuitBreakerInferredConfig circuitBreakerInferredConfig) {
    int currentBucketId = getCurrentBucketId(circuitHealth, circuitBreakerInferredConfig);
    updateLastUsedBucketId(currentBucketId, circuitHealth);
    circuitHealth.totalBuckets[currentBucketId].rejectedCount++;
}

documentation {
    Reset the bucket values to default ones.

    P{{circuitHealth}}  - Circuit Breaker health status.
    P{{bucketId}}  - Id of the bucket should reset.
}
function resetBucketStats(CircuitHealth circuitHealth, int bucketId) {
    circuitHealth.totalBuckets[bucketId] = {};
}

function getEffectiveErrorTime(CircuitHealth circuitHealth) returns time:Time {
    return (circuitHealth.lastErrorTime.time > circuitHealth.lastForcedOpenTime.time)
    ? circuitHealth.lastErrorTime : circuitHealth.lastForcedOpenTime;
}

documentation {
    Populate the `RollingWindow` statistics to handle circuit breaking within the `RollingWindow` time frame.

    P{{circuitHealth}}  Circuit Breaker health status
    P{{circuitBreakerInferredConfig}}   Configurations derived from `CircuitBreakerConfig`
}
function prepareRollingWindow(CircuitHealth circuitHealth, CircuitBreakerInferredConfig circuitBreakerInferredConfig) {

    int currentTime = time:currentTime().time;
    int idleTime = currentTime - circuitHealth.lastRequestTime.time;
    RollingWindow rollingWindow = circuitBreakerInferredConfig.rollingWindow;
    // If the time duration between two requests greater than timeWindowMillis values, reset the buckets to default.
    if (idleTime > rollingWindow.timeWindowMillis) {
        reInitializeBuckets(circuitHealth);
    } else {
        int currentBucketId = getCurrentBucketId(circuitHealth, circuitBreakerInferredConfig);
        int lastUsedBucketId = circuitHealth.lastUsedBucketId;
        // Check whether subsequent requests received within same bucket(sub time window). If the idle time is greater
        // than bucketSizeMillis means subsequent calls are received time exceeding the rolling window. if we need to
        // reset the buckets to default.
        if (currentBucketId == circuitHealth.lastUsedBucketId && idleTime > rollingWindow.bucketSizeMillis) {
            reInitializeBuckets(circuitHealth);
        // If the current bucket (sub time window) is less than last updated bucket. Stats of the current bucket to zeroth
        // bucket and Last bucket to last used bucket needs to be reset to default.
        } else if (currentBucketId < lastUsedBucketId) {
            int index = currentBucketId;
            while (index >= 0) {
                resetBucketStats(circuitHealth, index);
                index--;
            }
            int lastIndex = (lengthof circuitHealth.totalBuckets) - 1;
            while (lastIndex > currentBucketId) {
                resetBucketStats(circuitHealth, lastIndex);
                lastIndex--;
            }
        } else {
            // If the current bucket (sub time window) is greater than last updated bucket. Stats of current bucket to
            // last used bucket needs to be reset without resetting last used bucket stat.
            while (currentBucketId > lastUsedBucketId && idleTime > rollingWindow.bucketSizeMillis) {
                resetBucketStats(circuitHealth, currentBucketId);
                currentBucketId--;
            }
        }
    }
}

documentation {
    Reinitialize the Buckets to default state.

    P{{circuitHealth}}  Circuit Breaker health status
}
function reInitializeBuckets(CircuitHealth circuitHealth) {
    Bucket[] bucketArray = [];
    int bucketIndex = 0;
    while (bucketIndex < lengthof circuitHealth.totalBuckets) {
        bucketArray[bucketIndex] = {};
        bucketIndex++;
    }
    circuitHealth.totalBuckets = bucketArray;
}

documentation {
    Updates the `lastUsedBucketId` in `CircuitHealth`.

    P{{bucketId}}  Possition of the currrently used bucket
    P{{circuitHealth}}  Circuit Breaker health status
}
function updateLastUsedBucketId(int bucketId, CircuitHealth circuitHealth) {
    if (bucketId != circuitHealth.lastUsedBucketId) {
        resetBucketStats(circuitHealth, bucketId);
        circuitHealth.lastUsedBucketId = bucketId;
    }
}

documentation {
    Switches circuit state from open to half open state when reset time exceeded.

    P{{circuitBreakerInferredConfig}}  Configurations derived from `CircuitBreakerConfig`
    P{{circuitHealth}}  Circuit Breaker health status
    P{{currentState}}  current state of the circuit
    R{{}} Calculated state value of the circuit
}
function switchCircuitStateOpenToHalfOpenOnResetTime(CircuitBreakerInferredConfig circuitBreakerInferredConfig,
                                        CircuitHealth circuitHealth, CircuitState currentState) returns CircuitState {
    CircuitState currentCircuitState = currentState;
    if (currentState == CB_OPEN_STATE) {
        time:Time effectiveErrorTime = getEffectiveErrorTime(circuitHealth);
        int elapsedTime = time:currentTime().time - effectiveErrorTime.time;
        if (elapsedTime > circuitBreakerInferredConfig.resetTimeMillis) {
            currentCircuitState = CB_HALF_OPEN_STATE;
            log:printInfo("CircuitBreaker reset timeout reached. Circuit switched from OPEN to HALF_OPEN state.");
        }
    }
    return currentCircuitState;
}

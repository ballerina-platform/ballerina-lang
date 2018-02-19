package ballerina.net.http.resiliency;

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

@Description {value:"A Circuit Breaker implementation for to be used with the HTTP client connector to gracefully handle network errors"}
@Param {value:"httpClient: The HTTP client connector to be wrapped with the circuit breaker"}
@Param {value:"failureThreshold: The threshold for request failures. When this threshold is crossed, the circuit will trip. The threshold should be a value between 0 and 1."}
@Param {value:"resetTimeout: The time period to wait before attempting to make another request to the upstream service"}
// TODO: need to find a way to validate the arguments passed here
public connector CircuitBreaker (http:HttpClient httpClient, float failureThreshold, int resetTimeout) {

    endpoint<http:HttpClient> httpEP {
        httpClient;
    }

    CircuitHealth circuitHealth = {};
    // TODO: once enum init inside a struct is do-able, move this inside circuitHealth (issue #4340)
    CircuitState currentState = CircuitState.CLOSED;

    @Description {value:"The POST action implementation of the Circuit Breaker. Protects the invocation of the POST action of the underlying HTTP client connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: An OutRequest struct"}
    @Return {value:"The InResponse struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action post (string path, http:OutRequest request) (http:InResponse, http:HttpConnectorError) {
        currentState = updateCircuitState(circuitHealth, currentState, failureThreshold, resetTimeout);
        http:InResponse response;
        http:HttpConnectorError err;

        if (currentState == CircuitState.OPEN) {
            // TODO: Allow the user to handle this scenario. Maybe through a user provided function
            err = handleOpenCircuit(circuitHealth, resetTimeout);
        } else {
            response, err = httpEP.post(path, request);
            updateCircuitHealth(circuitHealth, err);
        }

        return response, err;
    }

    @Description {value:"The HEAD action implementation of the Circuit Breaker. Protects the invocation of the HEAD action of the underlying HTTP client connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: An OutRequest struct"}
    @Return {value:"The InResponse struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action head (string path, http:OutRequest request) (http:InResponse, http:HttpConnectorError) {
        currentState = updateCircuitState(circuitHealth, currentState, failureThreshold, resetTimeout);
        http:InResponse response;
        http:HttpConnectorError err;

        if (currentState == CircuitState.OPEN) {
            // TODO: Allow the user to handle this scenario. Maybe through a user provided function
            err = handleOpenCircuit(circuitHealth, resetTimeout);
        } else {
            response, err = httpEP.head(path, request);
            updateCircuitHealth(circuitHealth, err);
        }

        return response, err;
    }

    @Description {value:"The PUT action implementation of the Circuit Breaker. Protects the invocation of the PUT action of the underlying HTTP client connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: An OutRequest struct"}
    @Return {value:"The InResponse struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action put (string path, http:OutRequest request) (http:InResponse, http:HttpConnectorError) {
        currentState = updateCircuitState(circuitHealth, currentState, failureThreshold, resetTimeout);
        http:InResponse response;
        http:HttpConnectorError err;

        if (currentState == CircuitState.OPEN) {
            // TODO: Allow the user to handle this scenario. Maybe through a user provided function
            err = handleOpenCircuit(circuitHealth, resetTimeout);
        } else {
            response, err = httpEP.put(path, request);
            updateCircuitHealth(circuitHealth, err);
        }

        return response, err;
    }

    @Description {value:"Protects the invocation of the Execute action of the underlying HTTP client connector. The Execute action can be used to invoke an HTTP call with the given HTTP verb."}
    @Param {value:"httpVerb: HTTP verb to be used for the request"}
    @Param {value:"path: Resource path"}
    @Param {value:"request: An OutRequest struct"}
    @Return {value:"The InResponse struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action execute (string httpVerb, string path, http:OutRequest request) (http:InResponse, http:HttpConnectorError) {
        currentState = updateCircuitState(circuitHealth, currentState, failureThreshold, resetTimeout);
        http:InResponse response;
        http:HttpConnectorError err;

        if (currentState == CircuitState.OPEN) {
            // TODO: Allow the user to handle this scenario. Maybe through a user provided function
            err = handleOpenCircuit(circuitHealth, resetTimeout);
        } else {
            response, err = httpEP.execute(httpVerb, path, request);
            updateCircuitHealth(circuitHealth, err);
        }

        return response, err;
    }

    @Description {value:"The PATCH action implementation of the Circuit Breaker. Protects the invocation of the PATCH action of the underlying HTTP client connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: An OutRequest struct"}
    @Return {value:"The InResponse struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action patch (string path, http:OutRequest request) (http:InResponse, http:HttpConnectorError) {
        currentState = updateCircuitState(circuitHealth, currentState, failureThreshold, resetTimeout);
        http:InResponse response;
        http:HttpConnectorError err;

        if (currentState == CircuitState.OPEN) {
            // TODO: Allow the user to handle this scenario. Maybe through a user provided function
            err = handleOpenCircuit(circuitHealth, resetTimeout);
        } else {
            response, err = httpEP.patch(path, request);
            updateCircuitHealth(circuitHealth, err);
        }

        return response, err;
    }

    @Description {value:"The DELETE action implementation of the Circuit Breaker. Protects the invocation of the DELETE action of the underlying HTTP client connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: An OutRequest struct"}
    @Return {value:"The InResponse struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action delete (string path, http:OutRequest request) (http:InResponse, http:HttpConnectorError) {
        currentState = updateCircuitState(circuitHealth, currentState, failureThreshold, resetTimeout);
        http:InResponse response;
        http:HttpConnectorError err;

        if (currentState == CircuitState.OPEN) {
            // TODO: Allow the user to handle this scenario. Maybe through a user provided function
            err = handleOpenCircuit(circuitHealth, resetTimeout);
        } else {
            response, err = httpEP.delete(path, request);
            updateCircuitHealth(circuitHealth, err);
        }

        return response, err;
    }

    @Description {value:"The OPTIONS action implementation of the Circuit Breaker. Protects the invocation of the OPTIONS action of the underlying HTTP client connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: An OutRequest struct"}
    @Return {value:"The InResponse struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action options (string path, http:OutRequest request) (http:InResponse, http:HttpConnectorError) {
        currentState = updateCircuitState(circuitHealth, currentState, failureThreshold, resetTimeout);
        http:InResponse response;
        http:HttpConnectorError err;

        if (currentState == CircuitState.OPEN) {
            // TODO: Allow the user to handle this scenario. Maybe through a user provided function
            err = handleOpenCircuit(circuitHealth, resetTimeout);
        } else {
            response, err = httpEP.options(path, request);
            updateCircuitHealth(circuitHealth, err);
        }

        return response, err;
    }

    @Description {value:"Protects the invocation of the Forward action of the underlying HTTP client connector. The Forward action can be used to forward an incoming request to an upstream service as it is."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: An InRequest struct"}
    @Return {value:"The InResponse struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action forward (string path, http:InRequest request) (http:InResponse, http:HttpConnectorError) {
        currentState = updateCircuitState(circuitHealth, currentState, failureThreshold, resetTimeout);
        http:InResponse response;
        http:HttpConnectorError err;

        if (currentState == CircuitState.OPEN) {
            // TODO: Allow the user to handle this scenario. Maybe through a user provided function
            err = handleOpenCircuit(circuitHealth, resetTimeout);
        } else {
            response, err = httpEP.forward(path, request);
            updateCircuitHealth(circuitHealth, err);
        }

        return response, err;
    }

    @Description {value:"The GET action implementation of the Circuit Breaker. Protects the invocation of the GET action of the underlying HTTP client connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: An OutRequest struct"}
    @Return {value:"The InResponse struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action get (string path, http:OutRequest request) (http:InResponse, http:HttpConnectorError) {
        currentState = updateCircuitState(circuitHealth, currentState, failureThreshold, resetTimeout);
        http:InResponse response;
        http:HttpConnectorError err;

        if (currentState == CircuitState.OPEN) {
            // TODO: Allow the user to handle this scenario. Maybe through a user provided function
            err = handleOpenCircuit(circuitHealth, resetTimeout);
        } else {
            response, err = httpEP.get(path, request);
            updateCircuitHealth(circuitHealth, err);
        }

        return response, err;
    }
}

function updateCircuitState (CircuitHealth circuitHealth, CircuitState currentState, float failureThreshold,
                             int resetTimeout) (CircuitState) {
    if (currentState == CircuitState.OPEN) {
        time:Time currentT = time:currentTime();
        int elapsedTime = currentT.time - circuitHealth.lastErrorTime.time;

        if (elapsedTime > resetTimeout) {
            circuitHealth.errorCount = 0;
            circuitHealth.requestCount = 0;
            currentState = CircuitState.HALF_OPEN;
        }
    } else if (currentState == CircuitState.HALF_OPEN) {
        if (circuitHealth.errorCount > 0) {
            // If the trial run has failed, trip the circuit again
            currentState = CircuitState.OPEN;
        } else {
            // If the trial run was successful reset the circuit
            currentState = CircuitState.CLOSED;
        }
    } else {
        float currentFailureRate = 0;

        if (circuitHealth.requestCount > 0) {
            currentFailureRate = ((float) circuitHealth.errorCount / circuitHealth.requestCount);
        }

        if (currentFailureRate > failureThreshold) {
            currentState = CircuitState.OPEN;
        }
    }

    return currentState;
}

function updateCircuitHealth(CircuitHealth circuitHealth, http:HttpConnectorError err) {
    circuitHealth.requestCount = circuitHealth.requestCount + 1;

    if (err != null) {
        circuitHealth.errorCount = circuitHealth.errorCount + 1;
        circuitHealth.lastErrorTime = time:currentTime();
    }
}

function handleOpenCircuit (CircuitHealth circuitHealth, int resetTimeout) (http:HttpConnectorError) {
    time:Time currentT = time:currentTime();
    int timeDif = currentT.time - circuitHealth.lastErrorTime.time;
    int timeRemaining = resetTimeout - timeDif;

    http:HttpConnectorError err = {};
    err.message = "Upstream service unavailable. Requests to upstream service will be suspended for "
              + timeRemaining + " milliseconds.";
    return err;
}

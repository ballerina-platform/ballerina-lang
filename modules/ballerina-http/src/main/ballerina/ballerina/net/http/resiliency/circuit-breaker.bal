package ballerina.net.http.resiliency;

import ballerina.net.http;

enum CircuitState {
    OPEN, CLOSE, HALF_OPEN
}

struct CircuitHealth {
    float requestCount;
    float errorCount;
    Time lastErrorTime;
}

public connector CircuitBreaker (http:HttpClient httpClient, float failurePercentageThreshold, int resetTimeout) {

    endpoint<http:HttpClient> httpEP {
        httpClient;
    }

    CircuitHealth circuitHealth = {};
    // TODO: once enum init inside a struct is do-able, move this inside circuitHealth (issue #4340)
    CircuitState currentState = CircuitState.CLOSE;

    action post (string path, http:Request req) (http:Response, http:HttpConnectorError) {
        currentState = updateCircuitState(circuitHealth, currentState, failurePercentageThreshold, resetTimeout);
        http:Response res;
        http:HttpConnectorError e;

        if (currentState == CircuitState.CLOSE) {
            res, e = httpEP.post(path, req);
            circuitHealth.requestCount = circuitHealth.requestCount + 1;

            if (e != null) {
                circuitHealth.updateHealth();
                http:Response errorResponse = createErrorResponse(e.msg, e.statusCode);
                return errorResponse, e;
            }
        } else if (currentState == CircuitState.HALF_OPEN) {
            res, e = httpEP.post(path, req);
            circuitHealth.requestCount = circuitHealth.requestCount + 1;

            if (e != null) {
                circuitHealth.updateHealth();
                http:Response errorResponse = createErrorResponse(e.msg, e.statusCode);
                return errorResponse, e;
            }
        } else {
            // TODO: Allow the user to handle this scenario. Maybe through a user provided function
            http:Response errorResponse = handleOpenCircuit(circuitHealth, resetTimeout);
            return errorResponse, e;
        }

        return res, null;
    }

    action head (string path, http:Request req) (http:Response, http:HttpConnectorError) {
        currentState = updateCircuitState(circuitHealth, currentState, failurePercentageThreshold, resetTimeout);
        http:Response res;
        http:HttpConnectorError e;

        if (currentState == CircuitState.CLOSE) {
            res, e = httpEP.head(path, req);
            circuitHealth.requestCount = circuitHealth.requestCount + 1;

            if (e != null) {
                circuitHealth.updateHealth();
                http:Response errorResponse = createErrorResponse(e.msg, e.statusCode);
                return errorResponse, e;
            }
        } else if (currentState == CircuitState.HALF_OPEN) {
            res, e = httpEP.head(path, req);
            circuitHealth.requestCount = circuitHealth.requestCount + 1;

            if (e != null) {
                circuitHealth.updateHealth();
                http:Response errorResponse = createErrorResponse(e.msg, e.statusCode);
                return errorResponse, e;
            }
        } else {
            // TODO: Allow the user to handle this scenario. Maybe through a user provided function
            http:Response errorResponse = handleOpenCircuit(circuitHealth, resetTimeout);
            return errorResponse, e;
        }

        return res, null;
    }

    action put (string path, http:Request req) (http:Response, http:HttpConnectorError) {
        currentState = updateCircuitState(circuitHealth, currentState, failurePercentageThreshold, resetTimeout);
        http:Response res;
        http:HttpConnectorError e;

        if (currentState == CircuitState.CLOSE) {
            res, e = httpEP.put(path, req);
            circuitHealth.requestCount = circuitHealth.requestCount + 1;

            if (e != null) {
                circuitHealth.updateHealth();
                http:Response errorResponse = createErrorResponse(e.msg, e.statusCode);
                return errorResponse, e;
            }
        } else if (currentState == CircuitState.HALF_OPEN) {
            res, e = httpEP.put(path, req);
            circuitHealth.requestCount = circuitHealth.requestCount + 1;

            if (e != null) {
                circuitHealth.updateHealth();
                http:Response errorResponse = createErrorResponse(e.msg, e.statusCode);
                return errorResponse, e;
            }
        } else {
            // TODO: Allow the user to handle this scenario. Maybe through a user provided function
            http:Response errorResponse = handleOpenCircuit(circuitHealth, resetTimeout);
            return errorResponse, e;
        }

        return res, null;
    }

    action execute (string httpVerb, string path, http:Request req) (http:Response, http:HttpConnectorError) {
        currentState = updateCircuitState(circuitHealth, currentState, failurePercentageThreshold, resetTimeout);
        http:Response res;
        http:HttpConnectorError e;

        if (currentState == CircuitState.CLOSE) {
            res, e = httpEP.execute(httpVerb, path, req);
            circuitHealth.requestCount = circuitHealth.requestCount + 1;

            if (e != null) {
                circuitHealth.updateHealth();
                http:Response errorResponse = createErrorResponse(e.msg, e.statusCode);
                return errorResponse, e;
            }
        } else if (currentState == CircuitState.HALF_OPEN) {
            res, e = httpEP.execute(httpVerb, path, req);
            circuitHealth.requestCount = circuitHealth.requestCount + 1;

            if (e != null) {
                circuitHealth.updateHealth();
                http:Response errorResponse = createErrorResponse(e.msg, e.statusCode);
                return errorResponse, e;
            }
        } else {
            // TODO: Allow the user to handle this scenario. Maybe through a user provided function
            http:Response errorResponse = handleOpenCircuit(circuitHealth, resetTimeout);
            return errorResponse, e;
        }

        return res, null;
    }

    action patch (string path, http:Request req) (http:Response, http:HttpConnectorError) {
        currentState = updateCircuitState(circuitHealth, currentState, failurePercentageThreshold, resetTimeout);
        http:Response res;
        http:HttpConnectorError e;

        if (currentState == CircuitState.CLOSE) {
            res, e = httpEP.patch(path, req);
            circuitHealth.requestCount = circuitHealth.requestCount + 1;

            if (e != null) {
                circuitHealth.updateHealth();
                http:Response errorResponse = createErrorResponse(e.msg, e.statusCode);
                return errorResponse, e;
            }
        } else if (currentState == CircuitState.HALF_OPEN) {
            res, e = httpEP.patch(path, req);
            circuitHealth.requestCount = circuitHealth.requestCount + 1;

            if (e != null) {
                circuitHealth.updateHealth();
                http:Response errorResponse = createErrorResponse(e.msg, e.statusCode);
                return errorResponse, e;
            }
        } else {
            // TODO: Allow the user to handle this scenario. Maybe through a user provided function
            http:Response errorResponse = handleOpenCircuit(circuitHealth, resetTimeout);
            return errorResponse, e;
        }

        return res, null;
    }

    action delete (string path, http:Request req) (http:Response, http:HttpConnectorError) {
        currentState = updateCircuitState(circuitHealth, currentState, failurePercentageThreshold, resetTimeout);
        http:Response res;
        http:HttpConnectorError e;

        if (currentState == CircuitState.CLOSE) {
            res, e = httpEP.delete(path, req);
            circuitHealth.requestCount = circuitHealth.requestCount + 1;

            if (e != null) {
                circuitHealth.updateHealth();
                http:Response errorResponse = createErrorResponse(e.msg, e.statusCode);
                return errorResponse, e;
            }
        } else if (currentState == CircuitState.HALF_OPEN) {
            res, e = httpEP.delete(path, req);
            circuitHealth.requestCount = circuitHealth.requestCount + 1;

            if (e != null) {
                circuitHealth.updateHealth();
                http:Response errorResponse = createErrorResponse(e.msg, e.statusCode);
                return errorResponse, e;
            }
        } else {
            // TODO: Allow the user to handle this scenario. Maybe through a user provided function
            http:Response errorResponse = handleOpenCircuit(circuitHealth, resetTimeout);
            return errorResponse, e;
        }

        return res, null;
    }

    action options (string path, http:Request req) (http:Response, http:HttpConnectorError) {
        currentState = updateCircuitState(circuitHealth, currentState, failurePercentageThreshold, resetTimeout);
        http:Response res;
        http:HttpConnectorError e;

        if (currentState == CircuitState.CLOSE) {
            res, e = httpEP.options(path, req);
            circuitHealth.requestCount = circuitHealth.requestCount + 1;

            if (e != null) {
                circuitHealth.updateHealth();
                http:Response errorResponse = createErrorResponse(e.msg, e.statusCode);
                return errorResponse, e;
            }
        } else if (currentState == CircuitState.HALF_OPEN) {
            res, e = httpEP.options(path, req);
            circuitHealth.requestCount = circuitHealth.requestCount + 1;

            if (e != null) {
                circuitHealth.updateHealth();
                http:Response errorResponse = createErrorResponse(e.msg, e.statusCode);
                return errorResponse, e;
            }
        } else {
            // TODO: Allow the user to handle this scenario. Maybe through a user provided function
            http:Response errorResponse = handleOpenCircuit(circuitHealth, resetTimeout);
            return errorResponse, e;
        }

        return res, null;
    }

    action forward (string path, http:Request req) (http:Response, http:HttpConnectorError) {
        currentState = updateCircuitState(circuitHealth, currentState, failurePercentageThreshold, resetTimeout);
        http:Response res;
        http:HttpConnectorError e;

        if (currentState == CircuitState.CLOSE) {
            res, e = httpEP.forward(path, req);
            circuitHealth.requestCount = circuitHealth.requestCount + 1;

            if (e != null) {
                circuitHealth.updateHealth();
                http:Response errorResponse = createErrorResponse(e.msg, e.statusCode);
                return errorResponse, e;
            }
        } else if (currentState == CircuitState.HALF_OPEN) {
            res, e = httpEP.forward(path, req);
            circuitHealth.requestCount = circuitHealth.requestCount + 1;

            if (e != null) {
                circuitHealth.updateHealth();
                http:Response errorResponse = createErrorResponse(e.msg, e.statusCode);
                return errorResponse, e;
            }
        } else {
            // TODO: Allow the user to handle this scenario. Maybe through a user provided function
            http:Response errorResponse = handleOpenCircuit(circuitHealth, resetTimeout);
            return errorResponse, e;
        }

        return res, null;
    }

    action get (string path, http:Request req) (http:Response, http:HttpConnectorError) {
        currentState = updateCircuitState(circuitHealth, currentState, failurePercentageThreshold, resetTimeout);
        http:Response res;
        http:HttpConnectorError e;

        if (currentState == CircuitState.CLOSE) {
            res, e = httpEP.get(path, req);
            circuitHealth.requestCount = circuitHealth.requestCount + 1;

            if (e != null) {
                circuitHealth.updateHealth();
                http:Response errorResponse = createErrorResponse(e.msg, e.statusCode);
                return errorResponse, e;
            }
        } else if (currentState == CircuitState.HALF_OPEN) {
            res, e = httpEP.get(path, req);
            circuitHealth.requestCount = circuitHealth.requestCount + 1;

            if (e != null) {
                circuitHealth.updateHealth();
                http:Response errorResponse = createErrorResponse(e.msg, e.statusCode);
                return errorResponse, e;
            }
        } else {
            // TODO: Allow the user to handle this scenario. Maybe through a user provided function
            http:Response errorResponse = handleOpenCircuit(circuitHealth, resetTimeout);
            return errorResponse, e;
        }

        return res, null;
    }
}

function updateCircuitState (CircuitHealth circuitHealth, CircuitState currentState, float failureThreshold,
                             int resetTimeout) (CircuitState) {
    if (currentState == CircuitState.OPEN) {
        Time currentT = currentTime();
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
            currentState = CircuitState.CLOSE;
        }
    } else {
        float currentFailureRate;

        if (circuitHealth.requestCount > 0) {
            currentFailureRate = (circuitHealth.errorCount / circuitHealth.requestCount) * 100;
        } else {
            currentFailureRate = 0;
        }

        if (currentFailureRate > failureThreshold) {
            currentState = CircuitState.OPEN;
        }
    }

    return currentState;
}

function <CircuitHealth circuitHealth> updateHealth () {
    circuitHealth.errorCount = circuitHealth.errorCount + 1;
    circuitHealth.lastErrorTime = currentTime();
}

function createErrorResponse (string msg, int statusCode) (http:Response) {
    http:Response errorResponse = {};
    errorResponse.setStringPayload(msg);
    errorResponse.setStatusCode(statusCode);
    return errorResponse;
}

function handleOpenCircuit (CircuitHealth circuitHealth, int resetTimeout) (http:Response) {
    Time currentT = currentTime();
    int timeDif = currentT.time - circuitHealth.lastErrorTime.time;
    int timeRemaining = resetTimeout - timeDif;
    return createErrorResponse("Upstream service unavailable. Requests to upstream service will be suspended for "
                               + timeRemaining + " milliseconds", 503);
}

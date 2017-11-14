package ballerina.resiliency.cb;

import ballerina.net.http;
import ballerina.lang.time;
import ballerina.lang.messages;
import ballerina.lang.errors;


time:Time lastErrorTime = time:currentTime();
float errorCount;
float requestCount;
int circuitState;

connector ClientConnector<http:ClientConnector hc> (int requestVolumeThreshold, int failurePercentageThreshold,
                                                    int sleepWindowMillis) {
    action post (string path, message m) (message) {
        requestCount = requestCount + 1;
        int eligibility = checkEligibility(requestVolumeThreshold, failurePercentageThreshold, sleepWindowMillis);
        message response;
        if (eligibility == 0) {
            try {
                response = hc.post(path, m);
            } catch (errors:Error e) {
                errorCount = errorCount + 1;
                lastErrorTime = time:currentTime();
                message errorMsg = {};
                messages:setStringPayload(errorMsg, e.msg);
                http:setStatusCode(errorMsg, 500);
                return errorMsg;
            }
        } else if (eligibility == 1) {
            try {
                response = hc.post(path, m);
            } catch (errors:Error e) {
                errorCount = 0;
                lastErrorTime = time:currentTime();
                circuitState = 2;
                message errorMsg = {};
                messages:setStringPayload(errorMsg, e.msg);
                http:setStatusCode(errorMsg, 500);
                return errorMsg;
            }
        } else {
            message errorMsg = {};
            time:Time currentT = time:currentTime();
            int timeDifference = currentT.time - lastErrorTime.time;
            int remainingTime = sleepWindowMillis - timeDifference;
            messages:setStringPayload(errorMsg, "Back end system is suspended due to errors. Next invocation is in " +
                                                remainingTime + " milliseconds");
            http:setStatusCode(errorMsg, 500);
            return errorMsg;
        }

        return response;
    }

    action head (string path, message m) (message) {
        requestCount = requestCount + 1;
        int eligibility = checkEligibility(requestVolumeThreshold, failurePercentageThreshold, sleepWindowMillis);
        message response;
        if (eligibility == 0) {
            try {
                response = hc.head(path, m);
            } catch (errors:Error e) {
                errorCount = errorCount + 1;
                lastErrorTime = time:currentTime();
                message errorMsg = {};
                messages:setStringPayload(errorMsg, e.msg);
                http:setStatusCode(errorMsg, 500);
                return errorMsg;
            }
        } else if (eligibility == 1) {
            try {
                response = hc.head(path, m);
            } catch (errors:Error e) {
                errorCount = 0;
                lastErrorTime = time:currentTime();
                circuitState = 2;
                message errorMsg = {};
                messages:setStringPayload(errorMsg, e.msg);
                http:setStatusCode(errorMsg, 500);
                return errorMsg;
            }
        } else {
            message errorMsg = {};
            time:Time currentT = time:currentTime();
            int timeDifference = currentT.time - lastErrorTime.time;
            int remainingTime = sleepWindowMillis - timeDifference;
            messages:setStringPayload(errorMsg, "Back end system is suspended due to errors. Next invocation is in " +
                                                remainingTime + " milliseconds");
            http:setStatusCode(errorMsg, 500);
            return errorMsg;
        }

        return response;
    }

    action put (string path, message m) (message){
        requestCount = requestCount + 1;
        int eligibility = checkEligibility(requestVolumeThreshold, failurePercentageThreshold, sleepWindowMillis);
        message response;
        if (eligibility == 0) {
            try {
                response = hc.put(path, m);
            } catch (errors:Error e) {
                errorCount = errorCount + 1;
                lastErrorTime = time:currentTime();
                message errorMsg = {};
                messages:setStringPayload(errorMsg, e.msg);
                http:setStatusCode(errorMsg, 500);
                return errorMsg;
            }
        } else if (eligibility == 1) {
            try {
                response = hc.put(path, m);
            } catch (errors:Error e) {
                errorCount = 0;
                lastErrorTime = time:currentTime();
                circuitState = 2;
                message errorMsg = {};
                messages:setStringPayload(errorMsg, e.msg);
                http:setStatusCode(errorMsg, 500);
                return errorMsg;
            }
        } else {
            message errorMsg = {};
            time:Time currentT = time:currentTime();
            int timeDifference = currentT.time - lastErrorTime.time;
            int remainingTime = sleepWindowMillis - timeDifference;
            messages:setStringPayload(errorMsg, "Back end system is suspended due to errors. Next invocation is in " +
                                                remainingTime + " milliseconds");
            http:setStatusCode(errorMsg, 500);
            return errorMsg;
        }

        return response;
    }

    action execute (string httpVerb, string path, message m) (message){
        requestCount = requestCount + 1;
        int eligibility = checkEligibility(requestVolumeThreshold, failurePercentageThreshold, sleepWindowMillis);
        message response;
        if (eligibility == 0) {
            try {
                response = hc.execute(httpVerb, path, m);
            } catch (errors:Error e) {
                errorCount = errorCount + 1;
                lastErrorTime = time:currentTime();
                message errorMsg = {};
                messages:setStringPayload(errorMsg, e.msg);
                http:setStatusCode(errorMsg, 500);
                return errorMsg;
            }
        } else if (eligibility == 1) {
            try {
                response = hc.execute(httpVerb, path, m);
            } catch (errors:Error e) {
                errorCount = 0;
                lastErrorTime = time:currentTime();
                circuitState = 2;
                message errorMsg = {};
                messages:setStringPayload(errorMsg, e.msg);
                http:setStatusCode(errorMsg, 500);
                return errorMsg;
            }
        } else {
            message errorMsg = {};
            time:Time currentT = time:currentTime();
            int timeDifference = currentT.time - lastErrorTime.time;
            int remainingTime = sleepWindowMillis - timeDifference;
            messages:setStringPayload(errorMsg, "Back end system is suspended due to errors. Next invocation is in " +
                                                remainingTime + " milliseconds");
            http:setStatusCode(errorMsg, 500);
            return errorMsg;
        }

        return response;
    }

    action patch (string path, message m) (message){
        requestCount = requestCount + 1;
        int eligibility = checkEligibility(requestVolumeThreshold, failurePercentageThreshold, sleepWindowMillis);
        message response;
        if (eligibility == 0) {
            try {
                response = hc.patch(path, m);
            } catch (errors:Error e) {
                errorCount = errorCount + 1;
                lastErrorTime = time:currentTime();
                message errorMsg = {};
                messages:setStringPayload(errorMsg, e.msg);
                http:setStatusCode(errorMsg, 500);
                return errorMsg;
            }
        } else if (eligibility == 1) {
            try {
                response = hc.patch(path, m);
            } catch (errors:Error e) {
                errorCount = 0;
                lastErrorTime = time:currentTime();
                circuitState = 2;
                message errorMsg = {};
                messages:setStringPayload(errorMsg, e.msg);
                http:setStatusCode(errorMsg, 500);
                return errorMsg;
            }
        } else {
            message errorMsg = {};
            time:Time currentT = time:currentTime();
            int timeDifference = currentT.time - lastErrorTime.time;
            int remainingTime = sleepWindowMillis - timeDifference;
            messages:setStringPayload(errorMsg, "Back end system is suspended due to errors. Next invocation is in " +
                                                remainingTime + " milliseconds");
            http:setStatusCode(errorMsg, 500);
            return errorMsg;
        }

        return response;
    }

    action delete (string path, message m) (message){
        requestCount = requestCount + 1;
        int eligibility = checkEligibility(requestVolumeThreshold, failurePercentageThreshold, sleepWindowMillis);
        message response;
        if (eligibility == 0) {
            try {
                response = hc.delete(path, m);
            } catch (errors:Error e) {
                errorCount = errorCount + 1;
                lastErrorTime = time:currentTime();
                message errorMsg = {};
                messages:setStringPayload(errorMsg, e.msg);
                http:setStatusCode(errorMsg, 500);
                return errorMsg;
            }
        } else if (eligibility == 1) {
            try {
                response = hc.delete(path, m);
            } catch (errors:Error e) {
                errorCount = 0;
                lastErrorTime = time:currentTime();
                circuitState = 2;
                message errorMsg = {};
                messages:setStringPayload(errorMsg, e.msg);
                http:setStatusCode(errorMsg, 500);
                return errorMsg;
            }
        } else {
            message errorMsg = {};
            time:Time currentT = time:currentTime();
            int timeDifference = currentT.time - lastErrorTime.time;
            int remainingTime = sleepWindowMillis - timeDifference;
            messages:setStringPayload(errorMsg, "Back end system is suspended due to errors. Next invocation is in " +
                                                remainingTime + " milliseconds");
            http:setStatusCode(errorMsg, 500);
            return errorMsg;
        }

        return response;
    }

    action get (string path, message m) (message){
        requestCount = requestCount + 1;
        int eligibility = checkEligibility(requestVolumeThreshold, failurePercentageThreshold, sleepWindowMillis);
        message response;
        if (eligibility == 0) {
            try {
                response = hc.get(path, m);
            } catch (errors:Error e) {
                errorCount = errorCount + 1;
                lastErrorTime = time:currentTime();
                message errorMsg = {};
                messages:setStringPayload(errorMsg, e.msg);
                http:setStatusCode(errorMsg, 500);
                return errorMsg;
            }
        } else if (eligibility == 1) {
            try {
                response = hc.get(path, m);
            } catch (errors:Error e) {
                errorCount = 0;
                lastErrorTime = time:currentTime();
                circuitState = 2;
                message errorMsg = {};
                messages:setStringPayload(errorMsg, e.msg);
                http:setStatusCode(errorMsg, 500);
                return errorMsg;
            }
        } else {
            message errorMsg = {};
            time:Time currentT = time:currentTime();
            int timeDifference = currentT.time - lastErrorTime.time;
            int remainingTime = sleepWindowMillis - timeDifference;
            messages:setStringPayload(errorMsg, "Back end system is suspended due to errors. Next invocation is in " +
                                                remainingTime + " milliseconds");
            http:setStatusCode(errorMsg, 500);
            return errorMsg;
        }

        return response;
    }
}

function checkEligibility(int volumeThreshold, int failurePercentage, int sleepMillis)(int) {
    if (circuitState == 2) {
        time:Time currentT = time:currentTime();
        int timeDifference = currentT.time - lastErrorTime.time;
        if (timeDifference > sleepMillis) {
            errorCount = 0;
            requestCount = 0;
            //lastErrorTime = time:currentTime();
            circuitState = 1;
            return circuitState;
        }
        return circuitState;
    } else {
        if (requestCount < volumeThreshold) {
            return circuitState;
        } else {
            time:Time currentT = time:currentTime();
            int timeDifference = currentT.time - lastErrorTime.time;
            if (timeDifference > sleepMillis) {
                errorCount = 0;
                requestCount = 0;
                lastErrorTime = time:currentTime();
                circuitState = 1;
                return circuitState;
            } else {
                float currentFailurePercentage = (errorCount / requestCount) * 100;
                if (currentFailurePercentage < failurePercentage) {
                    return circuitState;
                } else {
                    circuitState = 1;
                    requestCount = 0;
                    errorCount = 0;
                    lastErrorTime = time:currentTime();
                    return circuitState;
                }
            }

        }
    }

}

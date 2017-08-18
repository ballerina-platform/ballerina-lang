package ballerina.resiliency.rt;

import ballerina.net.http;
import ballerina.lang.messages;
import ballerina.lang.errors;
import ballerina.lang.system;

int errorCount;

connector ClientConnector<http:ClientConnector hc> (int maxRetryCount, int retryDuration) {
    action post (string path, message m) (message) {
        message response;
        string err;
        while (maxRetryCount > errorCount) {
            try {
                response = hc.post(path, m);
            } catch (errors:Error e) {
                err = e.msg;
                errorCount = errorCount + 1;
                if (errorCount == maxRetryCount) {
                    break;
                }
            }
            if (response != null) {
                return response;
            }
            system:sleep(retryDuration);
        }

        message errorMsg = {};
        messages:setStringPayload(errorMsg, "Retry failed after " +
                                            maxRetryCount + " attempts with error: " + err);
        http:setStatusCode(errorMsg, 500);
        errorCount = 0;
        return errorMsg;
    }

    action head (string path, message m) (message) {
        message response;
        string err;
        while (maxRetryCount > errorCount) {
            system:println("Retry " + (errorCount+1));
            try {
                response = hc.head(path, m);
            } catch (errors:Error e) {
                err = e.msg;
                errorCount = errorCount + 1;
                if (errorCount == maxRetryCount) {
                    break;
                }
            }
            if (response != null) {
                return response;
            }
            system:sleep(retryDuration);
        }

        message errorMsg = {};
        messages:setStringPayload(errorMsg, "Retry failed after " +
                                            maxRetryCount + " attempts with error: " + err);
        http:setStatusCode(errorMsg, 500);
        errorCount = 0;
        return errorMsg;
    }

    action put (string path, message m) (message){
        message response;
        string err;
        while (maxRetryCount > errorCount) {
            system:println("Retry " + (errorCount+1));
            try {
                response = hc.put(path, m);
            } catch (errors:Error e) {
                err = e.msg;
                errorCount = errorCount + 1;
                if (errorCount == maxRetryCount) {
                    break;
                }
            }
            if (response != null) {
                return response;
            }
            system:sleep(retryDuration);
        }

        message errorMsg = {};
        messages:setStringPayload(errorMsg, "Retry failed after " +
                                            maxRetryCount + " attempts with error: " + err);
        http:setStatusCode(errorMsg, 500);
        errorCount = 0;
        return errorMsg;
    }

    action execute (string httpVerb, string path, message m) (message){
        message response;
        string err;
        while (maxRetryCount > errorCount) {
            system:println("Retry " + (errorCount+1));
            try {
                response = hc.execute(httpVerb, path, m);
            } catch (errors:Error e) {
                err = e.msg;
                errorCount = errorCount + 1;
                if (errorCount == maxRetryCount) {
                    break;
                }
            }
            if (response != null) {
                return response;
            }
            system:sleep(retryDuration);
        }

        message errorMsg = {};
        messages:setStringPayload(errorMsg, "Retry failed after " +
                                            maxRetryCount + " attempts with error: " + err);
        http:setStatusCode(errorMsg, 500);
        errorCount = 0;
        return errorMsg;
    }

    action patch (string path, message m) (message){
        message response;
        string err;
        while (maxRetryCount > errorCount) {
            system:println("Retry " + (errorCount+1));
            try {
                response = hc.patch(path, m);
            } catch (errors:Error e) {
                err = e.msg;
                errorCount = errorCount + 1;
                if (errorCount == maxRetryCount) {
                    break;
                }
            }
            if (response != null) {
                return response;
            }
            system:sleep(retryDuration);
        }

        message errorMsg = {};
        messages:setStringPayload(errorMsg, "Retry failed after " +
                                            maxRetryCount + " attempts with error: " + err);
        http:setStatusCode(errorMsg, 500);
        errorCount = 0;
        return errorMsg;
    }

    action delete (string path, message m) (message){
        message response;
        string err;
        while (maxRetryCount > errorCount) {
            system:println("Retry " + (errorCount+1));
            try {
                response = hc.delete(path, m);
            } catch (errors:Error e) {
                err = e.msg;
                errorCount = errorCount + 1;
                if (errorCount == maxRetryCount) {
                    break;
                }
            }
            if (response != null) {
                return response;
            }
            system:sleep(retryDuration);
        }

        message errorMsg = {};
        messages:setStringPayload(errorMsg, "Retry failed after " +
                                            maxRetryCount + " attempts with error: " + err);
        http:setStatusCode(errorMsg, 500);
        errorCount = 0;
        return errorMsg;
    }

    action get (string path, message m) (message){
        message response;
        string err;
        while (maxRetryCount > errorCount) {
            system:println("Retry " + (errorCount+1));
            try {
                response = hc.get(path, m);
            } catch (errors:Error e) {
                err = e.msg;
                errorCount = errorCount + 1;
                if (errorCount == maxRetryCount) {
                    break;
                }
            }
            if (response != null) {
                return response;
            }
            system:sleep(retryDuration);
        }

        message errorMsg = {};
        messages:setStringPayload(errorMsg, "Retry failed after " +
                                            maxRetryCount + " attempts with error: " + err);
        http:setStatusCode(errorMsg, 500);
        errorCount = 0;
        return errorMsg;
    }
}

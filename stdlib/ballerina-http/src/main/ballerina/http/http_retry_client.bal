package ballerina.http;

import ballerina/io;
import ballerina/runtime;
import ballerina/mime;
import ballerina/math;

documentation {
    Represents an HTTP Retry client to be used with the HTTP client to provide retrying over HTTP requests.

    F{{serviceUri}} - Target service url.
    F{{config}}  - HTTP ClientEndpointConfig to be used for HTTP client invocation.
    F{{retry}} - Retry related configuration.
    F{{httpClient}}  - HTTP client for outbound HTTP requests.
}
public type RetryClient object {
    public {
        string serviceUri;
        ClientEndpointConfig config;
        Retry retry;
        HttpClient httpClient;
    }

    public new (serviceUri, config, retry, httpClient) {}

    documentation {
        The POST function implementation of the HTTP retry client. Protects the invocation of the POST function attached to the underlying HTTP client.

        P{{path}} - Target service url.
        P{{request}}  - A request message.
    }
    public function post (string path, Request request) returns (Response | HttpConnectorError);

    documentation {
        The HEAD function implementation of the HTTP retry client. Protects the invocation of the HEAD function attached to the underlying HTTP client.

        P{{path}} - Target service url.
        P{{request}}  - A request message.
    }
    public function head (string path, Request request) returns (Response | HttpConnectorError);

    documentation {
        The PUT function implementation of the HTTP retry client. Protects the invocation of the PUT function attached to the underlying HTTP client.

        P{{path}} - Target service url.
        P{{request}}  - A request message.
    }
    public function put (string path, Request request) returns (Response | HttpConnectorError);

    documentation {
        The FORWARD function implementation of the HTTP retry client. Protects the invocation of the FORWARD function attached to the underlying HTTP client.

        P{{path}} - Target service url.
        P{{request}}  - A request message.
    }
    public function forward (string path, Request request) returns (Response | HttpConnectorError);

    documentation {
        The EXECUTE function implementation of the HTTP retry client. Protects the invocation of the EXECUTE function attached to the underlying HTTP client.

        P{{httpVerb}} - HTTP verb to be used for the request.
        P{{path}} - Target service url.
        P{{request}}  - A request message.
    }
    public function execute (string httpVerb, string path, Request request) returns (Response | HttpConnectorError);

    documentation {
        The PATCH function implementation of the HTTP retry client. Protects the invocation of the PATCH function attached to the underlying HTTP client.

        P{{path}} - Target service url.
        P{{request}}  - A request message.
    }
    public function patch (string path, Request request) returns (Response | HttpConnectorError);

    documentation {
        The DELETE function implementation of the HTTP retry client. Protects the invocation of the DELETE function attached to the underlying HTTP client.

        P{{path}} - Target service url.
        P{{request}}  - A request message.
    }
    public function delete (string path, Request request) returns (Response | HttpConnectorError);

    documentation {
        The GET function implementation of the HTTP retry client. Protects the invocation of the GET function attached to the underlying HTTP client.

        P{{path}} - Target service url.
        P{{request}}  - A request message.
    }
    public function get (string path, Request request) returns (Response | HttpConnectorError);

    documentation {
        The OPTIONS function implementation of the HTTP retry client. Protects the invocation of the OPTIONS function attached to the underlying HTTP client.

        P{{path}} - Target service url.
        P{{request}}  - A request message.
    }
    public function options (string path, Request request) returns (Response | HttpConnectorError);

    documentation {
        The SUBMIT function implementation of the HTTP retry client.

        P{{httpVerb}} - HTTP verb to be used for the request.
        P{{path}} - Target service url.
        P{{request}}  - A request message.
    }
    public function submit (string httpVerb, string path, Request request) returns (HttpFuture | HttpConnectorError);

    documentation {
        The getResponse function implementation of the HTTP retry client.

        P{{httpFuture}} -The Future which relates to previous async invocation.
    }
    public function getResponse (HttpFuture httpFuture) returns (Response | HttpConnectorError);

    documentation {
        The hasPromise function implementation of the HTTP retry client.

        P{{httpFuture}} -The Future which relates to previous async invocation.
    }
    public function hasPromise (HttpFuture httpFuture) returns (boolean);

    documentation {
        The getNextPromise function implementation of the HTTP retry client.

        P{{httpFuture}} -The Future which relates to previous async invocation.
    }
    public function getNextPromise (HttpFuture httpFuture) returns (PushPromise | HttpConnectorError);

    documentation {
        The getPromisedResponse function implementation of the HTTP retry client.

        P{{promise}} - The related Push Promise message.
    }
    public function getPromisedResponse (PushPromise promise) returns (Response | HttpConnectorError);

    documentation {
        The rejectPromise function implementation of the HTTP retry client.

        P{{promise}} - The Push Promise need to be rejected.
    }
    public function rejectPromise (PushPromise promise);
};

public function RetryClient::post (string path, Request request) returns (Response | HttpConnectorError) {
	return performRetryAction(path, request, HTTP_POST, self);
}

public function RetryClient::head (string path, Request request) returns (Response | HttpConnectorError) {
	return performRetryAction(path, request, HTTP_HEAD, self);
}

public function RetryClient::put (string path, Request request) returns (Response | HttpConnectorError) {
	return performRetryAction(path, request, HTTP_PUT, self);
}

public function RetryClient::forward (string path, Request request) returns (Response | HttpConnectorError) {
	return performRetryAction(path, request, HTTP_FORWARD, self);
}

public function RetryClient::execute (string httpVerb, string path, Request request) returns (Response | HttpConnectorError) {
	return performRetryClientExecuteAction(path, request, httpVerb, self);
}

public function RetryClient::patch (string path, Request request) returns (Response | HttpConnectorError) {
	return performRetryAction(path, request, HTTP_PATCH, self);
}

public function RetryClient::delete (string path, Request request) returns (Response | HttpConnectorError) {
	return performRetryAction(path, request, HTTP_DELETE, self);
}

public function RetryClient::get (string path, Request request) returns (Response | HttpConnectorError) {
	return performRetryAction(path, request, HTTP_GET, self);
}

public function RetryClient::options (string path, Request request) returns (Response | HttpConnectorError) {
	return performRetryAction(path, request, HTTP_OPTIONS, self);
}

public function RetryClient::submit (string httpVerb, string path, Request request) returns (HttpFuture | HttpConnectorError) {
    //TODO : Initialize the record type correctly once it is fixed.
	HttpConnectorError httpConnectorError = {statusCode:501};
	httpConnectorError.message = "Unsupported action for Circuit breaker";
	return httpConnectorError;
}

public function RetryClient::getResponse (HttpFuture httpFuture) returns (Response | HttpConnectorError) {
    //TODO : Initialize the record type correctly once it is fixed.
	HttpConnectorError httpConnectorError = {statusCode:501};
	httpConnectorError.message = "Unsupported action for Circuit breaker";
	return httpConnectorError;
}

public function RetryClient::hasPromise (HttpFuture httpFuture) returns (boolean) {
	return false;
}

public function RetryClient::getNextPromise (HttpFuture httpFuture) returns (PushPromise | HttpConnectorError) {
    //TODO : Initialize the record type once it is fixed.
	HttpConnectorError httpConnectorError = {statusCode:501};
	httpConnectorError.message = "Unsupported action for Circuit breaker";
	return httpConnectorError;
}

public function RetryClient::getPromisedResponse (PushPromise promise) returns (Response | HttpConnectorError) {
    //TODO : Initialize this correctly, once it is fixed.
	HttpConnectorError httpConnectorError = {statusCode:501};
	httpConnectorError.message = "Unsupported action for Circuit breaker";
	return httpConnectorError;
}

public function RetryClient::rejectPromise (PushPromise promise) {
}

// Performs execute action of the retry client. extract the corresponding http integer value representation
// of the http verb and invokes the perform action method.
function performRetryClientExecuteAction (string path, Request request, string httpVerb,
                               RetryClient retryClient) returns (Response | HttpConnectorError) {
    HttpOperation connectorAction = extractHttpOperation(httpVerb);
    return performRetryAction(path, request, connectorAction, retryClient);
}

// Handles all the actions exposed through the retry client.
function performRetryAction (string path, Request request, HttpOperation requestAction,
                                RetryClient retryClient) returns (Response | HttpConnectorError) {
    int currentRetryCount = 0;
    int retryCount = retryClient.retry.count;
    int interval = retryClient.retry.interval;
    float backOffFactor = retryClient.retry.backOffFactor;
    int maxWaitInterval = retryClient.retry.maxWaitInterval;
    if (backOffFactor <= 0) {
        backOffFactor = 1;
    }
    if (maxWaitInterval == 0) {
        maxWaitInterval = 60000;
    }
    HttpClient httpClient = retryClient.httpClient;
    Response response = new;
    //TODO : Initialize the record type correctly once it is fixed.
    HttpConnectorError httpConnectorError = {statusCode:501};
    Request inRequest = request;
    // When performing passthrough scenarios using retry client, message needs to be built before sending out the
    // to keep the request message to retry.
    var binaryPayload = check inRequest.getBinaryPayload();

    mime:Entity requestEntity = new;
    var mimeEntity = inRequest.getEntity();
    match mimeEntity {
        mime:Entity entity => requestEntity = entity;
        mime:EntityError => io:println("mimeEntity null");
    }

    while(currentRetryCount < (retryCount + 1)) {
        var invokedEndpoint = invokeEndpoint(path, inRequest, requestAction, httpClient);
        match invokedEndpoint {
            Response backendResponse => {
                return backendResponse;
            }
            HttpConnectorError errorResponse => {
                httpConnectorError = errorResponse;
            }
        }
        if (currentRetryCount != 0) {
           interval = getWaitTime(backOffFactor, maxWaitInterval, interval);
        }
        runtime:sleepCurrentWorker(interval);
        currentRetryCount = currentRetryCount + 1;
    }
    return httpConnectorError;
}

function getWaitTime(float backOffFactor, int maxWaitTime, int interval) returns (int) {
    int waitTime = math:round(interval * backOffFactor);
    waitTime = waitTime > maxWaitTime ? maxWaitTime : waitTime;
    return waitTime;
}

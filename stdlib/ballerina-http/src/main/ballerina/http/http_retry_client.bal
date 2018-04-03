package ballerina.http;

import ballerina/io;
import ballerina/runtime;
import ballerina/mime;

documentation {
    Represents an HTTP Retry client to be used with the HTTP client to provide retrying over HTTP requests.

    F{{serviceUri}} - Target service url.
    F{{config}}  - HTTP ClientEndpointConfiguration to be used for HTTP client invocation.
    F{{retry}} - Retry related configuration.
    F{{httpClient}}  - HTTP client for outbound HTTP requests.
}
public struct RetryClient {
   string serviceUri;
   ClientEndpointConfiguration config;
   Retry retry;
   HttpClient httpClient;
}

documentation {
    The POST function implementation of the HTTP retry client. Protects the invocation of the POST function attached to the underlying HTTP client.

    T{{client}} - RetryClient struct that the post function should be attached to.
    P{{path}} - Target service url.
    P{{request}}  - A request message.
}
public function <RetryClient client> post (string path, Request request) returns (Response | HttpConnectorError) {
	return performRetryAction(path, request, HttpOperation.POST, client);
}

documentation {
    The HEAD function implementation of the HTTP retry client. Protects the invocation of the HEAD function attached to the underlying HTTP client.

    T{{client}} - RetryClient struct that the head function should be attached to.
    P{{path}} - Target service url.
    P{{request}}  - A request message.
}
public function <RetryClient client> head (string path, Request request) returns (Response | HttpConnectorError) {
	return performRetryAction(path, request, HttpOperation.HEAD, client);
}

documentation {
    The PUT function implementation of the HTTP retry client. Protects the invocation of the PUT function attached to the underlying HTTP client.

    T{{client}} - RetryClient struct that the put function should be attached to.
    P{{path}} - Target service url.
    P{{request}}  - A request message.
}
public function <RetryClient client> put (string path, Request request) returns (Response | HttpConnectorError) {
	return performRetryAction(path, request, HttpOperation.PUT, client);
}

documentation {
    The FORWARD function implementation of the HTTP retry client. Protects the invocation of the FORWARD function attached to the underlying HTTP client.

    T{{client}} - RetryClient struct that the forward function should be attached to.
    P{{path}} - Target service url.
    P{{request}}  - A request message.
}
public function <RetryClient client> forward (string path, Request request) returns (Response | HttpConnectorError) {
	return performRetryAction(path, request, HttpOperation.FORWARD, client);
}

documentation {
    The EXECUTE function implementation of the HTTP retry client. Protects the invocation of the EXECUTE function attached to the underlying HTTP client.

    T{{client}} - RetryClient struct that the forward function should be attached to.
    P{{httpVerb}} - HTTP verb to be used for the request.
    P{{path}} - Target service url.
    P{{request}}  - A request message.
}
public function <RetryClient client> execute (string httpVerb, string path, Request request) returns (Response | HttpConnectorError) {
	return performRetryClientExecuteAction(path, request, httpVerb, client);
}

documentation {
    The PATCH function implementation of the HTTP retry client. Protects the invocation of the PATCH function attached to the underlying HTTP client.

    T{{client}} - RetryClient struct that the patch function should be attached to.
    P{{path}} - Target service url.
    P{{request}}  - A request message.
}
public function <RetryClient client> patch (string path, Request request) returns (Response | HttpConnectorError) {
	return performRetryAction(path, request, HttpOperation.PATCH, client);
}

documentation {
    The DELETE function implementation of the HTTP retry client. Protects the invocation of the DELETE function attached to the underlying HTTP client.

    T{{client}} - RetryClient struct that the delete function should be attached to.
    P{{path}} - Target service url.
    P{{request}}  - A request message.
}
public function <RetryClient client> delete (string path, Request request) returns (Response | HttpConnectorError) {
	return performRetryAction(path, request, HttpOperation.DELETE, client);
}

documentation {
    The GET function implementation of the HTTP retry client. Protects the invocation of the GET function attached to the underlying HTTP client.

    T{{client}} - RetryClient struct that the get function should be attached to.
    P{{path}} - Target service url.
    P{{request}}  - A request message.
}
public function <RetryClient client> get (string path, Request request) returns (Response | HttpConnectorError) {
	return performRetryAction(path, request, HttpOperation.GET, client);
}

documentation {
    The OPTIONS function implementation of the HTTP retry client. Protects the invocation of the OPTIONS function attached to the underlying HTTP client.

    T{{client}} - RetryClient struct that the options function should be attached to.
    P{{path}} - Target service url.
    P{{request}}  - A request message.
}
public function <RetryClient client> options (string path, Request request) returns (Response | HttpConnectorError) {
	return performRetryAction(path, request, HttpOperation.OPTIONS, client);
}

documentation {
    The SUBMIT function implementation of the HTTP retry client.

    T{{client}} - RetryClient struct that the delete function should be attached to.
    P{{httpVerb}} - HTTP verb to be used for the request.
    P{{path}} - Target service url.
    P{{request}}  - A request message.
}
public function <RetryClient client> submit (string httpVerb, string path, Request request) returns (HttpHandle | HttpConnectorError) {
	HttpConnectorError httpConnectorError = {};
	httpConnectorError.message = "Unsupported action for Circuit breaker";
	return httpConnectorError;
}

documentation {
    The getResponse function implementation of the HTTP retry client.

    T{{client}} - RetryClient struct that the delete function should be attached to.
    P{{handle}} -The Handle which relates to previous async invocation.
}
public function <RetryClient client> getResponse (HttpHandle handle) returns (Response | HttpConnectorError) {
	HttpConnectorError httpConnectorError = {};
	httpConnectorError.message = "Unsupported action for Circuit breaker";
	return httpConnectorError;
}

documentation {
    The hasPromise function implementation of the HTTP retry client.

    T{{client}} - RetryClient struct that the delete function should be attached to.
    P{{handle}} -The Handle which relates to previous async invocation.
}
public function <RetryClient client> hasPromise (HttpHandle handle) returns (boolean) {
	return false;
}

documentation {
    The getNextPromise function implementation of the HTTP retry client.

    T{{client}} - RetryClient struct that the getNextPromise function should be attached to.
    P{{handle}} -The Handle which relates to previous async invocation.
}
public function <RetryClient client> getNextPromise (HttpHandle handle) returns (PushPromise | HttpConnectorError) {
	HttpConnectorError httpConnectorError = {};
	httpConnectorError.message = "Unsupported action for Circuit breaker";
	return httpConnectorError;
}

documentation {
    The getPromisedResponse function implementation of the HTTP retry client.

    T{{client}} - RetryClient struct that the getNextPromise function should be attached to.
    P{{promise}} - The related Push Promise message.
}
public function <RetryClient client> getPromisedResponse (PushPromise promise) returns (Response | HttpConnectorError) {
	HttpConnectorError httpConnectorError = {};
	httpConnectorError.message = "Unsupported action for Circuit breaker";
	return httpConnectorError;
}

documentation {
    The rejectPromise function implementation of the HTTP retry client.

    T{{client}} - RetryClient struct that the getNextPromise function should be attached to.
    P{{promise}} - The Push Promise need to be rejected.
}
public function <RetryClient client> rejectPromise (PushPromise promise) returns (boolean) {
	return false;
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
    int backOffFactor = retryClient.retry.backOffFactor;
    if (backOffFactor == 0) {
        backOffFactor = 1;
    }
    HttpClient httpClient = retryClient.httpClient;
    Response response = {};
    HttpConnectorError httpConnectorError = {};
    Request inRequest = request;
    // When performing passthrough scenarios using retry client, message needs to be built before sending out the
    // to keep the request message to retry.
    var binaryPayload =? inRequest.getBinaryPayload();

    mime:Entity requestEntity = {};
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
           interval = interval * backOffFactor;
        }
        runtime:sleepCurrentWorker(interval);
        currentRetryCount = currentRetryCount + 1;
    }
    return httpConnectorError;
}

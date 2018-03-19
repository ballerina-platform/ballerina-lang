package ballerina.net.http.resiliency;

import ballerina.net.http;

public struct LoadBalanceEndpoint {

}

@Description {value:"Represents an error occurred in an action of the Load Balance connector."}
@Field {value:"message: An error message explaining about the error."}
@Field {value:"cause: The error that caused HttpConnectorError to get thrown."}
@Field {value:"stackTrace: Represents the invocation stack when LoadBalanceConnectorError is thrown."}
@Field {value:"statusCode: HTTP status code of the LoadBalanceConnectorError."}
@Field {value:"httpConnectorError: Array of HttpConnectorError error occurred at each endpoint."}
public struct LoadBalanceConnectorError {
    string message;
    error[] cause;
    int statusCode;
    http:HttpConnectorError[] httpConnectorError;
}

// Represents inferred load balance configurations passed to Load Balance connector.
//struct LoadBalanceInferredConfig {
//    http:HttpClient[] loadBalanceClientsArray;
//    function (http:HttpClient[])(http:HttpClient) algorithm;
//}

public struct LoadBalancer {
    http:HttpClient[] loadBalanceClientsArray;
    function (LoadBalancer, http:HttpClient[])(http:HttpClient) algorithm;
    private:
        int nextIndex; // Keeps to index which needs to be take the next load balance endpoint.
}

//@Description {value:"LoadBalancer Connector implementation to be used with the HTTP client connector to support load balance."}
//@Param {value:"loadBalanceClientsArray: Array of HttpClient connector to be load balanced."}
//@Param {value:"algorithm: Function pointer which implements the load balancing algorithm."}
//public function <LoadBalancer lb> LoadBalancer () {
//
//}

@Description {value:"The POST action implementation of the LoadBalancer Connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the action invocation, if any"}
public function <LoadBalancer lb> post (string path, http:Request request) (http:Response, http:HttpConnectorError) {
    return performLoadBalanceAction(path, request, null, HttpOperation.POST, lb.loadBalanceInferredConfig);
}

@Description {value:"The HEAD action implementation of the LoadBalancer Connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the action invocation, if any"}
public function <LoadBalancer lb> head (string path, http:Request request) (http:Response, http:HttpConnectorError) {
    return performLoadBalanceAction(path, request, null, HttpOperation.HEAD, lb.loadBalanceInferredConfig);
}

@Description {value:"The PATCH action implementation of the LoadBalancer Connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the action invocation, if any"}
public function <LoadBalancer lb> patch (string path, http:Request request) (http:Response, http:HttpConnectorError) {
    return performLoadBalanceAction(path, request, null, HttpOperation.PATCH, lb.loadBalanceInferredConfig);
}

@Description {value:"The PUT action implementation of the Load Balance Connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the action invocation, if any"}
public function <LoadBalancer lb> put (string path, http:Request request) (http:Response, http:HttpConnectorError) {
    return performLoadBalanceAction(path, request, null, HttpOperation.PUT, lb.loadBalanceInferredConfig);
}

@Description {value:"The OPTIONS action implementation of the LoadBalancer Connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the action invocation, if any"}
public function <LoadBalancer lb> options (string path, http:Request request) (http:Response, http:HttpConnectorError) {
    return performLoadBalanceAction(path, request, null, HttpOperation.OPTIONS, lb.loadBalanceInferredConfig);
}

@Description {value:"The FORWARD action implementation of the LoadBalancer Connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the action invocation, if any"}
public function <LoadBalancer lb> forward (string path, http:Request request) (http:Response, http:HttpConnectorError) {
    return performLoadBalanceAction(path, null, request, HttpOperation.FORWARD, lb.loadBalanceInferredConfig);
}

@Description {value:"The EXECUTE action implementation of the LoadBalancer Connector. The Execute action can be used to invoke an HTTP call with the given HTTP verb."}
@Param {value:"httpVerb: HTTP verb to be used for the request"}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the action invocation, if any"}
public function <LoadBalancer lb> execute (string httpVerb, string path, http:Request request) (http:Response, http:HttpConnectorError) {
    return performLoadBalanceExecuteAction(path, request, null, httpVerb, lb.loadBalanceInferredConfig);
}

@Description {value:"The DELETE action implementation of the LoadBalancer Connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the action invocation, if any"}
public function <LoadBalancer lb> delete (string path, http:Request request) (http:Response, http:HttpConnectorError) {
    return performLoadBalanceAction(path, request, null, HttpOperation.DELETE, lb.loadBalanceInferredConfig);
}

@Description {value:"The GET action implementation of the LoadBalancer Connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the action invocation, if any"}
public function <LoadBalancer lb> get (string path, http:Request request) (http:Response, http:HttpConnectorError) {
    return performLoadBalanceAction(path, request, null, HttpOperation.GET, lb.loadBalanceInferredConfig);
}

@Description { value:"The submit implementation of the LoadBalancer Connector."}
@Param { value:"httpVerb: The HTTP verb value" }
@Param { value:"path: The Resource path " }
@Param { value:"req: An HTTP outbound request message" }
@Return { value:"The Handle for further interactions" }
@Return { value:"The Error occured during HTTP client invocation" }
public function <LoadBalancer lb> submit (string httpVerb, string path, http:Request req) (http:HttpHandle, http:HttpConnectorError) {
    http:HttpConnectorError httpConnectorError = {};
    httpConnectorError.message = "Unsupported action for LoadBalancer Connector";
    return null, httpConnectorError;
}

@Description { value:"The getResponse implementation of the LoadBalancer Connector."}
@Param { value:"handle: The Handle which relates to previous async invocation" }
@Return { value:"The HTTP response message" }
@Return { value:"The Error occured during HTTP client invocation" }
public function <LoadBalancer lb> getResponse (http:HttpHandle handle) (http:Response, http:HttpConnectorError) {
    http:HttpConnectorError httpConnectorError = {};
    httpConnectorError.message = "Unsupported action for LoadBalancer Connector";
    return null, httpConnectorError;
}

@Description { value:"The hasPromise implementation of the LoadBalancer Connector."}
@Param { value:"handle: The Handle which relates to previous async invocation" }
@Return { value:"Whether push promise exists" }
public function <LoadBalancer lb> hasPromise (http:HttpHandle handle) (boolean) {
    return false;
}

@Description { value:"The getNextPromise implementation of the LoadBalancer Connector."}
@Param { value:"handle: The Handle which relates to previous async invocation" }
@Return { value:"The HTTP Push Promise message" }
@Return { value:"The Error occured during HTTP client invocation" }
public function <LoadBalancer lb> getNextPromise (http:HttpHandle handle) (http:PushPromise, http:HttpConnectorError) {
    http:HttpConnectorError httpConnectorError = {};
    httpConnectorError.message = "Unsupported action for LoadBalancer Connector";
    return null, httpConnectorError;
}

@Description { value:"The getPromisedResponse implementation of the LoadBalancer Connector."}
@Param { value:"promise: The related Push Promise message" }
@Return { value:"HTTP The Push Response message" }
@Return { value:"The Error occured during HTTP client invocation" }
public function <LoadBalancer lb> getPromisedResponse (http:PushPromise promise) (http:Response, http:HttpConnectorError) {
    http:HttpConnectorError httpConnectorError = {};
    httpConnectorError.message = "Unsupported action for LoadBalancer Connector";
    return null, httpConnectorError;
}

@Description { value:"The rejectPromise implementation of the LoadBalancer Connector."}
@Param { value:"promise: The Push Promise need to be rejected" }
@Return { value:"Whether operation is successful" }
public function <LoadBalancer lb> rejectPromise (http:PushPromise promise) (boolean) {
    return false;
}

// Performs execute action of the Load Balance connector. extract the corresponding http integer value representation
// of the http verb and invokes the perform action method.
function <LoadBalancer lb> performLoadBalanceExecuteAction (string path, http:Request outRequest, http:Request inRequest,
                               string httpVerb) (http:Response, http:HttpConnectorError) {
    HttpOperation connctorAction = extractHttpOperation(httpVerb);
    if (connctorAction != null) {
        return lb.performLoadBalanceAction(path, outRequest, inRequest, connctorAction);
    } else {
        http:HttpConnectorError httpConnectorError = {};
        httpConnectorError.statusCode = 400;
        httpConnectorError.message = "Unsupported connector action received.";
        return null, httpConnectorError;
    }
}

// Handles all the actions exposed through the Load Balance connector.
function <LoadBalancer lb> performLoadBalanceAction (string path, http:Request outRequest, http:Request inRequest,
                                   HttpOperation requestAction) (http:Response, http:HttpConnectorError) {

    http:HttpClient loadBalanceClient = lb.loadBalanceInferredConfig.algorithm(lb.loadBalanceInferredConfig.loadBalanceClientsArray);

    // Tracks at which point failover within the load balancing should be terminated.
    int loadBalanceTermination;
    LoadBalanceConnectorError loadBalanceConnectorError = {};
    http:HttpConnectorError httpConnectorError;
    loadBalanceConnectorError.httpConnectorError = [];
    http:Response inResponse;

    // When performing passthrough scenarios using Load Balance connector, message needs to be built before trying out the
    // load balance endpoints to keep the request message to load balance the messages in case of failure.
    if (inRequest != null && HttpOperation.FORWARD == requestAction) {
        blob binaryPayload = inRequest.getBinaryPayload();
    }

    inResponse, httpConnectorError = invokeEndpoint(path, outRequest, inRequest, requestAction, loadBalanceClient);

    while (httpConnectorError != null) {
        loadBalanceConnectorError.httpConnectorError[lb.nextIndex] = httpConnectorError;
        loadBalanceClient = lb.loadBalanceInferredConfig.algorithm(lb.loadBalanceInferredConfig.loadBalanceClientsArray);
        inResponse, httpConnectorError = invokeEndpoint(path, outRequest, inRequest, requestAction, loadBalanceClient);
        loadBalanceTermination = loadBalanceTermination + 1;
        if (loadBalanceTermination == (lengthof lb.loadBalanceInferredConfig.loadBalanceClientsArray - 1) && inResponse == null) {
            loadBalanceTermination = 0;
            inResponse, httpConnectorError = populateGenericLoadBalanceConnectorError(loadBalanceConnectorError, httpConnectorError, lb.nextIndex);
            break;
        }
    }
    loadBalanceTermination = 0;
    return inResponse, httpConnectorError;
}

// Round Robin Algorithm implementation with respect to load balancing endpoints.
public function (LoadBalancer lb, http:HttpClient [])(http:HttpClient) roundRobin =
                        function (LoadBalancer lb, http:HttpClient [] loadBalanceConfigArray) returns (http:HttpClient nextEndpoint) {

    http:HttpClient httpClient;

    lock {
        if (lb.nextIndex == ((lengthof (loadBalanceConfigArray)) - 1)) {
            httpClient = loadBalanceConfigArray[lb.nextIndex];
            lb.nextIndex = 0;
        } else {
            httpClient = loadBalanceConfigArray[lb.nextIndex];
            lb.nextIndex = lb.nextIndex + 1;
        }
    }

    return httpClient;
};

// Populates generic error specific to Load Balance connector by including all the errors returned from endpoints.
function populateGenericLoadBalanceConnectorError (LoadBalanceConnectorError loadBalanceConnectorError,
                                                http:HttpConnectorError httpConnectorError, int index)
(http:Response, http:HttpConnectorError) {
    loadBalanceConnectorError.statusCode = 500;
    loadBalanceConnectorError.httpConnectorError[index] = httpConnectorError;
    string lastErrorMsg = httpConnectorError.message;
    loadBalanceConnectorError.message = "All the load balance endpoints were failed. Last error was " + lastErrorMsg;
    return null, (http:HttpConnectorError) loadBalanceConnectorError;
}


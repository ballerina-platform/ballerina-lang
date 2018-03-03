package ballerina.net.http.resiliency;

import ballerina.net.http;

// Keeps to index which needs to be take the next load balance endpoint.
int nextIndex;

@Description {value:"Represents an error occurred in an action of the Load Balance connector."}
@Field {value:"message: An error message explaining about the error."}
@Field {value:"cause: The error that caused HttpConnectorError to get thrown."}
@Field {value:"stackTrace: Represents the invocation stack when LoadBalanceConnectorError is thrown."}
@Field {value:"statusCode: HTTP status code of the LoadBalanceConnectorError."}
@Field {value:"httpConnectorError: Array of HttpConnectorError error occurred at each endpoint."}
public struct LoadBalanceConnectorError {
    string message;
    error cause;
    int statusCode;
    http:HttpConnectorError[] httpConnectorError;
}

// Represents inferred load balance configurations passed to Load Balance connector.
struct LoadBalanceInferredConfig {
    http:HttpClient[] loadBalanceClientsArray;
    function (http:HttpClient[])(http:HttpClient) algorithm;
}

@Description {value:"LoadBalancer Connector implementation to be used with the HTTP client connector to support load balance."}
@Param {value:"loadBalanceClientsArray: Array of HttpClient connector to be load balanced."}
@Param {value:"algorithm: Function pointer which implements the load balancing algorithm."}
public connector LoadBalancer (http:HttpClient[] loadBalanceClientsArray, function (http:HttpClient[]) (http:HttpClient) algorithm) {

    LoadBalanceInferredConfig loadBalanceInferredConfig = {
                                                              loadBalanceClientsArray:loadBalanceClientsArray,
                                                              algorithm:algorithm
                                                          };

    @Description {value:"The POST action implementation of the LoadBalancer Connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: An OutRequest struct"}
    @Return {value:"The InResponse struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action post (string path, http:OutRequest request) (http:InResponse, http:HttpConnectorError) {
        return performLoadBalanceAction(path, request, null, HttpOperation.POST, loadBalanceInferredConfig);
    }

    @Description {value:"The HEAD action implementation of the LoadBalancer Connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: An OutRequest struct"}
    @Return {value:"The InResponse struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action head (string path, http:OutRequest request) (http:InResponse, http:HttpConnectorError) {
        return performLoadBalanceAction(path, request, null, HttpOperation.HEAD, loadBalanceInferredConfig);
    }

    @Description {value:"The PATCH action implementation of the LoadBalancer Connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: An OutRequest struct"}
    @Return {value:"The InResponse struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action patch (string path, http:OutRequest request) (http:InResponse, http:HttpConnectorError) {
        return performLoadBalanceAction(path, request, null, HttpOperation.PATCH, loadBalanceInferredConfig);
    }

    @Description {value:"The PUT action implementation of the Load Balance Connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: An OutRequest struct"}
    @Return {value:"The InResponse struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action put (string path, http:OutRequest request) (http:InResponse, http:HttpConnectorError) {
        return performLoadBalanceAction(path, request, null, HttpOperation.PUT, loadBalanceInferredConfig);
    }

    @Description {value:"The OPTIONS action implementation of the LoadBalancer Connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: An OutRequest struct"}
    @Return {value:"The InResponse struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action options (string path, http:OutRequest request) (http:InResponse, http:HttpConnectorError) {
        return performLoadBalanceAction(path, request, null, HttpOperation.OPTIONS, loadBalanceInferredConfig);
    }

    @Description {value:"The FORWARD action implementation of the LoadBalancer Connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: An InRequest struct"}
    @Return {value:"The InResponse struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action forward (string path, http:InRequest request) (http:InResponse, http:HttpConnectorError) {
        return performLoadBalanceAction(path, null, request, HttpOperation.FORWARD, loadBalanceInferredConfig);
    }

    @Description {value:"The EXECUTE action implementation of the LoadBalancer Connector. The Execute action can be used to invoke an HTTP call with the given HTTP verb."}
    @Param {value:"httpVerb: HTTP verb to be used for the request"}
    @Param {value:"path: Resource path"}
    @Param {value:"request: An OutRequest struct"}
    @Return {value:"The InResponse struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action execute (string httpVerb, string path, http:OutRequest request) (http:InResponse, http:HttpConnectorError) {
        return performLoadBalanceExecuteAction(path, request, null, httpVerb, loadBalanceInferredConfig);
    }

    @Description {value:"The DELETE action implementation of the LoadBalancer Connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: An OutRequest struct"}
    @Return {value:"The InResponse struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action delete (string path, http:OutRequest request) (http:InResponse, http:HttpConnectorError) {
        return performLoadBalanceAction(path, request, null, HttpOperation.DELETE, loadBalanceInferredConfig);
    }

    @Description {value:"The GET action implementation of the LoadBalancer Connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: An OutRequest struct"}
    @Return {value:"The InResponse struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action get (string path, http:OutRequest request) (http:InResponse, http:HttpConnectorError) {
        return performLoadBalanceAction(path, request, null, HttpOperation.GET, loadBalanceInferredConfig);
    }
}

// Performs execute action of the Load Balance connector. extract the corresponding http integer value representation
// of the http verb and invokes the perform action method.
function performLoadBalanceExecuteAction (string path, http:OutRequest outRequest, http:InRequest inRequest,
                               string httpVerb, LoadBalanceInferredConfig loadBalanceInferredConfig)
(http:InResponse, http:HttpConnectorError) {
    HttpOperation connctorAction = extractHttpOperation(httpVerb);
    if (connctorAction != null) {
        return performLoadBalanceAction(path, outRequest, inRequest, connctorAction, loadBalanceInferredConfig);
    } else {
        http:HttpConnectorError httpConnectorError = {};
        httpConnectorError.statusCode = 400;
        httpConnectorError.message = "Unsupported connector action received.";
        return null, httpConnectorError;
    }
}

// Handles all the actions exposed through the Load Balance connector.
function performLoadBalanceAction (string path, http:OutRequest outRequest, http:InRequest inRequest,
                                   HttpOperation requestAction, LoadBalanceInferredConfig loadBalanceInferredConfig)
(http:InResponse, http:HttpConnectorError) {

    http:HttpClient loadBalanceClient = loadBalanceInferredConfig.algorithm(loadBalanceInferredConfig.loadBalanceClientsArray);

    // Tracks at which point failover within the load balancing should be terminated.
    int loadBalanceTermination;
    LoadBalanceConnectorError loadBalanceConnectorError = {};
    http:HttpConnectorError httpConnectorError;
    loadBalanceConnectorError.httpConnectorError = [];
    http:InResponse inResponse;

    // When performing passthrough scenarios using Load Balance connector, message needs to be built before trying out the
    // load balance endpoints to keep the request message to load balance the messages in case of failure.
    if (inRequest != null && HttpOperation.FORWARD == requestAction) {
        blob binaryPayload = inRequest.getBinaryPayload();
    }

    inResponse, httpConnectorError = invokeEndpoint(path, outRequest, inRequest, requestAction, loadBalanceClient);

    while (httpConnectorError != null) {
        loadBalanceConnectorError.httpConnectorError[nextIndex] = httpConnectorError;
        loadBalanceClient = loadBalanceInferredConfig.algorithm(loadBalanceInferredConfig.loadBalanceClientsArray);
        inResponse, httpConnectorError = invokeEndpoint(path, outRequest, inRequest, requestAction, loadBalanceClient);
        loadBalanceTermination = loadBalanceTermination + 1;
        if (loadBalanceTermination == (lengthof loadBalanceInferredConfig.loadBalanceClientsArray - 1) && inResponse == null) {
            loadBalanceTermination = 0;
            inResponse, httpConnectorError = populateGenericLoadBalanceConnectorError(loadBalanceConnectorError, httpConnectorError, nextIndex);
            break;
        }
    }
    loadBalanceTermination = 0;
    return inResponse, httpConnectorError;
}

// Round Robin Algorithm implementation with respect to load balancing endpoints.
public function (http:HttpClient [])(http:HttpClient) roundRobin =
                        function (http:HttpClient [] loadBalaceConfigArray) returns (http:HttpClient nextEndpoint) {

    http:HttpClient httpClient;

    lock {
        if (nextIndex == ((lengthof (loadBalaceConfigArray)) - 1)) {
            httpClient = loadBalaceConfigArray[nextIndex];
            nextIndex = 0;
        } else {
            httpClient = loadBalaceConfigArray[nextIndex];
            nextIndex = nextIndex + 1;
        }
    }

    return httpClient;
};

// Populates generic error specific to Load Balance connector by including all the errors returned from endpoints.
function populateGenericLoadBalanceConnectorError (LoadBalanceConnectorError loadBalanceConnectorError,
                                                http:HttpConnectorError httpConnectorError, int index)
(http:InResponse, http:HttpConnectorError) {
    loadBalanceConnectorError.statusCode = 500;
    loadBalanceConnectorError.httpConnectorError[index] = httpConnectorError;
    string lastErrorMsg = httpConnectorError.message;
    loadBalanceConnectorError.message = "All the load balance endpoints were failed. Last error was " + lastErrorMsg;
    return null, (http:HttpConnectorError) loadBalanceConnectorError;
}


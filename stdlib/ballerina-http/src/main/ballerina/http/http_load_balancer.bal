// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package ballerina.http;

@Description {value:"Stands for the round robin algorithm for load balancing."}
@final public string ROUND_ROBIN = "round-robin";

@Description {value:"Load Balancer adds an additional layer to the HTTP client to make network interactions more resilient."}
public type LoadBalancer object {
   public {
       string serviceUri;
       ClientEndpointConfiguration config;
       HttpClient[] loadBalanceClientsArray;
       string algorithm;
       int nextIndex; // Keeps to index which needs to be take the next load balance endpoint.
   }

   public new (serviceUri, config, loadBalanceClientsArray, algorithm, nextIndex) {}

    @Description {value:"The POST action implementation of the LoadBalancer Connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    public function post (string path, Request request) returns (Response | HttpConnectorError);

    @Description {value:"The HEAD action implementation of the LoadBalancer Connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    public function head (string path, Request request) returns (Response | HttpConnectorError);

    @Description {value:"The PATCH action implementation of the LoadBalancer Connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    public function patch (string path, Request request) returns (Response | HttpConnectorError);

    @Description {value:"The PUT action implementation of the Load Balance Connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    public function put (string path, Request request) returns (Response | HttpConnectorError);

    @Description {value:"The OPTIONS action implementation of the LoadBalancer Connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    public function options (string path, Request request) returns (Response | HttpConnectorError);

    @Description {value:"The FORWARD action implementation of the LoadBalancer Connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    public function forward (string path, Request request) returns (Response | HttpConnectorError);

    @Description {value:"The EXECUTE action implementation of the LoadBalancer Connector. The Execute action can be used to invoke an HTTP call with the given HTTP verb."}
    @Param {value:"httpVerb: HTTP verb to be used for the request"}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    public function execute (string httpVerb, string path, Request request) returns (Response | HttpConnectorError);

    @Description {value:"The DELETE action implementation of the LoadBalancer Connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    public function delete (string path, Request request) returns (Response | HttpConnectorError);

    @Description {value:"The GET action implementation of the LoadBalancer Connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    public function get (string path, Request request) returns (Response | HttpConnectorError);

    @Description { value:"The submit implementation of the LoadBalancer Connector."}
    @Param { value:"httpVerb: The HTTP verb value" }
    @Param { value:"path: The Resource path " }
    @Param { value:"req: An HTTP outbound request message" }
    @Return { value:"The Handle for further interactions" }
    @Return { value:"The Error occured during HTTP client invocation" }
    public function submit (string httpVerb, string path, Request req) returns (HttpHandle | HttpConnectorError);

    @Description { value:"The getResponse implementation of the LoadBalancer Connector."}
    @Param { value:"handle: The Handle which relates to previous async invocation" }
    @Return { value:"The HTTP response message" }
    @Return { value:"The Error occured during HTTP client invocation" }
    public function getResponse (HttpHandle handle) returns (Response | HttpConnectorError);

    @Description { value:"The hasPromise implementation of the LoadBalancer Connector."}
    @Param { value:"handle: The Handle which relates to previous async invocation" }
    @Return { value:"Whether push promise exists" }
    public function hasPromise (HttpHandle handle) returns (boolean);

    @Description { value:"The getNextPromise implementation of the LoadBalancer Connector."}
    @Param { value:"handle: The Handle which relates to previous async invocation" }
    @Return { value:"The HTTP Push Promise message" }
    @Return { value:"The Error occured during HTTP client invocation" }
    public function getNextPromise (HttpHandle handle) returns (PushPromise | HttpConnectorError);

    @Description { value:"The getPromisedResponse implementation of the LoadBalancer Connector."}
    @Param { value:"promise: The related Push Promise message" }
    @Return { value:"HTTP The Push Response message" }
    @Return { value:"The Error occured during HTTP client invocation" }
    public function getPromisedResponse (PushPromise promise) returns (Response | HttpConnectorError);

    @Description { value:"The rejectPromise implementation of the LoadBalancer Connector."}
    @Param { value:"promise: The Push Promise need to be rejected" }
    @Return { value:"Whether operation is successful" }
    public function rejectPromise (PushPromise promise) returns (boolean);

};

@Description {value:"Represents an error occurred in an action of the Load Balance connector."}
@Field {value:"message: An error message explaining about the error."}
@Field {value:"cause: The error that caused HttpConnectorError to get thrown."}
@Field {value:"stackTrace: Represents the invocation stack when LoadBalanceConnectorError is thrown."}
@Field {value:"statusCode: HTTP status code of the LoadBalanceConnectorError."}
@Field {value:"httpConnectorError: Array of HttpConnectorError error occurred at each endpoint."}
public type LoadBalanceConnectorError {
    string message,
    error[] cause,
    int statusCode,
    HttpConnectorError[] httpConnectorError,
};


@Description {value:"The POST action implementation of the LoadBalancer Connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the action invocation, if any"}
public function LoadBalancer::post (string path, Request request) returns (Response | HttpConnectorError) {
    return performLoadBalanceAction(self, path, request, HTTP_POST);
}

@Description {value:"The HEAD action implementation of the LoadBalancer Connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the action invocation, if any"}
public function LoadBalancer::head (string path, Request request) returns (Response | HttpConnectorError) {
    return performLoadBalanceAction(self, path, request, HTTP_HEAD);
}

@Description {value:"The PATCH action implementation of the LoadBalancer Connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the action invocation, if any"}
public function LoadBalancer::patch (string path, Request request) returns (Response | HttpConnectorError) {
    return performLoadBalanceAction(self, path, request, HTTP_PATCH);
}

@Description {value:"The PUT action implementation of the Load Balance Connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the action invocation, if any"}
public function LoadBalancer::put (string path, Request request) returns (Response | HttpConnectorError) {
    return performLoadBalanceAction(self, path, request, HTTP_PUT);
}

@Description {value:"The OPTIONS action implementation of the LoadBalancer Connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the action invocation, if any"}
public function LoadBalancer::options (string path, Request request) returns (Response | HttpConnectorError) {
    return performLoadBalanceAction(self, path, request, HTTP_OPTIONS);
}

@Description {value:"The FORWARD action implementation of the LoadBalancer Connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the action invocation, if any"}
public function LoadBalancer::forward (string path, Request request) returns (Response | HttpConnectorError) {
    return performLoadBalanceAction(self, path, request, HTTP_FORWARD);
}

@Description {value:"The EXECUTE action implementation of the LoadBalancer Connector. The Execute action can be used to invoke an HTTP call with the given HTTP verb."}
@Param {value:"httpVerb: HTTP verb to be used for the request"}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the action invocation, if any"}
public function LoadBalancer::execute (string httpVerb, string path, Request request) returns (Response | HttpConnectorError) {
    return performLoadBalanceExecuteAction(self, path, request, httpVerb);
}

@Description {value:"The DELETE action implementation of the LoadBalancer Connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the action invocation, if any"}
public function LoadBalancer::delete (string path, Request request) returns (Response | HttpConnectorError) {
    return performLoadBalanceAction(self, path, request, HTTP_DELETE);
}

@Description {value:"The GET action implementation of the LoadBalancer Connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the action invocation, if any"}
public function LoadBalancer::get (string path, Request request) returns (Response | HttpConnectorError) {
    return performLoadBalanceAction(self, path, request, HTTP_GET);
}

@Description { value:"The submit implementation of the LoadBalancer Connector."}
@Param { value:"httpVerb: The HTTP verb value" }
@Param { value:"path: The Resource path " }
@Param { value:"req: An HTTP outbound request message" }
@Return { value:"The Handle for further interactions" }
@Return { value:"The Error occured during HTTP client invocation" }
public function LoadBalancer::submit (string httpVerb, string path, Request req) returns (HttpHandle | HttpConnectorError) {
    HttpConnectorError httpConnectorError = {message:"Unsupported action for LoadBalancer client."};
    return httpConnectorError;
}

@Description { value:"The getResponse implementation of the LoadBalancer Connector."}
@Param { value:"handle: The Handle which relates to previous async invocation" }
@Return { value:"The HTTP response message" }
@Return { value:"The Error occured during HTTP client invocation" }
public function LoadBalancer::getResponse (HttpHandle handle) returns (Response | HttpConnectorError) {
    HttpConnectorError httpConnectorError = {message:"Unsupported action for LoadBalancer client."};
    return httpConnectorError;
}

@Description { value:"The hasPromise implementation of the LoadBalancer Connector."}
@Param { value:"handle: The Handle which relates to previous async invocation" }
@Return { value:"Whether push promise exists" }
public function LoadBalancer::hasPromise (HttpHandle handle) returns (boolean) {
    return false;
}

@Description { value:"The getNextPromise implementation of the LoadBalancer Connector."}
@Param { value:"handle: The Handle which relates to previous async invocation" }
@Return { value:"The HTTP Push Promise message" }
@Return { value:"The Error occured during HTTP client invocation" }
public function LoadBalancer::getNextPromise (HttpHandle handle) returns (PushPromise | HttpConnectorError) {
    HttpConnectorError httpConnectorError = {message:"Unsupported action for LoadBalancer client."};
    return httpConnectorError;
}

@Description { value:"The getPromisedResponse implementation of the LoadBalancer Connector."}
@Param { value:"promise: The related Push Promise message" }
@Return { value:"HTTP The Push Response message" }
@Return { value:"The Error occured during HTTP client invocation" }
public function LoadBalancer::getPromisedResponse (PushPromise promise) returns (Response | HttpConnectorError) {
    HttpConnectorError httpConnectorError = {message:"Unsupported action for LoadBalancer client."};
    return httpConnectorError;
}

@Description { value:"The rejectPromise implementation of the LoadBalancer Connector."}
@Param { value:"promise: The Push Promise need to be rejected" }
@Return { value:"Whether operation is successful" }
public function LoadBalancer::rejectPromise (PushPromise promise) returns (boolean) {
    return false;
}

// Performs execute action of the Load Balance connector. extract the corresponding http integer value representation
// of the http verb and invokes the perform action method.
function performLoadBalanceExecuteAction (LoadBalancer lb, string path, Request outRequest,
                                          string httpVerb) returns (Response | HttpConnectorError) {
    HttpOperation connectorAction = extractHttpOperation(httpVerb);
    if (connectorAction != HTTP_NONE) {
        return performLoadBalanceAction(lb, path, outRequest, connectorAction);
    } else {
        HttpConnectorError httpConnectorError = {message:"Unsupported connector action received.", statusCode:501};
        return httpConnectorError;
    }
}

// Handles all the actions exposed through the Load Balance connector.
function performLoadBalanceAction (LoadBalancer lb, string path, Request outRequest, HttpOperation requestAction)
                                    returns (Response | HttpConnectorError) {
    // When performing passthrough scenarios using Load Balance connector, message needs to be built before trying out the
    // load balance endpoints to keep the request message to load balance the messages in case of failure.
    if (HTTP_FORWARD == requestAction) {
        match outRequest.getBinaryPayload() {
            // TODO: remove these dummy assignments once empty blocks are supported
            blob => {int x = 0;}
            mime:EntityError => {int x = 0;}
        }
    }

    int loadBalanceTermination = 0; // Tracks at which point failover within the load balancing should be terminated.
    //TODO: workaround to initialize a type inside a function. Change this once fix is aailable.
    LoadBalanceConnectorError loadBalanceConnectorError = {statusCode:500};
    loadBalanceConnectorError.httpConnectorError = [];

    while (loadBalanceTermination < lengthof lb.loadBalanceClientsArray) {
        HttpClient loadBalanceClient = roundRobin(lb, lb.loadBalanceClientsArray);

        match invokeEndpoint(path, outRequest, requestAction, loadBalanceClient) {
            Response inResponse => return inResponse;

            HttpConnectorError httpConnectorError => {
                loadBalanceConnectorError.httpConnectorError[lb.nextIndex] = httpConnectorError;
                loadBalanceClient = roundRobin(lb, lb.loadBalanceClientsArray);
                loadBalanceTermination = loadBalanceTermination + 1;
            }
        }
    }
    return populateGenericLoadBalanceConnectorError(loadBalanceConnectorError);
}

// Round Robin Algorithm implementation with respect to load balancing endpoints.
public function roundRobin(LoadBalancer lb, HttpClient[] loadBalanceConfigArray) returns (HttpClient) {
    HttpClient httpClient = new;

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
}

// Populates generic error specific to Load Balance connector by including all the errors returned from endpoints.
function populateGenericLoadBalanceConnectorError (LoadBalanceConnectorError loadBalanceConnectorError)
                                                    returns (HttpConnectorError) {
    int nErrs = lengthof loadBalanceConnectorError.httpConnectorError;
    loadBalanceConnectorError.statusCode = INTERNAL_SERVER_ERROR_500;
    loadBalanceConnectorError.message = "All the load balance endpoints failed. Last error was: "
                                        + loadBalanceConnectorError.httpConnectorError[nErrs - 1].message;
    HttpConnectorError httpConnectorError = loadBalanceConnectorError;
    return httpConnectorError;
}


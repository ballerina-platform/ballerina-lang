//// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
////
//// WSO2 Inc. licenses this file to you under the Apache License,
//// Version 2.0 (the "License"); you may not use this file except
//// in compliance with the License.
//// You may obtain a copy of the License at
////
//// http://www.apache.org/licenses/LICENSE-2.0
////
//// Unless required by applicable law or agreed to in writing,
//// software distributed under the License is distributed on an
//// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
//// KIND, either express or implied.  See the License for the
//// specific language governing permissions and limitations
//// under the License.
//
package ballerina.net.http;
//
//@Description {value:"Load Balancer adds an additional layer to the HTTP client to make network interactions more resilient."}
//@Field {value:"serviceUri: This is there just so that the struct is equivalent to HttpClient. This has no bearing on the functionality."}
//@Field {value:"config: The client endpoint configurations for the load balancer"}
//@Field {value:"loadBalanceClientsArray: The clients for the load balance endpoints"}
//@Field {value:"algorithm: The load balance algorithm. Round robin algorithm is provided out of the box. The user can also set their own load balancing algorithm."}
//@Field {value:"nextIndex: Indicates the next client to be used. Users should not edit this."}
//public struct LoadBalancer {
//    string serviceUri;
//    ClientEndpointConfiguration config;
//    HttpClient[] loadBalanceClientsArray;
//    function (LoadBalancer, HttpClient[]) (HttpClient) algorithm;
//    int nextIndex; // Keeps to index which needs to be take the next load balance endpoint.
//}
//
//@Description {value:"Represents an error occurred in an action of the Load Balance connector."}
//@Field {value:"message: An error message explaining about the error."}
//@Field {value:"cause: The error that caused HttpConnectorError to get thrown."}
//@Field {value:"stackTrace: Represents the invocation stack when LoadBalanceConnectorError is thrown."}
//@Field {value:"statusCode: HTTP status code of the LoadBalanceConnectorError."}
//@Field {value:"httpConnectorError: Array of HttpConnectorError error occurred at each endpoint."}
//public struct LoadBalanceConnectorError {
//    string message;
//    error[] cause;
//    int statusCode;
//    HttpConnectorError[] httpConnectorError;
//}
//
//
//@Description {value:"The POST action implementation of the LoadBalancer Connector."}
//@Param {value:"path: Resource path"}
//@Param {value:"request: A Request struct"}
//@Return {value:"The Response struct"}
//@Return {value:"Error occurred during the action invocation, if any"}
//public function <LoadBalancer lb> post (string path, Request request) returns (Response | HttpConnectorError) {
//    return performLoadBalanceAction(lb, path, request, null, HttpOperation.POST);
//}
//
//@Description {value:"The HEAD action implementation of the LoadBalancer Connector."}
//@Param {value:"path: Resource path"}
//@Param {value:"request: A Request struct"}
//@Return {value:"The Response struct"}
//@Return {value:"Error occurred during the action invocation, if any"}
//public function <LoadBalancer lb> head (string path, Request request) returns (Response | HttpConnectorError) {
//    return performLoadBalanceAction(lb, path, request, null, HttpOperation.HEAD);
//}
//
//@Description {value:"The PATCH action implementation of the LoadBalancer Connector."}
//@Param {value:"path: Resource path"}
//@Param {value:"request: A Request struct"}
//@Return {value:"The Response struct"}
//@Return {value:"Error occurred during the action invocation, if any"}
//public function <LoadBalancer lb> patch (string path, Request request) returns (Response | HttpConnectorError) {
//    return performLoadBalanceAction(lb, path, request, null, HttpOperation.PATCH);
//}
//
//@Description {value:"The PUT action implementation of the Load Balance Connector."}
//@Param {value:"path: Resource path"}
//@Param {value:"request: A Request struct"}
//@Return {value:"The Response struct"}
//@Return {value:"Error occurred during the action invocation, if any"}
//public function <LoadBalancer lb> put (string path, Request request) returns (Response | HttpConnectorError) {
//    return performLoadBalanceAction(lb, path, request, null, HttpOperation.PUT);
//}
//
//@Description {value:"The OPTIONS action implementation of the LoadBalancer Connector."}
//@Param {value:"path: Resource path"}
//@Param {value:"request: A Request struct"}
//@Return {value:"The Response struct"}
//@Return {value:"Error occurred during the action invocation, if any"}
//public function <LoadBalancer lb> options (string path, Request request) returns (Response | HttpConnectorError) {
//    return performLoadBalanceAction(lb, path, request, null, HttpOperation.OPTIONS);
//}
//
//@Description {value:"The FORWARD action implementation of the LoadBalancer Connector."}
//@Param {value:"path: Resource path"}
//@Param {value:"request: A Request struct"}
//@Return {value:"The Response struct"}
//@Return {value:"Error occurred during the action invocation, if any"}
//public function <LoadBalancer lb> forward (string path, Request request) returns (Response | HttpConnectorError) {
//    return performLoadBalanceAction(lb, path, null, request, HttpOperation.FORWARD);
//}
//
//@Description {value:"The EXECUTE action implementation of the LoadBalancer Connector. The Execute action can be used to invoke an HTTP call with the given HTTP verb."}
//@Param {value:"httpVerb: HTTP verb to be used for the request"}
//@Param {value:"path: Resource path"}
//@Param {value:"request: A Request struct"}
//@Return {value:"The Response struct"}
//@Return {value:"Error occurred during the action invocation, if any"}
//public function <LoadBalancer lb> execute (string httpVerb, string path, Request request) returns (Response | HttpConnectorError) {
//    return performLoadBalanceExecuteAction(lb, path, request, null, httpVerb);
//}
//
//@Description {value:"The DELETE action implementation of the LoadBalancer Connector."}
//@Param {value:"path: Resource path"}
//@Param {value:"request: A Request struct"}
//@Return {value:"The Response struct"}
//@Return {value:"Error occurred during the action invocation, if any"}
//public function <LoadBalancer lb> delete (string path, Request request) returns (Response | HttpConnectorError) {
//    return performLoadBalanceAction(lb, path, request, null, HttpOperation.DELETE);
//}
//
//@Description {value:"The GET action implementation of the LoadBalancer Connector."}
//@Param {value:"path: Resource path"}
//@Param {value:"request: A Request struct"}
//@Return {value:"The Response struct"}
//@Return {value:"Error occurred during the action invocation, if any"}
//public function <LoadBalancer lb> get (string path, Request request) returns (Response | HttpConnectorError) {
//    return performLoadBalanceAction(lb, path, request, null, HttpOperation.GET);
//}
//
//@Description { value:"The submit implementation of the LoadBalancer Connector."}
//@Param { value:"httpVerb: The HTTP verb value" }
//@Param { value:"path: The Resource path " }
//@Param { value:"req: An HTTP outbound request message" }
//@Return { value:"The Handle for further interactions" }
//@Return { value:"The Error occured during HTTP client invocation" }
//public function <LoadBalancer lb> submit (string httpVerb, string path, Request req) returns (HttpHandle | HttpConnectorError) {
//    HttpConnectorError httpConnectorError = {};
//    httpConnectorError.message = "Unsupported action for LoadBalancer Connector";
//    return null, httpConnectorError;
//}
//
//@Description { value:"The getResponse implementation of the LoadBalancer Connector."}
//@Param { value:"handle: The Handle which relates to previous async invocation" }
//@Return { value:"The HTTP response message" }
//@Return { value:"The Error occured during HTTP client invocation" }
//public function <LoadBalancer lb> getResponse (HttpHandle handle) returns (Response | HttpConnectorError) {
//    HttpConnectorError httpConnectorError = {};
//    httpConnectorError.message = "Unsupported action for LoadBalancer Connector";
//    return null, httpConnectorError;
//}
//
//@Description { value:"The hasPromise implementation of the LoadBalancer Connector."}
//@Param { value:"handle: The Handle which relates to previous async invocation" }
//@Return { value:"Whether push promise exists" }
//public function <LoadBalancer lb> hasPromise (HttpHandle handle) returns (boolean) {
//    return false;
//}
//
//@Description { value:"The getNextPromise implementation of the LoadBalancer Connector."}
//@Param { value:"handle: The Handle which relates to previous async invocation" }
//@Return { value:"The HTTP Push Promise message" }
//@Return { value:"The Error occured during HTTP client invocation" }
//public function <LoadBalancer lb> getNextPromise (HttpHandle handle) returns (PushPromise | HttpConnectorError) {
//    HttpConnectorError httpConnectorError = {};
//    httpConnectorError.message = "Unsupported action for LoadBalancer Connector";
//    return null, httpConnectorError;
//}
//
//@Description { value:"The getPromisedResponse implementation of the LoadBalancer Connector."}
//@Param { value:"promise: The related Push Promise message" }
//@Return { value:"HTTP The Push Response message" }
//@Return { value:"The Error occured during HTTP client invocation" }
//public function <LoadBalancer lb> getPromisedResponse (PushPromise promise) returns (Response | HttpConnectorError) {
//    HttpConnectorError httpConnectorError = {};
//    httpConnectorError.message = "Unsupported action for LoadBalancer Connector";
//    return null, httpConnectorError;
//}
//
//@Description { value:"The rejectPromise implementation of the LoadBalancer Connector."}
//@Param { value:"promise: The Push Promise need to be rejected" }
//@Return { value:"Whether operation is successful" }
//public function <LoadBalancer lb> rejectPromise (PushPromise promise) returns (boolean) {
//    return false;
//}
//
//// Performs execute action of the Load Balance connector. extract the corresponding http integer value representation
//// of the http verb and invokes the perform action method.
//function performLoadBalanceExecuteAction (LoadBalancer lb, string path, Request outRequest, Request inRequest,
//                                          string httpVerb) returns (Response | HttpConnectorError) {
//    HttpOperation connctorAction = extractHttpOperation(httpVerb);
//    if (connctorAction != null) {
//        return performLoadBalanceAction(lb, path, outRequest, inRequest, connctorAction);
//    } else {
//        HttpConnectorError httpConnectorError = {};
//        httpConnectorError.statusCode = 400;
//        httpConnectorError.message = "Unsupported connector action received.";
//        return httpConnectorError;
//    }
//}
//
//// Handles all the actions exposed through the Load Balance connector.
//function performLoadBalanceAction (LoadBalancer lb, string path, Request outRequest, Request inRequest,
//                                   HttpOperation requestAction) returns (Response | HttpConnectorError) {
//
//    HttpClient loadBalanceClient = lb.algorithm(lb, lb.loadBalanceClientsArray);
//
//    // Tracks at which point failover within the load balancing should be terminated.
//    int loadBalanceTermination;
//    LoadBalanceConnectorError loadBalanceConnectorError = {};
//    HttpConnectorError httpConnectorError;
//    loadBalanceConnectorError.httpConnectorError = [];
//    Response inResponse;
//
//    // When performing passthrough scenarios using Load Balance connector, message needs to be built before trying out the
//    // load balance endpoints to keep the request message to load balance the messages in case of failure.
//    if (inRequest != null && HttpOperation.FORWARD == requestAction) {
//        var binaryPayload, _ = inRequest.getBinaryPayload();
//    }
//
//    match invokeEndpoint(path, outRequest, inRequest, requestAction, loadBalanceClient) {
//        Response inResponse => { return inResponse; }
//        HttpConnectorError httpConnectorError => {
//            while (httpConnectorError != null) {
//                loadBalanceConnectorError.httpConnectorError[lb.nextIndex] = httpConnectorError;
//                loadBalanceClient = lb.algorithm(lb, lb.loadBalanceClientsArray);
//                match  invokeEndpoint(path, outRequest, inRequest, requestAction, loadBalanceClient) {
//                    Response inResponse => { return inResponse; }
//                    HttpConnectorError httpConnectorError => {
//                        loadBalanceTermination = loadBalanceTermination + 1;
//                        if (loadBalanceTermination == (lengthof lb.loadBalanceClientsArray - 1)) {
//                            loadBalanceTermination = 0;
//                            httpConnectorError = populateGenericLoadBalanceConnectorError(loadBalanceConnectorError, httpConnectorError, lb.nextIndex);
//                            break;
//                        }
//                    }
//                }
//            }
//            loadBalanceTermination = 0;
//            return httpConnectorError;
//        }
//    }
//}
//
//// Round Robin Algorithm implementation with respect to load balancing endpoints.
//public function (LoadBalancer, HttpClient[])(HttpClient) roundRobin =
//                        function (LoadBalancer lb, HttpClient [] loadBalanceConfigArray) returns (HttpClient) {
//    HttpClient httpClient;
//
//    lock {
//        if (lb.nextIndex == ((lengthof (loadBalanceConfigArray)) - 1)) {
//            httpClient = loadBalanceConfigArray[lb.nextIndex];
//            lb.nextIndex = 0;
//        } else {
//            httpClient = loadBalanceConfigArray[lb.nextIndex];
//            lb.nextIndex = lb.nextIndex + 1;
//        }
//    }
//
//    return httpClient;
//};
//
//// Populates generic error specific to Load Balance connector by including all the errors returned from endpoints.
//function populateGenericLoadBalanceConnectorError (LoadBalanceConnectorError loadBalanceConnectorError,
//                                                         HttpConnectorError httpConnectorError, int index)
//                                                         returns (HttpConnectorError) {
//    loadBalanceConnectorError.statusCode = 500;
//    loadBalanceConnectorError.httpConnectorError[index] = httpConnectorError;
//    string lastErrorMsg = httpConnectorError.message;
//    loadBalanceConnectorError.message = "All the load balance endpoints failed. Last error was " + lastErrorMsg;
//    return <HttpConnectorError> loadBalanceConnectorError;
//}
//

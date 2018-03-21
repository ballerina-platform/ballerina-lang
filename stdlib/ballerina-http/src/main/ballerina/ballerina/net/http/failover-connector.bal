//// Copyright (c) 2018 WSO2 Inc. (//www.wso2.org) All Rights Reserved.
////
//// WSO2 Inc. licenses this file to you under the Apache License,
//// Version 2.0 (the "License"); you may not use this file except
//// in compliance with the License.
//// You may obtain a copy of the License at
////
//// //www.apache.org/licenses/LICENSE-2.0
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
//import ballerina.mime;
//import ballerina.runtime;
//
//@Description {value:"Represents Failover connector retry configuration."}
//@Field {value:"failoverCodes: Array of http response status codes which required failover the requests."}
//@Field {value:"interval: Failover delay interval in millisecond."}
//public struct FailoverConfig {
//    int[] failoverCodes;
//    int interval;
//}
//
//@Description {value:"Represents an error occurred in an function of the Failover connector."}
//@Field {value:"message: An error message explaining about the error."}
//@Field {value:"cause: The error that caused HttpConnectorError to get thrown."}
//@Field {value:"stackTrace: Represents the invocation stack when FailoverConnectorError is thrown."}
//@Field {value:"statusCode: HTTP status code of the FailoverConnectorError."}
//@Field {value:"httpConnectorError: Array of HttpConnectorError error occurred at each endpoint."}
//public struct FailoverConnectorError {
//    string message;
//    error[] cause;
//    int statusCode;
//    HttpConnectorError[] httpConnectorError;
//}
//
//// Represents inferred failover configurations passed to Failover connector.
//struct FailoverInferredConfig {
//    HttpClient[] failoverClientsArray;
//    boolean[] failoverCodesIndex;
//    int failoverInterval;
//}
//
//@Description {value:"Failover client implementation to be used with the HTTP client connector to support failover."}
//@Field {value:"serviceUri: Service path."}
//@Field {value:"config: Represents options to be used for HTTP client invocation."}
//@Field {value:"failoverInferredConfig: Represents inferred failover configurations passed to Failover connector."}
//public struct Failover {
//    string serviceUri;
//    ClientEndpointConfiguration config;
//    FailoverInferredConfig failoverInferredConfig;
//}
//
//@Description {value:"The POST function implementation of the Failover Connector."}
//@Param {value:"path: Resource path"}
//@Param {value:"request: A Request struct"}
//@Return {value:"The Response struct"}
//@Return {value:"Error occurred during the function invocation, if any"}
//public function <Failover client> post(string path, Request request) returns (Response | HttpConnectorError) {
//    return performFailoverAction(path, request, null, HttpOperation.POST, client.failoverInferredConfig);
//}
//
//@Description {value:"The HEAD function implementation of the Failover Connector."}
//@Param {value:"path: Resource path"}
//@Param {value:"request: A Request struct"}
//@Return {value:"The Response struct"}
//@Return {value:"Error occurred during the function invocation, if any"}
//public function <Failover client> head(string path, Request request) returns (Response | HttpConnectorError) {
//    return performFailoverAction(path, request, null, HttpOperation.HEAD, client.failoverInferredConfig);
//}
//
//@Description {value:"The PATCH function implementation of the Failover Connector."}
//@Param {value:"path: Resource path"}
//@Param {value:"request: A Request struct"}
//@Return {value:"The Response struct"}
//@Return {value:"Error occurred during the functioninvocation, if any"}
//public function <Failover client> patch(string path, Request request) returns (Response | HttpConnectorError) {
//    return performFailoverAction(path, request, null, HttpOperation.PATCH, client.failoverInferredConfig);
//}
//
//@Description {value:"The PUT function  implementation of the Failover Connector."}
//@Param {value:"path: Resource path"}
//@Param {value:"request: A Request struct"}
//@Return {value:"The Response struct"}
//@Return {value:"Error occurred during the function invocation, if any"}
//public function <Failover client> put(string path, Request request) returns (Response|HttpConnectorError) {
//    return performFailoverAction(path, request, null, HttpOperation.PUT, client.failoverInferredConfig);
//}
//
//@Description {value:"The OPTIONS function implementation of the Failover Connector."}
//@Param {value:"path: Resource path"}
//@Param {value:"request: A Request struct"}
//@Return {value:"The Response struct"}
//@Return {value:"Error occurred during the function invocation, if any"}
//public function <Failover client> options(string path, Request request) returns (Response | HttpConnectorError) {
//    return performFailoverAction(path, request, null, HttpOperation.OPTIONS, client.failoverInferredConfig);
//}
//
//@Description {value:"The FORWARD function implementation of the Failover Connector."}
//@Param {value:"path: Resource path"}
//@Param {value:"request: A Request struct"}
//@Return {value:"The Response struct"}
//@Return {value:"Error occurred during the function invocation, if any"}
//public function <Failover client> forward(string path, Request request) returns (Response | HttpConnectorError) {
//    return performFailoverAction(path, null, request, HttpOperation.FORWARD, client.failoverInferredConfig);
//}
//
//@Description {value:"The EXECUTE function implementation of the Failover Connector. The Execute function can be used to invoke an HTTP call with the given HTTP verb."}
//@Param {value:"httpVerb: HTTP verb to be used for the request"}
//@Param {value:"path: Resource path"}
//@Param {value:"request: A Request struct"}
//@Return {value:"The Response struct"}
//@Return {value:"Error occurred during the function invocation, if any"}
//public function <Failover client> execute(string httpVerb, string path, Request request) returns (Response | HttpConnectorError) {
//    return performExecuteAction(path, request, null, httpVerb, client.failoverInferredConfig);
//}
//
//@Description {value:"The DELETE function implementation of the Failover Connector."}
//@Param {value:"path: Resource path"}
//@Param {value:"request: A Request struct"}
//@Return {value:"The Response struct"}
//@Return {value:"Error occurred during the function invocation, if any"}
//public function <Failover client> delete(string path, Request request) returns (Response | HttpConnectorError) {
//    return performFailoverAction(path, request, null, HttpOperation.DELETE, client.failoverInferredConfig);
//}
//
//@Description {value:"The GET function implementation of the Failover Connector."}
//@Param {value:"path: Resource path"}
//@Param {value:"request: A Request struct"}
//@Return {value:"The Response struct"}
//@Return {value:"Error occurred during the function invocation, if any"}
//public function <Failover client> get(string path, Request request) returns (Response | HttpConnectorError) {
//    return performFailoverAction(path, request, null, HttpOperation.GET, client.failoverInferredConfig);
//}
//
//@Description { value:"The submit implementation of the Failover Connector."}
//@Param { value:"httpVerb: The HTTP verb value" }
//@Param { value:"path: The Resource path " }
//@Param { value:"req: An HTTP outbound request message" }
//@Return { value:"The Handle for further interactions" }
//@Return { value:"The Error occured during HTTP client invocation" }
//public function <Failover client> submit(string httpVerb, string path, Request req) returns (HttpHandle | HttpConnectorError) {
//    HttpConnectorError httpConnectorError = {};
//    httpConnectorError.message = "Unsupported function for Failover Connector";
//    return httpConnectorError;
//}
//
//@Description { value:"The getResponse implementation of the Failover Connector."}
//@Param { value:"handle: The Handle which relates to previous async invocation" }
//@Return { value:"The HTTP response message" }
//@Return { value:"The Error occured during HTTP client invocation" }
//public function <Failover client> getResponse(HttpHandle handle) returns (HttpConnectorError) {
//    HttpConnectorError httpConnectorError = {};
//    httpConnectorError.message = "Unsupported function for Failover Connector";
//    return httpConnectorError;
//}
//
//@Description { value:"The hasPromise implementation of the Failover Connector."}
//@Param { value:"handle: The Handle which relates to previous async invocation" }
//@Return { value:"Whether push promise exists" }
//public function <Failover client> hasPromise(HttpHandle handle) returns (boolean) {
//    return false;
//}
//
//@Description { value:"The getNextPromise implementation of the Failover Connector."}
//@Param { value:"handle: The Handle which relates to previous async invocation" }
//@Return { value:"The HTTP Push Promise message" }
//@Return { value:"The Error occured during HTTP client invocation" }
//public function <Failover client> getNextPromise(HttpHandle handle) returns (PushPromise | HttpConnectorError) {
//    HttpConnectorError httpConnectorError = {};
//    httpConnectorError.message = "Unsupported function for Failover Connector";
//    return httpConnectorError;
//}
//
//@Description { value:"The getPromisedResponse implementation of the Failover Connector."}
//@Param { value:"promise: The related Push Promise message" }
//@Return { value:"HTTP The Push Response message" }
//@Return { value:"The Error occured during HTTP client invocation" }
//public function <Failover client> getPromisedResponse(PushPromise promise) returns (Response | HttpConnectorError) {
//    HttpConnectorError httpConnectorError = {};
//    httpConnectorError.message = "Unsupported function for Failover Connector";
//    return httpConnectorError;
//}
//
//@Description { value:"The rejectPromise implementation of the Failover Connector."}
//@Param { value:"promise: The Push Promise need to be rejected" }
//@Return { value:"Whether operation is successful" }
//public function <Failover client> rejectPromise(PushPromise promise) returns (boolean) {
//    return false;
//}
//
//// Performs execute action of the Failover connector. extract the corresponding http integer value representation
//// of the http verb and invokes the perform action method.
//function performExecuteAction (string path, Request outRequest, Request inRequest,
//                               string httpVerb, FailoverInferredConfig failoverInferredConfig)
//(Response, HttpConnectorError) {
//    HttpOperation connectorAction = extractHttpOperation(httpVerb);
//    return performFailoverAction(path, outRequest, inRequest, connectorAction, failoverInferredConfig);
//}
//
//// Handles all the actions exposed through the Failover connector.
//function performFailoverAction (string path, Request outRequest, Request inRequest,
//                                HttpOperation requestAction, FailoverInferredConfig failoverInferredConfig) returns
//                                                                        (Response | HttpConnectorError) {
//    boolean[] failoverCodeIndex = failoverInferredConfig.failoverCodesIndex;
//    int noOfEndpoints = lengthof (failoverInferredConfig.failoverClientsArray);
//    int currentIndex = 0;
//    int initialIndex = 0;
//    int startIndex = -1;
//    int failoverInterval = failoverInferredConfig.failoverInterval;
//
//    FailoverConnectorError failoverConnectorError = {};
//    HttpClient[] failoverClients = failoverInferredConfig.failoverClientsArray;
//    HttpClient failoverClient = failoverClients[currentIndex];
//    Response inResponse;
//    HttpConnectorError httpConnectorError = {};
//    failoverConnectorError.httpConnectorError = [];
//
//    // When performing passthrough scenarios using Failover connector, message needs to be built before trying out the
//    // failover endpoints to keep the request message to failover the messages.
//    if (inRequest != null && HttpOperation.FORWARD == requestAction) {
//        var binaryPayload,_ = inRequest.getBinaryPayload();
//    }
//
//    mime:Entity requestEntity = null;
//    if (outRequest != null) {
//        requestEntity,_ = outRequest.getEntity();
//    }
//    //TODO: refactor this logic to new syntax
//    // while (startIndex != currentIndex) {
//    //     startIndex = initialIndex;
//    //     currentIndex = currentIndex + 1;
//    //     match invokeEndpoint(path, outRequest, inRequest, requestAction, failoverClient);
//    //     Response inResponse = { return inResponse; }
//    //     inResponse, httpConnectorError = invokeEndpoint(path, outRequest, inRequest, requestAction, failoverClient);
//    //     if (inResponse == null && httpConnectorError != null) {
//    //         outRequest = {};
//    //         if (requestEntity != null) {
//    //             outRequest.setEntity(requestEntity);
//    //         }
//
//    //         if (noOfEndpoints > currentIndex) {
//    //             runtime:sleepCurrentWorker(failoverInterval);
//    //             failoverConnectorError.httpConnectorError[currentIndex - 1] = httpConnectorError;
//    //             failoverClient = failoverClients[currentIndex];
//    //         } else {
//    //             return populateGenericFailoverConnectorError(failoverConnectorError, httpConnectorError, currentIndex - 1);
//    //         }
//    //     }
//    //     if (inResponse != null) {
//    //         int httpStatusCode = inResponse.statusCode;
//    //         if (failoverCodeIndex[httpStatusCode] == true) {
//    //             if (noOfEndpoints > currentIndex) {
//    //                 outRequest = {};
//    //                 if (requestEntity != null) {
//    //                     outRequest.setEntity(requestEntity);
//    //                 }
//    //                 runtime:sleepCurrentWorker(failoverInterval);
//    //                 failoverClient = failoverClients[currentIndex];
//    //                 populateFailoverErrorHttpStatusCodes(inResponse, failoverConnectorError, currentIndex - 1);
//    //             } else {
//    //                 return populateErrorsFromLastResponse(inResponse, failoverConnectorError, httpConnectorError, currentIndex - 1);
//    //             }
//    //         } else {
//    //             break;
//    //         }
//    //     }
//    // }
//    return inResponse;
//}
//
//// Populates generic error specific to Failover connector by including all the errors returned from endpoints.
//function populateGenericFailoverConnectorError (FailoverConnectorError failoverConnectorErr,
//                                                HttpConnectorError httpConnectorError, int index) returns
//(Response | HttpConnectorError) {
//    failoverConnectorErr.statusCode = 500;
//    failoverConnectorErr.httpConnectorError[index] = httpConnectorError;
//    string lastErrorMsg = httpConnectorError.message;
//    failoverConnectorErr.message = "All the failover endpoints failed. Last error was " + lastErrorMsg;
//    return null, (HttpConnectorError) failoverConnectorErr;
//}
//
//// If leaf endpoint returns a response with status code configured to retry in the failover connector, failover error
//// will be generated with last response status code and generic failover response.
//function populateFailoverErrorHttpStatusCodes (Response inResponse,
//                                               FailoverConnectorError failoverConnectorError, int index) {
//    HttpConnectorError httpConnectorError = {};
//    httpConnectorError.message = "Endpoint " + index + " returned response is: "
//                                + inResponse.statusCode + " " + inResponse.reasonPhrase;
//    failoverConnectorError.httpConnectorError[index] = httpConnectorError;
//}
//
//// If leaf endpoint returns a response with status code configured to retry in the failover connector, generic
//// failover error and HttpConnectorError error will be generated.
//function populateErrorsFromLastResponse (Response inRsponse, FailoverConnectorError failoverConnectorError,
//                                                            HttpConnectorError httpConnectorError, int index)
//                                                                            returns (HttpConnectorError) {
//    HttpConnectorError lastHttpConnectorError = {};
//    lastHttpConnectorError.statusCode = inRsponse.statusCode;
//    lastHttpConnectorError.message = "Last endpoint returned response: " + inRsponse.statusCode + " "
//                                        + inRsponse.reasonPhrase;
//    failoverConnectorError.httpConnectorError[index] = lastHttpConnectorError;
//    failoverConnectorError.statusCode = 500;
//    failoverConnectorError.message = "All the failover endpoints failed. Last endpoint returned response is: "
//                                        + inRsponse.statusCode + " " + inRsponse.reasonPhrase;
//    httpConnectorError = <HttpConnectorError> failoverConnectorError;
//    return httpConnectorError;
//}
//

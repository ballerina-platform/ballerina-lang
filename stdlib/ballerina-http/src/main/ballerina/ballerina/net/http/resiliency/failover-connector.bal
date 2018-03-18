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

package ballerina.net.http.resiliency;

import ballerina.net.http;
import ballerina.runtime;
import ballerina.mime;

@Description {value:"Represents Failover connector retry configuration."}
@Field {value:"failoverCodes: Array of http response status codes which required failover the requests."}
@Field {value:"interval: Failover delay interval in millisecond."}
public struct FailoverConfig {
    int[] failoverCodes;
    int interval;
}

@Description {value:"Represents an error occurred in an action of the Failover connector."}
@Field {value:"message: An error message explaining about the error."}
@Field {value:"cause: The error that caused HttpConnectorError to get thrown."}
@Field {value:"stackTrace: Represents the invocation stack when FailoverConnectorError is thrown."}
@Field {value:"statusCode: HTTP status code of the FailoverConnectorError."}
@Field {value:"httpConnectorError: Array of HttpConnectorError error occurred at each endpoint."}
public struct FailoverConnectorError {
    string message;
    error[] cause;
    int statusCode;
    http:HttpConnectorError[] httpConnectorError;
}

// Represents inferred failover configurations passed to Failover connector.
struct FailoverInferredConfig {
    http:HttpClient[] failoverClientsArray;
    boolean[] failoverCodesIndex;
    int failoverInterval;
}

@Description {value:"Failover Connector implementation to be used with the HTTP client connector to support failover."}
@Param {value:"failoverClientsArray: Array of HttpClient connector to be failover."}
@Param {value:"failoverConfig: The failoverCodes which contains Response Http status codes needs to be faiover."}
public connector Failover (http:HttpClient[] failoverClientsArray, FailoverConfig failoverConfig) {

    boolean[] failoverCodes = populateErrorCodeIndex(failoverConfig.failoverCodes);

    FailoverInferredConfig failoverInferredConfig = {
                                                        failoverClientsArray:failoverClientsArray,
                                                        failoverCodesIndex:failoverCodes,
                                                        failoverInterval:failoverConfig.interval
                                                    };

    @Description {value:"The POST action implementation of the Failover Connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action post (string path, http:Request request) (http:Response, http:HttpConnectorError) {
        return performFailoverAction(path, request, null, HttpOperation.POST, failoverInferredConfig);
    }

    @Description {value:"The HEAD action implementation of the Failover Connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action head (string path, http:Request request) (http:Response, http:HttpConnectorError) {
        return performFailoverAction(path, request, null, HttpOperation.HEAD, failoverInferredConfig);
    }

    @Description {value:"The PATCH action implementation of the Failover Connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action patch (string path, http:Request request) (http:Response, http:HttpConnectorError) {
        return performFailoverAction(path, request, null, HttpOperation.PATCH, failoverInferredConfig);
    }

    @Description {value:"The PUT action implementation of the Failover Connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action put (string path, http:Request request) (http:Response, http:HttpConnectorError) {
        return performFailoverAction(path, request, null, HttpOperation.PUT, failoverInferredConfig);
    }

    @Description {value:"The OPTIONS action implementation of the Failover Connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action options (string path, http:Request request) (http:Response, http:HttpConnectorError) {
        return performFailoverAction(path, request, null, HttpOperation.OPTIONS, failoverInferredConfig);
    }

    @Description {value:"The FORWARD action implementation of the Failover Connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action forward (string path, http:Request request) (http:Response, http:HttpConnectorError) {
        return performFailoverAction(path, null, request, HttpOperation.FORWARD, failoverInferredConfig);
    }

    @Description {value:"The EXECUTE action implementation of the Failover Connector. The Execute action can be used to invoke an HTTP call with the given HTTP verb."}
    @Param {value:"httpVerb: HTTP verb to be used for the request"}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action execute (string httpVerb, string path, http:Request request) (http:Response, http:HttpConnectorError) {
        return performExecuteAction(path, request, null, httpVerb, failoverInferredConfig);
    }

    @Description {value:"The DELETE action implementation of the Failover Connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action delete (string path, http:Request request) (http:Response, http:HttpConnectorError) {
        return performFailoverAction(path, request, null, HttpOperation.DELETE, failoverInferredConfig);
    }

    @Description {value:"The GET action implementation of the Failover Connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the action invocation, if any"}
    action get (string path, http:Request request) (http:Response, http:HttpConnectorError) {
        return performFailoverAction(path, request, null, HttpOperation.GET, failoverInferredConfig);
    }

    @Description { value:"The submit implementation of the Failover Connector."}
    @Param { value:"httpVerb: The HTTP verb value" }
    @Param { value:"path: The Resource path " }
    @Param { value:"req: An HTTP outbound request message" }
    @Return { value:"The Handle for further interactions" }
    @Return { value:"The Error occured during HTTP client invocation" }
    action submit (string httpVerb, string path, http:Request req) (http:HttpHandle, http:HttpConnectorError) {
        http:HttpConnectorError httpConnectorError = {};
        httpConnectorError.message = "Unsupported action for Failover Connector";
        return null, httpConnectorError;
    }

    @Description { value:"The getResponse implementation of the Failover Connector."}
    @Param { value:"handle: The Handle which relates to previous async invocation" }
    @Return { value:"The HTTP response message" }
    @Return { value:"The Error occured during HTTP client invocation" }
    action getResponse (http:HttpHandle handle) (http:Response, http:HttpConnectorError) {
        http:HttpConnectorError httpConnectorError = {};
        httpConnectorError.message = "Unsupported action for Failover Connector";
        return null, httpConnectorError;
    }

    @Description { value:"The hasPromise implementation of the Failover Connector."}
    @Param { value:"handle: The Handle which relates to previous async invocation" }
    @Return { value:"Whether push promise exists" }
    action hasPromise (http:HttpHandle handle) (boolean) {
        return false;
    }

    @Description { value:"The getNextPromise implementation of the Failover Connector."}
    @Param { value:"handle: The Handle which relates to previous async invocation" }
    @Return { value:"The HTTP Push Promise message" }
    @Return { value:"The Error occured during HTTP client invocation" }
    action getNextPromise (http:HttpHandle handle) (http:PushPromise, http:HttpConnectorError) {
        http:HttpConnectorError httpConnectorError = {};
        httpConnectorError.message = "Unsupported action for Failover Connector";
        return null, httpConnectorError;
    }

    @Description { value:"The getPromisedResponse implementation of the Failover Connector."}
    @Param { value:"promise: The related Push Promise message" }
    @Return { value:"HTTP The Push Response message" }
    @Return { value:"The Error occured during HTTP client invocation" }
    action getPromisedResponse (http:PushPromise promise) (http:Response, http:HttpConnectorError) {
        http:HttpConnectorError httpConnectorError = {};
        httpConnectorError.message = "Unsupported action for Failover Connector";
        return null, httpConnectorError;
    }

    @Description { value:"The rejectPromise implementation of the Failover Connector."}
    @Param { value:"promise: The Push Promise need to be rejected" }
    @Return { value:"Whether operation is successful" }
    action rejectPromise (http:PushPromise promise) (boolean) {
        return false;
    }
}

// Performs execute action of the Failover connector. extract the corresponding http integer value representation
// of the http verb and invokes the perform action method.
function performExecuteAction (string path, http:Request outRequest, http:Request inRequest,
                               string httpVerb, FailoverInferredConfig failoverInferredConfig)
(http:Response, http:HttpConnectorError) {
    HttpOperation connectorAction = extractHttpOperation(httpVerb);
    return performFailoverAction(path, outRequest, inRequest, connectorAction, failoverInferredConfig);
}

// Handles all the actions exposed through the Failover connector.
function performFailoverAction (string path, http:Request outRequest, http:Request inRequest,
                                HttpOperation requestAction, FailoverInferredConfig failoverInferredConfig)
                                                                        (http:Response, http:HttpConnectorError) {
    boolean[] failoverCodeIndex = failoverInferredConfig.failoverCodesIndex;
    int noOfEndpoints = lengthof (failoverInferredConfig.failoverClientsArray);
    int currentIndex = 0;
    int initialIndex = 0;
    int startIndex = -1;
    int failoverInterval = failoverInferredConfig.failoverInterval;

    FailoverConnectorError failoverConnectorError = {};
    http:HttpClient[] failoverClients = failoverInferredConfig.failoverClientsArray;
    http:HttpClient failoverClient = failoverClients[currentIndex];
    http:Response inResponse;
    http:HttpConnectorError httpConnectorError = {};
    failoverConnectorError.httpConnectorError = [];

    // When performing passthrough scenarios using Failover connector, message needs to be built before trying out the
    // failover endpoints to keep the request message to failover the messages.
    if (inRequest != null && HttpOperation.FORWARD == requestAction) {
        blob binaryPayload = inRequest.getBinaryPayload();
    }

    mime:Entity requestEntity = null;
    if (outRequest != null) {
        requestEntity = outRequest.getEntity();
    }

    while (startIndex != currentIndex) {
        startIndex = initialIndex;
        currentIndex = currentIndex + 1;
        inResponse, httpConnectorError = invokeEndpoint(path, outRequest, inRequest, requestAction, failoverClient);

        if (inResponse == null && httpConnectorError != null) {
            outRequest = {};
            if (requestEntity != null) {
                outRequest.setEntity(requestEntity);
            }

            if (noOfEndpoints > currentIndex) {
                runtime:sleepCurrentWorker(failoverInterval);
                failoverConnectorError.httpConnectorError[currentIndex - 1] = httpConnectorError;
                failoverClient = failoverClients[currentIndex];
            } else {
                return populateGenericFailoverConnectorError(failoverConnectorError, httpConnectorError, currentIndex - 1);
            }
        }
        if (inResponse != null) {
            int httpStatusCode = inResponse.statusCode;
            if (failoverCodeIndex[httpStatusCode] == true) {
                if (noOfEndpoints > currentIndex) {
                    outRequest = {};
                    if (requestEntity != null) {
                        outRequest.setEntity(requestEntity);
                    }
                    runtime:sleepCurrentWorker(failoverInterval);
                    failoverClient = failoverClients[currentIndex];
                    populateFailoverErrorHttpStatusCodes(inResponse, failoverConnectorError, currentIndex - 1);
                } else {
                    return populateErrorsFromLastResponse(inResponse, failoverConnectorError, httpConnectorError, currentIndex - 1);
                }
            } else {
                break;
            }
        }
    }
    return inResponse, null;
}

// Populates generic error specific to Failover connector by including all the errors returned from endpoints.
function populateGenericFailoverConnectorError (FailoverConnectorError failoverConnectorErr,
                                                http:HttpConnectorError httpConnectorError, int index)
(http:Response, http:HttpConnectorError) {
    failoverConnectorErr.statusCode = 500;
    failoverConnectorErr.httpConnectorError[index] = httpConnectorError;
    string lastErrorMsg = httpConnectorError.message;
    failoverConnectorErr.message = "All the failover endpoints failed. Last error was " + lastErrorMsg;
    return null, (http:HttpConnectorError) failoverConnectorErr;
}

// If leaf endpoint returns a response with status code configured to retry in the failover connector, failover error
// will be generated with last response status code and generic failover response.
function populateFailoverErrorHttpStatusCodes (http:Response inResponse,
                                               FailoverConnectorError failoverConnectorError, int index) {
    http:HttpConnectorError httpConnectorError = {};
    httpConnectorError.message = "Endpoint " + index + " returned response is: "
                                + inResponse.statusCode + " " + inResponse.reasonPhrase;
    failoverConnectorError.httpConnectorError[index] = httpConnectorError;
}

// If leaf endpoint returns a response with status code configured to retry in the failover connector, generic
// failover error and HttpConnectorError error will be generated.
function populateErrorsFromLastResponse (http:Response inRsponse, FailoverConnectorError failoverConnectorError,
                                                            http:HttpConnectorError httpConnectorError, int index)
                                                                            (http:Response, http:HttpConnectorError) {
    http:HttpConnectorError lastHttpConnectorError = {};
    lastHttpConnectorError.statusCode = inRsponse.statusCode;
    lastHttpConnectorError.message = "Last endpoint returned response: " + inRsponse.statusCode + " "
                                        + inRsponse.reasonPhrase;
    failoverConnectorError.httpConnectorError[index] = lastHttpConnectorError;
    failoverConnectorError.statusCode = 500;
    failoverConnectorError.message = "All the failover endpoints failed. Last endpoint returned response is: "
                                        + inRsponse.statusCode + " " + inRsponse.reasonPhrase;
    httpConnectorError = (http:HttpConnectorError) failoverConnectorError;
    return null, httpConnectorError;
}


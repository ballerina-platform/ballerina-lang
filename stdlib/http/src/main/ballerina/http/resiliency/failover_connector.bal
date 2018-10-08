// Copyright (c) 2018 WSO2 Inc. (//www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// //www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
//

import ballerina/mime;
import ballerina/runtime;
import ballerina/io;

# Provides a set of configurations for controlling the failover behaviour of the endpoint.
#
# + failoverCodes - Array of HTTP response status codes for which the failover mechanism triggers
# + interval - Failover delay interval in milliseconds
public type FailoverConfig record {
    int[] failoverCodes;
    int interval;
    !...
};

# Defines an error which occurred during an attempt to failover.
#
# + message - An explanation on what went wrong
# + cause - The cause of the `FailoverActionError`, if available
# + statusCode - HTTP status code to be sent to the caller
# + httpActionErr - Errors which occurred at each endpoint during the failover
public type FailoverActionError record {
    string message;
    error? cause;
    int statusCode;
    error[] httpActionErr;
    !...
};

// TODO: This can be made package private
// Represents inferred failover configurations passed to Failover connector.
# Inferred failover configurations passed into the failover client.
#
# + failoverClientsArray - Array of HTTP Clients that needs to be Failover
# + failoverCodesIndex - An indexed array of HTTP response status codes for which the failover mechanism triggers
# + failoverInterval - Failover delay interval in milliseconds
public type FailoverInferredConfig record {
    CallerActions[] failoverClientsArray;
    boolean[] failoverCodesIndex;
    int failoverInterval;
    !...
};

# Failover caller actions which provides failover capabilities to the failover client endpoint.
#
# + serviceUri - The URL of the remote HTTP endpoint
# + config - The configurations of the client endpoint associated with this `Failover` instance
# + failoverInferredConfig - Configurations derived from `FailoverConfig`
# + succeededEndpointIndex - Index of the `CallerActions[]` array which given a successful response
public type FailoverActions object {

    public string serviceUri;
    public ClientEndpointConfig config;
    public FailoverInferredConfig failoverInferredConfig;
    public int succeededEndpointIndex;

    # Failover caller actions which provides failover capabilities to an HTTP client endpoint.
    #
    # + serviceUri - The URL of the remote HTTP endpoint
    # + config - The configurations of the client endpoint associated with this `Failover` instance
    # + failoverInferredConfig - Configurations derived from `FailoverConfig`
    public new (serviceUri, config, failoverInferredConfig) {}

    # The POST action implementation of the Failover Connector.
    #
    # + path - Resource path
    # + message - HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
    #             or `mime:Entity[]`
    # + return - The response or an `error` if failed to fulfill the request
    public function post(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                        message) returns Response|error;

    # The HEAD action implementation of the Failover Connector.
    #
    # + path - Resource path
    # + message - An optional HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
    #             or `mime:Entity[]`
    # + return - The response or an `error` if failed to fulfill the request
    public function head(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                        message = ()) returns Response|error;

    # The PATCH action implementation of the Failover Connector.
    #
    # + path - Resource path
    # + message - An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
    #             or `mime:Entity[]`
    # + return - The response or an `error` if failed to fulfill the request
    public function patch(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                            message) returns Response|error;

    # The PUT action  implementation of the Failover Connector.
    #
    # + path - Resource path
    # + message - An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
    #             or `mime:Entity[]`
    # + return - The response or an `error` if failed to fulfill the request
    public function put(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                        message) returns Response|error;

    # The OPTIONS action implementation of the Failover Connector.
    #
    # + path - Resource path
    # + message - An optional HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
    #             or `mime:Entity[]`
    # + return - The response or an `error` if failed to fulfill the request
    public function options(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                            message = ()) returns Response|error;

    # Invokes an HTTP call using the incoming request's HTTP method.
    #
    # + path - Resource path
    # + request - An HTTP request
    # + return - The response or an `error` if failed to fulfill the request
    public function forward(string path, Request request) returns Response|error;

    # Invokes an HTTP call with the specified HTTP method.
    #
    # + httpVerb - HTTP method to be used for the request
    # + path - Resource path
    # + message - An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
    #             or `mime:Entity[]`
    # + return - The response or an `error` if failed to fulfill the request
    public function execute(string httpVerb, string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                            message) returns Response|error;

    # The DELETE action implementation of the Failover Connector.
    #
    # + path - Resource path
    # + message - An HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
    #             or `mime:Entity[]`
    # + return - The response or an `error` if failed to fulfill the request
    public function delete(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                            message) returns Response|error;

    # The GET action implementation of the Failover Connector.
    #
    # + path - Resource path
    # + message - An optional HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
    #             or `mime:Entity[]`
    # + return - The response or an `error` if failed to fulfill the request
    public function get(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                        message = ()) returns Response|error;

    # Submits an HTTP request to a service with the specified HTTP verb. The `submit()` function does not return
    # a `Response` as the result, rather it returns an `HttpFuture` which can be used for subsequent interactions
    # with the HTTP endpoint.
    #
    # + httpVerb - The HTTP verb value
    # + path - The resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ByteChannel` or `mime:Entity[]`
    # + return - An `HttpFuture` that represents an asynchronous service invocation, or an `error` if the submission fails
    public function submit(string httpVerb, string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                            message) returns HttpFuture|error;

    # Retrieves the `Response` for a previously submitted request.
    #
    # + httpFuture - The `HttpFuture` related to a previous asynchronous invocation
    # + return - An HTTP response message, or an `error` if the invocation fails
    public function getResponse(HttpFuture httpFuture) returns error;

    # Checks whether a `PushPromise` exists for a previously submitted request.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - A `boolean` that represents whether a `PushPromise` exists
    public function hasPromise(HttpFuture httpFuture) returns boolean;

    # Retrieves the next available `PushPromise` for a previously submitted request.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - An HTTP Push Promise message, or an `error` if the invocation fails
    public function getNextPromise(HttpFuture httpFuture) returns PushPromise|error;

    # Retrieves the promised server push `Response` message.
    #
    # + promise - The related `PushPromise`
    # + return - A promised HTTP `Response` message, or an `error` if the invocation fails
    public function getPromisedResponse(PushPromise promise) returns Response|error;

    # Rejects a `PushPromise`. When a `PushPromise` is rejected, there is no chance of fetching a promised
    # response using the rejected promise.
    #
    # + promise - The Push Promise to be rejected
    public function rejectPromise(PushPromise promise);
};

function FailoverActions::post(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                    message) returns Response|error {
    Request req = buildRequest(message);
    return performFailoverAction(path, req, HTTP_POST, self);
}

function FailoverActions::head(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                    message = ()) returns Response|error {
    Request req = buildRequest(message);
    return performFailoverAction(path, req, HTTP_HEAD, self);
}

function FailoverActions::patch(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                        message) returns Response|error {
    Request req = buildRequest(message);
    return performFailoverAction(path, req, HTTP_PATCH, self);
}

function FailoverActions::put(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                    message) returns Response|error {
    Request req = buildRequest(message);
    return performFailoverAction(path, req, HTTP_PUT, self);
}

function FailoverActions::options(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                        message = ()) returns Response|error {
    Request req = buildRequest(message);
    return performFailoverAction(path, req, HTTP_OPTIONS, self);
}

function FailoverActions::forward(string path, Request request) returns Response|error {
    return performFailoverAction(path, request, HTTP_FORWARD, self);
}

function FailoverActions::execute(string httpVerb, string path, Request|string|xml|json|byte[]|io:ByteChannel
                                                                |mime:Entity[]|() message) returns Response|error {
    Request req = buildRequest(message);
    return performExecuteAction(path, req, httpVerb, self);
}

function FailoverActions::delete(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                        message) returns Response|error {
    Request req = buildRequest(message);
    return performFailoverAction(path, req, HTTP_DELETE, self);
}

function FailoverActions::get(string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                    message = ()) returns Response|error {
    Request req = buildRequest(message);
    return performFailoverAction(path, req, HTTP_GET, self);
}

function FailoverActions::submit(string httpVerb, string path, Request|string|xml|json|byte[]|io:ByteChannel
                                                            |mime:Entity[]|() message) returns HttpFuture|error {
    error err = {message:"Unsupported action for Failover client."};
    return err;
}

function FailoverActions::getResponse(HttpFuture httpFuture) returns (error) {
    error err = {message:"Unsupported action for Failover client."};
    return err;
}

function FailoverActions::hasPromise(HttpFuture httpFuture) returns (boolean) {
    return false;
}

function FailoverActions::getNextPromise(HttpFuture httpFuture) returns PushPromise|error {
    error err = {message:"Unsupported action for Failover client."};
    return err;
}

function FailoverActions::getPromisedResponse(PushPromise promise) returns Response|error {
    error err = {message:"Unsupported action for Failover client."};
    return err;
}

function FailoverActions::rejectPromise(PushPromise promise) {
}

// Performs execute action of the Failover connector. extract the corresponding http integer value representation
// of the http verb and invokes the perform action method.
function performExecuteAction (string path, Request request, string httpVerb,
                                    FailoverActions failoverActions) returns Response|error {
    HttpOperation connectorAction = extractHttpOperation(httpVerb);
    return performFailoverAction(path, request, connectorAction, failoverActions);
}

// Handles all the actions exposed through the Failover connector.
function performFailoverAction (string path, Request request, HttpOperation requestAction,
                                                FailoverActions failoverActions) returns Response|error {
    FailoverInferredConfig failoverInferredConfig = failoverActions.failoverInferredConfig;
    boolean[] failoverCodeIndex = failoverInferredConfig.failoverCodesIndex;
    int noOfEndpoints = lengthof (failoverInferredConfig.failoverClientsArray);
    // currentIndex and initialIndex are need to set to last succeeded endpoint index to start failover with
    // the endpoint which gave the expected results.
    int currentIndex = failoverActions.succeededEndpointIndex;
    int initialIndex = failoverActions.succeededEndpointIndex;
    int startIndex = -1;
    int failoverInterval = failoverInferredConfig.failoverInterval;

    FailoverActionError failoverActionErr;
    CallerActions[] failoverClients = failoverInferredConfig.failoverClientsArray;
    CallerActions failoverClient = failoverClients[failoverActions.succeededEndpointIndex];
    Response inResponse = new;
    Request failoverRequest = request;
    failoverActionErr.httpActionErr = [];
    mime:Entity requestEntity = new;

    if (isMultipartRequest(failoverRequest)) {
        failoverRequest = populateMultipartRequest(failoverRequest);
    } else {
        // When performing passthrough scenarios using Failover connector, message needs to be built before trying
        // out the failover endpoints to keep the request message to failover the messages.
        var binaryPayload = failoverRequest.getBinaryPayload();
        requestEntity = check failoverRequest.getEntity();
    }
    while (startIndex != currentIndex) {
        startIndex = initialIndex;
        currentIndex = currentIndex + 1;
        var invokedEndpoint = invokeEndpoint(path, failoverRequest, requestAction, failoverClient);
        match invokedEndpoint {
            Response response => {
                inResponse = response;
                int httpStatusCode = response.statusCode;
                // Check whether HTTP status code of the response falls into configued `failoverCodes`
                if (failoverCodeIndex[httpStatusCode] == true) {
                    // If the initialIndex == DEFAULT_FAILOVER_EP_STARTING_INDEX check successful, that means the first
                    // endpoint configured in the failover endpoints gave the response where its HTTP status code
                    // falls into configued `failoverCodes`
                    if (initialIndex == DEFAULT_FAILOVER_EP_STARTING_INDEX) {
                        if (noOfEndpoints > currentIndex) {
                            // If the execution lands here, that means there are endpoints that haven't tried out by
                            // failover endpoint. Hence response will be collected to generate final response.
                            populateFailoverErrorHttpStatusCodes(inResponse, failoverActionErr, currentIndex - 1);
                        } else {
                            // If the execution lands here, that means all the endpoints has been tried out and final
                            // endpoint gave the response where its HTTP status code falls into configued
                            // `failoverCodes`. Therefore appropriate error message needs to be generated and should
                            // return it to the client.
                            return populateErrorsFromLastResponse(inResponse, failoverActionErr, currentIndex - 1);
                        }
                    } else {
                        // If execution reaches here, that means failover has not started with the default starting index.
                        // Failover resumed from the last succeeded endpoint.
                        if (initialIndex == currentIndex) {
                            // If the execution lands here, that means all the endpoints has been tried out and final
                            // endpoint gives the response where its HTTP status code falls into configued
                            // `failoverCodes`. Therefore appropriate error message needs to be generated and should
                            // return it to the client.
                            return populateErrorsFromLastResponse(inResponse, failoverActionErr, currentIndex - 1);
                        } else if (noOfEndpoints == currentIndex) {
                            // If the execution lands here, that means the last endpoint has been tried out and
                            // endpoint gave a response where its HTTP status code falls into configued
                            // `failoverCodes`. Since failover resumed from the last succeeded endpoint we nned try out
                            // remaining endpoints. Therefore currentIndex need to be reset.
                            populateFailoverErrorHttpStatusCodes(inResponse, failoverActionErr, currentIndex - 1);
                            currentIndex = DEFAULT_FAILOVER_EP_STARTING_INDEX;
                        } else if (noOfEndpoints > currentIndex) {
                            // Collect the response to generate final response.
                            populateFailoverErrorHttpStatusCodes(inResponse, failoverActionErr, currentIndex - 1);
                        }
                    }
                } else {
                    // If the execution reaches here, that means the first endpoint configured in the failover endpoints
                    // gives the expected response.
                    failoverActions.succeededEndpointIndex = currentIndex - 1;
                    break;
                }
            }
            error httpConnectorErr => {
                // If the initialIndex == DEFAULT_FAILOVER_EP_STARTING_INDEX check successful, that means the first
                // endpoint configured in the failover endpoints gave the errornous response.
                if (initialIndex == DEFAULT_FAILOVER_EP_STARTING_INDEX) {
                    if (noOfEndpoints > currentIndex) {
                        // If the execution lands here, that means there are endpoints that haven't tried out by
                        // failover endpoint. Hence error will be collected to generate final response.
                        failoverActionErr.httpActionErr[currentIndex - 1] = httpConnectorErr;
                    } else {
                        // If the execution lands here, that means all the endpoints has been tried out and final
                        // endpoint gave an errornous response. Therefore appropriate error message needs to be
                        //  generated and should return it to the client.
                        return populateGenericFailoverActionError(failoverActionErr, httpConnectorErr, currentIndex - 1);
                    }
                } else {
                    // If execution reaches here, that means failover has not started with the default starting index.
                    // Failover resumed from the last succeeded endpoint.
                    if (initialIndex == currentIndex) {
                        // If the execution lands here, that means all the endpoints has been tried out and final
                        // endpoint gave an errornous response. Therefore appropriate error message needs to be
                        //  generated and should return it to the client.
                        return populateGenericFailoverActionError(failoverActionErr, httpConnectorErr, currentIndex - 1);
                    } else if (noOfEndpoints == currentIndex) {
                        // If the execution lands here, that means the last endpoint has been tried out and endpoint gave
                        // a errornous response. Since failover resumed from the last succeeded endpoint we need try out
                        // remaining endpoints. Therefore currentIndex need to be reset.
                        failoverActionErr.httpActionErr[currentIndex - 1] = httpConnectorErr;
                        currentIndex = DEFAULT_FAILOVER_EP_STARTING_INDEX;
                    } else if (noOfEndpoints > currentIndex) {
                        // Collect the error to generate final response.
                        failoverActionErr.httpActionErr[currentIndex - 1] = httpConnectorErr;
                    }
                }
            }
        }
        failoverRequest = createFailoverRequest(failoverRequest, requestEntity);
        runtime:sleep(failoverInterval);
        failoverClient = failoverClients[currentIndex];
    }
    return inResponse;
}

// Populates generic error specific to Failover connector by including all the errors returned from endpoints.
function populateGenericFailoverActionError (FailoverActionError failoverActionErr, error httpActionErr, int index)
           returns (error) {
    failoverActionErr.httpActionErr[index] = httpActionErr;
    string lastErrorMsg = httpActionErr.message;
    failoverActionErr.message = "All the failover endpoints failed. Last error was " + lastErrorMsg;
    error actionError = failoverActionErr;
    return actionError;
}

// If leaf endpoint returns a response with status code configured to retry in the failover connector, failover error
// will be generated with last response status code and generic failover response.
function populateFailoverErrorHttpStatusCodes (Response inResponse, FailoverActionError failoverActionErr, int index) {
    string failoverMessage = "Endpoint " + index + " returned response is: " + inResponse.statusCode + " " + inResponse.reasonPhrase;
    error httpActionErr = {message:failoverMessage};
    failoverActionErr.httpActionErr[index] = httpActionErr;
}

// If leaf endpoint returns a response with status code configured to retry in the failover connector, generic
// failover error and HTTP connector error will be generated.
function populateErrorsFromLastResponse (Response inResponse, FailoverActionError failoverActionErr, int index)
                                                                            returns (error) {
    string failoverMessage = "Last endpoint returned response: " + inResponse.statusCode + " " + inResponse.reasonPhrase;
    error lastHttpConnectorErr = {message:failoverMessage};
    failoverActionErr.httpActionErr[index] = lastHttpConnectorErr;
    failoverActionErr.statusCode = INTERNAL_SERVER_ERROR_500;
    failoverActionErr.message = "All the failover endpoints failed. Last endpoint returned response is: "
                                        + inResponse.statusCode + " " + inResponse.reasonPhrase;
    error actionError = failoverActionErr;
    return actionError;
}

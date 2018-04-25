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

documentation {
    Provides a set of configurations for controlling the failover behaviour of the endpoint.

    F{{failoverCodes}} Array of HTTP response status codes for which the failover mechanism triggers
    F{{interval}} Failover delay interval in milliseconds
}
public type FailoverConfig {
    int[] failoverCodes,
    int interval,
};

documentation {
    `FailoverConnectorError` record represents an error occurred during an attempt to failover.

    F{{message}} An explanation on what went wrong
    F{{cause}} The cause of the `FailoverConnectorError`, if available
    F{{statusCode}} HTTP status code to be sent to the caller
    F{{httpConnectorErr}} Errors which occurred at each endpoint during the failover
}
public type FailoverConnectorError {
    string message,
    error? cause,
    int statusCode,
    error[] httpConnectorErr,
};

// TODO: This can be made package private
// Represents inferred failover configurations passed to Failover connector.
public type FailoverInferredConfig {
    CallerActions[] failoverClientsArray,
    boolean[] failoverCodesIndex,
    int failoverInterval,
};

documentation {
    Failover caller actions which provides failover capabilities to an HTTP client endpoint.

    F{{serviceUri}} The URL of the remote HTTP endpoint
    F{{config}} The configurations of the client endpoint associated with this `Failover` instance
    F{{failoverInferredConfig}} Configurations derived from `FailoverConfig`
}
public type Failover object {

    public {
        string serviceUri;
        ClientEndpointConfig config;
        FailoverInferredConfig failoverInferredConfig;
    }

    public new (serviceUri, config, failoverInferredConfig) {}

    documentation {
        The POST action implementation of the Failover Connector.

        P{{path}} Resource path
        P{{request}} An optional HTTP request
        R{{}} The response or an `error` if failed to fulfill the request
    }
    public function post(string path, Request? request = ()) returns Response|error;

    documentation {
        The HEAD action implementation of the Failover Connector.

        P{{path}} Resource path
        P{{request}} An optional HTTP request
        R{{}} The response or an `error` if failed to fulfill the request
    }
    public function head(string path, Request? request = ()) returns Response|error;

    documentation {
        The PATCH action implementation of the Failover Connector.

        P{{path}} Resource path
        P{{request}} An optional HTTP request
        R{{}} The response or an `error` if failed to fulfill the request
    }
    public function patch(string path, Request? request = ()) returns Response|error;

    documentation {
        The PUT action  implementation of the Failover Connector.

        P{{path}} Resource path
        P{{request}} An optional HTTP request
        R{{}} The response or an `error` if failed to fulfill the request
    }
    public function put(string path, Request? request = ()) returns Response|error;

    documentation {
        The OPTIONS action implementation of the Failover Connector.

        P{{path}} Resource path
        P{{request}} An optional HTTP request
        R{{}} The response or an `error` if failed to fulfill the request
    }
    public function options(string path, Request? request = ()) returns Response|error;

    documentation {
        Invokes an HTTP call using the incoming request's HTTP method.

        P{{path}} Resource path
        P{{request}} An HTTP request
        R{{}} The response or an `error` if failed to fulfill the request
    }
    public function forward(string path, Request request) returns Response|error;

    documentation {
        Invokes an HTTP call with the specified HTTP method.

        P{{httpVerb}} HTTP method to be used for the request
        P{{path}} Resource path
        P{{request}} An HTTP request
        R{{}} The response or an `error` if failed to fulfill the request
    }
    public function execute(string httpVerb, string path, Request request) returns Response|error;

    documentation {
        The DELETE action implementation of the Failover Connector.

        P{{path}} Resource path
        P{{request}} An optional HTTP request
        R{{}} The response or an `error` if failed to fulfill the request
    }
    public function delete(string path, Request? request = ()) returns Response|error;

    documentation {
        The GET action implementation of the Failover Connector.

        P{{path}} Resource path
        P{{request}} An optional HTTP request
        R{{}} The response or an `error` if failed to fulfill the request
    }
    public function get(string path, Request? request = ()) returns Response|error;

    documentation {
        Submits an HTTP request to a service with the specified HTTP verb.

        P{{httpVerb}} The HTTP verb value
        P{{path}} The Resource path
        P{{request}} An HTTP request
        R{{}} The Future for further interactions or an `error` if failed to fulfill the request
    }
    public function submit(string httpVerb, string path, Request request) returns HttpFuture|error;

    documentation {
        Retrieves the response for a previously submitted request.

        P{{httpFuture}} The Future which relates to previous async invocation
        R{{}} `error` if failed to fulfill the request
    }
    public function getResponse(HttpFuture httpFuture) returns error;

    documentation {
        Checks whether server push exists for a previously submitted request.

        P{{httpFuture}} The Future which relates to previous async invocation
        R{{}} Returns true if the push promise exists
    }
    public function hasPromise(HttpFuture httpFuture) returns boolean;

    documentation {
        Retrieves the next available push promise for a previously submitted request.

        P{{httpFuture}} The Future which relates to previous async invocation
        R{{}} The HTTP Push Promise message or an `error` if failed to fulfill the request
    }
    public function getNextPromise(HttpFuture httpFuture) returns PushPromise|error;

    documentation {
        Retrieves the promised server push response.

        P{{promise}} The related Push Promise message
        R{{}} The HTTP Push Response message or an `error` if failed to fulfill the request
    }
    public function getPromisedResponse(PushPromise promise) returns Response|error;

    documentation {
        Rejects a push promise.

        P{{promise}} The Push Promise to be rejected
    }
    public function rejectPromise(PushPromise promise);
};

public function Failover::post(string path, Request? request = ()) returns Response|error {
    Request req = request ?: new;
    return performFailoverAction(path, req, HTTP_POST, self.failoverInferredConfig);
}

public function Failover::head(string path, Request? request = ()) returns Response|error {
    Request req = request ?: new;
    return performFailoverAction(path, req, HTTP_HEAD, self.failoverInferredConfig);
}

public function Failover::patch(string path, Request? request = ()) returns Response|error {
    Request req = request ?: new;
    return performFailoverAction(path, req, HTTP_PATCH, self.failoverInferredConfig);
}

public function Failover::put(string path, Request? request = ()) returns Response|error {
    Request req = request ?: new;
    return performFailoverAction(path, req, HTTP_PUT, self.failoverInferredConfig);
}

public function Failover::options(string path, Request? request = ()) returns Response|error {
    Request req = request ?: new;
    return performFailoverAction(path, req, HTTP_OPTIONS, self.failoverInferredConfig);
}

public function Failover::forward(string path, Request request) returns Response|error {
    return performFailoverAction(path, request, HTTP_FORWARD, self.failoverInferredConfig);
}

public function Failover::execute(string httpVerb, string path, Request request) returns Response|error {
    return performExecuteAction(path, request, httpVerb, self.failoverInferredConfig);
}

public function Failover::delete(string path, Request? request = ()) returns Response|error {
    Request req = request ?: new;
    return performFailoverAction(path, req, HTTP_DELETE, self.failoverInferredConfig);
}

public function Failover::get(string path, Request? request = ()) returns Response|error {
    Request req = request ?: new;
    return performFailoverAction(path, req, HTTP_GET, self.failoverInferredConfig);
}

public function Failover::submit(string httpVerb, string path, Request request) returns HttpFuture|error {
    error err = {message:"Unsupported action for Failover client."};
    return err;
}

public function Failover::getResponse(HttpFuture httpFuture) returns (error) {
    error err = {message:"Unsupported action for Failover client."};
    return err;
}

public function Failover::hasPromise(HttpFuture httpFuture) returns (boolean) {
    return false;
}

public function Failover::getNextPromise(HttpFuture httpFuture) returns PushPromise|error {
    error err = {message:"Unsupported action for Failover client."};
    return error;
}

public function Failover::getPromisedResponse(PushPromise promise) returns Response|error {
    error err = {message:"Unsupported action for Failover client."};
    return error;
}

public function Failover::rejectPromise(PushPromise promise) {
}

// Performs execute action of the Failover connector. extract the corresponding http integer value representation
// of the http verb and invokes the perform action method.
function performExecuteAction (string path, Request request, string httpVerb,
                               FailoverInferredConfig failoverInferredConfig) returns Response|error {
    HttpOperation connectorAction = extractHttpOperation(httpVerb);
    return performFailoverAction(path, request, connectorAction, failoverInferredConfig);
}

// Handles all the actions exposed through the Failover connector.
function performFailoverAction (string path, Request request, HttpOperation requestAction,
                                FailoverInferredConfig failoverInferredConfig) returns Response|error {
    boolean[] failoverCodeIndex = failoverInferredConfig.failoverCodesIndex;
    int noOfEndpoints = lengthof (failoverInferredConfig.failoverClientsArray);
    int currentIndex = 0;
    int initialIndex = 0;
    int startIndex = -1;
    int failoverInterval = failoverInferredConfig.failoverInterval;

    //TODO: workaround to initialize a type inside a function. Change this once fix is aailable.
    FailoverConnectorError failoverConnectorError = {};
    CallerActions[] failoverClients = failoverInferredConfig.failoverClientsArray;
    CallerActions failoverClient = failoverClients[currentIndex];
    Response inResponse = new;
    Request failoverRequest = request;
    failoverConnectorError.httpConnectorErr = [];

    // When performing passthrough scenarios using Failover connector, message needs to be built before trying out the
    // failover endpoints to keep the request message to failover the messages.
    var binaryPayload = failoverRequest.getBinaryPayload();

    mime:Entity requestEntity = new;
    var mimeEntity = failoverRequest.getEntity();
    match mimeEntity {
        mime:Entity entity => requestEntity = entity;
        error err=> io:println(err.message);
    }
    while (startIndex != currentIndex) {
        startIndex = initialIndex;
        currentIndex = currentIndex + 1;
        var invokedEndpoint = invokeEndpoint(path, failoverRequest, requestAction, failoverClient);
        match invokedEndpoint {
            Response response => {
                inResponse = response;
                int httpStatusCode = response.statusCode;
                if (failoverCodeIndex[httpStatusCode] == true) {
                    if (noOfEndpoints > currentIndex) {
                        Request newOutRequest = new;
                        populateRequestFields(failoverRequest, newOutRequest);
                        newOutRequest.setEntity(requestEntity);
                        failoverRequest = newOutRequest;
                        runtime:sleep(failoverInterval);
                        failoverClient = failoverClients[currentIndex];
                        populateFailoverErrorHttpStatusCodes(inResponse, failoverConnectorError, currentIndex - 1);
                    } else {
                        return populateErrorsFromLastResponse(inResponse, failoverConnectorError, currentIndex - 1);
                    }
                } else {
                    currentIndex = noOfEndpoints;
                    break;
                }
            }
            error httpConnectorErr => {
                Request newOutRequest = new;
                populateRequestFields(failoverRequest, newOutRequest);
                newOutRequest.setEntity(requestEntity);
                failoverRequest = newOutRequest;
                if (noOfEndpoints > currentIndex) {
                    runtime:sleep(failoverInterval);
                    failoverConnectorError.httpConnectorErr[currentIndex - 1] = httpConnectorErr;
                    failoverClient = failoverClients[currentIndex];
                } else {
                    return populateGenericFailoverConnectorError(failoverConnectorError, httpConnectorErr, currentIndex - 1);
                }
            }

        }
    }
    return inResponse;
}

// Populates generic error specific to Failover connector by including all the errors returned from endpoints.
function populateGenericFailoverConnectorError (FailoverConnectorError failoverConnectorError, error httpConnectorErr, int index)
           returns (error) {
    failoverConnectorError.httpConnectorErr[index] = httpConnectorErr;
    string lastErrorMsg = httpConnectorErr.message;
    failoverConnectorError.message = "All the failover endpoints failed. Last error was " + lastErrorMsg;
    error connectorError = failoverConnectorError;
    return connectorError;                                                                                                                                                                                                                                                                                                
}

// If leaf endpoint returns a response with status code configured to retry in the failover connector, failover error
// will be generated with last response status code and generic failover response.
function populateFailoverErrorHttpStatusCodes (Response inResponse, FailoverConnectorError failoverConnectorError, int index) {
    string failoverMessage = "Endpoint " + index + " returned response is: " + inResponse.statusCode + " " + inResponse.reasonPhrase;
    error httpConnectorErr = {message:failoverMessage};
    failoverConnectorError.httpConnectorErr[index] = httpConnectorErr;
}

// If leaf endpoint returns a response with status code configured to retry in the failover connector, generic
// failover error and HTTP connector error will be generated.
function populateErrorsFromLastResponse (Response inResponse, FailoverConnectorError failoverConnectorError, int index)
                                                                            returns (error) {
    string failoverMessage = "Last endpoint returned response: " + inResponse.statusCode + " " + inResponse.reasonPhrase;
    error lastHttpConnectorErr = {message:failoverMessage};
    failoverConnectorError.httpConnectorErr[index] = lastHttpConnectorErr;
    failoverConnectorError.statusCode = INTERNAL_SERVER_ERROR_500;
    failoverConnectorError.message = "All the failover endpoints failed. Last endpoint returned response is: "
                                        + inResponse.statusCode + " " + inResponse.reasonPhrase;
    error connectorError = failoverConnectorError;
    return connectorError;
}

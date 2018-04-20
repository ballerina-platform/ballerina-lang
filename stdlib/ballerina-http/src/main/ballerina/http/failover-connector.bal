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
package ballerina.http;

import ballerina/mime;
import ballerina/runtime;
import ballerina/io;

@Description {value:"Represents Failover connector retry configuration."}
@Field {value:"failoverCodes: Array of http response status codes which required failover the requests."}
@Field {value:"interval: Failover delay interval in millisecond."}
public type FailoverConfig {
    int[] failoverCodes,
    int interval,
};

@Description {value:"Represents an error occurred in an function of the Failover connector."}
@Field {value:"message: An error message explaining about the error."}
@Field {value:"cause: The error that caused HttpConnectorError to get thrown."}
@Field {value:"stackTrace: Represents the invocation stack when FailoverConnectorError is thrown."}
@Field {value:"statusCode: HTTP status code of the FailoverConnectorError."}
@Field {value:"httpConnectorError: Array of HttpConnectorError error occurred at each endpoint."}
public type FailoverConnectorError {
    string message,
    error? cause,
    int statusCode,
    HttpConnectorError[] httpConnectorError,
};

// Represents inferred failover configurations passed to Failover connector.
public type FailoverInferredConfig {
    HttpClient[] failoverClientsArray,
    boolean[] failoverCodesIndex,
    int failoverInterval,
};

@Description {value:"Failover client implementation to be used with the HTTP client connector to support failover."}
@Field {value:"serviceUri: Service path."}
@Field {value:"config: Represents options to be used for HTTP client invocation."}
@Field {value:"failoverInferredConfig: Represents inferred failover configurations passed to Failover connector."}
public type Failover object {
    public {
        string serviceUri;
        ClientEndpointConfig config;
        FailoverInferredConfig failoverInferredConfig;
    }

    public new (serviceUri, config, failoverInferredConfig) {}

    @Description {value:"The POST function implementation of the Failover Connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the function invocation, if any"}
    public function post(string path, Request req) returns (Response | HttpConnectorError);

    @Description {value:"The HEAD function implementation of the Failover Connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the function invocation, if any"}
    public function head(string path, Request? req = ()) returns (Response | HttpConnectorError);

    @Description {value:"The PATCH function implementation of the Failover Connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the functioninvocation, if any"}
    public function patch(string path, Request req) returns (Response | HttpConnectorError);

    @Description {value:"The PUT function  implementation of the Failover Connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the function invocation, if any"}
    public function put(string path, Request req) returns (Response|HttpConnectorError);

    @Description {value:"The OPTIONS function implementation of the Failover Connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the function invocation, if any"}
    public function options(string path, Request req) returns (Response | HttpConnectorError);

    @Description {value:"The FORWARD function implementation of the Failover Connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the function invocation, if any"}
    public function forward(string path, Request req) returns (Response | HttpConnectorError);

    @Description {value:"The EXECUTE function implementation of the Failover Connector. The Execute function can be used to invoke an HTTP call with the given HTTP verb."}
    @Param {value:"httpVerb: HTTP verb to be used for the request"}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the function invocation, if any"}
    public function execute(string httpVerb, string path, Request req) returns (Response | HttpConnectorError);

    @Description {value:"The DELETE function implementation of the Failover Connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the function invocation, if any"}
    public function delete(string path, Request req) returns (Response | HttpConnectorError);

    @Description {value:"The GET function implementation of the Failover Connector."}
    @Param {value:"path: Resource path"}
    @Param {value:"request: A Request struct"}
    @Return {value:"The Response struct"}
    @Return {value:"Error occurred during the function invocation, if any"}
    public function get(string path, Request? req = ()) returns (Response | HttpConnectorError);

    @Description { value:"The submit implementation of the Failover Connector."}
    @Param { value:"httpVerb: The HTTP verb value" }
    @Param { value:"path: The Resource path " }
    @Param { value:"req: An HTTP outbound request message" }
    @Return { value:"The Future for further interactions" }
    @Return { value:"The Error occured during HTTP client invocation" }
    public function submit(string httpVerb, string path, Request req) returns (HttpFuture | HttpConnectorError);

    @Description { value:"The getResponse implementation of the Failover Connector."}
    @Param { value:"httpFuture: The Future which relates to previous async invocation" }
    @Return { value:"The HTTP response message" }
    @Return { value:"The Error occured during HTTP client invocation" }
    public function getResponse(HttpFuture httpFuture) returns (HttpConnectorError);

    @Description { value:"The hasPromise implementation of the Failover Connector."}
    @Param { value:"httpFuture: The Future which relates to previous async invocation" }
    @Return { value:"Whether push promise exists" }
    public function hasPromise(HttpFuture httpFuture) returns (boolean);

    @Description { value:"The getNextPromise implementation of the Failover Connector."}
    @Param { value:"httpFuture: The Future which relates to previous async invocation" }
    @Return { value:"The HTTP Push Promise message" }
    @Return { value:"The Error occured during HTTP client invocation" }
    public function getNextPromise(HttpFuture httpFuture) returns (PushPromise | HttpConnectorError);

    @Description { value:"The getPromisedResponse implementation of the Failover Connector."}
    @Param { value:"promise: The related Push Promise message" }
    @Return { value:"HTTP The Push Response message" }
    @Return { value:"The Error occured during HTTP client invocation" }
    public function getPromisedResponse(PushPromise promise) returns (Response | HttpConnectorError);

    @Description { value:"The rejectPromise implementation of the Failover Connector."}
    @Param { value:"promise: The Push Promise need to be rejected" }
    public function rejectPromise(PushPromise promise);
};


@Description {value:"The POST function implementation of the Failover Connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the function invocation, if any"}
public function Failover::post(string path, Request req) returns (Response | HttpConnectorError) {
    return performFailoverAction(path, req, HTTP_POST, self.failoverInferredConfig);
}

@Description {value:"The HEAD function implementation of the Failover Connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the function invocation, if any"}
public function Failover::head(string path, Request? req = ()) returns (Response | HttpConnectorError) {
    Request request = req ?: new;
    return performFailoverAction(path, request, HTTP_HEAD, self.failoverInferredConfig);
}

@Description {value:"The PATCH function implementation of the Failover Connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the functioninvocation, if any"}
public function Failover::patch(string path, Request req) returns (Response | HttpConnectorError) {
    return performFailoverAction(path, req, HTTP_PATCH, self.failoverInferredConfig);
}

@Description {value:"The PUT function  implementation of the Failover Connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the function invocation, if any"}
public function Failover::put(string path, Request req) returns (Response|HttpConnectorError) {
    return performFailoverAction(path, req, HTTP_PUT, self.failoverInferredConfig);
}

@Description {value:"The OPTIONS function implementation of the Failover Connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the function invocation, if any"}
public function Failover::options(string path, Request req) returns (Response | HttpConnectorError) {
    return performFailoverAction(path, req, HTTP_OPTIONS, self.failoverInferredConfig);
}

@Description {value:"The FORWARD function implementation of the Failover Connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the function invocation, if any"}
public function Failover::forward(string path, Request req) returns (Response | HttpConnectorError) {
    return performFailoverAction(path, req, HTTP_FORWARD, self.failoverInferredConfig);
}

@Description {value:"The EXECUTE function implementation of the Failover Connector. The Execute function can be used to invoke an HTTP call with the given HTTP verb."}
@Param {value:"httpVerb: HTTP verb to be used for the request"}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the function invocation, if any"}
public function Failover::execute(string httpVerb, string path, Request req) returns (Response | HttpConnectorError) {
    return performExecuteAction(path, req, httpVerb, self.failoverInferredConfig);
}

@Description {value:"The DELETE function implementation of the Failover Connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the function invocation, if any"}
public function Failover::delete(string path, Request req) returns (Response | HttpConnectorError) {
    return performFailoverAction(path, req, HTTP_DELETE, self.failoverInferredConfig);
}

@Description {value:"The GET function implementation of the Failover Connector."}
@Param {value:"path: Resource path"}
@Param {value:"request: A Request struct"}
@Return {value:"The Response struct"}
@Return {value:"Error occurred during the function invocation, if any"}
public function Failover::get(string path, Request? req = ()) returns (Response | HttpConnectorError) {
    Request request = req ?: new;
    return performFailoverAction(path, request, HTTP_GET, self.failoverInferredConfig);
}

@Description { value:"The submit implementation of the Failover Connector."}
@Param { value:"httpVerb: The HTTP verb value" }
@Param { value:"path: The Resource path " }
@Param { value:"req: An HTTP outbound request message" }
@Return { value:"The Future for further interactions" }
@Return { value:"The Error occured during HTTP client invocation" }
public function Failover::submit(string httpVerb, string path, Request req) returns (HttpFuture | HttpConnectorError) {
    HttpConnectorError httpConnectorError = {message:"Unsupported action for Failover client."};
    return httpConnectorError;
}

@Description { value:"The getResponse implementation of the Failover Connector."}
@Param { value:"httpFuture: The Future which relates to previous async invocation" }
@Return { value:"The HTTP response message" }
@Return { value:"The Error occured during HTTP client invocation" }
public function Failover::getResponse(HttpFuture httpFuture) returns (HttpConnectorError) {
    HttpConnectorError httpConnectorError = {message:"Unsupported action for Failover client."};
    return httpConnectorError;
}

@Description { value:"The hasPromise implementation of the Failover Connector."}
@Param { value:"httpFuture: The Future which relates to previous async invocation" }
@Return { value:"Whether push promise exists" }
public function Failover::hasPromise(HttpFuture httpFuture) returns (boolean) {
    return false;
}

@Description { value:"The getNextPromise implementation of the Failover Connector."}
@Param { value:"httpFuture: The Future which relates to previous async invocation" }
@Return { value:"The HTTP Push Promise message" }
@Return { value:"The Error occured during HTTP client invocation" }
public function Failover::getNextPromise(HttpFuture httpFuture) returns (PushPromise | HttpConnectorError) {
    HttpConnectorError httpConnectorError = {message:"Unsupported action for Failover client."};
    return httpConnectorError;
}

@Description { value:"The getPromisedResponse implementation of the Failover Connector."}
@Param { value:"promise: The related Push Promise message" }
@Return { value:"HTTP The Push Response message" }
@Return { value:"The Error occured during HTTP client invocation" }
public function Failover::getPromisedResponse(PushPromise promise) returns (Response | HttpConnectorError) {
    HttpConnectorError httpConnectorError = {message:"Unsupported action for Failover client."};
    return httpConnectorError;
}

@Description { value:"The rejectPromise implementation of the Failover Connector."}
@Param { value:"promise: The Push Promise need to be rejected" }
public function Failover::rejectPromise(PushPromise promise) {
}

// Performs execute action of the Failover connector. extract the corresponding http integer value representation
// of the http verb and invokes the perform action method.
function performExecuteAction (string path, Request request, string httpVerb,
                               FailoverInferredConfig failoverInferredConfig) returns (Response | HttpConnectorError) {
    HttpOperation connectorAction = extractHttpOperation(httpVerb);
    return performFailoverAction(path, request, connectorAction, failoverInferredConfig);
}

// Handles all the actions exposed through the Failover connector.
function performFailoverAction (string path, Request request, HttpOperation requestAction,
                                FailoverInferredConfig failoverInferredConfig) returns (Response|HttpConnectorError) {
    boolean[] failoverCodeIndex = failoverInferredConfig.failoverCodesIndex;
    int noOfEndpoints = lengthof (failoverInferredConfig.failoverClientsArray);
    int currentIndex = 0;
    int initialIndex = 0;
    int startIndex = -1;
    int failoverInterval = failoverInferredConfig.failoverInterval;

    //TODO: workaround to initialize a type inside a function. Change this once fix is aailable.
    FailoverConnectorError failoverConnectorError = {statusCode:500};
    HttpClient[] failoverClients = failoverInferredConfig.failoverClientsArray;
    HttpClient failoverClient = failoverClients[currentIndex];
    Response inResponse = new;
    Request failoverRequest = request;
    failoverConnectorError.httpConnectorError = [];

    // When performing passthrough scenarios using Failover connector, message needs to be built before trying out the
    // failover endpoints to keep the request message to failover the messages.
    var binaryPayload = failoverRequest.getBinaryPayload();

    mime:Entity requestEntity = new;
    var mimeEntity = failoverRequest.getEntity();
    match mimeEntity {
        mime:Entity entity => requestEntity = entity;
        mime:EntityError => io:println("mimeEntity null");
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
                        newOutRequest.setEntity(requestEntity);
                        runtime:sleepCurrentWorker(failoverInterval);
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
            HttpConnectorError httpConnectorError => {
                Request newOutRequest = new;
                newOutRequest.setEntity(requestEntity);
                failoverRequest = newOutRequest;
                if (noOfEndpoints > currentIndex) {
                    runtime:sleepCurrentWorker(failoverInterval);
                    failoverConnectorError.httpConnectorError[currentIndex - 1] = httpConnectorError;
                    failoverClient = failoverClients[currentIndex];
                } else {
                    return populateGenericFailoverConnectorError(failoverConnectorError, httpConnectorError, currentIndex - 1);
                }
            }

        }
    }
    return inResponse;
}

// Populates generic error specific to Failover connector by including all the errors returned from endpoints.
function populateGenericFailoverConnectorError (FailoverConnectorError failoverConnectorError, HttpConnectorError httpConnectorError, int index)
           returns (HttpConnectorError) {
    httpConnectorError.statusCode = INTERNAL_SERVER_ERROR_500;
    failoverConnectorError.httpConnectorError[index] = httpConnectorError;
    string lastErrorMsg = httpConnectorError.message;
    failoverConnectorError.message = "All the failover endpoints failed. Last error was " + lastErrorMsg;
    HttpConnectorError connectorError = failoverConnectorError;
    return connectorError;                                                                                                                                                                                                                                                                                                
}

// If leaf endpoint returns a response with status code configured to retry in the failover connector, failover error
// will be generated with last response status code and generic failover response.
function populateFailoverErrorHttpStatusCodes (Response inResponse, FailoverConnectorError failoverConnectorError, int index) {
    string failoverMessage = "Endpoint " + index + " returned response is: " + inResponse.statusCode + " " + inResponse.reasonPhrase;
    HttpConnectorError httpConnectorError = {message:failoverMessage};
    failoverConnectorError.httpConnectorError[index] = httpConnectorError;
}

// If leaf endpoint returns a response with status code configured to retry in the failover connector, generic
// failover error and HttpConnectorError error will be generated.
function populateErrorsFromLastResponse (Response inResponse, FailoverConnectorError failoverConnectorError, int index)
                                                                            returns (HttpConnectorError) {
    string failoverMessage = "Last endpoint returned response: " + inResponse.statusCode + " " + inResponse.reasonPhrase;
    HttpConnectorError lastHttpConnectorError = {message:failoverMessage, statusCode:inResponse.statusCode};
    failoverConnectorError.httpConnectorError[index] = lastHttpConnectorError;
    failoverConnectorError.statusCode = INTERNAL_SERVER_ERROR_500;
    failoverConnectorError.message = "All the failover endpoints failed. Last endpoint returned response is: "
                                        + inResponse.statusCode + " " + inResponse.reasonPhrase;
    HttpConnectorError connectorError = failoverConnectorError;
    return connectorError;
}

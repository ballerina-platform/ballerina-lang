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

@Description { value:"An HTTP client for interacting with an HTTP server."}
public type HttpClient object {
    //These properties are populated from the init call to the client connector as these were needed later stage
    //for retry and other few places.
    public {
        string serviceUri;
        ClientEndpointConfig config;
    }

    @Description {value:"The POST action implementation of the HTTP Connector."}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    public native function post(@sensitive string path, Request req) returns (Response | HttpConnectorError);

    @Description {value:"The HEAD action implementation of the HTTP Connector."}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    public native function head(@sensitive string path, Request req) returns (Response | HttpConnectorError);

    @Description {value:"The PUT action implementation of the HTTP Connector."}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    public native function put(@sensitive string path, Request req) returns (Response | HttpConnectorError);

    @Description {value:"Invokes an HTTP call with the specified HTTP verb."}
    @Param {value:"httpVerb: HTTP verb value"}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    public native function execute(@sensitive string httpVerb, @sensitive string path, Request req) returns (Response | HttpConnectorError);

    @Description {value:"The PATCH action implementation of the HTTP Connector."}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    public native function patch(@sensitive string path, Request req) returns (Response | HttpConnectorError);

    @Description {value:"The DELETE action implementation of the HTTP connector"}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    public native function delete(@sensitive string path, Request req) returns (Response | HttpConnectorError);

    @Description {value:"GET action implementation of the HTTP Connector"}
    @Param {value:"path: Request path"}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    public native function get(@sensitive string path, Request req) returns (Response | HttpConnectorError);

    @Description {value:"OPTIONS action implementation of the HTTP Connector"}
    @Param {value:"path: Request path"}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    public native function options(@sensitive string path, Request req) returns (Response | HttpConnectorError);

    @Description {value:"Forward action can be used to invoke an HTTP call with inbound request's HTTP verb"}
    @Param {value:"path: Request path"}
    @Param {value:"req: An HTTP inbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    public native function forward(@sensitive string path, Request req) returns (Response | HttpConnectorError);

    @Description { value:"Submits an HTTP request to a service with the specified HTTP verb."}
    @Param { value:"httpVerb: The HTTP verb value" }
    @Param { value:"path: The Resource path " }
    @Param { value:"req: An HTTP outbound request message" }
    @Return { value:"The Future for further interactions" }
    @Return { value:"The Error occured during HTTP client invocation" }
    public native function submit(@sensitive string httpVerb, string path, Request req) returns (HttpFuture | HttpConnectorError);

    @Description { value:"Retrieves response for a previously submitted request."}
    @Param { value:"httpFuture: The Future which relates to previous async invocation" }
    @Return { value:"The HTTP response message" }
    @Return { value:"The Error occured during HTTP client invocation" }
    public native function getResponse(HttpFuture httpFuture) returns (Response | HttpConnectorError);

    @Description { value:"Checks whether server push exists for a previously submitted request."}
    @Param { value:"httpFuture: The Future which relates to previous async invocation" }
    @Return { value:"Whether push promise exists" }
    public native function hasPromise(HttpFuture httpFuture) returns (boolean);

    @Description { value:"Retrieves the next available push promise for a previously submitted request."}
    @Param { value:"httpFuture: The Future which relates to previous async invocation" }
    @Return { value:"The HTTP Push Promise message" }
    @Return { value:"The Error occured during HTTP client invocation" }
    public native function getNextPromise(HttpFuture httpFuture) returns (PushPromise | HttpConnectorError);

    @Description { value:"Retrieves the promised server push response."}
    @Param { value:"promise: The related Push Promise message" }
    @Return { value:"HTTP The Push Response message" }
    @Return { value:"The Error occured during HTTP client invocation" }
    public native function getPromisedResponse(PushPromise promise) returns (Response | HttpConnectorError);

    @Description { value:"Rejects a push promise."}
    @Param { value:"promise: The Push Promise need to be rejected" }
    public native function rejectPromise(PushPromise promise);
};

@Description { value:"HttpConnectorError record represents an error occured during the HTTP client invocation" }
@Field {value:"message:  An error message explaining the error"}
@Field {value:"cause: The error(s) which caused the HttpConnectorError"}
@Field {value:"statusCode: HTTP status code"}
public type HttpConnectorError {
    string message,
    error? cause,
    int statusCode,
};

@Description {value:"HttpTimeoutError record represents a timeout error occurred during service invocation."}
@Field {value:"message: An error message explaining the error"}
@Field {value:"cause: The error(s) which caused the HttpTimeoutError"}
@Field {value:"statusCode: HTTP status code."}
public type HttpTimeoutError {
    string message,
    error? cause,
    int statusCode,
};

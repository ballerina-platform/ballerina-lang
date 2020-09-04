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

import ballerina/java;

# Provides the HTTP actions for interacting with an HTTP server. Apart from the standard HTTP methods,
# `HttpClient.forward()` and `HttpClient.execute()` functions are provided. More complex and specific endpoint types
# can be created by wrapping this generic HTTP actions implementation.
#
# + url - The URL of the remote HTTP endpoint
# + config - The configurations associated with the HttpClient
public client class HttpClient {

    public ClientConfiguration config = {};
    public string url;

    # Gets invoked to initialize the native `client`. During initialization, the configurations are provided through the
    # `config`. The `HttpClient` lies inside every type of client in the chain holding the native client connector.
    #
    # + url - URL of the target service
    # + config - The configurations to be used when initializing the `client`
    public function init(string url, ClientConfiguration? config = ()) {
        self.config = config ?: {};
        self.url = url;
        createSimpleHttpClient(self, globalHttpClientConnPool);
    }

    # The `HttpClient.post()` function can be used to send HTTP POST requests to HTTP endpoints.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `http:ClientError` if failed to establish communication with the upstream server
    public remote function post(@untainted string path, RequestMessage message) returns Response|ClientError {
        return externExecuteClientAction(self, path, <Request>message, HTTP_POST);
    }

    # The `HttpClient.head()` function can be used to send HTTP HEAD requests to HTTP endpoints.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - An `http:Response` for the request or else an `http:ClientError` if failed to establish communication with the upstream server
    public remote function head(@untainted string path, RequestMessage message = ()) returns Response|ClientError {
        return externExecuteClientAction(self, path, <Request>message, HTTP_HEAD);
    }

    # The `HttpClient.put()` function can be used to send HTTP PUT requests to HTTP endpoints.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - An `http:Response` for the request or else an `http:ClientError` if failed to establish communication with the upstream server
    public remote function put(@untainted string path, RequestMessage message) returns Response|ClientError {
        return externExecuteClientAction(self, path, <Request>message, HTTP_PUT);
    }

    # Invokes an HTTP call with the specified HTTP verb.
    #
    # + httpVerb - HTTP verb value
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - An `http:Response` for the request or else an `http:ClientError` if failed to establish communication with the upstream server
    public remote function execute(@untainted string httpVerb, @untainted string path, RequestMessage message) returns Response|ClientError {
        return externExecute(self, httpVerb, path, <Request>message);
    }

    # The `HttpClient.patch()` function can be used to send HTTP PATCH requests to HTTP endpoints.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or else an `http:ClientError` if failed to establish communication with the upstream server
    public remote function patch(@untainted string path, RequestMessage message) returns Response|ClientError {
        return externExecuteClientAction(self, path, <Request>message, HTTP_PATCH);
    }

    # The `HttpClient.delete()` function can be used to send HTTP DELETE requests to HTTP endpoints.
    #
    # + path - Resource path
    # + message - An optional HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - An `http:Response` for the request or else an `http:ClientError` if failed to establish communication with the upstream server
    public remote function delete(@untainted string path, RequestMessage message = ()) returns Response|ClientError {
        return externExecuteClientAction(self, path, <Request>message, HTTP_DELETE);
    }

    # The `HttpClient.get()` function can be used to send HTTP GET requests to HTTP endpoints.
    #
    # + path - Request path
    # + message - An optional HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - An `http:Response` for the request or else an `http:ClientError` if failed to establish communication with the upstream server
    public remote function get(@untainted string path, RequestMessage message = ()) returns Response|ClientError {
        return externExecuteClientAction(self, path, <Request>message, HTTP_GET);
    }

    # The `HttpClient.options()` function can be used to send HTTP OPTIONS requests to HTTP endpoints.
    #
    # + path - Request path
    # + message - An optional HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or else an `http:ClientError` if failed to establish communication with the upstream server
    public remote function options(@untainted string path, RequestMessage message = ()) returns Response|ClientError {
        return externExecuteClientAction(self, path, <Request>message, HTTP_OPTIONS);
    }

    # The `HttpClient.forward()` function can be used to invoke an HTTP call with inbound request's HTTP verb
    #
    # + path - Request path
    # + request - An HTTP inbound request message
    # + return - An `http:Response` for the request or else an `http:ClientError` if failed to establish communication with the upstream server
    public remote function forward(@untainted string path, Request request) returns Response|ClientError {
        return externForward(self, path, request);
    }

    # Submits an HTTP request to a service with the specified HTTP verb.
    # The `HttpClient->submit()` function does not give out an `http:Response` as the result.
    # Rather, it returns an `http:HttpFuture` which can be used to do further interactions with the endpoint.
    #
    # + httpVerb - The HTTP verb value
    # + path - The resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - An `http:HttpFuture` that represents an asynchronous service invocation, or else an `http:ClientError` if the submission fails
    public remote function submit(@untainted string httpVerb, string path, RequestMessage message) returns HttpFuture|ClientError {
        return externSubmit(self, httpVerb, path, <Request>message);
    }

    # Retrieves the `http:Response` for a previously-submitted request.
    #
    # + httpFuture - The `http:HttpFuture` related to a previous asynchronous invocation
    # + return - An `http:Response` message or else an `http:ClientError` if the invocation fails
    public remote function getResponse(HttpFuture httpFuture) returns Response|ClientError {
        return externGetResponse(self, httpFuture);
    }

    # Checks whether an `http:PushPromise` exists for a previously-submitted request.
    #
    # + httpFuture - The `http:HttpFuture` related to a previous asynchronous invocation
    # + return - A `boolean`, which represents whether an `http:PushPromise` exists
    public remote function hasPromise(HttpFuture httpFuture) returns boolean {
        return externHasPromise(self, httpFuture);
    }

    # Retrieves the next available `http:PushPromise` for a previously-submitted request.
    #
    # + httpFuture - The `http:HttpFuture` related to a previous asynchronous invocation
    # + return - An `http:PushPromise` message or else an `http:ClientError` if the invocation fails
    public remote function getNextPromise(HttpFuture httpFuture) returns PushPromise|ClientError {
        return externGetNextPromise(self, httpFuture);
    }

    # Retrieves the promised server push `http:Response` message.
    #
    # + promise - The related `http:PushPromise`
    # + return - A promised `http:Response` message or else an `http:ClientError` if the invocation fails
    public remote function getPromisedResponse(PushPromise promise) returns Response|ClientError {
        return externGetPromisedResponse(self, promise);
    }

    # Rejects an `http:PushPromise`. When an `http:PushPromise` is rejected, there is no chance of fetching a promised
    # response using the rejected promise.
    #
    # + promise - The Push Promise to be rejected
    public remote function rejectPromise(PushPromise promise) {
        return externRejectPromise(self, promise);
    }
}

function externGetResponse(HttpClient httpClient, HttpFuture httpFuture) returns Response|ClientError =
@java:Method {
    'class: "org.ballerinalang.net.http.actions.httpclient.GetResponse",
    name: "getResponse"
} external;

function externHasPromise(HttpClient httpClient, HttpFuture httpFuture) returns boolean =
@java:Method {
    'class: "org.ballerinalang.net.http.actions.httpclient.HasPromise",
    name: "hasPromise"
} external;

function externGetNextPromise(HttpClient httpClient, HttpFuture httpFuture) returns PushPromise|ClientError =
@java:Method {
    'class: "org.ballerinalang.net.http.actions.httpclient.GetNextPromise",
    name: "getNextPromise"
} external;

function externGetPromisedResponse(HttpClient httpClient, PushPromise promise) returns Response|ClientError =
@java:Method {
    'class: "org.ballerinalang.net.http.actions.httpclient.GetPromisedResponse",
    name: "getPromisedResponse"
} external;

function externRejectPromise(HttpClient httpClient, PushPromise promise) =
@java:Method {
    'class: "org.ballerinalang.net.http.actions.httpclient.HttpClientAction",
    name: "rejectPromise"
} external;

function externExecute(HttpClient caller, string httpVerb, string path, Request req) returns Response|ClientError =
@java:Method {
    'class: "org.ballerinalang.net.http.actions.httpclient.Execute",
    name: "execute"
} external;

function externSubmit(HttpClient caller, string httpVerb, string path, Request req) returns HttpFuture|ClientError =
@java:Method {
    'class: "org.ballerinalang.net.http.actions.httpclient.Submit",
    name: "submit"
} external;

function externForward(HttpClient caller, string path, Request req) returns Response|ClientError =
@java:Method {
    'class: "org.ballerinalang.net.http.actions.httpclient.Forward",
    name: "forward"
} external;

function externExecuteClientAction(HttpClient caller, string path, Request req, string httpMethod)
                                  returns Response|ClientError =
@java:Method {
    'class: "org.ballerinalang.net.http.actions.httpclient.HttpClientAction",
    name: "executeClientAction"
} external;

# Defines a timeout error occurred during service invocation.
#
# + message - An explanation on what went wrong
# + cause - The error which caused the `HttpTimeoutError`
# + statusCode - HTTP status code
public type HttpTimeoutError record {|
    string message = "";
    error? cause = ();
    int statusCode = 0;
|};

function createClient(string url, ClientConfiguration config) returns HttpClient|ClientError {
    HttpClient simpleClient = new(url, config);
    return simpleClient;
}

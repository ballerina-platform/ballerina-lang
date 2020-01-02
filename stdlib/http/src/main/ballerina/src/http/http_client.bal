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

import ballerinax/java;

# Provides the HTTP actions for interacting with an HTTP server. Apart from the standard HTTP methods,
# `HttpClient.forward()` and `HttpClient.execute()` functions are provided. More complex and specific endpoint types
# can be created by wrapping this generic HTTP actions implementation.
#
# + url - The URL of the remote HTTP endpoint
# + config - The configurations associated with the HttpClient
public type HttpClient client object {

    public ClientConfiguration config = {};
    public string url;

    public function __init(string url, public ClientConfiguration? config = ()) {
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
        return externExecuteClientAction(self, java:fromString(path), <Request>message, java:fromString(HTTP_POST));
    }

    # The `HttpClient.head()` function can be used to send HTTP HEAD requests to HTTP endpoints.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `ClientError` if failed to establish communication with the upstream server
    public remote function head(@untainted string path, public RequestMessage message = ()) returns Response|ClientError {
        return externExecuteClientAction(self, java:fromString(path), <Request>message, java:fromString(HTTP_HEAD));
    }

    # The `HttpClient.put()` function can be used to send HTTP PUT requests to HTTP endpoints.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `ClientError` if failed to establish communication with the upstream server
    public remote function put(@untainted string path, RequestMessage message) returns Response|ClientError {
        return externExecuteClientAction(self, java:fromString(path), <Request>message, java:fromString(HTTP_PUT));
    }

    # Invokes an HTTP call with the specified HTTP verb.
    #
    # + httpVerb - HTTP verb value
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `ClientError` if failed to establish communication with the upstream server
    public remote function execute(@untainted string httpVerb, @untainted string path, RequestMessage message) returns Response|ClientError {
        return externExecute(self, java:fromString(httpVerb), java:fromString(path), <Request>message);
    }

    # The `HttpClient.patch()` function can be used to send HTTP PATCH requests to HTTP endpoints.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `ClientError` if failed to establish communication with the upstream server
    public remote function patch(@untainted string path, RequestMessage message) returns Response|ClientError {
        return externExecuteClientAction(self, java:fromString(path), <Request>message, java:fromString(HTTP_PATCH));
    }

    # The `HttpClient.delete()` function can be used to send HTTP DELETE requests to HTTP endpoints.
    #
    # + path - Resource path
    # + message - An optional HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `ClientError` if failed to establish communication with the upstream server
    public remote function delete(@untainted string path, public RequestMessage message = ()) returns Response|ClientError {
        return externExecuteClientAction(self, java:fromString(path), <Request>message, java:fromString(HTTP_DELETE));
    }

    # The `HttpClient.get()` function can be used to send HTTP GET requests to HTTP endpoints.
    #
    # + path - Request path
    # + message - An optional HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `ClientError` if failed to establish communication with the upstream server
    public remote function get(@untainted string path, public RequestMessage message = ()) returns Response|ClientError {
        return externExecuteClientAction(self, java:fromString(path), <Request>message, java:fromString(HTTP_GET));
    }

    # The `HttpClient.options()` function can be used to send HTTP OPTIONS requests to HTTP endpoints.
    #
    # + path - Request path
    # + message - An optional HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `ClientError` if failed to establish communication with the upstream server
    public remote function options(@untainted string path, public RequestMessage message = ()) returns Response|ClientError {
        return externExecuteClientAction(self, java:fromString(path), <Request>message, java:fromString(HTTP_OPTIONS));
    }

    # The `HttpClient.forward()` function can be used to invoke an HTTP call with inbound request's HTTP verb
    #
    # + path - Request path
    # + request - An HTTP inbound request message
    # + return - The response for the request or an `ClientError` if failed to establish communication with the upstream server
    public remote function forward(@untainted string path, Request request) returns Response|ClientError {
        return externForward(self, java:fromString(path), request);
    }

    # Submits an HTTP request to a service with the specified HTTP verb.
    # The `HttpClient.submit()` function does not give out a `Response` as the result,
    # rather it returns an `HttpFuture` which can be used to do further interactions with the endpoint.
    #
    # + httpVerb - The HTTP verb value
    # + path - The resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - An `HttpFuture` that represents an asynchronous service invocation, or an `ClientError` if the submission fails
    public remote function submit(@untainted string httpVerb, string path, RequestMessage message) returns HttpFuture|ClientError {
        return externSubmit(self, java:fromString(httpVerb), java:fromString(path), <Request>message);
    }

    # Retrieves the `Response` for a previously submitted request.
    #
    # + httpFuture - The `HttpFuture` related to a previous asynchronous invocation
    # + return - An HTTP response message, or an `ClientError` if the invocation fails
    public remote function getResponse(HttpFuture httpFuture) returns Response|ClientError {
        return externGetResponse(self, httpFuture);
    }

    # Checks whether a `PushPromise` exists for a previously submitted request.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - A `boolean` that represents whether a `PushPromise` exists
    public remote function hasPromise(HttpFuture httpFuture) returns boolean {
        return externHasPromise(self, httpFuture);
    }

    # Retrieves the next available `PushPromise` for a previously submitted request.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - An HTTP Push Promise message, or an `ClientError` if the invocation fails
    public remote function getNextPromise(HttpFuture httpFuture) returns PushPromise|ClientError {
        return externGetNextPromise(self, httpFuture);
    }

    # Retrieves the promised server push `Response` message.
    #
    # + promise - The related `PushPromise`
    # + return - A promised HTTP `Response` message, or an `ClientError` if the invocation fails
    public remote function getPromisedResponse(PushPromise promise) returns Response|ClientError {
        return externGetPromisedResponse(self, promise);
    }

    # Rejects a `PushPromise`. When a `PushPromise` is rejected, there is no chance of fetching a promised
    # response using the rejected promise.
    #
    # + promise - The Push Promise to be rejected
    public remote function rejectPromise(PushPromise promise) {
        return externRejectPromise(self, promise);
    }
};

function externGetResponse(HttpClient httpClient, HttpFuture httpFuture) returns Response|ClientError =
@java:Method {
    class: "org.ballerinalang.net.http.actions.httpclient.GetResponse",
    name: "getResponse"
} external;

function externHasPromise(HttpClient httpClient, HttpFuture httpFuture) returns boolean =
@java:Method {
    class: "org.ballerinalang.net.http.actions.httpclient.HasPromise",
    name: "hasPromise"
} external;

function externGetNextPromise(HttpClient httpClient, HttpFuture httpFuture) returns PushPromise|ClientError =
@java:Method {
    class: "org.ballerinalang.net.http.actions.httpclient.GetNextPromise",
    name: "getNextPromise"
} external;

function externGetPromisedResponse(HttpClient httpClient, PushPromise promise) returns Response|ClientError =
@java:Method {
    class: "org.ballerinalang.net.http.actions.httpclient.GetPromisedResponse",
    name: "getPromisedResponse"
} external;

function externRejectPromise(HttpClient httpClient, PushPromise promise) =
@java:Method {
    class: "org.ballerinalang.net.http.actions.httpclient.HttpClientAction",
    name: "rejectPromise"
} external;

function externExecute(HttpClient caller , handle httpVerb, handle path,
                                                        Request req) returns Response|ClientError =
@java:Method {
    class: "org.ballerinalang.net.http.actions.httpclient.Execute",
    name: "execute"
} external;

function externSubmit(HttpClient caller , handle httpVerb, handle path, Request req)
                                                            returns HttpFuture|ClientError =
@java:Method {
    class: "org.ballerinalang.net.http.actions.httpclient.Submit",
    name: "submit"
} external;

function externForward(HttpClient caller , handle path, Request req) returns Response|ClientError =
@java:Method {
    class: "org.ballerinalang.net.http.actions.httpclient.Forward",
    name: "forward"
} external;

function externExecuteClientAction(HttpClient caller , handle path, Request req, handle httpMethod) returns Response|ClientError =
@java:Method {
    class: "org.ballerinalang.net.http.actions.httpclient.HttpClientAction",
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

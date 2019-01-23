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

# Provides the HTTP remote functions for interacting with an HTTP server. Apart from the standard HTTP methods, `forward()`
# and `execute()` functions are provided. More complex and specific endpoint types can be created by wrapping this
# generic HTTP remote functions implementation.
#
# + url - The URL of the remote HTTP endpoint
# + config - HTTP ClientEndpointConfig to be used for HTTP client invocation
# + httpCaller - HTTP client for outbound HTTP requests
# + httpClient - Chain of different HTTP clients which provides the capability for initiating contact with a remote
#                HTTP service in resilient manner.
public type HttpClient client object {

    public string url;
    public ClientEndpointConfig config;
    public HttpCaller httpCaller;
    public Client httpClient;

    public function __init(string url, ClientEndpointConfig config) {
        self.httpCaller = new(url, config);
        self.url = url;
        self.config = config;
        self.httpClient = createSimpleHttpClient(url, self.config, globalHttpClientConnPool);
    }

    # The `post()` function can be used to send HTTP POST requests to HTTP endpoints.
    #
    # + path - Resource path
    # + message - A Request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `error` if failed to establish communication with the upstream server
    public remote function post(string path, RequestMessage message) returns Response|error;

    # The `head()` function can be used to send HTTP HEAD requests to HTTP endpoints.
    #
    # + path - Resource path
    # + message - A Request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `error` if failed to establish communication with the upstream server
    public remote function head(string path, RequestMessage message = ()) returns Response|error;

    # The `put()` function can be used to send HTTP PUT requests to HTTP endpoints.
    #
    # + path - Resource path
    # + message - A Request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `error` if failed to establish communication with the upstream server
    public remote function put(string path, RequestMessage message) returns Response|error;

    # Invokes an HTTP call with the specified HTTP verb.
    #
    # + httpVerb - HTTP verb to be used for the request
    # + path - Resource path
    # + message - A Request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `error` if failed to establish communication with the upstream server
    public remote function execute(string httpVerb, string path, RequestMessage message) returns Response|error;

    # The `patch()` function can be used to send HTTP PATCH requests to HTTP endpoints.
    #
    # + path - Resource path
    # + message - A Request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `error` if failed to establish communication with the upstream server
    public remote function patch(string path, RequestMessage message) returns Response|error;

    # The `delete()` function can be used to send HTTP DELETE requests to HTTP endpoints.
    #
    # + path - Resource path
    # + message - A Request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `error` if failed to establish communication with the upstream server
    public remote function delete(string path, RequestMessage message) returns Response|error;

    # The `get()` function can be used to send HTTP GET requests to HTTP endpoints.
    #
    # + path - Resource path
    # + message - An optional HTTP request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response for the request or an `error` if failed to establish communication with the upstream server
    public remote function get(string path, RequestMessage message = ()) returns Response|error;

    # The `options()` function can be used to send HTTP OPTIONS requests to HTTP endpoints.
    #
    # + path - Resource path
    # + message - An optional HTTP Request or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - The response for the request or an `error` if failed to establish communication with the upstream server
    public remote function options(string path, RequestMessage message = ()) returns Response|error;

    # The `forward()` function can be used to invoke an HTTP call with inbound request's HTTP verb
    #
    # + path - Resource path
    # + request - A Request struct
    # + return - The response for the request or an `error` if failed to establish communication with the upstream server
    public remote function forward(string path, Request request) returns Response|error;

    # Submits an HTTP request to a service with the specified HTTP verb.
    #
    # + httpVerb - The HTTP verb value
    # + path - The resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - An `HttpFuture` that represents an asynchronous service invocation, or an `error` if the submission fails
    public remote function submit(string httpVerb, string path, RequestMessage message) returns HttpFuture|error;

    # Retrieves the `Response` for a previously submitted request.
    #
    # + httpFuture - The `HttpFuture` related to a previous asynchronous invocation
    # + return - An HTTP response message, or an `error` if the invocation fails
    public remote function getResponse(HttpFuture httpFuture) returns Response|error;

    # Retrieves the next available `PushPromise` for a previously submitted request.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - A `boolean` that represents whether a `PushPromise` exists
    public remote function hasPromise(HttpFuture httpFuture) returns (boolean);

    # Retrieves the promised server push `Response` message.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - An HTTP `PushPromise` message, or an `error` if the invocation fails
    public remote function getNextPromise(HttpFuture httpFuture) returns PushPromise|error;

    # Retrieves the promised server push `Response` message.
    #
    # + promise - The related `PushPromise`
    # + return - A promised HTTP `Response` message, or an `error` if the invocation fails
    public remote function getPromisedResponse(PushPromise promise) returns Response|error;

    # Rejects a `PushPromise`. When a `PushPromise` is rejected, there is no chance of fetching a promised
    # response using the rejected promise.
    #
    # + promise - The `PushPromise` to be rejected
    public remote function rejectPromise(PushPromise promise);
};

public remote function HttpClient.post(string path, RequestMessage message) returns Response|error {
    return self.httpCaller->post(path, message);
}

public remote function HttpClient.head(string path, RequestMessage message = ()) returns Response|error {
    return self.httpCaller->head(path, message = message);
}

public remote function HttpClient.put(string path, RequestMessage message) returns Response|error {
    return self.httpCaller->put(path, message);
}

public remote function HttpClient.execute(string httpVerb, string path, RequestMessage message) returns Response|error {
    return self.httpCaller->execute(httpVerb, path, message);
}

public remote function HttpClient.patch(string path, RequestMessage message) returns Response|error {
    return self.httpCaller->patch(path, message);
}

public remote function HttpClient.delete(string path, RequestMessage message) returns Response|error {
    return self.httpCaller->delete(path, message);
}

public remote function HttpClient.get(string path, RequestMessage message = ()) returns Response|error {
    return self.httpCaller->get(path, message = message);
}

public remote function HttpClient.options(string path, RequestMessage message = ()) returns Response|error {
    return self.httpCaller->options(path, message = message);
}

public remote function HttpClient.forward(string path, Request request) returns Response|error {
    return self.httpCaller->forward(path, request);
}

public remote function HttpClient.submit(string httpVerb, string path, RequestMessage message) returns HttpFuture|error {
    return self.httpCaller->submit(httpVerb, path, message);
}

public remote function HttpClient.getResponse(HttpFuture httpFuture) returns Response|error {
    return self.httpCaller->getResponse(httpFuture);
}

public remote function HttpClient.hasPromise(HttpFuture httpFuture) returns boolean {
    return self.httpCaller->hasPromise(httpFuture);
}

public remote function HttpClient.getNextPromise(HttpFuture httpFuture) returns PushPromise|error {
    return self.httpCaller->getNextPromise(httpFuture);
}

public remote function HttpClient.getPromisedResponse(PushPromise promise) returns Response|error {
    return self.httpCaller->getPromisedResponse(promise);
}

public remote function HttpClient.rejectPromise(PushPromise promise) {
    return self.httpCaller->rejectPromise(promise);
}

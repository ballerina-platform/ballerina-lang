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


documentation {
    Provides the HTTP actions for interacting with an HTTP server. Apart from the standard HTTP methods, `forward()`
    and `execute()` functions are provided. More complex and specific endpoint types can be created by wrapping this
    generic HTTP actions implementation.

    F{{serviceUri}} The URL of the remote HTTP endpoint
    F{{config}} The configurations of the client endpoint associated with this HttpActions instance
}
public type CallerActions object {
    //These properties are populated from the init call to the client connector as these were needed later stage
    //for retry and other few places.
    public string serviceUri;
    public ClientEndpointConfig config;

    documentation {
        The `post()` function can be used to send HTTP POST requests to HTTP endpoints.

        P{{path}} Resource path
        P{{message}} An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
                     `io:ByteChannel` or `mime:Entity[]`
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public function post(@sensitive string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                    message) returns Response|error {
        Request req = buildRequest(message);
        return nativePost(self, path, req);
    }

    documentation {
        The `head()` function can be used to send HTTP HEAD requests to HTTP endpoints.

        P{{path}} Resource path
        P{{message}} An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
                     `io:ByteChannel` or `mime:Entity[]`
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public function head(@sensitive string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                    message = ()) returns Response|error {
        Request req = buildRequest(message);
        return nativeHead(self, path, req);
    }

    documentation {
        The `put()` function can be used to send HTTP PUT requests to HTTP endpoints.

        P{{path}} Resource path
        P{{message}} An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
                     `io:ByteChannel` or `mime:Entity[]`
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public function put(@sensitive string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                        message) returns Response|error {
        Request req = buildRequest(message);
        return nativePut(self, path, req);
    }

    documentation {
		Invokes an HTTP call with the specified HTTP verb.

        P{{httpVerb}} HTTP verb value
        P{{path}} Resource path
        P{{message}} An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
                     `io:ByteChannel` or `mime:Entity[]`
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public function execute(@sensitive string httpVerb, @sensitive string path, Request|string|xml|json|byte[]
                                                        |io:ByteChannel|mime:Entity[]|() message) returns Response|error {
        Request req = buildRequest(message);
        return nativeExecute(self, httpVerb, path, req);
    }

    documentation {
        The `patch()` function can be used to send HTTP PATCH requests to HTTP endpoints.

        P{{path}} Resource path
        P{{message}} An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
                     `io:ByteChannel` or `mime:Entity[]`
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public function patch(@sensitive string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                            message) returns Response|error {
        Request req = buildRequest(message);
        return nativePatch(self, path, req);
    }

    documentation {
        The `delete()` function can be used to send HTTP DELETE requests to HTTP endpoints.

        P{{path}} Resource path
        P{{message}} An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
                     `io:ByteChannel` or `mime:Entity[]`
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public function delete(@sensitive string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                            message) returns Response|error {
        Request req = buildRequest(message);
        return nativeDelete(self, path, req);
    }

    documentation {
        The `get()` function can be used to send HTTP GET requests to HTTP endpoints.

        P{{path}} Request path
        P{{message}} An optional HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
                     `io:ByteChannel` or `mime:Entity[]`
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public function get(@sensitive string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                        message = ()) returns Response|error {
        Request req = buildRequest(message);
        return nativeGet(self, path, req);
    }

    documentation {
        The `options()` function can be used to send HTTP OPTIONS requests to HTTP endpoints.

        P{{path}} Request path
        P{{message}} An optional HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
                     `io:ByteChannel` or `mime:Entity[]`
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public function options(@sensitive string path, Request|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|()
                                                            message = ()) returns Response|error {
        Request req = buildRequest(message);
        return nativeOptions(self, path, req);
    }

    documentation {
        The `forward()` function can be used to invoke an HTTP call with inbound request's HTTP verb

        P{{path}} Request path
        P{{request}} An HTTP inbound request message
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public extern function forward(@sensitive string path, Request request) returns Response|error;

    documentation {
        Submits an HTTP request to a service with the specified HTTP verb.
        The `submit()` function does not give out a `Response` as the result,
        rather it returns an `HttpFuture` which can be used to do further interactions with the endpoint.

        P{{httpVerb}} The HTTP verb value
        P{{path}} The resource path
        P{{message}} An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
                     `io:ByteChannel` or `mime:Entity[]`
        R{{}} An `HttpFuture` that represents an asynchronous service invocation, or an `error` if the submission fails
    }
    public function submit(@sensitive string httpVerb, string path, Request|string|xml|json|byte[]|
                                                    io:ByteChannel|mime:Entity[]|() message) returns HttpFuture|error {
        Request req = buildRequest(message);
        return nativeSubmit(self, httpVerb, path, req);
    }

    documentation {
        Retrieves the `Response` for a previously submitted request.

        P{{httpFuture}} The `HttpFuture` related to a previous asynchronous invocation
        R{{}} An HTTP response message, or an `error` if the invocation fails
    }
    public extern function getResponse(HttpFuture httpFuture) returns Response|error;

    documentation {
        Checks whether a `PushPromise` exists for a previously submitted request.

        P{{httpFuture}} The `HttpFuture` relates to a previous asynchronous invocation
        R{{}} A `boolean` that represents whether a `PushPromise` exists
    }
    public extern function hasPromise(HttpFuture httpFuture) returns (boolean);

    documentation {
        Retrieves the next available `PushPromise` for a previously submitted request.

        P{{httpFuture}} The `HttpFuture` relates to a previous asynchronous invocation
        R{{}} An HTTP Push Promise message, or an `error` if the invocation fails
    }
    public extern function getNextPromise(HttpFuture httpFuture) returns PushPromise|error;

    documentation {
        Retrieves the promised server push `Response` message.

        P{{promise}} The related `PushPromise`
        R{{}} A promised HTTP `Response` message, or an `error` if the invocation fails
    }
    public extern function getPromisedResponse(PushPromise promise) returns Response|error;

    documentation {
        Rejects a `PushPromise`. When a `PushPromise` is rejected, there is no chance of fetching a promised
        response using the rejected promise.

        P{{promise}} The Push Promise to be rejected
    }
    public extern function rejectPromise(PushPromise promise);
};

documentation {
    Defines a timeout error occurred during service invocation.

    F{{message}} An explanation on what went wrong
    F{{cause}} The error which caused the `HttpTimeoutError`
    F{{statusCode}} HTTP status code
}
public type HttpTimeoutError record {
    string message,
    error? cause,
    int statusCode,
};

//Since the struct equivalency doesn't work with private keyword, following functions are defined outside the object
extern function nativePost(CallerActions callerActions, @sensitive string path, Request req) returns Response|error;

extern function nativeHead(CallerActions callerActions, @sensitive string path, Request req) returns Response|error;

extern function nativePut(CallerActions callerActions, @sensitive string path, Request req) returns Response|error;

extern function nativeExecute(CallerActions callerActions, @sensitive string httpVerb, @sensitive string path,
                                                                                Request req) returns Response|error;

extern function nativePatch(CallerActions callerActions, @sensitive string path, Request req) returns Response|error;

extern function nativeDelete(CallerActions callerActions, @sensitive string path, Request req) returns Response|error;

extern function nativeGet(CallerActions callerActions, @sensitive string path, Request req) returns Response|error;

extern function nativeOptions(CallerActions callerActions, @sensitive string path, Request req) returns Response|error;

extern function nativeSubmit(CallerActions callerActions, @sensitive string httpVerb, string path, Request req)
                                                                                            returns HttpFuture|error;

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
    public {
        string serviceUri;
        ClientEndpointConfig config;
    }

    documentation {
        The `post()` function can be used to send HTTP POST requests to HTTP endpoints.

        P{{path}} Resource path
        P{{request}} An HTTP outbound request message
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public native function post(@sensitive string path, Request? request = ()) returns Response|HttpConnectorError;

    documentation {
        The `head()` function can be used to send HTTP HEAD requests to HTTP endpoints.

        P{{path}} Resource path
        P{{request}} An HTTP outbound request message
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public native function head(@sensitive string path, Request? request = ()) returns Response|HttpConnectorError;

    documentation {
        The `put()` function can be used to send HTTP PUT requests to HTTP endpoints.

        P{{path}} Resource path
        P{{request}} An HTTP outbound request message
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public native function put(@sensitive string path, Request? request = ()) returns Response|HttpConnectorError;

    documentation {
		Invokes an HTTP call with the specified HTTP verb.
		
        P{{httpVerb}} HTTP verb value
        P{{path}} Resource path
        P{{request}} An HTTP outbound request message
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public native function execute(@sensitive string httpVerb, @sensitive string path, Request request)
                                                                                returns Response|HttpConnectorError;

    documentation {
        The `patch()` function can be used to send HTTP PATCH requests to HTTP endpoints.

        P{{path}} Resource path
        P{{request}} An HTTP outbound request message
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public native function patch(@sensitive string path, Request? request = ()) returns Response|HttpConnectorError;

    documentation {
        The `delete()` function can be used to send HTTP DELETE requests to HTTP endpoints.

        P{{path}} Resource path
        P{{request}} An HTTP outbound request message
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public native function delete(@sensitive string path, Request? request = ()) returns Response|HttpConnectorError;

    documentation {
        The `get()` function can be used to send HTTP GET requests to HTTP endpoints.

        P{{path}} Request path
        P{{request}} An HTTP outbound request message
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public native function get(@sensitive string path, Request? request = ()) returns Response|HttpConnectorError;

    documentation {
        The `options()` function can be used to send HTTP OPTIONS requests to HTTP endpoints.

        P{{path}} Request path
        P{{request}} An HTTP outbound request message
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public native function options(@sensitive string path, Request? request = ()) returns Response|HttpConnectorError;

    documentation {
        The `forward()` function can be used to invoke an HTTP call with inbound request's HTTP verb

        P{{path}} Request path
        P{{request}} An HTTP inbound request message
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public native function forward(@sensitive string path, Request request) returns Response|HttpConnectorError;

    documentation {
        Submits an HTTP request to a service with the specified HTTP verb.
        The `submit()` function does not give out a `Response` as the result,
        rather it returns an `HttpFuture` which can be used to do further interactions with the endpoint.

        P{{httpVerb}} The HTTP verb value
        P{{path}} The resource path
        P{{request}} An HTTP outbound request message
        R{{}} An `HttpFuture` that represents an asynchronous service invocation, or an `error` if the submission fails
    }
    public native function submit(@sensitive string httpVerb, string path, Request request)
                                                                            returns HttpFuture|HttpConnectorError;

    documentation {
        Retrieves the `Response` for a previously submitted request.

        P{{httpFuture}} The `HttpFuture` related to a previous asynchronous invocation
        R{{}} An HTTP response message, or an `error` if the invocation fails
    }
    public native function getResponse(HttpFuture httpFuture) returns Response|HttpConnectorError;

    documentation {
        Checks whether a `PushPromise` exists for a previously submitted request.

        P{{httpFuture}} The `HttpFuture` relates to a previous asynchronous invocation
        R{{}} A `boolean` that represents whether a `PushPromise` exists
    }
    public native function hasPromise(HttpFuture httpFuture) returns (boolean);

    documentation {
        Retrieves the next available `PushPromise` for a previously submitted request.

        P{{httpFuture}} The `HttpFuture` relates to a previous asynchronous invocation
        R{{}} An HTTP Push Promise message, or an `error` if the invocation fails
    }
    public native function getNextPromise(HttpFuture httpFuture) returns PushPromise|HttpConnectorError;

    documentation {
        Retrieves the promised server push `Response` message.

        P{{promise}} The related `PushPromise`
        R{{}} A promised HTTP `Response` message, or an `error` if the invocation fails
    }
    public native function getPromisedResponse(PushPromise promise) returns Response|HttpConnectorError;

    documentation {
        Rejects a `PushPromise`. When a `PushPromise` is rejected, there is no chance of fetching a promised
        response using the rejected promise.

        P{{promise}} The Push Promise to be rejected
    }
    public native function rejectPromise(PushPromise promise);
};

documentation {
    Defines an error occurred during the HTTP client invocation.

    F{{message}}  An explanation on what went wrong
    F{{cause}} The error which caused the `HttpConnectorError`
    F{{statusCode}} HTTP status code
}
public type HttpConnectorError {
    string message,
    error? cause,
    int statusCode,
};

documentation {
    Defines a timeout error occurred during service invocation.

    F{{message}} An explanation on what went wrong
    F{{cause}} The error which caused the `HttpTimeoutError`
    F{{statusCode}} HTTP status code
}
public type HttpTimeoutError {
    string message,
    error? cause,
    int statusCode,
};

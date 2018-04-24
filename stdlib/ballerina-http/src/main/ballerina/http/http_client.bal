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
    and `execute()` functions are provided. `forward()` takes an incoming HTTP requests and sends it to an upstream
    HTTP endpoint while `execute()` can be used for sending HTTP requests with custom verbs. More complex and specific
    endpoint types can be created by wrapping this generic HTTP actions implementation.

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
		The `POST()` function can be used to send HTTP POST requests to HTTP endpoints.
		
        P{{path}} Resource path 
        P{{req}} An HTTP outbound request message
        R{{}} The inbound response message
        R{{}} The error occurred while attempting to fulfill the HTTP request
	}
    public native function post(@sensitive string path, Request? request = ()) returns Response|HttpConnectorError;

    documentation {
		The `HEAD()` function can be used to send HTTP HEAD requests to HTTP endpoints.
		
        P{{path}} Resource path 
        P{{req}} An HTTP outbound request message
        R{{}} The inbound response message
        R{{}} The error occurred while attempting to fulfill the HTTP request
	}
    public native function head(@sensitive string path, Request? request = ()) returns Response|HttpConnectorError;

    documentation {
		The `PUT()` function can be used to send HTTP PUT requests to HTTP endpoints.
		
        P{{path}} Resource path 
        P{{req}} An HTTP outbound request message
        R{{}} The inbound response message
        R{{}} The error occurred while attempting to fulfill the HTTP request
	}
    public native function put(@sensitive string path, Request? request = ()) returns Response|HttpConnectorError;

    documentation {
		Invokes an HTTP call with the specified HTTP verb.
		
        P{{httpVerb}} HTTP verb value
        P{{path}} Resource path 
        P{{req}} An HTTP outbound request message
        R{{}} The inbound response message
        R{{}} The error occurred while attempting to fulfill the HTTP request
	}
    public native function execute(@sensitive string httpVerb, @sensitive string path, Request request)
                                                                                returns Response|HttpConnectorError;

    documentation {
		The `PATCH()` function can be used to send HTTP PATCH requests to HTTP endpoints.
		
        P{{path}} Resource path 
        P{{req}} An HTTP outbound request message
        R{{}} The inbound response message
        R{{}} The error occurred while attempting to fulfill the HTTP request
	}
    public native function patch(@sensitive string path, Request? request = ()) returns Response|HttpConnectorError;

    documentation {
		The `DELETE()` function can be used to send HTTP DELETE requests to HTTP endpoints.
		
        P{{path}} Resource path 
        P{{req}} An HTTP outbound request message
        R{{}} The inbound response message
        R{{}} The error occurred while attempting to fulfill the HTTP request
	}
    public native function delete(@sensitive string path, Request? request = ()) returns Response|HttpConnectorError;

    documentation {
		The `GET()` function can be used to send HTTP GET requests to HTTP endpoints.
		
        P{{path}} Request path
        P{{req}} An HTTP outbound request message
        R{{}} The inbound response message
        R{{}} The error occurred while attempting to fulfill the HTTP request
	}
    public native function get(@sensitive string path, Request? request = ()) returns Response|HttpConnectorError;

    documentation {
		The `OPTIONS()` function can be used to send HTTP OPTIONS requests to HTTP endpoints.
		
        P{{path}} Request path
        P{{req}} An HTTP outbound request message
        R{{}} The inbound response message
        R{{}} The error occurred while attempting to fulfill the HTTP request
	}
    public native function options(@sensitive string path, Request? request = ()) returns Response|HttpConnectorError;

    documentation {
		The `Forward()` function can be used to invoke an HTTP call with inbound request's HTTP verb
		
        P{{path}} Request path
        P{{req}} An HTTP inbound request message
        R{{}} The inbound response message
        R{{}} The error occurred while attempting to fulfill the HTTP request
	}
    public native function forward(@sensitive string path, Request request) returns Response|HttpConnectorError;

    documentation {
		Submits an HTTP request to a service with the specified HTTP verb.
		
        P{{httpVerb}} The HTTP verb value
        P{{path}} The Resource path 
        P{{req}} An HTTP outbound request message
        R{{}} The Future for further interactions
        R{{}} The error occurred while attempting to fulfill the HTTP request
	}
    public native function submit(@sensitive string httpVerb, string path, Request request)
                                                                            returns HttpFuture|HttpConnectorError;

    documentation {
		Retrieves response for a previously submitted request.
		
        P{{httpFuture}} The Future which relates to previous async invocation
        R{{}} The HTTP response message
        R{{}} The error occurred while attempting to fulfill the HTTP request
	}
    public native function getResponse(HttpFuture httpFuture) returns Response|HttpConnectorError;

    documentation {
		Checks whether server push exists for a previously submitted request.
		
        P{{httpFuture}} The Future which relates to previous async invocation
        R{{}} Returns true if the push promise exists
	}
    public native function hasPromise(HttpFuture httpFuture) returns (boolean);

    documentation {
		Retrieves the next available push promise for a previously submitted request.
		
        P{{httpFuture}} The Future which relates to previous async invocation
        R{{}} The HTTP Push Promise message
        R{{}} The error occurred while attempting to fulfill the HTTP request
	}
    public native function getNextPromise(HttpFuture httpFuture) returns PushPromise|HttpConnectorError;

    documentation {
		Retrieves the promised server push response.
		
        P{{promise}} The related Push Promise message
        R{{}}HTTP The Push Response message
        R{{}} The error occurred while attempting to fulfill the HTTP request
	}
    public native function getPromisedResponse(PushPromise promise) returns Response|HttpConnectorError;

    documentation {
		Rejects a push promise.
		
        P{{promise}} The Push Promise to be rejected
	}
    public native function rejectPromise(PushPromise promise);
};

documentation {
    `HttpConnectorError` record represents an error occurred during the HTTP client invocation.

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
    `HttpTimeoutError` record represents a timeout error occurred during service invocation.

    F{{message}} An explanation on what went wrong
    F{{cause}} The error which caused the `HttpTimeoutError`
    F{{statusCode}} HTTP status code.
}
public type HttpTimeoutError {
    string message,
    error? cause,
    int statusCode,
};

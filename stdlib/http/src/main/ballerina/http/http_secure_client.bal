// Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


import ballerina/io;
import ballerina/mime;
import ballerina/runtime;

const string EMPTY_STRING = "";
const string WHITE_SPACE = " ";

public type AuthScheme BASIC_AUTH|OAUTH2|JWT_AUTH;

public const BASIC_AUTH = "Basic";
public const OAUTH2 = "OAuth2";
public const JWT_AUTH = "JWT";

# Specifies how the authentication credentials should be sent when using the refresh token to refresh the access token
public type CredentialBearer AUTH_HEADER_BEARER|POST_BODY_BEARER;

# Indicates that the authentication credentials should be sent via the Authentication Header
public const AUTH_HEADER_BEARER = "AUTH_HEADER_BEARER";

# Indicates that the authentication credentials should be sent via the body of the POST request
public const POST_BODY_BEARER = "POST_BODY_BEARER";

# Provides secure HTTP actions for interacting with HTTP endpoints. This will make use of the authentication schemes
# configured in the HTTP client endpoint to secure the HTTP requests.
#
# + serviceUri - The URL of the remote HTTP endpoint
# + config - The configurations of the client endpoint associated with this HttpActions instance
# + httpClient - The underlying `HttpActions` instance which will be making the actual network calls
public type HttpSecureClient object {
    //These properties are populated from the init call to the client connector as these were needed later stage
    //for retry and other few places.
    public string serviceUri = "";
    public ClientEndpointConfig config = {};
    public CallerActions httpClient;

    public new(serviceUri, config) {
        self.httpClient = createSimpleHttpClient(serviceUri, config);
    }

    # This wraps the `post()` function of the underlying HTTP actions provider. Add relevant authentication headers
    # to the request and send the request to actual network call.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The inbound response message or an error occurred while attempting to fulfill the HTTP request
    public function post(string path, Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                        message) returns (Response|error) {
        Request req = buildRequest(message);
        check generateSecureRequest(req, self.config);
        Response response = check self.httpClient.post(path, req);
        boolean isRetry = isRetryRequired(response, self.config);
        if (isRetry) {
            check updateRequestAndConfig(req, self.config);
            return self.httpClient.post(path, req);
        }
        return response;
    }

    # This wraps the `head()` function of the underlying HTTP actions provider. Add relevant authentication headers
    # to the request and send the request to actual network call.
    #
    # + path - Resource path
    # + message - An optional HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The inbound response message or an error occurred while attempting to fulfill the HTTP request
    public function head(string path, Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                        message = ()) returns (Response|error) {
        Request req = buildRequest(message);
        check generateSecureRequest(req, self.config);
        Response response = check self.httpClient.head(path, message = req);
        boolean isRetry = isRetryRequired(response, self.config);
        if (isRetry) {
            check updateRequestAndConfig(req, self.config);
            return self.httpClient.head(path, message = req);
        }
        return response;
    }

    # This wraps the `put()` function of the underlying HTTP actions provider. Add relevant authentication headers
    # to the request and send the request to actual network call.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The inbound response message or an error occurred while attempting to fulfill the HTTP request
    public function put(string path,  Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                        message) returns (Response|error) {
        Request req = buildRequest(message);
        check generateSecureRequest(req, self.config);
        Response response = check self.httpClient.put(path, req);
        boolean isRetry = isRetryRequired(response, self.config);
        if (isRetry) {
            check updateRequestAndConfig(req, self.config);
            return self.httpClient.put(path, req);
        }
        return response;
    }

    # This wraps the `execute()` function of the underlying HTTP actions provider. Add relevant authentication headers
    # to the request and send the request to actual network call.
    #
    # + httpVerb - HTTP verb value
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The inbound response message or an error occurred while attempting to fulfill the HTTP request
    public function execute(string httpVerb, string path, Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                             message) returns (Response|error) {
        Request req = buildRequest(message);
        check generateSecureRequest(req, self.config);
        Response response = check self.httpClient.execute(httpVerb, path, req);
        boolean isRetry = isRetryRequired(response, self.config);
        if (isRetry) {
            check updateRequestAndConfig(req, self.config);
            return self.httpClient.execute(httpVerb, path, req);
        }
        return response;
    }

    # This wraps the `patch()` function of the underlying HTTP actions provider. Add relevant authentication headers
    # to the request and send the request to actual network call.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The inbound response message or an error occurred while attempting to fulfill the HTTP request
    public function patch(string path,  Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                            message) returns (Response|error) {
        Request req = buildRequest(message);
        check generateSecureRequest(req, self.config);
        Response response = check self.httpClient.patch(path, req);
        boolean isRetry = isRetryRequired(response, self.config);
        if (isRetry) {
            check updateRequestAndConfig(req, self.config);
            return self.httpClient.patch(path, req);
        }
        return response;
    }

    # This wraps the `delete()` function of the underlying HTTP actions provider. Add relevant authentication headers
    # to the request and send the request to actual network call.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The inbound response message or an error occurred while attempting to fulfill the HTTP request
    public function delete(string path, Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                            message) returns (Response|error) {
        Request req = buildRequest(message);
        check generateSecureRequest(req, self.config);
        Response response = check self.httpClient.delete(path, req);
        boolean isRetry = isRetryRequired(response, self.config);
        if (isRetry) {
            check updateRequestAndConfig(req, self.config);
            return self.httpClient.delete(path, req);
        }
        return response;
    }

    # This wraps the `get()` function of the underlying HTTP actions provider. Add relevant authentication headers
    # to the request and send the request to actual network call.
    #
    # + path - Request path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The inbound response message or an error occurred while attempting to fulfill the HTTP request
    public function get(string path, Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                        message = ()) returns (Response|error) {
        Request req = buildRequest(message);
        check generateSecureRequest(req, self.config);
        Response response = check self.httpClient.get(path, message = req);
        boolean isRetry = isRetryRequired(response, self.config);
        if (isRetry) {
            check updateRequestAndConfig(req, self.config);
            return self.httpClient.get(path, message = req);
        }
        return response;
    }

    # This wraps the `options()` function of the underlying HTTP actions provider. Add relevant authentication headers
    # to the request and send the request to actual network call.
    #
    # + path - Request path
    # + message - An optional HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The inbound response message or an error occurred while attempting to fulfill the HTTP request
    public function options(string path, Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                            message = ()) returns (Response|error) {
        Request req = buildRequest(message);
        check generateSecureRequest(req, self.config);
        Response response = check self.httpClient.options(path, message = req);
        boolean isRetry = isRetryRequired(response, self.config);
        if (isRetry) {
            check updateRequestAndConfig(req, self.config);
            return self.httpClient.options(path, message = req);
        }
        return response;
    }

    # This wraps the `forward()` function of the underlying HTTP actions provider. Add relevant authentication headers
    # to the request and send the request to actual network call.
    #
    # + path - Request path
    # + request - An HTTP inbound request message
    # + return - The inbound response message or an error occurred while attempting to fulfill the HTTP request
    public function forward(string path, Request request) returns (Response|error) {
        check generateSecureRequest(request, self.config);
        Response response = check self.httpClient.forward(path, request);
        boolean isRetry = isRetryRequired(response, self.config);
        if (isRetry) {
            check updateRequestAndConfig(request, self.config);
            return self.httpClient.forward(path, request);
        }
        return response;
    }

    # This wraps the `submit()` function of the underlying HTTP actions provider. Add relevant authentication headers
    # to the request and send the request to actual network call.
    #
    # + httpVerb - The HTTP verb value
    # + path - The resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - An `HttpFuture` that represents an asynchronous service invocation, or an error if the submission fails
    public function submit(string httpVerb, string path, Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|()
                                                            message) returns (HttpFuture|error) {
        Request req = buildRequest(message);
        check generateSecureRequest(req, self.config);
        return self.httpClient.submit(httpVerb, path, req);
    }

    # This just pass the request to actual network call.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - An HTTP response message, or an error if the invocation fails
    public function getResponse(HttpFuture httpFuture) returns (Response|error) {
        return self.httpClient.getResponse(httpFuture);
    }

    # This just pass the request to actual network call.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - A `boolean` that represents whether a `PushPromise` exists
    public function hasPromise(HttpFuture httpFuture) returns boolean {
        return self.httpClient.hasPromise(httpFuture);
    }

    # This just pass the request to actual network call.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - An HTTP Push Promise message, or an error if the invocation fails
    public function getNextPromise(HttpFuture httpFuture) returns (PushPromise|error) {
        return self.httpClient.getNextPromise(httpFuture);
    }

    # This just pass the request to actual network call.
    #
    # + promise - The related `PushPromise`
    # + return - A promised HTTP `Response` message, or an error if the invocation fails
    public function getPromisedResponse(PushPromise promise) returns (Response|error) {
        return self.httpClient.getPromisedResponse(promise);
    }

    # This just pass the request to actual network call.
    #
    # + promise - The Push Promise to be rejected
    public function rejectPromise(PushPromise promise) {
        return self.httpClient.rejectPromise(promise);
    }
};

# Creates an HTTP client capable of securing HTTP requests with authentication.
#
# + url - Base URL
# + config - Client endpoint configurations
# + return - Created secure HTTP client
public function createHttpSecureClient(string url, ClientEndpointConfig config) returns CallerActions {
    if (config.auth is AuthConfig) {
        HttpSecureClient httpSecureClient = new(url, config);
        return httpSecureClient;
    } else {
        CallerActions httpClient = createSimpleHttpClient(url, config);
        return httpClient;
    }
}

# Prepare HTTP request with the required headers for authentication.
#
# + req - An HTTP outbound request message
# + config - Client endpoint configurations
# + return - The Error occured during HTTP client invocation
function generateSecureRequest(Request req, ClientEndpointConfig config) returns (()|error) {
    var scheme = config.auth.scheme;
    if (scheme is AuthScheme) {
        if (scheme == BASIC_AUTH) {
            string username = config.auth.username ?: "";
            string password = config.auth.password ?: "";
            string str = username + ":" + password;
            string token = check str.base64Encode();
            req.setHeader(AUTH_HEADER, AUTH_SCHEME_BASIC + WHITE_SPACE + token);
        } else if (scheme == OAUTH2) {
            string accessToken = config.auth.accessToken ?: "";
            if (accessToken == EMPTY_STRING) {
                return updateRequestAndConfig(req, config);
            } else {
                req.setHeader(AUTH_HEADER, AUTH_SCHEME_BEARER + WHITE_SPACE + accessToken);
            }
        } else if (scheme == JWT_AUTH) {
            string authToken = runtime:getInvocationContext().authContext.authToken;
            if (authToken == EMPTY_STRING) {
                error err = error("Authentication token is not set at invocation context");
                return err;
            }
            req.setHeader(AUTH_HEADER, AUTH_SCHEME_BEARER + WHITE_SPACE + authToken);
        } else {
            error err = error("Invalid authentication scheme. It should be basic, oauth2 or jwt");
            return err;
        }
    }
    return ();
}

# Update request and client config with new access tokens retrieved.
#
# + req - `Request` object to be updated
# + config - Client endpoint configurations
# + return - The Error occured during HTTP client invocation
function updateRequestAndConfig(Request req, ClientEndpointConfig config) returns (()|error) {
    string accessToken = check getAccessTokenFromRefreshToken(config);
    req.setHeader(AUTH_HEADER, AUTH_SCHEME_BEARER + WHITE_SPACE + accessToken);
    AuthConfig? authConfig = config.auth;
    if (authConfig is AuthConfig) {
        authConfig.accessToken = accessToken;
    }
    return ();
}

# Request an access token from authorization server using the provided refresh token.
#
# + config - Client endpoint configurations
# + return - AccessToken received from the authorization server or `error` if error occured during HTTP client invocation
function getAccessTokenFromRefreshToken(ClientEndpointConfig config) returns (string|error) {
    string refreshToken = config.auth.refreshToken ?: "";
    string clientId = config.auth.clientId ?: "";
    string clientSecret = config.auth.clientSecret ?: "";
    string refreshUrl = config.auth.refreshUrl ?: "";
    string[] scopes = config.auth.scopes ?: [];

    if (refreshToken == EMPTY_STRING || clientId == EMPTY_STRING || clientSecret == EMPTY_STRING || refreshUrl == EMPTY_STRING) {
        error err = error("AccessTokenError", { message: "Failed to generate new access token since one or more of refresh token, client id, client secret,
        refresh url are not provided" });
        return err;
    }

    CallerActions refreshTokenClient = createSimpleHttpClient(refreshUrl, {});
    Request refreshTokenRequest = new;
    string textPayload = "grant_type=refresh_token&refresh_token=" + refreshToken;
    string scopeString = EMPTY_STRING;
    foreach requestScope in scopes {
        scopeString = scopeString + WHITE_SPACE + requestScope;
    }
    if (scopeString != EMPTY_STRING) {
        textPayload = textPayload + "&scope=" + scopeString.trim();
    }
    if (config.auth.credentialBearer == AUTH_HEADER_BEARER) {
        string clientIdSecret = clientId + ":" + clientSecret;
        refreshTokenRequest.addHeader(AUTH_HEADER, AUTH_SCHEME_BASIC + WHITE_SPACE + check clientIdSecret.base64Encode());
    } else {
        textPayload = textPayload + "&client_id=" + clientId + "&client_secret=" + clientSecret;
    }
    refreshTokenRequest.setTextPayload(textPayload, contentType = mime:APPLICATION_FORM_URLENCODED);
    Response refreshTokenResponse = check refreshTokenClient.post(EMPTY_STRING, refreshTokenRequest);

    json generatedToken = check refreshTokenResponse.getJsonPayload();
    if (refreshTokenResponse.statusCode == OK_200) {
        return generatedToken.access_token.toString();
    } else {
        error err = error("AccessTokenError", { message:
            "Failed to generate new access token from the given refresh token" });
        return err;
    }
}

# Check whether retry is required for the response. This returns true if the scheme is OAuth and the response status
# is 401 only. That implies user has given a expired access token and the client should update it with the given
# refresh url.
#
# + response - Response object
# + config - Client endpoint configurations
# + return - Whether the client should retry or not
function isRetryRequired(Response response, ClientEndpointConfig config) returns boolean {
    var scheme = config.auth.scheme;
    if (scheme is AuthScheme) {
        if (scheme == OAUTH2 && response.statusCode == UNAUTHORIZED_401) {
            return true;
        }
    }
    return false;
}

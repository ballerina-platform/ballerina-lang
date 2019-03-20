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

import ballerina/encoding;
import ballerina/io;
import ballerina/log;
import ballerina/mime;
import ballerina/runtime;

const string EMPTY_STRING = "";
const string WHITE_SPACE = " ";

# Specifies how the authentication credentials should be sent when token exchanging
public type CredentialBearer AUTH_HEADER_BEARER|POST_BODY_BEARER|NO_BEARER;

# Indicates that the authentication credentials should be sent via the Authentication Header
public const AUTH_HEADER_BEARER = "AUTH_HEADER_BEARER";

# Indicates that the authentication credentials should be sent via the body of the POST request
public const POST_BODY_BEARER = "POST_BODY_BEARER";

# Indicates that the authentication credentials should not be sent
public const NO_BEARER = "NO_BEARER";

# Specifies type of the the OAuth2 grant type
public type OAuth2GrantType CLIENT_CREDENTIALS_GRANT|PASSWORD_GRANT|DIRECT_TOKEN;

# Indicates OAuth2 client credentials grant type
public const CLIENT_CREDENTIALS_GRANT = "CLIENT_CREDENTIALS_GRANT";

# Indicates OAuth2 password grant type
public const PASSWORD_GRANT = "PASSWORD_GRANT";

# Indicates direct token as a grant type where this is considered as custom way of providing access tokens by user
public const DIRECT_TOKEN = "DIRECT_TOKEN";

# CachedTokenConfig record is used to store the received values from the authroization/token server, in order to use
# for the latter requests without requesting tokens again.
#
# + accessToken - Access token for authorization server
# + refreshToken - Refresh token for refresh token server
# + expiryTime - Expiry time of the access token in milli-seconds
type CachedTokenConfig record {
    string accessToken;
    string refreshToken;
    int expiryTime;
};

# RequestConfig record is used to prepare the HTTP request which is to be sent to authorization server.
#
# + payload - Payload of the request
# + clientId - Clietnt ID for client credentials grant authentication
# + clientSecret - Client secret for client credentials grant authentication
# + scopes - Scope of the access request
# + credentialBearer - How authentication credentials are sent to authorization server (AuthHeaderBearer, PostBodyBearer)
type RequestConfig record {
    string payload;
    string clientId?;
    string clientSecret?;
    string[]? scopes;
    CredentialBearer credentialBearer;
    !...;
};

CachedTokenConfig tokenCache = {
    accessToken: "",
    refreshToken: "",
    expiryTime: 0
};

# Provides secure HTTP remote functions for interacting with HTTP endpoints. This will make use of the authentication
# schemes configured in the HTTP client endpoint to secure the HTTP requests.
#
# + url - The URL of the remote HTTP endpoint
# + config - The configurations of the client endpoint associated with this HttpActions instance
# + httpClient - The underlying `HttpActions` instance which will be making the actual network calls
public type HttpSecureClient client object {
    //These properties are populated from the init call to the client connector as these were needed later stage
    //for retry and other few places.
    public string url = "";
    public ClientEndpointConfig config = {};
    public Client httpClient;

    public function __init(string url, ClientEndpointConfig config) {
        self.url = url;
        self.config = config;
        var simpleClient = createClient(url, self.config);
        if (simpleClient is Client) {
            self.httpClient = simpleClient;
        } else {
            panic simpleClient;
        }
    }

    # This wraps the `post()` function of the underlying HTTP remote functions provider. Add relevant authentication
    # headers to the request and send the request to actual network call.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The inbound response message or an error occurred while attempting to fulfill the HTTP request
    public remote function post(string path, RequestMessage message) returns Response|error {
        Request req = <Request>message;
        boolean retryRequired = check generateSecureRequest(req, self.config);
        Response res = check self.httpClient->post(path, req);
        retryRequired = isRetryRequired(retryRequired, res, self.config);
        if (retryRequired) {
            check updateRequest(req, self.config);
            return self.httpClient->post(path, req);
        }
        return res;
    }

    # This wraps the `head()` function of the underlying HTTP remote functions provider. Add relevant authentication
    # headers to the request and send the request to actual network call.
    #
    # + path - Resource path
    # + message - An optional HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The inbound response message or an error occurred while attempting to fulfill the HTTP request
    public remote function head(string path, RequestMessage message = ()) returns Response|error {
        Request req = <Request>message;
        boolean retryRequired = check generateSecureRequest(req, self.config);
        Response res = check self.httpClient->head(path, message = req);
        retryRequired = isRetryRequired(retryRequired, res, self.config);
        if (retryRequired) {
            check updateRequest(req, self.config);
            return self.httpClient->head(path, message = req);
        }
        return res;
    }

    # This wraps the `put()` function of the underlying HTTP remote functions provider. Add relevant authentication
    # headers to the request and send the request to actual network call.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The inbound response message or an error occurred while attempting to fulfill the HTTP request
    public remote function put(string path, RequestMessage message) returns Response|error {
        Request req = <Request>message;
        boolean retryRequired = check generateSecureRequest(req, self.config);
        Response res = check self.httpClient->put(path, req);
        retryRequired = isRetryRequired(retryRequired, res, self.config);
        if (retryRequired) {
            check updateRequest(req, self.config);
            return self.httpClient->put(path, req);
        }
        return res;
    }

    # This wraps the `execute()` function of the underlying HTTP remote functions provider. Add relevant authentication
    # headers o the request and send the request to actual network call.
    #
    # + httpVerb - HTTP verb value
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The inbound response message or an error occurred while attempting to fulfill the HTTP request
    public remote function execute(string httpVerb, string path, RequestMessage message) returns Response|error {
        Request req = <Request>message;
        boolean retryRequired = check generateSecureRequest(req, self.config);
        Response res = check self.httpClient->execute(httpVerb, path, req);
        retryRequired = isRetryRequired(retryRequired, res, self.config);
        if (retryRequired) {
            check updateRequest(req, self.config);
            return self.httpClient->execute(httpVerb, path, req);
        }
        return res;
    }

    # This wraps the `patch()` function of the underlying HTTP remote functions provider. Add relevant authentication
    # headers to the request and send the request to actual network call.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The inbound response message or an error occurred while attempting to fulfill the HTTP request
    public remote function patch(string path, RequestMessage message) returns Response|error {
        Request req = <Request>message;
        boolean retryRequired = check generateSecureRequest(req, self.config);
        Response res = check self.httpClient->patch(path, req);
        retryRequired = isRetryRequired(retryRequired, res, self.config);
        if (retryRequired) {
            check updateRequest(req, self.config);
            return self.httpClient->patch(path, req);
        }
        return res;
    }

    # This wraps the `delete()` function of the underlying HTTP remote functions provider. Add relevant authentication
    # headers to the request and send the request to actual network call.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The inbound response message or an error occurred while attempting to fulfill the HTTP request
    public remote function delete(string path, RequestMessage message) returns Response|error {
        Request req = <Request>message;
        boolean retryRequired = check generateSecureRequest(req, self.config);
        Response res = check self.httpClient->delete(path, req);
        retryRequired = isRetryRequired(retryRequired, res, self.config);
        if (retryRequired) {
            check updateRequest(req, self.config);
            return self.httpClient->delete(path, req);
        }
        return res;
    }

    # This wraps the `get()` function of the underlying HTTP remote functions provider. Add relevant authentication
    # headers to the request and send the request to actual network call.
    #
    # + path - Request path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The inbound response message or an error occurred while attempting to fulfill the HTTP request
    public remote function get(string path, RequestMessage message = ()) returns Response|error {
        Request req = <Request>message;
        boolean retryRequired = check generateSecureRequest(req, self.config);
        Response res = check self.httpClient->get(path, message = req);
        retryRequired = isRetryRequired(retryRequired, res, self.config);
        if (retryRequired) {
            check updateRequest(req, self.config);
            return self.httpClient->get(path, message = req);
        }
        return res;
    }

    # This wraps the `options()` function of the underlying HTTP remote functions provider. Add relevant authentication
    # headers to the request and send the request to actual network call.
    #
    # + path - Request path
    # + message - An optional HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The inbound response message or an error occurred while attempting to fulfill the HTTP request
    public remote function options(string path, RequestMessage message = ()) returns Response|error {
        Request req = <Request>message;
        boolean retryRequired = check generateSecureRequest(req, self.config);
        Response res = check self.httpClient->options(path, message = req);
        retryRequired = isRetryRequired(retryRequired, res, self.config);
        if (retryRequired) {
            check updateRequest(req, self.config);
            return self.httpClient->options(path, message = req);
        }
        return res;
    }

    # This wraps the `forward()` function of the underlying HTTP remote functions provider. Add relevant authentication
    # headers to the request and send the request to actual network call.
    #
    # + path - Request path
    # + request - An HTTP inbound request message
    # + return - The inbound response message or an error occurred while attempting to fulfill the HTTP request
    public remote function forward(string path, Request request) returns Response|error {
        boolean retryRequired = check generateSecureRequest(request, self.config);
        Response res = check self.httpClient->forward(path, request);
        retryRequired = isRetryRequired(retryRequired, res, self.config);
        if (retryRequired) {
            check updateRequest(request, self.config);
            return self.httpClient->forward(path, request);
        }
        return res;
    }

    # This wraps the `submit()` function of the underlying HTTP remote functions provider. Add relevant authentication
    # headers to the request and send the request to actual network call.
    #
    # + httpVerb - The HTTP verb value
    # + path - The resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - An `HttpFuture` that represents an asynchronous service invocation, or an error if the submission fails
    public remote function submit(string httpVerb, string path, RequestMessage message) returns HttpFuture|error {
        Request req = <Request>message;
        boolean retryRequired = check generateSecureRequest(req, self.config);
        return self.httpClient->submit(httpVerb, path, req);
    }

    # This just pass the request to actual network call.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - An HTTP response message, or an error if the invocation fails
    public remote function getResponse(HttpFuture httpFuture) returns Response|error {
        return self.httpClient->getResponse(httpFuture);
    }

    # This just pass the request to actual network call.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - A `boolean` that represents whether a `PushPromise` exists
    public remote function hasPromise(HttpFuture httpFuture) returns boolean {
        return self.httpClient->hasPromise(httpFuture);
    }

    # This just pass the request to actual network call.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - An HTTP Push Promise message, or an error if the invocation fails
    public remote function getNextPromise(HttpFuture httpFuture) returns PushPromise|error {
        return self.httpClient->getNextPromise(httpFuture);
    }

    # This just pass the request to actual network call.
    #
    # + promise - The related `PushPromise`
    # + return - A promised HTTP `Response` message, or an error if the invocation fails
    public remote function getPromisedResponse(PushPromise promise) returns Response|error {
        return self.httpClient->getPromisedResponse(promise);
    }

    # This just pass the request to actual network call.
    #
    # + promise - The Push Promise to be rejected
    public remote function rejectPromise(PushPromise promise) {
        return self.httpClient->rejectPromise(promise);
    }
};

# Creates an HTTP client capable of securing HTTP requests with authentication.
#
# + url - Base URL
# + config - Client endpoint configurations
# + return - Created secure HTTP client
public function createHttpSecureClient(string url, ClientEndpointConfig config) returns Client|error {
    HttpSecureClient httpSecureClient;
    if (config.auth is AuthConfig) {
        httpSecureClient = new(url, config);
        return httpSecureClient;
    } else {
        return createClient(url, config);
    }
}

# Prepare HTTP request with the required headers for authentication based on the scheme and return a flag saying whether
# retry is required if the response will be 401.
#
# + req - An HTTP outbound request message
# + config - Client endpoint configurations
# + return - Whether retry is required for 401 response or `error` if error occured during HTTP client invocation
function generateSecureRequest(Request req, ClientEndpointConfig config) returns boolean|error {
    var auth = config.auth;
    if (auth is AuthConfig) {
        var authConfig = auth["config"];
        if (auth.scheme == BASIC_AUTH) {
            if (authConfig is BasicAuthConfig) {
                string username = authConfig.username;
                string password = authConfig.password;
                if (username == EMPTY_STRING || password == EMPTY_STRING) {
                    string errMsg = "Username or password cannot be empty.";
                    log:printError(errMsg);
                    error e = error(HTTP_ERROR_CODE, { message: errMsg });
                    return e;
                }
                string str = username + ":" + password;
                string token = encoding:encodeBase64(str.toByteArray("UTF-8"));
                req.setHeader(AUTH_HEADER, AUTH_SCHEME_BASIC + WHITE_SPACE + token);
                log:printDebug(function() returns string {
                        return "Secure request generated for basic auth scheme.";
                    });
            } else {
                string errMsg = "Basic auth config not provided.";
                log:printError(errMsg);
                error e = error(HTTP_ERROR_CODE, { message: errMsg });
                return e;
            }
        } else if (auth.scheme == OAUTH2) {
            if (authConfig is OAuth2AuthConfig) {
                var grantType = authConfig.grantType;
                var grantTypeConfig = authConfig.config;
                if (grantType is PASSWORD_GRANT) {
                    if (grantTypeConfig is PasswordGrantConfig) {
                        string cachedAccessToken = tokenCache.accessToken;
                        if (cachedAccessToken == EMPTY_STRING) {
                            string accessToken = check getAccessTokenFromAuthorizationRequest(grantTypeConfig);
                            req.setHeader(AUTH_HEADER, AUTH_SCHEME_BEARER + WHITE_SPACE + accessToken);
                            log:printDebug(function() returns string {
                                    return "Secure request generated for OAuth2 password grant type with the access token received from authorization request.";
                                });
                        } else {
                            if (isValidAccessToken()) {
                                req.setHeader(AUTH_HEADER, AUTH_SCHEME_BEARER + WHITE_SPACE + cachedAccessToken);
                                log:printDebug(function() returns string {
                                        return "Secure request generated for OAuth2 password grant type with cached access token.";
                                    });
                                return grantTypeConfig.retryRequest;
                            } else {
                                // TODO: introduce locking mechanism to limit the refreshing
                                string accessToken = check getAccessTokenFromRefreshRequest(grantTypeConfig);
                                req.setHeader(AUTH_HEADER, AUTH_SCHEME_BEARER + WHITE_SPACE + accessToken);
                                log:printDebug(function() returns string {
                                        return "Secure request generated for OAuth2 password grant type with the new access token received from refresh request.";
                                    });
                            }
                        }
                    } else {
                        string errMsg = "Invalid config is provided for the password grant type.";
                        log:printError(errMsg);
                        error e = error(HTTP_ERROR_CODE, { message: errMsg });
                        return e;
                    }
                } else if (grantType is CLIENT_CREDENTIALS_GRANT) {
                    if (grantTypeConfig is ClientCredentialsGrantConfig) {
                        string cachedAccessToken = tokenCache.accessToken;
                        if (cachedAccessToken == EMPTY_STRING) {
                            string accessToken = check getAccessTokenFromAuthorizationRequest(grantTypeConfig);
                            req.setHeader(AUTH_HEADER, AUTH_SCHEME_BEARER + WHITE_SPACE + accessToken);
                            log:printDebug(function() returns string {
                                    return "Secure request generated for OAuth2 client credentials grant type with the access token received from authorization request.";
                                });
                        } else {
                            if (isValidAccessToken()) {
                                req.setHeader(AUTH_HEADER, AUTH_SCHEME_BEARER + WHITE_SPACE + cachedAccessToken);
                                log:printDebug(function() returns string {
                                        return "Secure request generated for OAuth2 client credentials grant type with cached access token";
                                    });
                                return grantTypeConfig.retryRequest;
                            } else {
                                string accessToken = check getAccessTokenFromAuthorizationRequest(grantTypeConfig);
                                req.setHeader(AUTH_HEADER, AUTH_SCHEME_BEARER + WHITE_SPACE + accessToken);
                                log:printDebug(function() returns string {
                                        return "Secure request generated for OAuth2 client credentials grant type with the new access token received from authorization request.";
                                    });
                            }
                        }
                    } else {
                        string errMsg = "Invalid config is provided for the password grant type.";
                        log:printError(errMsg);
                        error e = error(HTTP_ERROR_CODE, { message: errMsg });
                        return e;
                    }
                } else {
                    // Within this code block, the grant type is DIRECT_TOKEN
                    if (grantTypeConfig is DirectTokenConfig) {
                        var directAccessToken = grantTypeConfig["accessToken"];
                        if (directAccessToken is string && directAccessToken != EMPTY_STRING) {
                            req.setHeader(AUTH_HEADER, AUTH_SCHEME_BEARER + WHITE_SPACE + directAccessToken);
                            log:printDebug(function() returns string {
                                    return "Secure request generated for OAuth2 direct token mode with the user given access token.";
                                });
                            return grantTypeConfig.retryRequest;
                        } else {
                            string accessToken = check getAccessTokenFromRefreshRequest(grantTypeConfig);
                            req.setHeader(AUTH_HEADER, AUTH_SCHEME_BEARER + WHITE_SPACE + accessToken);
                            log:printDebug(function() returns string {
                                    return "Secure request generated for OAuth2 direct token mode with the new access token received from refresh request.";
                                });
                        }
                    } else {
                        string errMsg = "Invalid config is provided for the direct token mode.";
                        log:printError(errMsg);
                        error e = error(HTTP_ERROR_CODE, { message: errMsg });
                        return e;
                    }
                }
            } else {
                string errMsg = "OAuth2 config not provided.";
                log:printError(errMsg);
                error e = error(HTTP_ERROR_CODE, { message: errMsg });
                return e;
            }
        } else if (auth.scheme == JWT_AUTH) {
            string authToken = EMPTY_STRING;
            if (authConfig is OAuth2AuthConfig || authConfig is BasicAuthConfig) {
                string errMsg = "JWT auth config not provided.";
                log:printError(errMsg);
                error e = error(HTTP_ERROR_CODE, { message: errMsg });
                return e;
            } else if (authConfig is JwtAuthConfig) {
                var jwtIssuerConfig = authConfig["inferredJwtIssuerConfig"];
                if (jwtIssuerConfig is ()) {
                    authToken = runtime:getInvocationContext().authenticationContext.authToken;
                } else {
                    auth:JwtHeader header = { alg: jwtIssuerConfig.signingAlg, typ: "JWT" };
                    auth:JwtPayload payload = {
                        sub: runtime:getInvocationContext().principal.username,
                        iss: jwtIssuerConfig.issuer,
                        exp: time:currentTime().time / 1000 + jwtIssuerConfig.expTime,
                        iat: time:currentTime().time / 1000,
                        nbf: time:currentTime().time / 1000,
                        jti: system:uuid(),
                        aud: jwtIssuerConfig.audience
                    };
                    auth:JWTIssuerConfig issuerConfig = {
                        keyStore: jwtIssuerConfig.keyStore,
                        keyAlias: jwtIssuerConfig.keyAlias,
                        keyPassword: jwtIssuerConfig.keyPassword
                    };
                    var token = auth:issueJwt(header, payload, issuerConfig);
                    // TODO: cache the token per-user per-client and reuse it
                    if (token is string) {
                        authToken = token;
                    } else {
                        return token;
                    }
                }
            } else {
                authToken = runtime:getInvocationContext().authenticationContext.authToken;
            }
            if (authToken == EMPTY_STRING) {
                string errMsg = "JWT was not used during inbound authentication. Provide InferredJwtIssuerConfig to issue new token.";
                log:printError(errMsg);
                error e = error(HTTP_ERROR_CODE, { message: errMsg });
                return e;
            }
            req.setHeader(AUTH_HEADER, AUTH_SCHEME_BEARER + WHITE_SPACE + authToken);
        } else {
            string errMsg = "Unsupported auth scheme.";
            log:printError(errMsg);
            error e = error(HTTP_ERROR_CODE, { message: errMsg });
            return e;
        }
    }
    return false;
}

# Check the validity of the access token which is in the cache. If expiry time is 0, that means no expiry time is
# returned at the authorization request which implies that the token is valid forever.
#
# + return - Whether the access token is valid or not
function isValidAccessToken() returns boolean {
    // TODO: introduce clock-skew
    int expiryTime = tokenCache.expiryTime;
    if (expiryTime == 0) {
        log:printDebug(function() returns string {
                return "Expiry time is 0, which means cached access token is always valid until we get a 401 response.";
            });
        return true;
    }
    int currentSystemTime = time:currentTime().time;
    if (currentSystemTime > expiryTime) {
        log:printDebug(function() returns string {
                return "Current tume > expiry time, which means cached access token is valid until we get a 401 response.";
            });
        return true;
    }
    log:printDebug(function() returns string {
            return "Cached access token is invalid.";
        });
    return false;
}

# Request an access token from authorization server using the provided configurations.
#
# + config - Grant type configuration
# + return - Access token received or `error` if error occured during HTTP client invocation
function getAccessTokenFromAuthorizationRequest(ClientCredentialsGrantConfig|PasswordGrantConfig config) returns string|error {
    Client authorizationClient;
    RequestConfig requestConfig;
    if (config is ClientCredentialsGrantConfig) {
        if (config.clientId == EMPTY_STRING || config.clientSecret == EMPTY_STRING) {
            string errMsg = "Client id or client secret cannot be empty.";
            log:printError(errMsg);
            error e = error(HTTP_ERROR_CODE, { message: errMsg });
            return e;
        }
        authorizationClient = check createClient(config.tokenUrl, {});
        requestConfig = {
            payload: "grant_type=client_credentials",
            clientId: config.clientId,
            clientSecret: config.clientSecret,
            scopes: config["scopes"],
            credentialBearer: config.credentialBearer
        };
    } else {
        authorizationClient = check createClient(config.tokenUrl, {});
        var clientId = config["clientId"];
        var clientSecret = config["clientSecret"];
        if (clientId is string && clientSecret is string) {
            if (clientId == EMPTY_STRING || clientSecret == EMPTY_STRING) {
                string errMsg = "Client id or client secret cannot be empty.";
                log:printError(errMsg);
                error e = error(HTTP_ERROR_CODE, { message: errMsg });
                return e;
            }
            requestConfig = {
                payload: "grant_type=password&username=" + config.username + "&password=" + config.password,
                clientId: clientId,
                clientSecret: clientSecret,
                scopes: config["scopes"],
                credentialBearer:config.credentialBearer
            };
        } else {
            requestConfig = {
                payload: "grant_type=password&username=" + config.username + "&password=" + config.password,
                scopes: config["scopes"],
                credentialBearer:config.credentialBearer
            };
        }
    }
    Request authorizationRequest = check prepareRequest(requestConfig);
    log:printDebug(function() returns string {
            return "Authorization request is prepared successfully.";
        });
    Response authorizationResponse = check authorizationClient->post(EMPTY_STRING, authorizationRequest);
    return extractAccessTokenFromResponse(authorizationResponse);
}

# Request an access token from authorization server using the provided refresh configurations.
#
# + config - Password grant type configuration or direct token configuration
# + return - Access token received or `error` if error occured during HTTP client invocation
function getAccessTokenFromRefreshRequest(PasswordGrantConfig|DirectTokenConfig config) returns string|error {
    Client refreshClient;
    RequestConfig requestConfig;
    if (config is PasswordGrantConfig) {
        var refreshConfig = config["refreshConfig"];
        if (refreshConfig is RefreshConfig) {
            if (config.clientId == EMPTY_STRING || config.clientSecret == EMPTY_STRING) {
                string errMsg = "Client id or client secret cannot be empty.";
                log:printError(errMsg);
                error e = error(HTTP_ERROR_CODE, { message: errMsg });
                return e;
            }
            refreshClient = check createClient(refreshConfig.refreshUrl, {});
            requestConfig = {
                payload: "grant_type=refresh_token&refresh_token=" + tokenCache.refreshToken,
                clientId: config.clientId,
                clientSecret: config.clientSecret,
                scopes: refreshConfig["scopes"],
                credentialBearer: refreshConfig.credentialBearer
            };
        } else {
            string errMsg = "Failed to refresh access token since RefreshTokenConfig is not provided.";
            log:printError(errMsg);
            error e = error(HTTP_ERROR_CODE, { message: errMsg });
            return e;
        }
    } else {
        var refreshConfig = config["refreshConfig"];
        if (refreshConfig is DirectTokenRefreshConfig) {
            if (refreshConfig.clientId == EMPTY_STRING || refreshConfig.clientSecret == EMPTY_STRING) {
                string errMsg = "Client id or client secret cannot be empty.";
                log:printError(errMsg);
                error e = error(HTTP_ERROR_CODE, { message: errMsg });
                return e;
            }
            refreshClient = check createClient(refreshConfig.refreshUrl, {});
            requestConfig = {
                payload: "grant_type=refresh_token&refresh_token=" + refreshConfig.refreshToken,
                clientId: refreshConfig.clientId,
                clientSecret: refreshConfig.clientSecret,
                scopes: refreshConfig["scopes"],
                credentialBearer: refreshConfig.credentialBearer
            };
        } else {
            string errMsg = "Failed to refresh access token since RefreshTokenConfig is not provided.";
            log:printError(errMsg);
            error e = error(HTTP_ERROR_CODE, { message: errMsg });
            return e;
        }
    }
    Request refreshRequest = check prepareRequest(requestConfig);
    log:printDebug(function() returns string {
            return "Refresh request is prepared successfully.";
        });
    Response refreshResponse = check refreshClient->post(EMPTY_STRING, refreshRequest);
    return extractAccessTokenFromResponse(refreshResponse);
}

# Prepare the request which is to be sent to authorization server by adding relevant headers and payloads.
#
# + config - Request configurations record
# + return - Prepared HTTP request object
function prepareRequest(RequestConfig config) returns Request|error {
    Request req = new;
    string textPayload = config.payload;
    string scopeString = EMPTY_STRING;
    string[]? scopes = config.scopes;
    if (scopes is string[]) {
        foreach var requestScope in scopes {
            scopeString = scopeString + WHITE_SPACE + requestScope;
        }
    }
    if (scopeString != EMPTY_STRING) {
        textPayload = textPayload + "&scope=" + scopeString.trim();
    }

    var clientId = config["clientId"];
    var clientSecret = config["clientSecret"];
    if (config.credentialBearer == AUTH_HEADER_BEARER) {
        if (clientId is string && clientSecret is string) {
            string clientIdSecret = clientId + ":" + clientSecret;
            req.addHeader(AUTH_HEADER, AUTH_SCHEME_BASIC + WHITE_SPACE +
                    encoding:encodeBase64(clientIdSecret.toByteArray("UTF-8")));
        } else {
            string errMsg = "Client ID or client secret is not provided for client authentication.";
            log:printError(errMsg);
            error e = error(HTTP_ERROR_CODE, { message: errMsg });
            return e;
        }
    } else if (config.credentialBearer == POST_BODY_BEARER) {
        if (clientId is string && clientSecret is string) {
            textPayload = textPayload + "&client_id=" + clientId + "&client_secret=" + clientSecret;
        } else {
            string errMsg = "Client ID or client secret is not provided for client authentication.";
            log:printError(errMsg);
            error e = error(HTTP_ERROR_CODE, { message: errMsg });
            return e;
        }
    }
    req.setTextPayload(untaint textPayload, contentType = mime:APPLICATION_FORM_URLENCODED);
    return req;
}

# Extract access token from the json payload of given HTTP response and update the token cache.
#
# + response - HTTP response object
# + return - Extracted access token or `error` if error occured during HTTP client invocation
function extractAccessTokenFromResponse(Response response) returns string|error {
    json payload = check response.getJsonPayload();
    if (response.statusCode == OK_200) {
        log:printDebug(function() returns string {
                return "Response from the token endpoint is valid.";
            });
        check updateTokenCache(payload);
        return payload.access_token.toString();
    } else {
        string errMsg = "Failed to retrieve access token from the response.";
        log:printError(errMsg);
        error e = error(HTTP_ERROR_CODE, { message: errMsg, statusCode: response.statusCode, payload: payload });
        return e;
    }
}

# Update token cache with the received json payload of the response.
#
# + responsePayload - Payload of the response
# + return - If any error occurred due to convertion of parameters
function updateTokenCache(json responsePayload) returns ()|error {
    int issueTime = time:currentTime().time;
    string accessToken = responsePayload.access_token.toString();
    tokenCache.accessToken = accessToken;
    if (responsePayload["expires_in"] is int) {
        int expiresIn = check int.convert(responsePayload.expires_in);
        tokenCache.expiryTime = issueTime + expiresIn * 1000;
    }
    if (responsePayload["refresh_token"] is string) {
        string refreshToken = responsePayload.refresh_token.toString();
        tokenCache.refreshToken = refreshToken;
    }
    log:printDebug(function() returns string {
            return "Updated token cache with the new parameters received.";
        });
    return ();
}

# Check whether retry is required for the response. This returns true if the scheme is OAuth and the response status
# is 401 only. That implies user has given a expired access token or the retrieved access token from cahce is expired
# and the client should update it with the given refresh configurations.
#
# + retryRequired - Status of retry required
# + res - Request object
# + config - Client endpoint configurations
# + return - Whether the client should retry or not
function isRetryRequired(boolean retryRequired, Response res, ClientEndpointConfig config) returns boolean {
    if (retryRequired && res.statusCode == UNAUTHORIZED_401) {
        var auth = config.auth;
        if (auth is AuthConfig) {
            var authConfig = auth.config;
            if (auth.scheme == OAUTH2 && authConfig is OAuth2AuthConfig) {
                var grantType = authConfig.grantType;
                var grantTypeConfig = authConfig.config;
                if ((grantType is PASSWORD_GRANT || grantType is DIRECT_TOKEN) &&
                    (grantTypeConfig is PasswordGrantConfig || grantTypeConfig is DirectTokenConfig)) {
                    log:printDebug(function() returns string {
                            return "Retry is required for the given request.";
                        });
                    return true;
                }
            }
        }
    }
    return false;
}

# Update the request with new access token for the retry request.
#
# + config - Client endpoint configurations
# + return - `error` if error occured during HTTP client invocation
function updateRequest(Request req, ClientEndpointConfig config) returns ()|error {
    var auth = config.auth;
    if (auth is AuthConfig) {
        var authConfig = auth.config;
        if (authConfig is OAuth2AuthConfig) {
            var grantTypeConfig = authConfig.config;
            if (grantTypeConfig is ClientCredentialsGrantConfig) {
                string accessToken = check getAccessTokenFromAuthorizationRequest(grantTypeConfig);
                req.setHeader(AUTH_HEADER, AUTH_SCHEME_BEARER + WHITE_SPACE + accessToken);
                log:printDebug(function() returns string {
                        return "Updated secure request generated for OAuth2 client credentials grant type with the access token received from authorization request.";
                    });
            } else {
                string accessToken = check getAccessTokenFromRefreshRequest(grantTypeConfig);
                req.setHeader(AUTH_HEADER, AUTH_SCHEME_BEARER + WHITE_SPACE + accessToken);
                log:printDebug(function() returns string {
                        return "Updated secure request generated for OAuth2 direct token mode with the access token received from refresh request.";
                    });
            }
        }
    }
    return ();
}

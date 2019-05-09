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

# Specifies how to send the authentication credentials when exchanging tokens.
public type CredentialBearer AUTH_HEADER_BEARER|POST_BODY_BEARER|NO_BEARER;

# Indicates that the authentication credentials should be sent via the Authentication Header
public const AUTH_HEADER_BEARER = "AUTH_HEADER_BEARER";

# Indicates that the authentication credentials should be sent via the body of the POST request
public const POST_BODY_BEARER = "POST_BODY_BEARER";

# Indicates that the authentication credentials should not be sent
public const NO_BEARER = "NO_BEARER";

# Specifies the type of the OAuth2 grant type
public type OAuth2GrantType CLIENT_CREDENTIALS_GRANT|PASSWORD_GRANT|DIRECT_TOKEN;

# Indicates OAuth2 client credentials grant type
public const CLIENT_CREDENTIALS_GRANT = "CLIENT_CREDENTIALS_GRANT";

# Indicates OAuth2 password grant type
public const PASSWORD_GRANT = "PASSWORD_GRANT";

# Indicates `direct token` as a grant type, where this is considered as a custom way of providing access tokens by the user
public const DIRECT_TOKEN = "DIRECT_TOKEN";

#The `CachedToken` stores the values received from the authorization/token server to use them
# for the latter requests without requesting tokens again.
#
# + accessToken - Access token for the  authorization server
# + refreshToken - Refresh token for the refresh token server
# + expiryTime - Expiry time of the access token in milliseconds
public type CachedToken record {
    string accessToken;
    string refreshToken;
    int expiryTime;
};

# The `RequestConfig` record prepares the HTTP request, which is to be sent to the authorization server.
#
# + payload - Payload of the request
# + clientId - Client ID for client credentials grant authentication
# + clientSecret - Client secret for client credentials grant authentication
# + scopes - Scope of the access request
# + credentialBearer - How authentication credentials are sent to the authorization server
type RequestConfig record {|
    string payload;
    string clientId?;
    string clientSecret?;
    string[]? scopes;
    CredentialBearer credentialBearer;
|};

# Provides secure HTTP remote functions for interacting with HTTP endpoints. This will make use of the authentication
# schemes configured in the HTTP client endpoint to secure the HTTP requests.
#
# + url - The URL of the remote HTTP endpoint
# + config - The configurations of the client endpoint associated with this `HttpActions` instance
# + httpClient - The underlying `HttpActions` instance, which will make the actual network calls
# + tokenCache - Cached token configurations
public type HttpSecureClient client object {
    //These properties are populated from the init call and sent to the client connector as these will be needed at a
    //later stage for retrying and in other few places.
    public string url = "";
    public ClientEndpointConfig config = {};
    public Client httpClient;
    public CachedToken tokenCache;

    public function __init(string url, ClientEndpointConfig config) {
        self.url = url;
        self.config = config;
        var simpleClient = createClient(url, self.config);
        if (simpleClient is Client) {
            self.httpClient = simpleClient;
            self.tokenCache = {
                accessToken: "",
                refreshToken: "",
                expiryTime: 0
            };
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
    # + return - The inbound response message or the error if one occurred while attempting to fulfill the HTTP request
    public remote function post(string path, RequestMessage message) returns Response|error {
        Request req = <Request>message;
        boolean retryRequired = check generateSecureRequest(req, self.config, self.tokenCache);
        Response res = check self.httpClient->post(path, req);
        retryRequired = isRetryRequired(retryRequired, res, self.config);
        if (retryRequired) {
            check updateRequest(req, self.config, self.tokenCache);
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
    # + return - The inbound response message or the error if one occurred while attempting to fulfill the HTTP request
    public remote function head(string path, RequestMessage message = ()) returns Response|error {
        Request req = <Request>message;
        boolean retryRequired = check generateSecureRequest(req, self.config, self.tokenCache);
        Response res = check self.httpClient->head(path, message = req);
        retryRequired = isRetryRequired(retryRequired, res, self.config);
        if (retryRequired) {
            check updateRequest(req, self.config, self.tokenCache);
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
        boolean retryRequired = check generateSecureRequest(req, self.config, self.tokenCache);
        Response res = check self.httpClient->put(path, req);
        retryRequired = isRetryRequired(retryRequired, res, self.config);
        if (retryRequired) {
            check updateRequest(req, self.config, self.tokenCache);
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
        boolean retryRequired = check generateSecureRequest(req, self.config, self.tokenCache);
        Response res = check self.httpClient->execute(httpVerb, path, req);
        retryRequired = isRetryRequired(retryRequired, res, self.config);
        if (retryRequired) {
            check updateRequest(req, self.config, self.tokenCache);
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
        boolean retryRequired = check generateSecureRequest(req, self.config, self.tokenCache);
        Response res = check self.httpClient->patch(path, req);
        retryRequired = isRetryRequired(retryRequired, res, self.config);
        if (retryRequired) {
            check updateRequest(req, self.config, self.tokenCache);
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
    # + return - The inbound response message or the error if one occurred while attempting to fulfill the HTTP request
    public remote function delete(string path, RequestMessage message) returns Response|error {
        Request req = <Request>message;
        boolean retryRequired = check generateSecureRequest(req, self.config, self.tokenCache);
        Response res = check self.httpClient->delete(path, req);
        retryRequired = isRetryRequired(retryRequired, res, self.config);
        if (retryRequired) {
            check updateRequest(req, self.config, self.tokenCache);
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
    # + return - The inbound response message or the error if one occurred while attempting to fulfill the HTTP request
    public remote function get(string path, RequestMessage message = ()) returns Response|error {
        Request req = <Request>message;
        boolean retryRequired = check generateSecureRequest(req, self.config, self.tokenCache);
        Response res = check self.httpClient->get(path, message = req);
        retryRequired = isRetryRequired(retryRequired, res, self.config);
        if (retryRequired) {
            check updateRequest(req, self.config, self.tokenCache);
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
    # + return - The inbound response message or the error if one  occurred while attempting to fulfill the HTTP request
    public remote function options(string path, RequestMessage message = ()) returns Response|error {
        Request req = <Request>message;
        boolean retryRequired = check generateSecureRequest(req, self.config, self.tokenCache);
        Response res = check self.httpClient->options(path, message = req);
        retryRequired = isRetryRequired(retryRequired, res, self.config);
        if (retryRequired) {
            check updateRequest(req, self.config, self.tokenCache);
            return self.httpClient->options(path, message = req);
        }
        return res;
    }

    # This wraps the `forward()` function of the underlying HTTP remote functions provider. Add relevant authentication
    # headers to the request and send the request to actual network call.
    #
    # + path - Request path
    # + request - An HTTP inbound request message
    # + return - The inbound response message or the error if one occurred while attempting to fulfill the HTTP request
    public remote function forward(string path, Request request) returns Response|error {
        boolean retryRequired = check generateSecureRequest(request, self.config, self.tokenCache);
        Response res = check self.httpClient->forward(path, request);
        retryRequired = isRetryRequired(retryRequired, res, self.config);
        if (retryRequired) {
            check updateRequest(request, self.config, self.tokenCache);
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
        boolean retryRequired = check generateSecureRequest(req, self.config, self.tokenCache);
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
    if (config.auth is OutboundAuthConfig) {
        httpSecureClient = new(url, config);
        return httpSecureClient;
    } else {
        return createClient(url, config);
    }
}

# Prepare an HTTP request with the required headers for authentication based on the scheme and return a flag saying
# whether retrying is required if the response will be 401.
#
# + req - An HTTP outbound request message
# + config - Client endpoint configurations
# + tokenCache - Cached token configurations
# + return - Whether retrying is required for a 401 response or
# `error` if an error occurred during the HTTP client invocation
function generateSecureRequest(Request req, ClientEndpointConfig config, CachedToken tokenCache) returns boolean|error {
    var auth = config.auth;
    if (auth is OutboundAuthConfig) {
        var authConfig = auth["config"];
        if (auth.scheme == BASIC_AUTH) {
            if (authConfig is BasicAuthConfig) {
                string authToken = check getAuthTokenForBasicAuth(authConfig);
                req.setHeader(AUTH_HEADER, AUTH_SCHEME_BASIC + WHITE_SPACE + authToken);
                return false;
            } else {
                return prepareError("Basic auth config not provided.");
            }
        } else if (auth.scheme == OAUTH2) {
            if (authConfig is OAuth2AuthConfig) {
                string authToken;
                boolean retryRequired;
                (authToken, retryRequired) = check getAuthTokenForOAuth2(authConfig, tokenCache, false);
                req.setHeader(AUTH_HEADER, AUTH_SCHEME_BEARER + WHITE_SPACE + authToken);
                return retryRequired;
            } else {
                return prepareError("OAuth2 config not provided.");
            }
        } else if (auth.scheme == JWT_AUTH) {
            string authToken = EMPTY_STRING;
            if (authConfig is JwtAuthConfig) {
                authToken = check getAuthTokenForJWTAuth(authConfig);
            } else if (authConfig is OAuth2AuthConfig || authConfig is BasicAuthConfig) {
                return prepareError("JWT auth config not provided.");
            } else {
                authToken = runtime:getInvocationContext().authenticationContext.authToken;
            }
            if (authToken == EMPTY_STRING) {
                return prepareError("JWT was not used during inbound authentication.
                                    Provide InferredJwtIssuerConfig to issue new token.");
            }
            req.setHeader(AUTH_HEADER, AUTH_SCHEME_BEARER + WHITE_SPACE + authToken);
            return false;
        }
    }
    return false;
}

# Process the auth token for basic auth.
#
# + authConfig - Basic auth configurations
# + return - Auth token or `error` if an error occured during validation
function getAuthTokenForBasicAuth(BasicAuthConfig authConfig) returns string|error {
    string username = authConfig.username;
    string password = authConfig.password;
    if (username == EMPTY_STRING || password == EMPTY_STRING) {
        return prepareError("Username or password cannot be empty.");
    }
    string str = username + ":" + password;
    string token = encoding:encodeBase64(str.toByteArray("UTF-8"));
    log:printDebug(function () returns string {
        return "Authorization header is generated for basic auth scheme.";
    });
    return token;
}

# Process auth token for OAuth2.
#
# + authConfig - OAuth2 configurations
# + tokenCache - Cached token configurations
# + updateRequest - Check if the request is updated after a 401 response
# + return - Auth token with the status whether retrying is required for a 401 response or returns
# `error` if the validation fails
function getAuthTokenForOAuth2(OAuth2AuthConfig authConfig, CachedToken tokenCache,
                               boolean updateRequest) returns (string, boolean)|error {
    var grantType = authConfig.grantType;
    var grantTypeConfig = authConfig.config;
    if (grantType is PASSWORD_GRANT) {
        if (grantTypeConfig is PasswordGrantConfig) {
            return getAuthTokenForOAuth2PasswordGrant(grantTypeConfig, tokenCache);
        } else {
            return prepareError("Invalid config is provided for the password grant type.");
        }
    } else if (grantType is CLIENT_CREDENTIALS_GRANT) {
        if (grantTypeConfig is ClientCredentialsGrantConfig) {
            return getAuthTokenForOAuth2ClientCredentialsGrant(grantTypeConfig, tokenCache);
        } else {
            return prepareError("Invalid config is provided for the password grant type.");
        }
    } else {
        // Within this code block, the grant type is DIRECT_TOKEN
        if (grantTypeConfig is DirectTokenConfig) {
            if (updateRequest) {
                grantTypeConfig.accessToken = EMPTY_STRING;
            }
            return getAuthTokenForOAuth2DirectTokenMode(grantTypeConfig, tokenCache);
        } else {
            return prepareError("Invalid config is provided for the direct token mode.");
        }
    }
}

# Process the auth token for OAuth2 password grant.
#
# + grantTypeConfig - Password grant configurations
# + tokenCache - Cached token configurations
# + return - Auth token with the status whether retrying is required for a 401 response or
# `error` if an error occurred during the HTTP client invocation or validation
function getAuthTokenForOAuth2PasswordGrant(PasswordGrantConfig grantTypeConfig,
                                            CachedToken tokenCache) returns (string, boolean)|error {
    string cachedAccessToken = tokenCache.accessToken;
    if (cachedAccessToken == EMPTY_STRING) {
        string accessToken = check getAccessTokenFromAuthorizationRequest(grantTypeConfig, tokenCache);
        log:printDebug(function () returns string {
            return "OAuth2 password grant type; Access token received from authorization request. Cache is empty.";
        });
        return (accessToken, false);
    } else {
        if (isCachedTokenValid(tokenCache)) {
            log:printDebug(function () returns string {
                return "OAuth2 password grant type; Access token received from cache.";
            });
            return (cachedAccessToken, grantTypeConfig.retryRequest);
        } else {
            lock {
                if (isCachedTokenValid(tokenCache)) {
                    cachedAccessToken = tokenCache.accessToken;
                    log:printDebug(function () returns string {
                        return "OAuth2 password grant type; Access token received from cache.";
                    });
                    return (cachedAccessToken, grantTypeConfig.retryRequest);
                } else {
                    string accessToken = check getAccessTokenFromRefreshRequest(grantTypeConfig, tokenCache);
                    log:printDebug(function () returns string {
                        return "OAuth2 password grant type; Access token received from refresh request.";
                    });
                    return (accessToken, false);
                }
            }
        }
    }
}

# Process the auth token for OAuth2 client credentials grant.
#
# + grantTypeConfig - Client credentials grant configurations
# + tokenCache - Cached token configurations
# + return - Auth token with the status whether retrying is required for a 401 response or
# `error` if an error occurred during the HTTP client invocation or validation
function getAuthTokenForOAuth2ClientCredentialsGrant(ClientCredentialsGrantConfig grantTypeConfig,
                                                     CachedToken tokenCache) returns (string, boolean)|error {
    string cachedAccessToken = tokenCache.accessToken;
    if (cachedAccessToken == EMPTY_STRING) {
        string accessToken = check getAccessTokenFromAuthorizationRequest(grantTypeConfig, tokenCache);
        log:printDebug(function () returns string {
            return "OAuth2 client credentials grant type; Access token received from authorization request.
                Cache is empty.";
        });
        return (accessToken, false);
    } else {
        if (isCachedTokenValid(tokenCache)) {
            log:printDebug(function () returns string {
                return "OAuth2 client credentials grant type; Access token received from cache.";
            });
            return (cachedAccessToken, grantTypeConfig.retryRequest);
        } else {
            lock {
                if (isCachedTokenValid(tokenCache)) {
                    cachedAccessToken = tokenCache.accessToken;
                    log:printDebug(function () returns string {
                        return "OAuth2 client credentials grant type; Access token received from cache.";
                    });
                    return (cachedAccessToken, grantTypeConfig.retryRequest);
                } else {
                    string accessToken = check getAccessTokenFromAuthorizationRequest(grantTypeConfig, tokenCache);
                    log:printDebug(function () returns string {
                        return "OAuth2 client credentials grant type; Access token received from authorization request.";
                    });
                    return (accessToken, false);
                }
            }
        }
    }
}

# Process the auth token for OAuth2 direct token mode.
#
# + grantTypeConfig - Direct token configurations
# + tokenCache - Cached token configurations
# + return - Auth token with the status whether retrying is required for a 401 response or
# `error` if an error occurred during the HTTP client invocation or validation
function getAuthTokenForOAuth2DirectTokenMode(DirectTokenConfig grantTypeConfig,
                                              CachedToken tokenCache) returns (string, boolean)|error {
    string cachedAccessToken = tokenCache.accessToken;
    if (cachedAccessToken == EMPTY_STRING) {
        var directAccessToken = grantTypeConfig["accessToken"];
        if (directAccessToken is string && directAccessToken != EMPTY_STRING) {
            log:printDebug(function () returns string {
                return "OAuth2 direct token mode; Access token received from user given request. Cache is empty.";
            });
            return (directAccessToken, grantTypeConfig.retryRequest);
        } else {
            string accessToken = check getAccessTokenFromRefreshRequest(grantTypeConfig, tokenCache);
            log:printDebug(function () returns string {
                return "OAuth2 direct token mode; Access token received from refresh request. Cache is empty.";
            });
            return (accessToken, false);
        }
    } else {
        if (isCachedTokenValid(tokenCache)) {
            log:printDebug(function () returns string {
                return "OAuth2 client credentials grant type; Access token received from cache.";
            });
            return (cachedAccessToken, grantTypeConfig.retryRequest);
        } else {
            lock {
                if (isCachedTokenValid(tokenCache)) {
                    cachedAccessToken = tokenCache.accessToken;
                    log:printDebug(function () returns string {
                        return "OAuth2 client credentials grant type; Access token received from cache.";
                    });
                    return (cachedAccessToken, grantTypeConfig.retryRequest);
                } else {
                    string accessToken = check getAccessTokenFromRefreshRequest(grantTypeConfig, tokenCache);
                    log:printDebug(function () returns string {
                        return "OAuth2 direct token mode; Access token received from refresh request.";
                    });
                    return (accessToken, false);
                }
            }
        }
    }
}

# Process auth token for JWT auth.
#
# + authConfig - JWT auth configurations
# + return - Auth token or `error` if an error occurred during the JWT issuing or validation
function getAuthTokenForJWTAuth(JwtAuthConfig authConfig) returns string|error {
    var jwtIssuerConfig = authConfig["inferredJwtIssuerConfig"];
    if (jwtIssuerConfig is ()) {
        return runtime:getInvocationContext().authenticationContext.authToken;
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
        // TODO: cache the token per-user per-client and reuse it
        return auth:issueJwt(header, payload, issuerConfig);
    }
}

# Check the validity of the access toke,n which is in the cache. If the expiry time is 0, that means no expiry time is
# returned with the authorization request. This implies that the token is valid forever.
#
# + tokenCache - Cached token configurations
# + return - Whether the access token is valid or not
function isCachedTokenValid(CachedToken tokenCache) returns boolean {
    int expiryTime = tokenCache.expiryTime;
    if (expiryTime == 0) {
        log:printDebug(function () returns string {
            return "Expiry time is 0, which means cached access token is always valid until we get a 401 response.";
        });
        return true;
    }
    int currentSystemTime = time:currentTime().time;
    if (currentSystemTime > expiryTime) {
        log:printDebug(function () returns string {
            return "Current tume > expiry time, which means cached access token is valid until we get a 401 response.";
        });
        return true;
    }
    log:printDebug(function () returns string {
        return "Cached access token is invalid.";
    });
    return false;
}

# Request an access token from the authorization server using the provided configurations.
#
# + config - Grant type configuration
# + tokenCache - Cached token configurations
# + return - Access token received or `error` if an error occurred during the HTTP client invocation
function getAccessTokenFromAuthorizationRequest(ClientCredentialsGrantConfig|PasswordGrantConfig config,
                                                CachedToken tokenCache) returns string|error {
    Client authorizationClient;
    RequestConfig requestConfig;
    int clockSkew;
    // TODO: Remove EMPTY_STRING initialization after fixing https://github.com/ballerina-platform/ballerina-lang/issues/14779
    string tokenUrl = EMPTY_STRING;

    if (config is ClientCredentialsGrantConfig) {
        if (config.clientId == EMPTY_STRING || config.clientSecret == EMPTY_STRING) {
            return prepareError("Client id or client secret cannot be empty.");
        }
        tokenUrl = config.tokenUrl;
        var clientCreation = createClient(tokenUrl, {});
        if (clientCreation is Client) {
            authorizationClient = clientCreation;
        }  else {
            return prepareError("Failed to create the authorization client with the URL: " + tokenUrl,
                                err = clientCreation);
        }
        requestConfig = {
            payload: "grant_type=client_credentials",
            clientId: config.clientId,
            clientSecret: config.clientSecret,
            scopes: config["scopes"],
            credentialBearer: config.credentialBearer
        };
        clockSkew = config.clockSkew;
    } else {
        tokenUrl = config.tokenUrl;
        var clientCreation = createClient(tokenUrl, {});
        if (clientCreation is Client) {
            authorizationClient = clientCreation;
        } else {
            return prepareError("Failed to create the authorization client with the URL: " + tokenUrl,
                                err = clientCreation);
        }
        var clientId = config["clientId"];
        var clientSecret = config["clientSecret"];
        if (clientId is string && clientSecret is string) {
            if (clientId == EMPTY_STRING || clientSecret == EMPTY_STRING) {
                return prepareError("Client id or client secret cannot be empty.");
            }
            requestConfig = {
                payload: "grant_type=password&username=" + config.username + "&password=" + config.password,
                clientId: clientId,
                clientSecret: clientSecret,
                scopes: config["scopes"],
                credentialBearer: config.credentialBearer
            };
        } else {
            requestConfig = {
                payload: "grant_type=password&username=" + config.username + "&password=" + config.password,
                scopes: config["scopes"],
                credentialBearer: config.credentialBearer
            };
        }
        clockSkew = config.clockSkew;
    }

    Request authorizationRequest = check prepareRequest(requestConfig);
    var authorizationResponse = authorizationClient->post(EMPTY_STRING, authorizationRequest);
    if (authorizationResponse is Response) {
        log:printDebug(function () returns string {
            return "Authorization request sent successfully to URL: " + tokenUrl;
        });
        return extractAccessTokenFromResponse(authorizationResponse, tokenCache, clockSkew);
    } else {
        return prepareError("Failed to send authorization request to URL: " + tokenUrl, err = authorizationResponse);
    }
}

# Request an access token from the authorization server using the provided refresh configurations.
#
# + config - Password grant type configuration or direct token configuration
# + tokenCache - Cached token configurations
# + return - Access token received or `error` if an error occurred during HTTP client invocation
function getAccessTokenFromRefreshRequest(PasswordGrantConfig|DirectTokenConfig config,
                                          CachedToken tokenCache) returns string|error {
    Client refreshClient;
    RequestConfig requestConfig;
    int clockSkew;
    // TODO: Remove EMPTY_STRING initialization after fixing https://github.com/ballerina-platform/ballerina-lang/issues/14779
    string refreshUrl = EMPTY_STRING;

    if (config is PasswordGrantConfig) {
        var refreshConfig = config["refreshConfig"];
        if (refreshConfig is RefreshConfig) {
            if (config.clientId == EMPTY_STRING || config.clientSecret == EMPTY_STRING) {
                return prepareError("Client id or client secret cannot be empty.");
            }
            refreshUrl = untaint refreshConfig.refreshUrl;
            var clientCreation = createClient(refreshUrl, {});
            if (clientCreation is Client) {
                refreshClient = clientCreation;
            }  else {
                return prepareError("Failed to create the refresh client with the URL: " + refreshUrl,
                                    err = clientCreation);
            }
            requestConfig = {
                payload: "grant_type=refresh_token&refresh_token=" + tokenCache.refreshToken,
                clientId: config.clientId,
                clientSecret: config.clientSecret,
                scopes: refreshConfig["scopes"],
                credentialBearer: refreshConfig.credentialBearer
            };
        } else {
            return prepareError("Failed to refresh access token since RefreshTokenConfig is not provided.");
        }
        clockSkew = config.clockSkew;
    } else {
        var refreshConfig = config["refreshConfig"];
        if (refreshConfig is DirectTokenRefreshConfig) {
            if (refreshConfig.clientId == EMPTY_STRING || refreshConfig.clientSecret == EMPTY_STRING) {
                string errMsg = "Client id or client secret cannot be empty.";
                return prepareError(errMsg);
            }
            refreshUrl = refreshConfig.refreshUrl;
            var clientCreation = createClient(refreshUrl, {});
            if (clientCreation is Client) {
                refreshClient = clientCreation;
            }  else {
                return prepareError("Failed to create the refresh client with the URL: " + refreshUrl,
                                    err = clientCreation);
            }
            requestConfig = {
                payload: "grant_type=refresh_token&refresh_token=" + refreshConfig.refreshToken,
                clientId: refreshConfig.clientId,
                clientSecret: refreshConfig.clientSecret,
                scopes: refreshConfig["scopes"],
                credentialBearer: refreshConfig.credentialBearer
            };
        } else {
            return prepareError("Failed to refresh access token since DirectRefreshTokenConfig is not provided.");
        }
        clockSkew = config.clockSkew;
    }

    Request refreshRequest = check prepareRequest(requestConfig);
    var refreshResponse = refreshClient->post(EMPTY_STRING, refreshRequest);
    if (refreshResponse is Response) {
        log:printDebug(function () returns string {
            return "Refresh request sent successfully to URL: " + refreshUrl;
        });
        return extractAccessTokenFromResponse(refreshResponse, tokenCache, clockSkew);
    } else {
        return prepareError("Failed to send refresh request to URL: " + refreshUrl, err = refreshResponse);
    }
}

# Prepare the request to be sent to the authorization server by adding the relevant headers and payloads.
#
# + config - `RequestConfig` record
# + return - Prepared HTTP request object
function prepareRequest(RequestConfig config) returns Request|error {
    Request req = new;
    string textPayload = config.payload;
    string scopeString = EMPTY_STRING;
    string[]? scopes = config.scopes;
    if (scopes is string[]) {
        foreach var requestScope in scopes {
            string trimmedRequestScope = requestScope.trim();
            if (trimmedRequestScope != EMPTY_STRING) {
                scopeString = scopeString + WHITE_SPACE + trimmedRequestScope;
            }
        }
    }
    if (scopeString != EMPTY_STRING) {
        textPayload = textPayload + "&scope=" + scopeString;
    }

    var clientId = config["clientId"];
    var clientSecret = config["clientSecret"];
    if (config.credentialBearer == AUTH_HEADER_BEARER) {
        if (clientId is string && clientSecret is string) {
            string clientIdSecret = clientId + ":" + clientSecret;
            req.addHeader(AUTH_HEADER, AUTH_SCHEME_BASIC + WHITE_SPACE +
                    encoding:encodeBase64(clientIdSecret.toByteArray("UTF-8")));
        } else {
            return prepareError("Client ID or client secret is not provided for client authentication.");
        }
    } else if (config.credentialBearer == POST_BODY_BEARER) {
        if (clientId is string && clientSecret is string) {
            textPayload = textPayload + "&client_id=" + clientId + "&client_secret=" + clientSecret;
        } else {
            return prepareError("Client ID or client secret is not provided for client authentication.");
        }
    }
    req.setTextPayload(untaint textPayload, contentType = mime:APPLICATION_FORM_URLENCODED);
    return req;
}

# Extract the access token from the JSON payload of a given HTTP response and update the token cache.
#
# + response - HTTP response object
# + tokenCache - Cached token configurations
# + clockSkew - Clock skew in seconds
# + return - Extracted access token or `error` if an error occurred during the HTTP client invocation
function extractAccessTokenFromResponse(Response response, CachedToken tokenCache, int clockSkew) returns string|error {
    if (response.statusCode == OK_200) {
        var payload = response.getJsonPayload();
        if (payload is json) {
            log:printDebug(function () returns string {
                return "Received an valid response. Extracting access token from the payload";
            });
            check updateTokenCache(payload, tokenCache, clockSkew);
            return payload.access_token.toString();
        } else {
            return prepareError("Failed to retrieve access token since the response payload is not a JSON.", err = payload);
        }
    } else {
        var payload = response.getTextPayload();
        if (payload is string) {
            return prepareError("Received an invalid response. StatusCode: " + response.statusCode + " Payload: " + payload);
        } else {
            return prepareError("Received an invalid response. StatusCode: " + response.statusCode, err = payload);
        }
    }
}

# Log, prepare and return the `error`.
#
# + message - Error message
# + err - `error` instance
# + return - Prepared `error` instance
function prepareError(string message, error? err = ()) returns error {
    log:printDebug(function () returns string { return message; });
    error preparedError = error(HTTP_ERROR_CODE, { message: message, reason: err.reason() });
    return preparedError;
}

# Update the token cache with the received JSON payload of the response.
#
# + responsePayload - Payload of the response
# + tokenCache - Cached token configurations
# + clockSkew - Clock skew in seconds
# + return - `error` if an error occurred during the conversion of the parameters
function updateTokenCache(json responsePayload, CachedToken tokenCache, int clockSkew) returns error? {
    int issueTime = time:currentTime().time;
    string accessToken = responsePayload.access_token.toString();
    tokenCache.accessToken = accessToken;
    var expiresIn = responsePayload["expires_in"];
    if (expiresIn is int) {
        tokenCache.expiryTime = issueTime + (expiresIn - clockSkew) * 1000;
    }
    if (responsePayload["refresh_token"] is string) {
        string refreshToken = responsePayload.refresh_token.toString();
        tokenCache.refreshToken = refreshToken;
    }
    log:printDebug(function () returns string {
        return "Updated token cache with the new parameters of the response.";
    });
    return ();
}

# Check whether retrying is required for the response. This returns 'true' if the scheme is OAuth and the response
# status is 401. That implies that the user has given an expired access token or the retrieved access token from the
# cache is expired. Thus, the client should update it with the given refresh configurations.
#
# + retryRequired - Whether retrying is required or not
# + res - Request object
# + config - Client endpoint configurations
# + return - Whether the client should retry or not
function isRetryRequired(boolean retryRequired, Response res, ClientEndpointConfig config) returns boolean {
    if (retryRequired && res.statusCode == UNAUTHORIZED_401) {
        var auth = config.auth;
        if (auth is OutboundAuthConfig) {
            var authConfig = auth.config;
            if (auth.scheme == OAUTH2 && authConfig is OAuth2AuthConfig) {
                var grantType = authConfig.grantType;
                var grantTypeConfig = authConfig.config;
                if ((grantType is PASSWORD_GRANT || grantType is DIRECT_TOKEN) &&
                    (grantTypeConfig is PasswordGrantConfig || grantTypeConfig is DirectTokenConfig)) {
                    log:printDebug(function () returns string {
                        return "Retry is required for the given request and configurations.";
                    });
                    return true;
                }
            }
        }
    }
    log:printDebug(function () returns string {
        return "Retry is not required for the given request.";
    });
    return false;
}

# Update the retry request with the new access token.
#
# + req - HTTP request object
# + config - Client endpoint configurations
# + tokenCache - Cached token configurations
# + return - Returns `error` if an error occurred during the HTTP client invocation
function updateRequest(Request req, ClientEndpointConfig config, CachedToken tokenCache) returns error? {
    var auth = config.auth;
    if (auth is OutboundAuthConfig) {
        var authConfig = auth.config;
        if (authConfig is OAuth2AuthConfig) {
            string authToken;
            boolean retryRequired;
            (authToken, retryRequired) = check getAuthTokenForOAuth2(authConfig, tokenCache, true);
            req.setHeader(AUTH_HEADER, AUTH_SCHEME_BEARER + WHITE_SPACE + authToken);
        }
    }
    return ();
}

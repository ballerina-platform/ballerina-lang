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
import ballerina/mime;
import ballerina/runtime;

const string EMPTY_STRING = "";
const string WHITE_SPACE = " ";

# Specifies how the authentication credentials should be sent when token exchanging
public type CredentialBearer AUTH_HEADER_BEARER|POST_BODY_BEARER;

# Indicates that the authentication credentials should be sent via the Authentication Header
public const AUTH_HEADER_BEARER = "AUTH_HEADER_BEARER";

# Indicates that the authentication credentials should be sent via the body of the POST request
public const POST_BODY_BEARER = "POST_BODY_BEARER";

# Specifies type of the the OAuth2 grant type
public type OAuth2GrantType CLIENT_CREDENTIALS_GRANT|PASSWORD_GRANT;

# Indicates OAuth2 client credentials grant type
public const CLIENT_CREDENTIALS_GRANT = "CLIENT_CREDENTIALS_GRANT";

# Indicates OAuth2 password grant type
public const PASSWORD_GRANT = "PASSWORD_GRANT";

public const AUTH_HEADER_STATUS = "AUTH_HEADER_STATUS";

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
        check generateSecureRequest(req, self.config);
        Response res = check self.httpClient->post(path, req);
        boolean retryRequired = check generateSecureRequestIfRetryRequired(req, res, self.config);
        if (retryRequired) {
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
        check generateSecureRequest(req, self.config);
        Response res = check self.httpClient->head(path, message = req);
        boolean retryRequired = check generateSecureRequestIfRetryRequired(req, res, self.config);
        if (retryRequired) {
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
        check generateSecureRequest(req, self.config);
        Response res = check self.httpClient->put(path, req);
        boolean retryRequired = check generateSecureRequestIfRetryRequired(req, res, self.config);
        if (retryRequired) {
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
        check generateSecureRequest(req, self.config);
        Response res = check self.httpClient->execute(httpVerb, path, req);
        boolean retryRequired = check generateSecureRequestIfRetryRequired(req, res, self.config);
        if (retryRequired) {
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
        check generateSecureRequest(req, self.config);
        Response res = check self.httpClient->patch(path, req);
        boolean retryRequired = check generateSecureRequestIfRetryRequired(req, res, self.config);
        if (retryRequired) {
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
        check generateSecureRequest(req, self.config);
        Response res = check self.httpClient->delete(path, req);
        boolean retryRequired = check generateSecureRequestIfRetryRequired(req, res, self.config);
        if (retryRequired) {
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
        check generateSecureRequest(req, self.config);
        Response res = check self.httpClient->get(path, message = req);
        boolean retryRequired = check generateSecureRequestIfRetryRequired(req, res, self.config);
        if (retryRequired) {
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
        check generateSecureRequest(req, self.config);
        Response res = check self.httpClient->options(path, message = req);
        boolean retryRequired = check generateSecureRequestIfRetryRequired(req, res, self.config);
        if (retryRequired) {
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
        check generateSecureRequest(request, self.config);
        Response res = check self.httpClient->forward(path, request);
        boolean retryRequired = check generateSecureRequestIfRetryRequired(request, res, self.config);
        if (retryRequired) {
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
        check generateSecureRequest(req, self.config);
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

type CachedTokenConfig record {
    string accessToken;
    string refreshToken;
    int expiryTime;
};

CachedTokenConfig tokenCache = {
    accessToken: "",
    refreshToken: "",
    expiryTime: 0
};

# Prepare HTTP request with the required headers for authentication based on the scheme.
#
# + req - An HTTP outbound request message
# + config - Client endpoint configurations
# + return - The Error occured during HTTP client invocation
function generateSecureRequest(Request req, ClientEndpointConfig config) returns ()|error {
    var auth = config.auth;
    if (auth is AuthConfig) {
        var authConfig = auth["config"];
        if (auth.scheme == BASIC_AUTH) {
            if (authConfig is BasicAuthConfig) {
                string username = authConfig.username;
                string password = authConfig.password;
                string str = username + ":" + password;
                string token = encoding:encodeBase64(str.toByteArray("UTF-8"));
                req.setHeader(AUTH_HEADER, AUTH_SCHEME_BASIC + WHITE_SPACE + token);
            } else {
                error e = error(HTTP_ERROR_CODE, { message: "Basic auth config not provided" });
                panic e;
            }
        } else if (auth.scheme == OAUTH2) {
            if (authConfig is OAuth2AuthConfig) {
                var grantType = authConfig.grantType;
                var grantTypeConfig = authConfig.config;
                if (grantType is PASSWORD_GRANT) {
                    if (grantTypeConfig is PasswordGrantTypeConfig) {
                        string cachedAccessToken = tokenCache.accessToken;
                        if (cachedAccessToken == EMPTY_STRING) {
                            string accessToken = check getAccessTokenFromAuthorizationRequest(grantTypeConfig);
                            req.setHeader(AUTH_HEADER, AUTH_SCHEME_BEARER + WHITE_SPACE + accessToken);
                        } else {
                            if (isValidAccessToken()) {
                                req.setHeader(AUTH_HEADER, AUTH_SCHEME_BEARER + WHITE_SPACE + cachedAccessToken);
                                // TODO: discuss how to validate only the 401 scenario where we get the access token from the cache
                            } else {
                                // TODO: introduce locking mechanism to limit the refreshing
                                string accessToken = check getAccessTokenFromRefreshToken(grantTypeConfig);
                                req.setHeader(AUTH_HEADER, AUTH_SCHEME_BEARER + WHITE_SPACE + accessToken);
                            }
                        }
                    } else {
                        error e = error(HTTP_ERROR_CODE,
                        { message: "Invalid config is provided for the password grant type" });
                        return e;
                    }
                } else {
                    // Within this code block, the grant type is CLIENT_CREDENTIALS_GRANT_TYPE
                    if (grantTypeConfig is ClientCredentialsGrantTypeConfig) {
                        // TODO: implement the logic
                    } else {
                        error e = error(HTTP_ERROR_CODE,
                        { message: "Invalid config is provided for the password grant type" });
                        return e;
                    }
                }
            } else {
                error e = error(HTTP_ERROR_CODE, { message: "OAuth2 config not provided" });
                panic e;
            }
        } else if (auth.scheme == JWT_AUTH) {
            string authToken = EMPTY_STRING;
            if (authConfig is OAuth2AuthConfig || authConfig is BasicAuthConfig) {
                error e = error(HTTP_ERROR_CODE, { message: "JWT auth config not provided" });
                panic e;
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
                error err = error(HTTP_ERROR_CODE, { message: "JWT was not used during inbound authentication.
                                                     Provide InferredJwtIssuerConfig to issue new token." });
                return err;
            }
            req.setHeader(AUTH_HEADER, AUTH_SCHEME_BEARER + WHITE_SPACE + authToken);
        } else {
            error err = error(HTTP_ERROR_CODE, { message: "Unsupported auth scheme" });
            return err;
        }
    }
    return ();
}

# Check the validity of the access token which is in the cache.
#
# + return - Whether the access token is valid or not
function isValidAccessToken() returns boolean {
    // TODO: introduce clock-skew
    int expiryTime = tokenCache.expiryTime;
    int currentSystemTime = time:currentTime().time;
    if (currentSystemTime > expiryTime) {
        return true;
    }
    return false;
}

# Request an access token from authorization server using the provided configurations.
#
# + config - Passwordd grant type configuration
# + return - Access token received or `error` if error occured during HTTP client invocation
function getAccessTokenFromAuthorizationRequest(PasswordGrantTypeConfig config) returns string|error {
    Client authorizationClient;
    string tokenUrl = config.tokenUrl;
    string username = config.username;
    string password = config.password;
    string clientId = config.clientId;
    string clientSecret = config.clientSecret;
    string[] scopes = config.scopes;
    CredentialBearer credentialBearer = config.credentialBearer;

    authorizationClient = check createClient(tokenUrl, {});
    Request authorizationRequest = new;
    string textPayload = "grant_type=password&username=" + username + "&password=" + password;
    string scopeString = EMPTY_STRING;
    foreach var requestScope in scopes {
        scopeString = scopeString + WHITE_SPACE + requestScope;
    }
    if (scopeString != EMPTY_STRING) {
        textPayload = textPayload + "&scope=" + scopeString.trim();
    }
    if (credentialBearer == AUTH_HEADER_BEARER) {
        string clientIdSecret = clientId + ":" + clientSecret;
        authorizationRequest.addHeader(AUTH_HEADER, AUTH_SCHEME_BASIC + WHITE_SPACE +
                encoding:encodeBase64(clientIdSecret.toByteArray("UTF-8")));
    } else {
        textPayload = textPayload + "&client_id=" + clientId + "&client_secret=" + clientSecret;
    }
    authorizationRequest.setTextPayload(untaint textPayload, contentType = mime:APPLICATION_FORM_URLENCODED);
    Response authorizationResponse = check authorizationClient->post(EMPTY_STRING, authorizationRequest);
    json authorizationResponsePayload = check authorizationResponse.getJsonPayload();
    if (authorizationResponse.statusCode == OK_200) {
        updateTokenCache(authorizationResponsePayload);
        return authorizationResponsePayload.access_token.toString();
    } else {
        error e = error(HTTP_ERROR_CODE,
        { message: "Failed to retrieve access token from the given credentials" });
        return e;
    }
}

# Request an access token from authorization server using the provided refresh configurations.
#
# + config - Password grant type configuration
# + return - Access token received or `error` if error occured during HTTP client invocation
function getAccessTokenFromRefreshToken(PasswordGrantTypeConfig config) returns string|error {
    Client refreshTokenClient;
    RefreshTokenConfig? refreshTokenConfig = config.refreshTokenConfig;
    if (refreshTokenConfig is ()) {
        error e = error(HTTP_ERROR_CODE,
        { message: "Failed to refresh access token since RefreshTokenConfig is not provided" });
        return e;
    } else {
        string clientId = config.clientId;
        string clientSecret = config.clientSecret;
        string refreshToken = tokenCache.refreshToken;
        string refreshUrl = refreshTokenConfig.refreshUrl;
        refreshTokenClient = check createClient(refreshUrl, {});
        Request refreshTokenRequest = new;
        string textPayload = "grant_type=refresh_token&refresh_token=" + refreshToken;
        string scopeString = EMPTY_STRING;
        foreach var requestScope in refreshTokenConfig.scopes {
            scopeString = scopeString + WHITE_SPACE + requestScope;
        }
        if (scopeString != EMPTY_STRING) {
            textPayload = textPayload + "&scope=" + scopeString.trim();
        }
        if (refreshTokenConfig.credentialBearer == AUTH_HEADER_BEARER) {
            string clientIdSecret = clientId + ":" + clientSecret;
            refreshTokenRequest.addHeader(AUTH_HEADER, AUTH_SCHEME_BASIC + WHITE_SPACE +
                    encoding:encodeBase64(clientIdSecret.toByteArray("UTF-8")));
        } else {
            textPayload = textPayload + "&client_id=" + clientId + "&client_secret=" + clientSecret;
        }
        refreshTokenRequest.setTextPayload(untaint textPayload, contentType = mime:APPLICATION_FORM_URLENCODED);
        Response refreshTokenResponse = check refreshTokenClient->post(EMPTY_STRING, refreshTokenRequest);

        json refreshTokenResponsePayload = check refreshTokenResponse.getJsonPayload();
        if (refreshTokenResponsePayload.statusCode == OK_200) {
            updateTokenCache(refreshTokenResponsePayload);
            return refreshTokenResponsePayload.access_token.toString();
        } else {
            error err = error(HTTP_ERROR_CODE,
                { message: "Failed to refresh the access token from the given refresh configurations" });
            return err;
        }
    }
}

# Update token cache with the received json payload of the response.
#
# + responsePayload - Payload of the response
function updateTokenCache(json responsePayload) {
    int issueTime = time:currentTime().time;
    string accessToken = responsePayload.access_token.toString();
    int expiresIn = <int>responsePayload.expires_in;
    if (responsePayload.refresh_token.toString() != EMPTY_STRING) {
        string refreshToken = responsePayload.refresh_token.toString();
        tokenCache.refreshToken = refreshToken;
    }
    tokenCache.accessToken = accessToken;
    tokenCache.expiryTime = issueTime + expiresIn * 1000;
}

# Check whether retry is required for the response. This returns true if the scheme is OAuth and the response status
# is 401 only. That implies user has given a expired access token and the client should update it with the given
# refresh url.
#
# + req - Request object
# + res - Response object
# + config - Client endpoint configurations
# + return - Whether the client should retry or not
function generateSecureRequestIfRetryRequired(Request req, Response res, ClientEndpointConfig config) returns boolean|error {
    if (res.statusCode == UNAUTHORIZED_401) {
        var auth = config.auth;
        if (auth is AuthConfig) {
            var authConfig = auth.config;
            if (auth.scheme == OAUTH2 && authConfig is OAuth2AuthConfig) {
                var grantType = authConfig.grantType;
                var grantTypeConfig = authConfig.config;
                if (grantType is PASSWORD_GRANT && grantTypeConfig is PasswordGrantTypeConfig) {
                    string accessToken = check getAccessTokenFromRefreshToken(grantTypeConfig);
                    req.setHeader(AUTH_HEADER, AUTH_SCHEME_BEARER + WHITE_SPACE + accessToken);
                    return true;
                }
            }
        }
    }
    return false;
}

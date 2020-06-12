// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/auth;
import ballerina/http;
import ballerina/log;
import ballerina/mime;
import ballerina/runtime;
import ballerina/time;

# Represents the grant type configs supported for OAuth2.
type GrantTypeConfig ClientCredentialsGrantConfig|PasswordGrantConfig|DirectTokenConfig;

# Represents the outbound OAuth2 provider, which generates OAtuh2 tokens. This supports the client credentials grant type,
# password grant type, and the direct token mode, which sends the access token directly.
# The `oauth2:OutboundOAuth2Provider` is an implementation of the `auth:OutboundAuthProvider` interface.
#
# 1. Client Credentials Grant Type
# ```ballerina
# oauth2:OutboundOAuth2Provider oauth2Provider1 = new({
#     tokenUrl: "https://localhost:9196/oauth2/token",
#     clientId: "3MVG9YDQS5WtC11paU2WcQjBB3L",
#     clientSecret: "9205371918321623741",
#     scopes: ["token-scope1", "token-scope2"]
# });
# ```
#
# 2. Password Grant Type
# ```ballerina
# oauth2:OutboundOAuth2Provider oauth2Provider5 = new({
#     tokenUrl: "https://localhost:9196/oauth2/token/authorize/header",
#     username: "johndoe",
#     password: "A3ddj3w",
#     clientId: "3MVG9YDQS5WtC11paU2WcQjBB3L",
#     clientSecret: "9205371918321623741",
#     scopes: ["token-scope1", "token-scope2"]
# });
# ```
#
# 3. Direct Token Mode
# ```ballerina
# oauth2:OutboundOAuth2Provider oauth2Provider13 = new({
#     accessToken: "2YotnFZFEjr1zCsicMWpAA",
#     refreshConfig: {
#         refreshUrl: "https://localhost:9196/oauth2/token/refresh",
#         refreshToken: "XlfBs91yquexJqDaKEMzVg==",
#         clientId: "3MVG9YDQS5WtC11paU2WcQjBB3L",
#         clientSecret: "9205371918321623741",
#         scopes: ["token-scope1", "token-scope2"]
#     }
# });
# ```
#
# + oauth2ProviderConfig - Outbound OAuth2 provider configurations
# + oauth2CacheEntry - Outbound OAuth2 cache entry
public type OutboundOAuth2Provider object {

    *auth:OutboundAuthProvider;

    public GrantTypeConfig? oauth2ProviderConfig;
    public OutboundOAuth2CacheEntry oauth2CacheEntry;

    # Provides authentication based on the provided OAuth2 configuration.
    #
    # + oauth2ProviderConfig - Outbound OAuth2 provider configurations
    public function init(GrantTypeConfig? oauth2ProviderConfig = ()) {
        self.oauth2ProviderConfig = oauth2ProviderConfig;
        self.oauth2CacheEntry = {
            accessToken: "",
            refreshToken: "",
            expTime: 0
        };
    }

# Generate a token for the OAuth2 authentication.
# ```ballerina
# string:auth:Error token = outboundOAuth2Provider.generateToken();
# ```
#
# + return - Generated `string` token or else an `auth:Error` if an error occurred
    public function generateToken() returns @tainted (string|auth:Error) {
        GrantTypeConfig? oauth2ProviderConfig = self.oauth2ProviderConfig;
        if (oauth2ProviderConfig is ()) {
            runtime:AuthenticationContext? authContext = runtime:getInvocationContext()?.authenticationContext;
            if (authContext is runtime:AuthenticationContext) {
                string? authToken = authContext?.authToken;
                if (authToken is string) {
                    return authToken;
                }
            }
            return prepareAuthError("Failed to generate OAuth2 token since OAuth2 provider config is not defined and auth token is not defined in the authentication context at invocation context.");
        } else {
            string|Error authToken = generateOAuth2Token(oauth2ProviderConfig, self.oauth2CacheEntry);
            if (authToken is string) {
                return authToken;
            } else {
                return prepareAuthError("Failed to generate OAuth2 token.", authToken);
            }
        }
    }

# Inspects the incoming data and generates the token for the OAuth2 authentication.
# ```ballerina
# string:auth:Error? token = outboundOAuth2Provider.inspect(data);
# ```
#
# + data - Map of data, which is extracted from the HTTP response
# + return - Generated `string` token, an `auth:Error` occurred while generating the token, or else 
#            `()` if nothing is to be returned
    public function inspect(map<anydata> data) returns @tainted (string|auth:Error?) {
        GrantTypeConfig? oauth2ProviderConfig = self.oauth2ProviderConfig;
        if (oauth2ProviderConfig is ()) {
            return ();
        } else {
            if (data[http:STATUS_CODE] == http:STATUS_UNAUTHORIZED) {
                string|Error authToken = inspectAuthTokenForOAuth2(oauth2ProviderConfig, self.oauth2CacheEntry);
                if (authToken is string) {
                    return authToken;
                } else {
                    return prepareAuthError("Failed to generate OAuth2 token at inspection.", authToken);
                }
            }
            return ();
        }
    }
};

# The data structure, which is used to configure the OAuth2 client credentials grant type.
#
# + tokenUrl - Token URL for the authorization endpoint
# + clientId - Client ID for the client credentials grant authentication
# + clientSecret - Client secret for the client credentials grant authentication
# + scopes - Scope(s) of the access request
# + clockSkewInSeconds - Clock skew in seconds
# + retryRequest - Retry the request if the initial request returns a 401 response
# + credentialBearer - Bearer of the authentication credentials, which is sent to the authorization endpoint
# + clientConfig - HTTP client configurations, which are used to call the authorization endpoint
public type ClientCredentialsGrantConfig record {|
    string tokenUrl;
    string clientId;
    string clientSecret;
    string[] scopes?;
    int clockSkewInSeconds = 0;
    boolean retryRequest = true;
    http:CredentialBearer credentialBearer = http:AUTH_HEADER_BEARER;
    http:ClientConfiguration clientConfig = {};
|};

# The data structure, which is used to configure the OAuth2 password grant type.
#
# + tokenUrl - Token URL for the authorization endpoint
# + username - Username for the password grant authentication
# + password - Password for the password grant authentication
# + clientId - Client ID for the password grant authentication
# + clientSecret - Client secret for the password grant authentication
# + scopes - Scope(s) of the access request
# + refreshConfig - Configurations for refreshing the access token
# + clockSkewInSeconds - Clock skew in seconds
# + retryRequest - Retry the request if the initial request returns a 401 response
# + credentialBearer - Bearer of the authentication credentials, which is sent to the authorization endpoint
# + clientConfig - HTTP client configurations, which are used to call the authorization endpoint
public type PasswordGrantConfig record {|
    string tokenUrl;
    string username;
    string password;
    string clientId?;
    string clientSecret?;
    string[] scopes?;
    RefreshConfig refreshConfig?;
    int clockSkewInSeconds = 0;
    boolean retryRequest = true;
    http:CredentialBearer credentialBearer = http:AUTH_HEADER_BEARER;
    http:ClientConfiguration clientConfig = {};
|};

# The data structure, which is used to configure the OAuth2 access token directly.
#
# + accessToken - Access token for the authorization endpoint
# + refreshConfig - Configurations for refreshing the access token
# + clockSkewInSeconds - Clock skew in seconds
# + retryRequest - Retry the request if the initial request returns a 401 response
# + credentialBearer - Bearer of the authentication credentials, which is sent to the authorization endpoint
public type DirectTokenConfig record {|
    string accessToken?;
    DirectTokenRefreshConfig refreshConfig?;
    int clockSkewInSeconds = 0;
    boolean retryRequest = true;
    http:CredentialBearer credentialBearer = http:AUTH_HEADER_BEARER;
|};

# The data structure, which can be used to pass the configurations for refreshing the access token of
# the password grant type.
#
# + refreshUrl - Refresh token URL for the refresh token server
# + scopes - Scope(s) of the access request
# + credentialBearer - Bearer of the authentication credentials, which is sent to the authorization endpoint
# + clientConfig - HTTP client configurations, which are used to call the authorization endpoint
public type RefreshConfig record {|
    string refreshUrl;
    string[] scopes?;
    http:CredentialBearer credentialBearer = http:AUTH_HEADER_BEARER;
    http:ClientConfiguration clientConfig = {};
|};

# The data structure, which can be used to pass the configurations for refreshing the access token directly.
#
# + refreshUrl - Refresh token URL for the refresh token server
# + refreshToken - Refresh token for the refresh token server
# + clientId - Client ID for authentication with the authorization endpoint
# + clientSecret - Client secret for authentication with the authorization endpoint
# + scopes - Scope(s) of the access request
# + credentialBearer - Bearer of authentication credentials, which is sent to the authorization endpoint
# + clientConfig - HTTP client configurations, which are used to call the authorization endpoint
public type DirectTokenRefreshConfig record {|
    string refreshUrl;
    string refreshToken;
    string clientId;
    string clientSecret;
    string[] scopes?;
    http:CredentialBearer credentialBearer = http:AUTH_HEADER_BEARER;
    http:ClientConfiguration clientConfig = {};
|};

# The data structure, which stores the values received from the authorization/token server to use them
# for the latter requests without requesting tokens again.
#
# + accessToken - Access token received from the authorization endpoint
# + refreshToken - Refresh token for the refresh token server
# + expTime - Expiry time (milliseconds since the Epoch) of the access token
public type OutboundOAuth2CacheEntry record {
    string accessToken;
    string refreshToken;
    int expTime;
};

# The data structure, which stores the values needed to prepare the HTTP request, which are to be sent to the
# authorization endpoint.
#
# + payload - Payload of the request
# + clientId - Client ID for the client credentials grant authentication
# + clientSecret - Client secret for the client credentials grant authentication
# + scopes - Scope(s) of the access request
# + credentialBearer - Bearer of the authentication credentials, which is sent to the authorization endpoint
type RequestConfig record {|
    string payload;
    string clientId?;
    string clientSecret?;
    string[]? scopes;
    http:CredentialBearer credentialBearer;
|};

# Generates the OAuth2 token.
#
# + authConfig - OAuth2 configurations
# + oauth2CacheEntry - OAuth2 cache entry
# + return - OAuth2 token or else an `oauth2:Error` if the validation failed
function generateOAuth2Token(GrantTypeConfig authConfig, @tainted OutboundOAuth2CacheEntry oauth2CacheEntry)
                             returns @tainted (string|Error) {
    if (authConfig is PasswordGrantConfig) {
        return getOAuth2TokenForPasswordGrant(authConfig, oauth2CacheEntry);
    } else if (authConfig is ClientCredentialsGrantConfig) {
        return getOAuth2TokenForClientCredentialsGrant(authConfig, oauth2CacheEntry);
    } else {
        return getOAuth2TokenForDirectTokenMode(authConfig, oauth2CacheEntry);
    }
}

# Processes the OAuth2 token at the inspection flow.
#
# + authConfig - OAuth2 configurations
# + oauth2CacheEntry - OAuth2 cache entry
# + return - OAuth2 token or else an `oauth2:Error` if the validation failed
function inspectAuthTokenForOAuth2(GrantTypeConfig authConfig, @tainted OutboundOAuth2CacheEntry oauth2CacheEntry)
                                   returns @tainted (string|Error) {
    if (authConfig is PasswordGrantConfig) {
        if (authConfig.retryRequest) {
            return getOAuth2TokenForPasswordGrant(authConfig, oauth2CacheEntry);
        }
    } else if (authConfig is ClientCredentialsGrantConfig) {
        if (authConfig.retryRequest) {
            return getOAuth2TokenForClientCredentialsGrant(authConfig, oauth2CacheEntry);
        }
    } else {
        if (authConfig.retryRequest) {
            authConfig.accessToken = "";
            return getOAuth2TokenForDirectTokenMode(authConfig, oauth2CacheEntry);
        }
    }
    return prepareError("Failed to get the access token since retry request is set as false.");
}

# Processes the OAuth2 token for the password grant type.
#
# + grantTypeConfig - Password grant type configurations
# + oauth2CacheEntry - OAuth2 cache entry
# + return - OAuth2 token or else an `oauth2:Error` occurred during the HTTP client invocation or validation
function getOAuth2TokenForPasswordGrant(PasswordGrantConfig grantTypeConfig,
                                        @tainted OutboundOAuth2CacheEntry oauth2CacheEntry)
                                        returns @tainted (string|Error) {
    string cachedAccessToken = oauth2CacheEntry.accessToken;
    if (cachedAccessToken == "") {
        string accessToken = check getAccessTokenFromAuthorizationRequest(grantTypeConfig, oauth2CacheEntry);
        log:printDebug(function () returns string {
            return "OAuth2 password grant type; Access token received from authorization request. Cache is empty.";
        });
        return accessToken;
    } else {
        if (isOAuth2CacheEntryValid(oauth2CacheEntry)) {
            log:printDebug(function () returns string {
                return "OAuth2 password grant type; Access token received from cache.";
            });
            return cachedAccessToken;
        } else {
            lock {
                if (isOAuth2CacheEntryValid(oauth2CacheEntry)) {
                    cachedAccessToken = oauth2CacheEntry.accessToken;
                    log:printDebug(function () returns string {
                        return "OAuth2 password grant type; Access token received from cache.";
                    });
                    return cachedAccessToken;
                } else {
                    string accessToken = check getAccessTokenFromRefreshRequest(grantTypeConfig, oauth2CacheEntry);
                    log:printDebug(function () returns string {
                        return "OAuth2 password grant type; Access token received from refresh request.";
                    });
                    return accessToken;
                }
            }
        }
    }
}

# Processes the OAuth2 token for the client credentials grant type.
#
# + grantTypeConfig - Client credentials grant type configurations
# + oauth2CacheEntry - OAuth2 cache entry
# + return - OAuth2 token or else an `oauth2:Error` occurred during the HTTP client invocation or validation
function getOAuth2TokenForClientCredentialsGrant(ClientCredentialsGrantConfig grantTypeConfig,
                                                 @tainted OutboundOAuth2CacheEntry oauth2CacheEntry)
                                                 returns @tainted (string|Error) {
    string cachedAccessToken = oauth2CacheEntry.accessToken;
    if (cachedAccessToken == "") {
        string accessToken = check getAccessTokenFromAuthorizationRequest(grantTypeConfig, oauth2CacheEntry);
        log:printDebug(function () returns string {
            return "OAuth2 client credentials grant type; Access token received from authorization request. Cache is empty.";
        });
        return accessToken;
    } else {
        if (isOAuth2CacheEntryValid(oauth2CacheEntry)) {
            log:printDebug(function () returns string {
                return "OAuth2 client credentials grant type; Access token received from cache.";
            });
            return cachedAccessToken;
        } else {
            lock {
                if (isOAuth2CacheEntryValid(oauth2CacheEntry)) {
                    cachedAccessToken = oauth2CacheEntry.accessToken;
                    log:printDebug(function () returns string {
                        return "OAuth2 client credentials grant type; Access token received from cache.";
                    });
                    return cachedAccessToken;
                } else {
                    string accessToken = check getAccessTokenFromAuthorizationRequest(grantTypeConfig, oauth2CacheEntry);
                    log:printDebug(function () returns string {
                        return "OAuth2 client credentials grant type; Access token received from authorization request.";
                    });
                    return accessToken;
                }
            }
        }
    }
}

# Processes the OAuth2 token for the direct token mode.
#
# + grantTypeConfig - Direct token mode configurations
# + oauth2CacheEntry - OAuth2 cache entry
# + return -OAuth2 token or else an `oauth2:Error` occurred during the HTTP client invocation or validation
function getOAuth2TokenForDirectTokenMode(DirectTokenConfig grantTypeConfig,
                                          @tainted OutboundOAuth2CacheEntry oauth2CacheEntry)
                                          returns @tainted (string|Error) {
    string cachedAccessToken = oauth2CacheEntry.accessToken;
    if (cachedAccessToken == "") {
        string? directAccessToken = grantTypeConfig?.accessToken;
        if (directAccessToken is string && directAccessToken != "") {
            log:printDebug(function () returns string {
                return "OAuth2 direct token mode; Access token received from user given request. Cache is empty.";
            });
            return directAccessToken;
        } else {
            string accessToken = check getAccessTokenFromRefreshRequest(grantTypeConfig, oauth2CacheEntry);
            log:printDebug(function () returns string {
                return "OAuth2 direct token mode; Access token received from refresh request. Cache is empty.";
            });
            return accessToken;
        }
    } else {
        if (isOAuth2CacheEntryValid(oauth2CacheEntry)) {
            log:printDebug(function () returns string {
                return "OAuth2 client credentials grant type; Access token received from cache.";
            });
            return cachedAccessToken;
        } else {
            lock {
                if (isOAuth2CacheEntryValid(oauth2CacheEntry)) {
                    cachedAccessToken = oauth2CacheEntry.accessToken;
                    log:printDebug(function () returns string {
                        return "OAuth2 client credentials grant type; Access token received from cache.";
                    });
                    return cachedAccessToken;
                } else {
                    string accessToken = check getAccessTokenFromRefreshRequest(grantTypeConfig, oauth2CacheEntry);
                    log:printDebug(function () returns string {
                        return "OAuth2 direct token mode; Access token received from refresh request.";
                    });
                    return accessToken;
                }
            }
        }
    }
}

# Checks the validity of the access token, which is in the cache. If the expiry time is 0, that means no expiry time is
# returned with the authorization request. This implies that the token is valid forever.
#
# + oauth2CacheEntry - OAuth2 cache entry
# + return - `true` if the access token is valid or else `false`
function isOAuth2CacheEntryValid(OutboundOAuth2CacheEntry oauth2CacheEntry) returns boolean {
    int expTime = oauth2CacheEntry.expTime;
    if (expTime == 0) {
        log:printDebug(function () returns string {
            return "Expiry time is 0, which means cached access token is always valid.";
        });
        return true;
    }
    int currentSystemTime = time:currentTime().time;
    if (currentSystemTime < expTime) {
        log:printDebug(function () returns string {
            return "Current time < expiry time, which means cached access token is valid.";
        });
        return true;
    }
    log:printDebug(function () returns string {
        return "Cached access token is invalid.";
    });
    return false;
}

# Requests an access token from the authorization endpoint using the provided configurations.
#
# + config - OAuth2 grant type configurations
# + oauth2CacheEntry - OAuth2 cache entry
# + return - Received OAuth2 access token or else an `oauth2:Error` occurred during the HTTP client invocation
function getAccessTokenFromAuthorizationRequest(ClientCredentialsGrantConfig|PasswordGrantConfig config,
                                                @tainted OutboundOAuth2CacheEntry oauth2CacheEntry)
                                                returns @tainted (string|Error) {
    RequestConfig requestConfig;
    int clockSkewInSeconds;
    string tokenUrl;
    http:ClientConfiguration clientConfig;

    if (config is ClientCredentialsGrantConfig) {
        if (config.clientId == "" || config.clientSecret == "") {
            return prepareError("Client id or client secret cannot be empty.");
        }
        tokenUrl = config.tokenUrl;
        requestConfig = {
            payload: "grant_type=client_credentials",
            clientId: config.clientId,
            clientSecret: config.clientSecret,
            scopes: config?.scopes,
            credentialBearer: config.credentialBearer
        };
        clockSkewInSeconds = config.clockSkewInSeconds;
        clientConfig = config.clientConfig;
    } else {
        tokenUrl = config.tokenUrl;
        string? clientId = config?.clientId;
        string? clientSecret = config?.clientSecret;
        if (clientId is string && clientSecret is string) {
            if (clientId == "" || clientSecret == "") {
                return prepareError("Client id or client secret cannot be empty.");
            }
            requestConfig = {
                payload: "grant_type=password&username=" + config.username + "&password=" + config.password,
                clientId: clientId,
                clientSecret: clientSecret,
                scopes: config?.scopes,
                credentialBearer: config.credentialBearer
            };
        } else {
            requestConfig = {
                payload: "grant_type=password&username=" + config.username + "&password=" + config.password,
                scopes: config?.scopes,
                credentialBearer: config.credentialBearer
            };
        }
        clockSkewInSeconds = config.clockSkewInSeconds;
        clientConfig = config.clientConfig;
    }

    http:Request authorizationRequest = check prepareRequest(requestConfig);
    return doRequest(tokenUrl, authorizationRequest, clientConfig, oauth2CacheEntry, clockSkewInSeconds);
}

# Requests an access token from the authorization endpoint using the provided refresh configurations.
#
# + config - Password grant type configuration or direct token configuration
# + oauth2CacheEntry - OAuth2 cache entry
# + return - Received access token or else an `oauth2:Error` occurred during the HTTP client invocation
function getAccessTokenFromRefreshRequest(PasswordGrantConfig|DirectTokenConfig config,
                                          @tainted OutboundOAuth2CacheEntry oauth2CacheEntry)
                                          returns @tainted (string|Error) {
    RequestConfig requestConfig;
    int clockSkewInSeconds;
    string refreshUrl;
    http:ClientConfiguration clientConfig;

    if (config is PasswordGrantConfig) {
        RefreshConfig? refreshConfig = config?.refreshConfig;
        if (refreshConfig is RefreshConfig) {
            string? clientId = config?.clientId;
            string? clientSecret = config?.clientSecret;
            if (clientId is string && clientSecret is string) {
                if (clientId == "" || clientSecret == "") {
                    return prepareError("Client id or client secret cannot be empty.");
                }
                refreshUrl = <@untainted> refreshConfig.refreshUrl;
                requestConfig = {
                    payload: "grant_type=refresh_token&refresh_token=" + oauth2CacheEntry.refreshToken,
                    clientId: clientId,
                    clientSecret: clientSecret,
                    scopes: refreshConfig?.scopes,
                    credentialBearer: refreshConfig.credentialBearer
                };
                clientConfig = refreshConfig.clientConfig;
            } else {
                return prepareError("Client id or client secret cannot be empty.");
            }
        } else {
            return prepareError("Failed to refresh access token since RefreshTokenConfig is not provided.");
        }
        clockSkewInSeconds = config.clockSkewInSeconds;
    } else {
        DirectTokenRefreshConfig? refreshConfig = config?.refreshConfig;
        if (refreshConfig is DirectTokenRefreshConfig) {
            if (refreshConfig.clientId == "" || refreshConfig.clientSecret == "") {
                return prepareError("Client id or client secret cannot be empty.");
            }
            refreshUrl = refreshConfig.refreshUrl;
            requestConfig = {
                payload: "grant_type=refresh_token&refresh_token=" + refreshConfig.refreshToken,
                clientId: refreshConfig.clientId,
                clientSecret: refreshConfig.clientSecret,
                scopes: refreshConfig?.scopes,
                credentialBearer: refreshConfig.credentialBearer
            };
            clientConfig = refreshConfig.clientConfig;
        } else {
            return prepareError("Failed to refresh access token since DirectRefreshTokenConfig is not provided.");
        }
        clockSkewInSeconds = config.clockSkewInSeconds;
    }

    http:Request refreshRequest = check prepareRequest(requestConfig);
    return doRequest(refreshUrl, refreshRequest, clientConfig, oauth2CacheEntry, clockSkewInSeconds);
}

# Executes the actual request and gets the access token from the authorization endpoint.
#
# + url - URL of the authorization endpoint
# + request - Prepared request to be sent to the authorization endpoint
# + clientConfig - HTTP client configurations, which are used to call the authorization endpoint
# + oauth2CacheEntry - OAuth2 cache entry
# + clockSkewInSeconds - Clock skew in seconds
# + return - Received OAuth2 access token or else an `oauth2:Error` occurred during the HTTP client invocation
function doRequest(string url, http:Request request, http:ClientConfiguration clientConfig,
                   @tainted OutboundOAuth2CacheEntry oauth2CacheEntry, int clockSkewInSeconds)
                   returns @tainted (string|Error) {
    http:Client clientEP = new(url, clientConfig);
    http:Response|http:ClientError response = clientEP->post("", request);
    if (response is http:Response) {
        log:printDebug(function () returns string {
            return "Request sent successfully to URL: " + url;
        });
        return extractAccessTokenFromResponse(response, oauth2CacheEntry, clockSkewInSeconds);
    } else {
        return prepareError("Failed to send request to URL: " + url, response);
    }
}

# Prepares the request to be sent to the authorization endpoint by adding the relevant headers and payloads.
#
# + config - The `oauth2:RequestConfig` record
# + return - Prepared HTTP request object or else an `oauth2:Error` occurred while preparing the request
function prepareRequest(RequestConfig config) returns http:Request|Error {
    http:Request req = new;
    string textPayload = config.payload;
    string scopeString = "";
    string[]? scopes = config.scopes;
    if (scopes is string[]) {
        foreach string requestScope in scopes {
            string trimmedRequestScope = requestScope.trim();
            if (trimmedRequestScope != "") {
                scopeString = scopeString + " " + trimmedRequestScope;
            }
        }
    }
    if (scopeString != "") {
        textPayload = textPayload + "&scope=" + scopeString.trim();
    }

    string? clientId = config?.clientId;
    string? clientSecret = config?.clientSecret;
    if (config.credentialBearer == http:AUTH_HEADER_BEARER) {
        if (clientId is string && clientSecret is string) {
            string clientIdSecret = clientId + ":" + clientSecret;
            req.addHeader(http:AUTH_HEADER, auth:AUTH_SCHEME_BASIC + clientIdSecret.toBytes().toBase64());
        } else {
            return prepareError("Client ID or client secret is not provided for client authentication.");
        }
    } else if (config.credentialBearer == http:POST_BODY_BEARER) {
        if (clientId is string && clientSecret is string) {
            textPayload = textPayload + "&client_id=" + clientId + "&client_secret=" + clientSecret;
        } else {
            return prepareError("Client ID or client secret is not provided for client authentication.");
        }
    }
    req.setTextPayload(<@untainted> textPayload, mime:APPLICATION_FORM_URLENCODED);
    return req;
}

# Extracts the access token from the JSON payload of a given HTTP response and updates the token cache.
#
# + response - HTTP response object
# + oauth2CacheEntry - OAuth2 cache entry
# + clockSkewInSeconds - Clock skew in seconds
# + return - Extracted access token or else an `oauth2:Error` occurred during the HTTP client invocation
function extractAccessTokenFromResponse(http:Response response, @tainted OutboundOAuth2CacheEntry oauth2CacheEntry,
                                        int clockSkewInSeconds) returns @tainted (string|Error) {
    if (response.statusCode == http:STATUS_OK) {
        json|http:ClientError payload = response.getJsonPayload();
        if (payload is json) {
            log:printDebug(function () returns string {
                return "Received an valid response. Extracting access token from the payload.";
            });
            updateOAuth2CacheEntry(payload, oauth2CacheEntry, clockSkewInSeconds);
            return payload.access_token.toString();
        } else {
            return prepareError("Failed to retrieve access token since the response payload is not a JSON.", payload);
        }
    } else {
        string|http:ClientError payload = response.getTextPayload();
        if (payload is string) {
            return prepareError("Received an invalid response with status-code: " + response.statusCode.toString() + "; and payload: " + payload);
        } else {
            return prepareError("Received an invalid response with status-code: " + response.statusCode.toString(), payload);
        }
    }
}

# Updates the OAuth2 token entry with the received JSON payload of the response.
#
# + responsePayload - Payload of the response
# + oauth2CacheEntry - OAuth2 cache entry
# + clockSkewInSeconds - Clock skew in seconds
function updateOAuth2CacheEntry(json responsePayload, OutboundOAuth2CacheEntry oauth2CacheEntry,
                                int clockSkewInSeconds) {
    int issueTime = time:currentTime().time;
    string accessToken = responsePayload.access_token.toString();
    oauth2CacheEntry.accessToken = accessToken;
    json|error expiresIn = responsePayload?.expires_in;
    if (expiresIn is int) {
        oauth2CacheEntry.expTime = issueTime + (expiresIn - clockSkewInSeconds) * 1000;
    }
    if (responsePayload.refresh_token is string) {
        string refreshToken = responsePayload.refresh_token.toString();
        oauth2CacheEntry.refreshToken = refreshToken;
    }
    log:printDebug(function () returns string {
        return "Updated token cache with the new parameters of the response.";
    });
    return ();
}

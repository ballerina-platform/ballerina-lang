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

import ballerina/crypto;
import ballerina/config;
import ballerina/http;
import ballerina/stringutils;

// Values that the grant_type parameter can hold.
const GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
const GRANT_TYPE_PASSWORD = "password";
const GRANT_TYPE_REFRESH_TOKEN = "refresh_token";

// The following constants are unique for the mock OAuth2 server.
const string USERNAME = "johndoe";
const string PASSWORD = "A3ddj3w";
const string CLIENT_ID = "3MVG9YDQS5WtC11paU2WcQjBB3L5w4gz52uriT8ksZ3nUVjKvrfQMrU4uvZohTftxStwNEW4cfStBEGRxRL68";
const string CLIENT_SECRET = "9205371918321623741";

// The bearer mode of the client ID and client secret.
const string HEADER_BEARER = "header";
const string BODY_BEARER = "body";
const string NO_BEARER = "none";

const string ACCESS_GRANTED = "access_granted";
const string ACCESS_DENIED = "access_denied";
const string INVALID_CLIENT = "invalid_client";
const string INVALID_REQUEST = "invalid_request";
const string INVALID_GRANT = "invalid_grant";
const string UNAUTHORIZED_CLIENT = "unauthorized_client";
const string AUTHORIZATION_HEADER_NOT_PROVIDED = "authorization_header_not_provided";

string refreshTokenString = CLIENT_ID + CLIENT_SECRET;
string refreshTokenHash = crypto:hashMd5(refreshTokenString.toBytes()).toBase64();

string[] accessTokenStore = ["2YotnFZFEjr1zCsicMWpAA"];

// The mock OAuth2 server, which is capable of issuing access tokens with related to the grant type and also of refreshing the
// already-issued access tokens. This keeps the set of issued access tokens for the validation purpose of the API  endpoint.
listener http:Listener oauth2Server = new(20299, {
        secureSocket: {
            keyStore: {
                path: config:getAsString("keystore"),
                password: "ballerina"
            }
        }
    });

service oauth2 on oauth2Server {

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/token/authorize/{bearer}"
    }
    // This issues an access token with reference to the received grant type.
    // For the `client_credentials` grant type, the new access token will have the MD5 hash of client_id + client_secret + scopes.
    // For password grant type, the new access token will have the MD5 hash of client_id + client_secret + username +
    // password + scopes.
    resource function authorize(http:Caller caller, http:Request req, string bearer) {
        http:Response res = new;
        // Get the authorization header, which should contain the base64-encoded `client_id` and `client_secret` with the colon
        // delimiter in most of the cases.
        if (bearer == HEADER_BEARER) {
            var authorizationHeader = trap req.getHeader("Authorization");
            if (authorizationHeader is string) {
                res = getResponseForHeaderBearerRequest(req, authorizationHeader, bearer);
            } else {
                // Invalid client. (Refer: https://tools.ietf.org/html/rfc6749#section-5.2)
                res.statusCode = http:STATUS_UNAUTHORIZED;
                res.setPayload(INVALID_CLIENT);
            }
        } else if (bearer == BODY_BEARER) {
            // If there is not an authorization header present with the base64-encoded `client_id` and `client_secret` with colon delimiter,
            // then, the `client_id` and `client_secret` should be in the text payload of the request.
            var payload = req.getTextPayload();
            if (payload is string) {
                res = getResponseForPostBodyBearerRequest(payload, bearer);
            } else {
                // Invalid client. (Refer: https://tools.ietf.org/html/rfc6749#section-5.2)
                res.statusCode = http:STATUS_UNAUTHORIZED;
                res.setPayload(INVALID_CLIENT);
            }
        } else if (bearer == NO_BEARER) {
            var payload = req.getTextPayload();
            if (payload is string) {
                res = getResponseForNoBearerRequest(payload, bearer);
            } else {
                // Invalid request. (Refer: https://tools.ietf.org/html/rfc6749#section-5.2)
                res.statusCode = http:STATUS_BAD_REQUEST;
                res.setPayload(INVALID_REQUEST);
            }
        }
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/token/refresh"
    }
    // This refreshes the access token but does not issue a new refresh token. The new access
    // token will have the MD5 hash of client_id + client_secret + refresh_token + scopes.
    resource function refresh(http:Caller caller, http:Request req) {
        http:Response res = new;
        var authorizationHeader = trap req.getHeader("Authorization");
        if (authorizationHeader is string) {
            res = getResponseForRefreshRequest(req, authorizationHeader);
        } else {
            // Invalid client. (Refer: https://tools.ietf.org/html/rfc6749#section-5.2)
            res.statusCode = http:STATUS_UNAUTHORIZED;
            res.setPayload(INVALID_CLIENT);
        }
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/token/introspect"
    }
    // This introspect the access token against the access token store, which holds the issued access tokens.
    resource function introspect(http:Caller caller, http:Request req) {
        http:Response res = new;
        var authorizationHeader = trap req.getHeader("Authorization");
        if (authorizationHeader is string) {
            res = getResponseForIntrospectRequest(req, authorizationHeader);
        } else {
            // Invalid client. (Refer: https://tools.ietf.org/html/rfc6749#section-5.2)
            res.statusCode = http:STATUS_UNAUTHORIZED;
            res.setPayload(INVALID_CLIENT);
        }
        checkpanic caller->respond(res);
    }
}

function getResponseForHeaderBearerRequest(http:Request req, string authorizationHeader, string bearer) returns http:Response {
    http:Response res = new;
    if (isAuthorizedClient(authorizationHeader)) {
        var payload = req.getTextPayload();
        if (payload is string) {
            string[] params = stringutils:split(payload, "&");
            string grantType = "";
            string scopes = "";
            string username = "";
            string password = "";
            foreach string param in params {
                if (stringutils:contains(param, "grant_type")) {
                    grantType = stringutils:split(param, "=")[1];
                } else if (stringutils:contains(param, "scope")) {
                    scopes = stringutils:split(param, "=")[1];
                } else if (stringutils:contains(param, "username")) {
                    username = stringutils:split(param, "=")[1];
                } else if (stringutils:contains(param, "password")) {
                    password = stringutils:split(param, "=")[1];
                }
            }
            res = prepareResponse(res, grantType, scopes, username, password, bearer);
        } else {
            // Invalid request. (Refer: https://tools.ietf.org/html/rfc6749#section-5.2)
            res.statusCode = http:STATUS_BAD_REQUEST;
            res.setPayload(INVALID_REQUEST);
        }
    } else {
        // Invalid client. (Refer: https://tools.ietf.org/html/rfc6749#section-5.2)
        res.statusCode = http:STATUS_UNAUTHORIZED;
        res.setPayload(INVALID_CLIENT);
    }
    return res;
}

function getResponseForPostBodyBearerRequest(string payload, string bearer) returns http:Response {
    http:Response res = new;
    if (stringutils:contains(payload, "client_id") && stringutils:contains(payload, "client_secret")) {
        string[] params = stringutils:split(payload, "&");
        string grantType = "";
        string scopes = "";
        string username = "";
        string password = "";
        string clientId = "";
        string clientSecret = "";
        foreach string param in params {
            if (stringutils:contains(param, "grant_type")) {
                grantType = stringutils:split(param, "=")[1];
            } else if (stringutils:contains(param, "scope")) {
                scopes = stringutils:split(param, "=")[1];
            } else if (stringutils:contains(param, "username")) {
                username = stringutils:split(param, "=")[1];
            } else if (stringutils:contains(param, "password")) {
                password = stringutils:split(param, "=")[1];
            } else if (stringutils:contains(param, "client_id")) {
                clientId = stringutils:split(param, "=")[1];
            } else if (stringutils:contains(param, "client_secret")) {
                clientSecret = stringutils:split(param, "=")[1];
            }
        }

        if (clientId == CLIENT_ID && clientSecret == CLIENT_SECRET) {
            res = prepareResponse(res, grantType, scopes, username, password, bearer);
        } else {
            // Invalid client. (Refer: https://tools.ietf.org/html/rfc6749#section-5.2)
            res.statusCode = http:STATUS_UNAUTHORIZED;
            res.setPayload(INVALID_CLIENT);
        }
    } else {
        // Invalid client. (Refer: https://tools.ietf.org/html/rfc6749#section-5.2)
        res.statusCode = http:STATUS_UNAUTHORIZED;
        res.setPayload(INVALID_CLIENT);
    }
    return res;
}

function getResponseForNoBearerRequest(string payload, string bearer) returns http:Response {
    http:Response res = new;
    string[] params = stringutils:split(payload, "&");
    string grantType = "";
    string scopes = "";
    string username = "";
    string password = "";
    foreach string param in params {
        if (stringutils:contains(param, "grant_type")) {
            grantType = stringutils:split(param, "=")[1];
        } else if (stringutils:contains(param, "scope")) {
            scopes = stringutils:split(param, "=")[1];
        } else if (stringutils:contains(param, "username")) {
            username = stringutils:split(param, "=")[1];
        } else if (stringutils:contains(param, "password")) {
            password = stringutils:split(param, "=")[1];
        }
    }
    res = prepareResponse(res, grantType, scopes, username, password, bearer);
    return res;
}

function getResponseForRefreshRequest(http:Request req, string authorizationHeader) returns http:Response {
    http:Response res = new;
    if (isAuthorizedClient(authorizationHeader)) {
        var payload = req.getTextPayload();
        if (payload is string) {
            string[] params = stringutils:split(payload, "&");
            string grantType = "";
            string refreshToken = "";
            string scopes = "";
            foreach string param in params {
                if (stringutils:contains(param, "grant_type")) {
                    grantType = stringutils:split(param, "=")[1];
                } else if (stringutils:contains(param, "refresh_token")) {
                    refreshToken = stringutils:split(param, "=")[1];
                    // If the refresh token contains the `=` symbol, then it is required to concatenate all the parts of the value since
                    // the String split breaks all those into separate parts.
                    if (param.endsWith("==")) {
                        refreshToken += "==";
                    }
                } else if (stringutils:contains(param, "scope")) {
                    scopes = stringutils:split(param, "=")[1];
                }
            }

            if (grantType == GRANT_TYPE_REFRESH_TOKEN) {
                if (refreshToken == refreshTokenHash) {
                    string input = CLIENT_ID + CLIENT_SECRET + refreshToken + scopes;
                    string accessToken = crypto:hashMd5(input.toBytes()).toBase64();
                    addToAccessTokenStore(accessToken);
                    json response = {
                        "access_token": accessToken,
                        "token_type": "example",
                        "expires_in": 3600,
                        "example_parameter": "example_value"
                    };
                    res.setPayload(response);
                } else {
                    // Invalid `grant_type`. (Refer: https://tools.ietf.org/html/rfc6749#section-5.2)
                    res.statusCode = http:STATUS_BAD_REQUEST;
                    res.setPayload(INVALID_GRANT);
                }
            } else {
                // Invalid `grant_type`. (Refer: https://tools.ietf.org/html/rfc6749#section-5.2)
                res.statusCode = http:STATUS_BAD_REQUEST;
                res.setPayload(INVALID_GRANT);
            }
        } else {
            // Invalid request. (Refer: https://tools.ietf.org/html/rfc6749#section-5.2)
            res.statusCode = http:STATUS_BAD_REQUEST;
            res.setPayload(INVALID_REQUEST);
        }
    } else {
        // Invalid client. (Refer: https://tools.ietf.org/html/rfc6749#section-5.2)
        res.statusCode = http:STATUS_UNAUTHORIZED;
        res.setPayload(INVALID_CLIENT);
    }
    return res;
}

function getResponseForIntrospectRequest(http:Request req, string authorizationHeader) returns http:Response {
    http:Response res = new;
    if (isAuthorizedClient(authorizationHeader)) {
        var payload = req.getTextPayload();
        if (payload is string) {
            string[] params = stringutils:split(payload, "&");
            string token = "";
            string tokenTypeHint = "";
            foreach string param in params {
                if (stringutils:contains(param, "token")) {
                    token = stringutils:split(param, "=")[1];
                } else if (stringutils:contains(param, "token_type_hint")) {
                    tokenTypeHint = stringutils:split(param, "=")[1];
                }
            }

            boolean tokenAvailable = false;
            foreach string accessToken in accessTokenStore {
                if (accessToken == token) {
                    tokenAvailable = true;
                }
            }
            if (tokenAvailable) {
                json responsePayload = { "active": true };
                res.setPayload(responsePayload);
            } else {
                json responsePayload = { "active": false };
                res.setPayload(responsePayload);
            }
        } else {
            // Invalid request. (Refer: https://tools.ietf.org/html/rfc6749#section-5.2)
            res.statusCode = http:STATUS_BAD_REQUEST;
            res.setPayload(INVALID_REQUEST);
        }
    } else {
        // Invalid client. (Refer: https://tools.ietf.org/html/rfc6749#section-5.2)
        res.statusCode = http:STATUS_UNAUTHORIZED;
        res.setPayload(INVALID_CLIENT);
    }
    return res;
}

function prepareResponse(http:Response res, string grantType, string scopes, string username, string password,
                         string bearer) returns http:Response {
    if (grantType == GRANT_TYPE_CLIENT_CREDENTIALS) {
        string input = CLIENT_ID + CLIENT_SECRET + scopes;
        string accessToken =crypto:hashMd5(input.toBytes()).toBase64();
        addToAccessTokenStore(accessToken);
        json response = {
            "access_token": accessToken,
            "token_type": "example",
            "expires_in": 3600,
            "example_parameter": "example_value"
        };
        res.setPayload(response);
    } else if (grantType == GRANT_TYPE_PASSWORD) {
        if (username == USERNAME && password == PASSWORD) {
            if (bearer == NO_BEARER) {
                string input = username + password + scopes;
                string accessToken = crypto:hashMd5(input.toBytes()).toBase64();
                addToAccessTokenStore(accessToken);
                json response = {
                    "access_token": accessToken,
                    "token_type": "example",
                    "expires_in": 3600,
                    "example_parameter": "example_value"
                };
                res.setPayload(response);
            } else {
                string input = CLIENT_ID + CLIENT_SECRET + username + password + scopes;
                string accessToken = crypto:hashMd5(input.toBytes()).toBase64();
                addToAccessTokenStore(accessToken);
                json response = {
                    "access_token": accessToken,
                    "token_type": "example",
                    "expires_in": 3600,
                    "refresh_token": refreshTokenHash,
                    "example_parameter": "example_value"
                };
                res.setPayload(response);
            }
        } else {
            // Invalid client. (Refer: https://tools.ietf.org/html/rfc6749#section-5.2)
            res.statusCode = http:STATUS_UNAUTHORIZED;
            res.setPayload(UNAUTHORIZED_CLIENT);
        }
    } else {
        // Invalid `grant_type`. (Refer: https://tools.ietf.org/html/rfc6749#section-5.2)
        res.statusCode = http:STATUS_BAD_REQUEST;
        res.setPayload(INVALID_GRANT);
    }
    return res;
}

function isAuthorizedClient(string authorizationHeader) returns boolean {
    string clientIdSecret = CLIENT_ID + ":" + CLIENT_SECRET;
    string expectedAuthorizationHeader = "Basic " + clientIdSecret.toBytes().toBase64();
    return authorizationHeader == expectedAuthorizationHeader;
}

function addToAccessTokenStore(string accessToken) {
    int index = accessTokenStore.length();
    accessTokenStore[index] = accessToken;
}

// ---------------------------------------------------------------------------------------------------------------------

//The API endpoint, which is responsible for processing the request after validating the access token in the authorization header.
// The token should be listed in the accessTokenStore, which keeps the tokens issued by the mock OAuth2 server.
listener http:Listener apiEndpoint = new(20298, {
        secureSocket: {
            keyStore: {
                path: config:getAsString("keystore"),
                password: "ballerina"
            }
        }
    });

service foo on apiEndpoint {

    resource function bar(http:Caller caller, http:Request req) {
        http:Response res = new;
        var authorizationHeader = trap req.getHeader("Authorization");
        if (authorizationHeader is string) {
            string accessToken = stringutils:split(authorizationHeader, " ")[1];
            boolean tokenAvailable = false;
            foreach string token in accessTokenStore {
                if (token == accessToken) {
                    tokenAvailable = true;
                }
            }
            if (tokenAvailable) {
                res.setPayload(ACCESS_GRANTED);
                checkpanic caller->respond(res);
            } else {
                res.statusCode = http:STATUS_UNAUTHORIZED;
                res.setPayload(ACCESS_DENIED);
                checkpanic caller->respond(res);
            }
        } else {
            res.statusCode = http:STATUS_UNAUTHORIZED;
            res.setPayload(AUTHORIZATION_HEADER_NOT_PROVIDED);
            checkpanic caller->respond(res);
        }
    }
}

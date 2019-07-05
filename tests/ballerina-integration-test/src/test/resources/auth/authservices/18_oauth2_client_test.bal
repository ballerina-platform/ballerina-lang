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

import ballerina/http;
import ballerina/io;
import ballerina/oauth2;

// Test the client credentials grant type with valid credentials
oauth2:OutboundOAuth2Provider oauth2Provider1 = new({
    tokenUrl: "https://localhost:9196/oauth2/token/authorize/header",
    clientId: "3MVG9YDQS5WtC11paU2WcQjBB3L5w4gz52uriT8ksZ3nUVjKvrfQMrU4uvZohTftxStwNEW4cfStBEGRxRL68",
    clientSecret: "9205371918321623741",
    scopes: ["token-scope1", "token-scope2"]
});
http:BearerAuthHandler oauth2Handler1 = new(oauth2Provider1);
http:Client clientEP1 = new("https://localhost:9195", config = {
    auth: {
        authHandler: oauth2Handler1
    }
});

// Test the client credentials grant type with invalid client credentials
oauth2:OutboundOAuth2Provider oauth2Provider2 = new({
    tokenUrl: "https://localhost:9196/oauth2/token/authorize/header",
    clientId: "invalid_client_id",
    clientSecret: "invalid_client_secret",
    scopes: ["token-scope1", "token-scope2"]
});
http:BearerAuthHandler oauth2Handler2 = new(oauth2Provider2);
http:Client clientEP2 = new("https://localhost:9195", config = {
    auth: {
        authHandler: oauth2Handler2
    }
});

// Test the client credentials grant type with a post-body bearer and valid credentials
oauth2:OutboundOAuth2Provider oauth2Provider3 = new({
    credentialBearer: http:POST_BODY_BEARER,
    tokenUrl: "https://localhost:9196/oauth2/token/authorize/body",
    clientId: "3MVG9YDQS5WtC11paU2WcQjBB3L5w4gz52uriT8ksZ3nUVjKvrfQMrU4uvZohTftxStwNEW4cfStBEGRxRL68",
    clientSecret: "9205371918321623741",
    scopes: ["token-scope1", "token-scope2"]
});
http:BearerAuthHandler oauth2Handler3 = new(oauth2Provider3);
http:Client clientEP3 = new("https://localhost:9195", config = {
    auth: {
        authHandler: oauth2Handler3
    }
});

// Test the client credentials grant type with a post-body bearer and invalid credentials
oauth2:OutboundOAuth2Provider oauth2Provider4 = new({
    credentialBearer: http:POST_BODY_BEARER,
    tokenUrl: "https://localhost:9196/oauth2/token/authorize/body",
    clientId: "invalid_client_id",
    clientSecret: "invalid_client_secret",
    scopes: ["token-scope1", "token-scope2"]
});
http:BearerAuthHandler oauth2Handler4 = new(oauth2Provider4);
http:Client clientEP4 = new("https://localhost:9195", config = {
    auth: {
        authHandler: oauth2Handler4
    }
});

// Test the password grant type with valid credentials
oauth2:OutboundOAuth2Provider oauth2Provider5 = new({
    tokenUrl: "https://localhost:9196/oauth2/token/authorize/header",
    username: "johndoe",
    password: "A3ddj3w",
    clientId: "3MVG9YDQS5WtC11paU2WcQjBB3L5w4gz52uriT8ksZ3nUVjKvrfQMrU4uvZohTftxStwNEW4cfStBEGRxRL68",
    clientSecret: "9205371918321623741",
    scopes: ["token-scope1", "token-scope2"]
});
http:BearerAuthHandler oauth2Handler5 = new(oauth2Provider5);
http:Client clientEP5 = new("https://localhost:9195", config = {
    auth: {
        authHandler: oauth2Handler5
    }
});

// Test the password grant type with valid credentials and a valid refresh config
oauth2:OutboundOAuth2Provider oauth2Provider6 = new({
    tokenUrl: "https://localhost:9196/oauth2/token/authorize/header",
    username: "johndoe",
    password: "A3ddj3w",
    clientId: "3MVG9YDQS5WtC11paU2WcQjBB3L5w4gz52uriT8ksZ3nUVjKvrfQMrU4uvZohTftxStwNEW4cfStBEGRxRL68",
    clientSecret: "9205371918321623741",
    scopes: ["token-scope1", "token-scope2"],
    refreshConfig: {
        refreshUrl: "https://localhost:9196/oauth2/token/refresh",
        scopes: ["token-scope1", "token-scope2"]
    }
});
http:BearerAuthHandler oauth2Handler6 = new(oauth2Provider6);
http:Client clientEP6 = new("https://localhost:9195", config = {
    auth: {
        authHandler: oauth2Handler6
    }
});

// Test the password grant type with an invalid username, password, and a valid refresh config
oauth2:OutboundOAuth2Provider oauth2Provider7 = new({
    tokenUrl: "https://localhost:9196/oauth2/token/authorize/header",
    username: "invalid_username",
    password: "invalid_password",
    clientId: "3MVG9YDQS5WtC11paU2WcQjBB3L5w4gz52uriT8ksZ3nUVjKvrfQMrU4uvZohTftxStwNEW4cfStBEGRxRL68",
    clientSecret: "9205371918321623741",
    scopes: ["token-scope1", "token-scope2"],
    refreshConfig: {
        refreshUrl: "https://localhost:9196/oauth2/token/refresh",
        scopes: ["token-scope1", "token-scope2"]
    }
});
http:BearerAuthHandler oauth2Handler7 = new(oauth2Provider7);
http:Client clientEP7 = new("https://localhost:9195", config = {
    auth: {
        authHandler: oauth2Handler7
    }
});

// Test the password grant type with a bearer without credentials and a valid username and password
oauth2:OutboundOAuth2Provider oauth2Provider8 = new({
    credentialBearer: http:NO_BEARER,
    tokenUrl: "https://localhost:9196/oauth2/token/authorize/none",
    username: "johndoe",
    password: "A3ddj3w",
    scopes: ["token-scope1", "token-scope2"]
});
http:BearerAuthHandler oauth2Handler8 = new(oauth2Provider8);
http:Client clientEP8 = new("https://localhost:9195", config = {
    auth: {
        authHandler: oauth2Handler8
    }
});

// Test the direct token mode with valid credentials and without a refresh config
oauth2:OutboundOAuth2Provider oauth2Provider9 = new({
    accessToken: "2YotnFZFEjr1zCsicMWpAA"
});
http:BearerAuthHandler oauth2Handler9 = new(oauth2Provider9);
http:Client clientEP9 = new("https://localhost:9195", config = {
    auth: {
        authHandler: oauth2Handler9
    }
});

// Test the direct token mode with an invalid access token and without a refresh config
oauth2:OutboundOAuth2Provider oauth2Provider10 = new({
    accessToken: "invalid_access_token"
});
http:BearerAuthHandler oauth2Handler10 = new(oauth2Provider10);
http:Client clientEP10 = new("https://localhost:9195", config = {
    auth: {
        authHandler: oauth2Handler10
    }
});

// Test the direct token mode with an invalid access token and a valid refresh config
oauth2:OutboundOAuth2Provider oauth2Provider11 = new({
    accessToken: "invalid_access_token",
    refreshConfig: {
        refreshUrl: "https://localhost:9196/oauth2/token/refresh",
        refreshToken: "XlfBs91yquexJqDaKEMzVg==",
        clientId: "3MVG9YDQS5WtC11paU2WcQjBB3L5w4gz52uriT8ksZ3nUVjKvrfQMrU4uvZohTftxStwNEW4cfStBEGRxRL68",
        clientSecret: "9205371918321623741",
        scopes: ["token-scope1", "token-scope2"]
    }
});
http:BearerAuthHandler oauth2Handler11 = new(oauth2Provider11);
http:Client clientEP11 = new("https://localhost:9195", config = {
    auth: {
        authHandler: oauth2Handler11
    }
});

// Test the direct token mode (with the retrying request set as false) with an invalid access token and without a refresh config
oauth2:OutboundOAuth2Provider oauth2Provider12 = new({
    accessToken: "invalid_access_token",
    retryRequest: false
});
http:BearerAuthHandler oauth2Handler12 = new(oauth2Provider12);
http:Client clientEP12 = new("https://localhost:9195", config = {
    auth: {
        authHandler: oauth2Handler12
    }
});

// Test the direct token mode (with the retrying request set as false) with an invalid access token and a valid refresh config
oauth2:OutboundOAuth2Provider oauth2Provider13 = new({
    accessToken: "invalid_access_token",
    retryRequest: false,
    refreshConfig: {
        refreshUrl: "https://localhost:9196/oauth2/token/refresh",
        refreshToken: "XlfBs91yquexJqDaKEMzVg==",
        clientId: "3MVG9YDQS5WtC11paU2WcQjBB3L5w4gz52uriT8ksZ3nUVjKvrfQMrU4uvZohTftxStwNEW4cfStBEGRxRL68",
        clientSecret: "9205371918321623741",
        scopes: ["token-scope1", "token-scope2"]
    }
});
http:BearerAuthHandler oauth2Handler13 = new(oauth2Provider13);
http:Client clientEP13 = new("https://localhost:9195", config = {
    auth: {
        authHandler: oauth2Handler13
    }
});

// Test the direct token mode with an invalid access token and an invalid refresh config
oauth2:OutboundOAuth2Provider oauth2Provider14 = new({
    accessToken: "invalid_access_token",
    refreshConfig: {
        refreshUrl: "https://localhost:9196/oauth2/token/refresh",
        refreshToken: "invalid_refresh_token",
        clientId: "3MVG9YDQS5WtC11paU2WcQjBB3L5w4gz52uriT8ksZ3nUVjKvrfQMrU4uvZohTftxStwNEW4cfStBEGRxRL68",
        clientSecret: "9205371918321623741",
        scopes: ["token-scope1", "token-scope2"]
    }
});
http:BearerAuthHandler oauth2Handler14 = new(oauth2Provider14);
http:Client clientEP14 = new("https://localhost:9195", config = {
    auth: {
        authHandler: oauth2Handler14
    }
});

listener http:Listener listener18 = new(9190, config = {
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});

@http:ServiceConfig {
    basePath: "/echo"
}
service echo18 on listener18 {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/oauth2/{testCase}"
    }
    resource function test(http:Caller caller, http:Request req, string testCase) {
        http:Response|error resp = new;
        http:Request request = new;
        if (testCase == "CLIENT_CREDENTIALS_GRANT_TYPE_WITH_VALID_CREDENTIALS") {
            resp = clientEP1->post("/foo/bar", request);
        } else if (testCase == "CLIENT_CREDENTIALS_GRANT_TYPE_WITH_INVALID_CREDENTIALS") {
            resp = clientEP2->post("/foo/bar", request);
        } else if (testCase == "CLIENT_CREDENTIALS_GRANT_TYPE_WITH_POST_BODY_BEARER_AND_VALID_CREDENTIALS") {
            resp = clientEP3->post("/foo/bar", request);
        } else if (testCase == "CLIENT_CREDENTIALS_GRANT_TYPE_WITH_POST_BODY_BEARER_AND_INVALID_CREDENTIALS") {
            resp = clientEP4->post("/foo/bar", request);
        } else if (testCase == "PASSWORD_GRANT_TYPE_WITH_VALID_CREDENTIALS") {
            resp = clientEP5->post("/foo/bar", request);
        } else if (testCase == "PASSWORD_GRANT_TYPE_WITH_VALID_CREDENTIALS_AND_VALID_REFRESH_CONFIG") {
            resp = clientEP6->post("/foo/bar", request);
        } else if (testCase == "PASSWORD_GRANT_TYPE_WITH_INVALID_CREDENTIALS_AND_VALID_REFRESH_CONFIG") {
            resp = clientEP7->post("/foo/bar", request);
        } else if (testCase == "PASSWORD_GRANT_TYPE_WITH_NO_BEARER_AND_VALID_CREDENTIALS") {
            resp = clientEP8->post("/foo/bar", request);
        }else if (testCase == "DIRECT_TOKEN_WITH_VALID_CREDENTIALS_AND_NO_REFRESH_CONFIG") {
            resp = clientEP9->post("/foo/bar", request);
        } else if (testCase == "DIRECT_TOKEN_WITH_INVALID_CREDENTIALS_AND_NO_REFRESH_CONFIG") {
            resp = clientEP10->post("/foo/bar", request);
        } else if (testCase == "DIRECT_TOKEN_WITH_INVALID_CREDENTIALS_AND_VALID_REFRESH_CONFIG") {
            resp = clientEP11->post("/foo/bar", request);
        } else if (testCase == "DIRECT_TOKEN_WITH_INVALID_CREDENTIALS_AND_NO_REFRESH_CONFIG_BUT_RETRY_REQUEST_FALSE") {
            resp = clientEP12->post("/foo/bar", request);
        } else if (testCase == "DIRECT_TOKEN_WITH_INVALID_CREDENTIALS_AND_VALID_REFRESH_CONFIG_BUT_RETRY_REQUEST_FALSE") {
            resp = clientEP13->post("/foo/bar", request);
        } else if (testCase == "DIRECT_TOKEN_WITH_INVALID_CREDENTIALS_AND_INVALID_REFRESH_CONFIG") {
            resp = clientEP14->post("/foo/bar", request);
        }

        if (resp is http:Response) {
            checkpanic caller->respond(resp);
        } else {
            http:Response res = new;
            res.statusCode = http:INTERNAL_SERVER_ERROR_500;
            json errMsg = { message: <string>resp.detail().message };
            res.setPayload(errMsg);
            checkpanic caller->respond(res);
        }
    }
}

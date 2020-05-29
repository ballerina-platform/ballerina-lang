// Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/config;
import ballerina/crypto;
import ballerina/http;

OutboundCustomAuthProvider outboundCustomAuthProvider = new;
OutboundCustomAuthHandler outboundCustomAuthHandler = new(outboundCustomAuthProvider);

http:Client client06 = new("https://localhost:20008", {
    auth: {
        authHandler: outboundCustomAuthHandler
    },
    secureSocket: {
       trustStore: {
           path: config:getAsString("truststore"),
           password: "ballerina"
       }
    }
});

listener http:Listener listener06_1 = new(20007, {
    secureSocket: {
        keyStore: {
            path: config:getAsString("keystore"),
            password: "ballerina"
        }
    }
});

@http:ServiceConfig {
    basePath: "/echo"
}
service passthrough on listener06_1 {

    @http:ResourceConfig {
        path: "/{testCase}"
    }
    resource function test(http:Caller caller, http:Request req, string testCase) {
        var response = client06->get("/echo/" + <@untainted> testCase);
        if (response is http:Response) {
            checkpanic caller->respond(response);
        } else {
            http:Response resp = new;
            json errMsg = { "error": "error occurred while invoking the service: " + <string>response.detail()?.message };
            resp.statusCode = 500;
            resp.setPayload(errMsg);
            checkpanic caller->respond(resp);
        }
    }
}

InboundCustomAuthProvider inboundCustomAuthProvider = new;
InboundCustomAuthHandler inboundCustomAuthHandler = new(inboundCustomAuthProvider);

listener http:Listener listener06_2 = new(20008, {
    auth: {
        authHandlers: [inboundCustomAuthHandler]
    },
    secureSocket: {
        keyStore: {
            path: config:getAsString("keystore"),
            password: "ballerina"
        }
    }
});

@http:ServiceConfig {
    basePath: "/echo"
}
service echo06 on listener06_2 {

    @http:ResourceConfig {
        auth: {
            scopes: ["plumless"]
        }
    }
    resource function test1(http:Caller caller, http:Request req) {
        checkpanic caller->respond("Hello Ballerina!");
    }

    @http:ResourceConfig {
        auth: {
            scopes: ["buckeroo"]
        }
    }
    resource function test2(http:Caller caller, http:Request req) {
        checkpanic caller->respond("Hello Ballerina!");
    }

    @http:ResourceConfig {
        auth: {
            scopes: ["oxueekz"]
        }
    }
    resource function test3(http:Caller caller, http:Request req) {
        checkpanic caller->respond("Hello Ballerina!");
    }
}


// ---------- inbound_custom_auth_handler ----------

public type InboundCustomAuthHandler object {

    *http:InboundAuthHandler;

    public auth:InboundAuthProvider authProvider;

    public function __init(auth:InboundAuthProvider authProvider) {
        self.authProvider = authProvider;
    }

    public function process(http:Request req) returns boolean|http:AuthenticationError {
        var customAuthHeader = req.getHeader(http:AUTH_HEADER);
        string credential = customAuthHeader.substring(6, customAuthHeader.length()).trim();
        var authenticationResult = self.authProvider.authenticate(credential);
        if (authenticationResult is boolean) {
            return authenticationResult;
        } else {
            string message = "Failed to authenticate with custom auth hanndler.";
            http:AuthenticationError authError = error(http:AUTHN_FAILED, message = message, cause = authenticationResult);
            return authError;
        }
    }

    public function canProcess(http:Request req) returns @tainted boolean {
        var customAuthHeader = req.getHeader(http:AUTH_HEADER);
        return customAuthHeader.startsWith("Custom");
    }
};

// ---------- inbound_custom_auth_provider ----------

public type InboundCustomAuthProvider object {

    *auth:InboundAuthProvider;

    public function authenticate(string credential) returns boolean|auth:Error {
        string token = "4ddb0c25";
        boolean authenticated = crypto:crc32b(credential.toBytes()) == token;
        if (authenticated) {
            auth:setPrincipal(token, token, [credential]);
        }
        return authenticated;
    }
};

// ---------- outbound_custom_auth_handler ----------

public type OutboundCustomAuthHandler object {

    *http:OutboundAuthHandler;

    public auth:OutboundAuthProvider authProvider;

    public function __init(auth:OutboundAuthProvider authProvider) {
        self.authProvider = authProvider;
    }

    public function prepare(http:Request req) returns http:Request|http:AuthenticationError {
        string|auth:Error token = self.authProvider.generateToken();
        if (token is string) {
            req.setHeader(http:AUTH_HEADER, "Custom " + token);
            return req;
        } else {
            return http:AuthenticationError(message = token.reason(), cause = token);
        }
    }

    public function inspect(http:Request req, http:Response resp) returns http:Request|http:AuthenticationError? {
        map<anydata> headerMap = { "STATUS_CODE": resp.statusCode };
        string[] headerNames = <@untainted> resp.getHeaderNames();
        foreach string header in headerNames {
            string[] headerValues = resp.getHeaders(header);
            headerMap[header] = headerValues;
        }
        string|auth:Error? token = self.authProvider.inspect(headerMap);
        if (token is string) {
            req.setHeader(http:AUTH_HEADER, "Custom " + token);
            return req;
        } else if (token is auth:Error) {
            return http:AuthenticationError(message = token.reason(), cause = token);
        }
    }
};

// ---------- outbound_custom_auth_provider ----------

public type OutboundCustomAuthProvider object {

    *auth:OutboundAuthProvider;

    public function generateToken() returns string|auth:Error {
        string token = "plumless";
        return token;
    }

    public function inspect(map<anydata> data) returns string|auth:Error? {
        if (data[http:STATUS_CODE] == http:STATUS_FORBIDDEN) {
            string token = "buckeroo";
            return token;
        }
    }
};

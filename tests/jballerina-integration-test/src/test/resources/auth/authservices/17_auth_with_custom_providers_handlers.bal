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
import ballerina/encoding;
import ballerina/http;
import ballerina/runtime;

auth:OutboundCustomAuthProvider outboundCustomAuthProvider = new;
http:OutboundCustomAuthHandler outboundCustomAuthHandler = new(outboundCustomAuthProvider);

http:Client client17 = new("https://localhost:9114", config = {
    auth: {
        authHandler: outboundCustomAuthHandler
    }
});

listener http:Listener listener17_1 = new(9113, config = {
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
service passthrough on listener17_1 {

    @http:ResourceConfig {
        path: "/{testCase}"
    }
    resource function test(http:Caller caller, http:Request req, string testCase) {
        var response = client17->get("/echo/" + testCase);
        if (response is http:Response) {
            checkpanic caller->respond(response);
        } else {
            http:Response resp = new;
            json errMsg = { "error": "error occurred while invoking the service: " + response.reason() };
            resp.statusCode = 500;
            resp.setPayload(errMsg);
            checkpanic caller->respond(resp);
        }
    }
}

InboundCustomAuthProvider inboundCustomAuthProvider = new;
InboundCustomAuthHandler inboundCustomAuthHandler = new(inboundCustomAuthProvider);

listener http:Listener listener17_2 = new(9114, config = {
    auth: {
        authHandlers: [inboundCustomAuthHandler]
    },
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
service echo17 on listener17 {

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

    public function handle(http:Request req) returns boolean|error {
        var customAuthHeader = req.getHeader(http:AUTH_HEADER);
        string credential = customAuthHeader.substring(6, customAuthHeader.length()).trim();
        return self.authProvider.authenticate(credential);
    }

    public function canHandle(http:Request req) returns boolean {
        var customAuthHeader = req.getHeader(http:AUTH_HEADER);
        return customAuthHeader.hasPrefix("Custom");
    }
};

// ---------- inbound_custom_auth_provider ----------

public type InboundCustomAuthProvider object {

    *auth:InboundAuthProvider;

    public function authenticate(string credential) returns boolean|error {
        string token = "4ddb0c25";
        boolean authenticated = crypto:crc32b(credential) == token;
        if (authenticated) {
            runtime:Principal principal = runtime:getInvocationContext().principal;
            principal.userId = token;
            principal.username = token;
            principal.scopes = [credential];
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

    public function prepare(http:Request req) returns Request|error {
        string token = check authProvider.generateToken();
        req.setHeader(http:AUTH_HEADER, auth:AUTH_SCHEME_BEARER + token);
        return req;
    }

    public function inspect(http:Request req, http:Response resp) returns Request|error? {
        map<anydata> headerMap = { http:STATUS_CODE: resp.statusCode };
        string[] headerNames = resp.getHeaderNames();
        foreach string header in headerNames {
            string[] headerValues = resp.getHeaders(untaint header);
            headerMap[header] = headerValues;
        }
        string? token = check authProvider.inspect(headerMap);
        if (token is string) {
            req.setHeader(http:AUTH_HEADER, auth:AUTH_SCHEME_BEARER + token);
            return req;
        }
        return ();
    }
};

// ---------- outbound_custom_auth_provider ----------

public type OutboundCustomAuthProvider object {

    *auth:OutboundAuthProvider;

    public function generateToken() returns string|error {
        string token = "plumless";
        return crypto:crc32b(token);
    }

    public function inspect(map<anydata> data) returns string|error? {
        if (data[http:STATUS_CODE] == http:FORBIDDEN_403) {
            string token = "buckeroo";
            return crypto:crc32b(token);
        }
    }
};

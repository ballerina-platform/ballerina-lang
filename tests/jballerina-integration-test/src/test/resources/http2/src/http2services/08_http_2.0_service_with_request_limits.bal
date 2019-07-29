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

import ballerina/http;
import ballerina/log;

listener http:Listener requestUriLimits = new(9102, { httpVersion: "2.0", requestLimits: { maxUriLength:15 } });
listener http:Listener requestHeaderLimits = new(9103, { httpVersion: "2.0", requestLimits: { maxHeaderSize:5 } });
listener http:Listener initServiceEp = new(9104, { httpVersion: "2.0" });


@http:ServiceConfig {
    basePath: "/uriLimit"
}
service helloWorld on requestUriLimits {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function reqUriLimitResource(http:Caller caller, http:Request req) {
        var result = caller->respond("Hello World!");
        if (result is error) {
            log:printError("Failed to respond", err = result);
        }
    }
}

@http:ServiceConfig {
    basePath: "/headerLimit"
}
service reqLimitService2 on requestHeaderLimits {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function reqHeaderLimitResource(http:Caller caller, http:Request req) {
        var result = caller->respond("Hello World!");
        if (result is error) {
            log:printError("Failed to respond", err = result);
        }
    }
}

@http:ServiceConfig {
    basePath: "/initial"
}
service initService on initServiceEp {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/uriTooLong"
    }
    resource function uriTooLongResource(http:Caller caller, http:Request req) {
        http:Client clientEP = new("http://localhost:9102", { httpVersion: "2.0" });
        var uriResponse = clientEP->get("/uriLimit");
        if (uriResponse is error) {
            log:printError("Error sending response", uriResponse);
        } else {
            var result = caller->respond(uriResponse.statusCode.toString());
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/entityHeaderTooLong"
    }
    resource function entityHeaderTooLongResource(http:Caller caller, http:Request req) {
        http:Client clientEP = new("http://localhost:9103", { httpVersion: "2.0" });
        http:Request request = new;
        request.setHeader("Header1", "header1");
        request.setHeader("Header2", "header2");
        var resp = clientEP->get("/headerLimit", request);
        if (resp is http:Response) {
            var result = caller->respond(resp.statusCode.toString());
        } else {
            log:printError(<string>resp.detail()["message"]);
        }
    }
}

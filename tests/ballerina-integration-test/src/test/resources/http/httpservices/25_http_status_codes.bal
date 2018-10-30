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

@http:ServiceConfig {
    basePath: "/code"
}
service<http:Service> differentStatusCodes bind { port: 9223 } {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/okWithBody"
    }
    sendOKWithBody(endpoint caller, http:Request req) {
        _ = caller->ok("OK Response");
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/okWithoutBody"
    }
    sendOKWithoutBody(endpoint caller, http:Request req) {
        _ = caller->ok(());
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/createdWithBody"
    }
    sendCreatedWithBody(endpoint caller, http:Request req) {
        _ = caller->created("/newResourceURI", message = "Created Response");
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/createdWithoutBody"
    }
    sendCreatedWithoutBody(endpoint caller, http:Request req) {
        _ = caller->created("/newResourceURI");
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/createdWithEmptyURI"
    }
    sendCreatedWithEmptyURI(endpoint caller, http:Request req) {
        _ = caller->created("");
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/acceptedWithBody"
    }
    sendAcceptedWithBody(endpoint caller, http:Request req) {
        _ = caller->accepted(message = { msg: "accepted response" });
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/acceptedWithoutBody"
    }
    sendAcceptedWithoutBody(endpoint caller, http:Request req) {
        _ = caller->accepted();
    }
}

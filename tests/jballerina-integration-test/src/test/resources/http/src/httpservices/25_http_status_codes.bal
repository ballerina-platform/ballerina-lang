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
service differentStatusCodes on new http:Listener(9223) {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/okWithBody"
    }
    resource function sendOKWithBody(http:Caller caller, http:Request req) {
       checkpanic caller->ok("OK Response");
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/okWithoutBody"
    }
    resource function sendOKWithoutBody(http:Caller caller, http:Request req) {
        checkpanic caller->ok();
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/createdWithBody"
    }
    resource function sendCreatedWithBody(http:Caller caller, http:Request req) {
        checkpanic caller->created("/newResourceURI", "Created Response");
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/createdWithoutBody"
    }
    resource function sendCreatedWithoutBody(http:Caller caller, http:Request req) {
        checkpanic caller->created("/newResourceURI");
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/createdWithEmptyURI"
    }
    resource function sendCreatedWithEmptyURI(http:Caller caller, http:Request req) {
        checkpanic caller->created("");
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/acceptedWithBody"
    }
    resource function sendAcceptedWithBody(http:Caller caller, http:Request req) {
        checkpanic caller->accepted({ msg: "accepted response" });
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/acceptedWithoutBody"
    }
    resource function sendAcceptedWithoutBody(http:Caller caller, http:Request req) {
        checkpanic caller->accepted();
    }
}

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

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/noContentWithBody"
    }
    resource function sendNoContentWithBody(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setHeader("x-custom-header", "custom-header-value");
        res.setPayload(xml `<test>No Content</test>`);
        checkpanic caller->noContent(res); //Body will be removed
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/noContentWithoutBody"
    }
    resource function sendNoContentWithoutBody(http:Caller caller, http:Request req) {
        checkpanic caller->noContent();
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/badRequestWithBody"
    }
    resource function sendBadRequestWithBody(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setPayload(xml `<test>Bad Request</test>`);
        checkpanic caller->badRequest(res);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/badRequestWithoutBody"
    }
    resource function sendBadRequestWithoutBody(http:Caller caller, http:Request req) {
        checkpanic caller->badRequest();
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/notFoundWithBody"
    }
    resource function sendNotFoundWithBody(http:Caller caller, http:Request req) {
        checkpanic caller->notFound(xml `<test>artifacts not found</test>`);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/notFoundWithoutBody"
    }
    resource function sendNotFoundWithoutBody(http:Caller caller, http:Request req) {
        checkpanic caller->notFound();
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/serverErrWithBody"
    }
    resource function sendServerErrWithBody(http:Caller caller, http:Request req) {
        checkpanic caller->internalServerError(xml `<test>Internal Server Error Occurred</test>`);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/serverErrWithoutBody"
    }
    resource function sendServerErrWithoutBody(http:Caller caller, http:Request req) {
        checkpanic caller->internalServerError();
    }
}

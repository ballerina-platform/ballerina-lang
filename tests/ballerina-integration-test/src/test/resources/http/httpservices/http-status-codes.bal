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
    basePath:"/code"
}
service<http:Service> differentStatusCodes bind {port : 9223} {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/okWithBody"
    }
    sendOKWithBody (endpoint caller, http:Request req) {
        _ = caller->ok(message = "OK Response");
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/okWithoutBody"
    }
    sendOKWithoutBody (endpoint caller, http:Request req) {
        _ = caller->ok();
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/createdWithBody"
    }
    sendCreatedWithBody (endpoint caller, http:Request req) {
        _ = caller -> created(message = "Created Response");
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/createdWithoutBody"
    }
    sendCreatedWithoutBody (endpoint caller, http:Request req) {
        _ = caller -> created();
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/acceptedWithBody"
    }
    sendAcceptedWithBody (endpoint caller, http:Request req) {
        _ = caller -> accepted(message = {msg : "accepted response"});
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/acceptedWithoutBody"
    }
    sendAcceptedWithoutBody (endpoint caller, http:Request req) {
        _ = caller -> accepted();
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/noContentWithBody"
    }
    sendNoContentWithBody (endpoint caller, http:Request req) {
        http:Response res = new;
        res.setHeader("x-custom-header", "custom-header-value");
        res.setPayload(xml `<test>No Content</test>`);
        _ = caller -> noContent(message = res); //Body will be removed
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/noContentWithoutBody"
    }
    sendNoContentWithoutBody (endpoint caller, http:Request req) {
        _ = caller -> noContent();
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/badRequestWithBody"
    }
    sendBadRequestWithBody (endpoint caller, http:Request req) {
        http:Response res = new;
        res.setPayload(xml `<test>Bad Request</test>`);
        _ = caller -> badRequest(message = res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/badRequestWithoutBody"
    }
    sendBadRequestWithoutBody (endpoint caller, http:Request req) {
        _ = caller -> badRequest();
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/serverErrWithBody"
    }
    sendServerErrWithBody (endpoint caller, http:Request req) {
        _ = caller -> internalServerError(message = xml `<test>Internal Server Error Occurred</test>`);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/serverErrWithoutBody"
    }
    sendServerErrWithoutBody (endpoint caller, http:Request req) {
        _ = caller -> internalServerError();
    }
}

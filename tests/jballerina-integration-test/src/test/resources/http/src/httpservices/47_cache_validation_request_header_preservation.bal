// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

http:Client cachingEP4 = new("http://localhost:9243", { cache: { isShared: true } });

@http:ServiceConfig {
    basePath: "/validation-request"
}
service cachingProxy2 on cachingProxyListener {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function cacheableProxyResource(http:Caller caller, http:Request req) {
        var response = cachingEP4->forward("/validation-req-be", req);
        if (response is http:Response) {
            checkpanic caller->respond(response);
        } else {
            http:Response res = new;
            res.statusCode = 500;
            res.setPayload(response.message());
            checkpanic caller->respond(res);
        }
    }
}

@http:ServiceConfig {
    basePath: "/validation-req-be"
}
service cachingBackEnd2 on cachingBackendListener {

    @http:ResourceConfig { path: "/" }
    resource function mustRevalidate(http:Caller caller, http:Request req) {
        json payload = {"message":"Hello, World!"};
        http:Response res = new;
        http:ResponseCacheControl resCC = new;
        resCC.mustRevalidate = true;
        resCC.maxAge = 3;
        res.cacheControl = resCC;
        res.setETag(payload);
        res.setPayload(payload);
        res.setHeader("x-caller-req-header", req.getHeader("x-caller-req-header"));

        checkpanic caller->respond(<@untainted>res);
    }
}


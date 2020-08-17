// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

listener http:Listener cachingProxyListener = new(9244);
http:Client cachingEP1 = new("http://localhost:9243", { cache: { isShared: true } });

@http:ServiceConfig {
    basePath: "/nocache"
}
service cachingProxyService on cachingProxyListener {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function cacheableProxyResource(http:Caller caller, http:Request req) {
        var response = cachingEP1->forward("/nocachebackend", req);
        if (response is http:Response) {
            checkpanic caller->respond(response);
        } else {
            http:Response res = new;
            res.statusCode = 500;
            res.setPayload(<@untainted> response.message());
            checkpanic caller->respond(res);
        }
    }
}

listener http:Listener cachingBackendListener = new(9243);
int nocachehitcount = 0;

@http:ServiceConfig {
    basePath: "/nocachebackend"
}
service nocacheBackend on cachingBackendListener {

    @http:ResourceConfig { path: "/" }
    resource function sayHello(http:Caller caller, http:Request req) {
        json nocachePayload = {};
        http:Response res = new;
        http:ResponseCacheControl resCC = new;
        if (nocachehitcount < 1) {
            nocachePayload = { "message": "1st response" };
            res.cacheControl = resCC;
        } else {
            nocachePayload = { "message": "2nd response" };
        }
        resCC.noCache = true;
        res.setETag(nocachePayload);
        nocachehitcount += 1;
        res.setHeader("x-service-hit-count", nocachehitcount.toString());
        res.setPayload(nocachePayload);

        checkpanic caller->respond(res);
    }
}

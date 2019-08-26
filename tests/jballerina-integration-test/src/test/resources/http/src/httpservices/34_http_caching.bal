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

http:Client cachingEP = new("http://localhost:9240", { cache: { isShared: true } });
int cachingProxyHitcount = 0;

@http:ServiceConfig {
    basePath: "/cache"
}
service cachingProxy on new http:Listener(9239) {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function cacheableResource(http:Caller caller, http:Request req) {
        var response = cachingEP->forward("/hello", req);

        if (response is http:Response) {
            cachingProxyHitcount += 1;
            response.setHeader("x-proxy-hit-count", cachingProxyHitcount.toString());
            checkpanic caller->respond(response);
        } else {
            http:Response res = new;
            res.statusCode = 500;
            res.setPayload(response.reason());
            checkpanic caller->respond(res);
        }
    }
}

json payload = { "message": "Hello, World!" };
int hitcount = 0;

@http:ServiceConfig {
    basePath: "/hello"
}
service cachingBackend on new http:Listener(9240) {

    @http:ResourceConfig { path: "/" }
    resource function sayHello(http:Caller caller, http:Request req) {
        http:Response res = new;

        http:ResponseCacheControl resCC = new;
        resCC.maxAge = 60;
        resCC.isPrivate = false;

        res.cacheControl = resCC;

        res.setETag(payload);
        res.setLastModified();

        hitcount += 1;
        res.setHeader("x-service-hit-count", hitcount.toString());

        res.setPayload(payload);

        checkpanic caller->respond(res);
    }
}

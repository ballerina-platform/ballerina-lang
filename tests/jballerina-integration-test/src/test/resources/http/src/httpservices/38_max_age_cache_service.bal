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

http:Client maxAgeCacheEp = new("http://localhost:9246", { cache: { isShared: true } });

@http:ServiceConfig {
    basePath: "/maxAge"
}
service maxAgeProxyService on new http:Listener(9245) {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function maxAgeProxyResource(http:Caller caller, http:Request req) {
        var response = maxAgeCacheEp->forward("/maxAgeBackend", req);
        if (response is http:Response) {
            json responsePayload;
            if (response.hasHeader("cache-control")) {
                responsePayload = response.getHeader("cache-control");
                checkpanic caller->respond(<@untainted> responsePayload);
            } else {
                checkpanic caller->respond(response);
            }
        } else {
            http:Response res = new;
            res.statusCode = 500;
            res.setPayload(response.reason());
            checkpanic caller->respond(res);
        }
    }
}

json maxAgePayload = {};
int maxAgehitcount = 0;

@http:ServiceConfig {
    basePath: "/maxAgeBackend"
}
service maxAgeBackend on new http:Listener(9246) {

    @http:ResourceConfig { path: "/" }
    resource function sayHello(http:Caller caller, http:Request req) {
        http:Response res = new;
        http:ResponseCacheControl resCC = new;
        if (maxAgehitcount < 1) {
            resCC.maxAge = 5;
            res.cacheControl = resCC;
            maxAgePayload = { "message": "before cache expiration" };
            res.setETag(maxAgePayload);
        } else {
            maxAgePayload = { "message": "after cache expiration" };
        }
        maxAgehitcount += 1;
        res.setPayload(maxAgePayload);

        checkpanic caller->respond(res);
    }
}

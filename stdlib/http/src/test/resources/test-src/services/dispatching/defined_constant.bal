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

listener http:MockListener pathEP = new(9090);

const string RESOURCE_PATH = "/{id}";
const string SERVICE_BASE_PATH = "/hello";
const string SERVICE_HOST = "b7a.default";
const string RESOURCE_BODY = "person";

@http:ServiceConfig {
    basePath: SERVICE_BASE_PATH,
    host: SERVICE_HOST
}
service resourcePath on pathEP {

    @http:ResourceConfig {
        path: RESOURCE_PATH,
        methods: ["GET"],
        body: RESOURCE_BODY
    }

    resource function hello(http:Caller caller, http:Request req, string id, json person) {
        http:Response res = new;
        json message = {message:"Hi"};
        res.statusCode = 200;
        res.setJsonPayload(<@untainted json> message);
        checkpanic caller->respond(res);
    }
}

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
import ballerina/log;

// Filter 1
public type Filter12 object {
    public function filterRequest(http:Caller caller, http:Request request, http:FilterContext context)
                        returns boolean {
        log:printInfo("Intercepting request for filter 1");
        return true;
    }

    public function filterResponse(http:Response response, http:FilterContext context) returns boolean {
        return true;
    }
};

Filter12 filter12 = new;

listener http:Listener echoEP05 = new(9095, { filters: [filter12] });

@http:ServiceConfig {
    basePath: "/echo"
}
service echo05 on  echoEP05 {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/test"
    }
    resource function echo(http:Caller caller, http:Request req) {
        http:Response res = new;
        checkpanic caller->respond(res);
    }
}

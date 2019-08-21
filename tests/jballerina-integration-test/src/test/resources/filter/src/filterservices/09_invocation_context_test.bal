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
import ballerina/runtime;

final string attributeName = "attribute";
final string attributeValue = "value";

public type Filter15 object {
    public function filterRequest(http:Caller caller, http:Request request, http:FilterContext context)
                        returns boolean {
        log:printInfo("Add attribute to invocation context from filter");
        var ctx = runtime:getInvocationContext();
        ctx.attributes[attributeName] = attributeValue;
        return true;
    }

    public function filterResponse(http:Response response, http:FilterContext context) returns boolean {
        return true;
    }
};

Filter15 filter15 = new;

listener http:Listener echoEP08 = new(9098, { filters: [filter15] });

@http:ServiceConfig {
    basePath:"/echo"
}
service echo08 on echoEP08 {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/test"
    }
    resource function echo(http:Caller caller, http:Request req) {
        http:Response res = new;
        if (attributeValue == <string>runtime:getInvocationContext().attributes[attributeName]) {
            checkpanic caller->respond(res);
        } else {
            res.statusCode = 500;
            checkpanic caller->respond(res);
        }
    }
}


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
import ballerina/log;

service sample on new http:Listener(9090) {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/path/{foo}"
    }
    resource function params (http:Caller caller, http:Request req, string foo) {
        var bar = req.getQueryParamValue("bar");

        secureFunction(foo, foo);
        secureFunction(bar, bar);
    }

    resource function hi(http:Caller caller, http:Request request) {
        http:Response response = new;
        var req = request.getJsonPayload();
        if (req is json) {
            response.setJsonPayload(req);
        } else {
             log:printError("Invalid JSON!");
        }
        var result = caller->respond(response);
        if (result is error) {
            log:printError("Error sending response", err = result);
        }
    }
}

public function secureFunction (@untainted any secureIn, any insecureIn) {

}

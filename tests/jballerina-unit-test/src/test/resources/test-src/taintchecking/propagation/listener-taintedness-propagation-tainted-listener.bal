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

listener http:Listener helloWorldEP = new (19093);
@tainted listener http:Listener helloWorldEP1 = new (19094);

// Services created using service constructor expressions are dynamically bound to listeners using listener.__attach
// mechanism hence we consider those services to be tainted.
service ss = service {
    resource function x(http:Caller caller, http:Request req) {
        sensitiveFunc(req);
        sensitiveFunc(caller);
    }
};

// Service bound to tainted listener is considered tainted.
service sample on helloWorldEP1 {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/path/{foo}"
    }
    resource function params (http:Caller caller, http:Request req, string foo) {
        var bar = req.getQueryParamValue("bar");
        sensitiveFunc(foo);
    }
}

function sensitiveFunc(@untainted any p) {
    // do some stuff
}

// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

listener http:MockListener testEP = new(9090);

@http:ServiceConfig {
    basePath:"/identifierLiteral",
    cors: {
              allowOrigins :["http://www.m3.com", "http://www.hello.com"],
              allowCredentials : true,
              allowHeaders :["CORELATION_ID"],
              exposeHeaders :["CORELATION_ID"],
              maxAge : 1
          }
}
service 'ʂαɱρʆę_Service_\ \/\:\@\[\`\{\~\u{2324} on testEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/resource1"
    }
    resource function 'ʂαɱρʆę_Resource1_\ \/\:\@\[\`\{\~\u{2324} (http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"key":"keyVal", "value":"valueOfTheString"};
        res.setJsonPayload(responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/resource2"
    }
    resource function 'ʂαɱρʆę_Resource2_\ \/\:\@\[\`\{\~\u{2324} (http:Caller caller, http:Request req) {
        http:Response res = new;
        string 'ʂαɱρʆę_Var_\ \/\:\@\[\`\{\~\u{2324} = "hello";
        res.setTextPayload('ʂαɱρʆę_Var_\ \/\:\@\[\`\{\~\u{2324});
        checkpanic caller->respond(res);
    }
}

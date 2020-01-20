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

http:Client urlClient = new ("http://localhost:9254//url", { cache: { enabled: false }});

@http:ServiceConfig {
    basePath: "/url//test"
}
service urlBackend on new http:Listener(9254) {

    @http:ResourceConfig {
        path: "//"
    }
    resource function replyText(http:Caller caller, http:Request req) {
        checkpanic caller->respond("Hello");
    }
}

@http:ServiceConfig {
    basePath: "//url"
}
service urlClientTest on new http:Listener(9255)  {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "//"
    }
    resource function hello(http:Caller caller, http:Request request) {
        string value = "";
        var response = urlClient->get("//test");
        if (response is http:Response) {
            var result = response.getTextPayload();
            if (result is string) {
                value = result;
            } else {
                value = result.reason();
            }
        }
        checkpanic caller->respond(<@untainted> value);
    }
}

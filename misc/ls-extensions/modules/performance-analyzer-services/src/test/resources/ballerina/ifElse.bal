// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/http as http2;
import project.http as http;

service / on new http:Listener(8090) {
    resource function post .(http:Request req) returns json|error {
        http:Client clientEP = check new ("https://postman-echo.com/post1");
        json clientResponse = check clientEP->forward("/", req);
        if (false) {
            json clientResponse2 = check clientEP->forward("/", req);
        } else {
            http:Client clientEP1 = check new (url = "https://postman-echo.com/post2");
            json clientResponse1 = check clientEP1->forward("/", req);
        }
        return clientResponse;
    }
}

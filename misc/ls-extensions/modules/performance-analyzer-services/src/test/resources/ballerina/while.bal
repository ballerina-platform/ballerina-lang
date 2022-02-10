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

service /hello on new http2:Listener(9090) {
    resource function get sayHello(string name) returns string|error {
        do {
            http:Client httpEndpoint = check new ("https://api.chucknorris.io/");

            string r = "";
            int k = 0;
            while (k < 5) {
                json getResponse = check httpEndpoint->get("/jokes/random");
                r += getResponse.toJsonString();
                k = k + 1;
            }

            return r;
        } on fail var e {
            io:println(e);

        }
    }
}

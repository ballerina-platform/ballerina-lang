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
import ballerina/io;

endpoint http:Client clientEP {
    url:"http://localhost:9218",
    proxy: {
        host:"localhost",
        port:9219
    }
};

public function main (string... args) {
    http:Request req = new;
    var resp = clientEP->post("/proxy/server", req);
    match resp {
        error err => io:println(err.reason());
        http:Response response => {
            match (response.getTextPayload()) {
                error payloadError => io:println(payloadError.reason());
                string res => io:println(res);
            }
        }
    }
}

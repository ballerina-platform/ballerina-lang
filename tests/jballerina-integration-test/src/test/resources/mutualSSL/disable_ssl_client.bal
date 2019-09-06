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
import ballerina/io;

http:ClientConfiguration disableSslClientConf = {
    secureSocket:{
        disable:true
    }
};

public function main() {
    http:Client httpClient = new("https://localhost:9238", disableSslClientConf);
    var resp = httpClient->get("/helloService");
    if (resp is http:Response) {
        var payload = resp.getTextPayload();
        if (payload is string) {
            io:println(payload);
        } else {
            error err = payload;
            io:println(<string> err.detail()["message"]);
        }
    } else {
        error err = resp;
        io:println(<string> err.detail()["message"]);
    }
}

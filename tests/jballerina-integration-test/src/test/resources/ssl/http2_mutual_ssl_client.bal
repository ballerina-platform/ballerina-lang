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

import ballerina/config;
import ballerina/http;
import ballerina/io;

http:ClientConfiguration mutualSslClientConf = {
    secureSocket:{
        keyStore:{
            path: config:getAsString("keystore"),
            password: "ballerina"
        },
        trustStore:{
            path: config:getAsString("truststore"),
            password: "ballerina"
        },
        protocol:{
            name: "TLSv1.2",
            versions: ["TLSv1.2"]
        },
        ciphers: ["TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"]
    },
    httpVersion: "2.0",
    http2Settings: { http2PriorKnowledge: true }
};

public function main() {
    http:Client httpClient = new("https://localhost:9104", mutualSslClientConf);
    var resp = httpClient->get("/echo/");
    if (resp is http:Response) {
        var payload = resp.getTextPayload();
        if (payload is string) {
            io:println(payload);
        } else {
            error err = payload;
            io:println(<string> err.detail()?.message);
        }
    } else {
        error err = resp;
        io:println(<string> err.detail()?.message);
    }
}

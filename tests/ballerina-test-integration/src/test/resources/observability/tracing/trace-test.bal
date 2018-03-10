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

import ballerina.net.http;
import ballerina.testing;

service<http> echoService {

    resource resourceOne (http:Connection conn, http:InRequest req) {
        endpoint<http:HttpClient> httpEndpoint {
            create http:HttpClient("http://localhost:9090/echoService", {});
        }
        http:OutResponse res = {};
        http:InResponse resp = {};
        http:OutRequest request = {};
        resp, _ = httpEndpoint.get("/resourceTwo", request);
        res.setStringPayload("Hello, World!");
        _ = conn.respond(res);
    }

    resource resourceTwo (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        res.setStringPayload("Hello, World 2!");
        _ = conn.respond(res);
    }

    resource getFinishedSpansCount(http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        string returnString = testing:getFinishedSpansCount();
        res.setStringPayload(returnString);
        _ = conn.respond(res);
    }
}

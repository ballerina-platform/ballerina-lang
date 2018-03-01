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

@http:configuration {
    port:8890
}
service<http> participant2 {
    resource task1 (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        res.setStringPayload("Resource is invoked");
        _ = conn.respond(res);
    }

    resource task2 (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        string result = "incorrect id";
        transaction {
            if (req.getHeader("X-XID") == req.getHeader("participant-id")) {
                result = "equal id";
            }
        }
        res.setStringPayload(result);
        _ = conn.respond(res);
    }
}

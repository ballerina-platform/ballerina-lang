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

endpoint http:ServiceEndpoint participant2EP {
    port:8890
};

@http:serviceConfig {
}
service<http:Service> participant2 bind participant2EP {
    task1 (endpoint conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("Resource is invoked");
        _ = conn -> respond(res);
    }

    task2 (endpoint conn, http:Request req) {
        http:Response res = {};
        string result = "incorrect id";
        transaction {
            if (req.getHeader("X-XID") == req.getHeader("participant-id")) {
                result = "equal id";
            }
        }
        res.setStringPayload(result);
        _ = conn -> respond(res);
    }
}

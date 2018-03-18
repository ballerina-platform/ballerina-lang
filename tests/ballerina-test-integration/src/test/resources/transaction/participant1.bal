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

import ballerina.io;
import ballerina.net.http;

endpoint http:ServiceEndpoint participant1EP {
    port:8889
};

@http:serviceConfig {
}
service<http:Service> participant1 bind participant1EP {

    @http:resourceConfig {
        path:"/"
    }
    member (endpoint conn, http:Request req) {
        endpoint http:ClientEndpoint ep {
            targets: [{uri: "http://localhost:8890/participant2"}]
        };
        http:Request newReq = {};
        newReq.setHeader("participant-id", req.getHeader("X-XID"));
        http:Response clientResponse2;
        transaction {
            var clientResponse1, _ = ep -> forward("/task1", req);
            clientResponse2, _ = ep -> get("/task2", newReq);
        } onretry {
            io:println("Participant1 failed");
        }
        _ = conn -> forward(clientResponse2);
    }
}

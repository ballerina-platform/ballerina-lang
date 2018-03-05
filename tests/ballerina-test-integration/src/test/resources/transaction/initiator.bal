// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina.io;

@http:configuration {
    basePath:"/",
    port:8888
}
service<http> InitiatorService {

    @http:resourceConfig {
        path:"/"
    }
    resource member (http:Connection conn, http:InRequest req) {
        endpoint<http:HttpClient> endPoint {
            create http:HttpClient("http://localhost:8889/participant1", {});
        }
        http:OutRequest newReq = {};
        http:InResponse clientResponse1;
        transaction {
            clientResponse1, _ = endPoint.get("/", newReq);
        } failed {
            io:println("Intiator failed");
        }
        _ = conn.forward(clientResponse1);
    }
}

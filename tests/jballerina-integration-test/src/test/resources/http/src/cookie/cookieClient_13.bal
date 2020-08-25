// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public function main() {
    http:Client clientEP = new("http://localhost:9253");
    http:Request req = new;
    req.addHeader("Cookie", "user=John; asd=; =sdsdfsf; =gffg; ");
    var resp = clientEP->get("/cookie/cookieBackend_13", req);
    if (resp is http:Response) {
        var payload = resp.getTextPayload();
        if (payload is string) {
            io:print(payload);
        } else {
            io:print(payload.message());
        }
    } else {
        io:print(resp.message());
    }
}

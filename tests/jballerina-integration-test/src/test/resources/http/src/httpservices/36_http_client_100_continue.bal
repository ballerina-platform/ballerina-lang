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

import ballerina/io;
import ballerina/http;

public function main() {
    http:Client clientEP = new("http://localhost:9241");
    http:Request req = new();
    req.addHeader("content-type", "text/plain");
    req.addHeader("Expect", "100-continue");
    req.setPayload("Hello World!");
    var response = clientEP->post("/continue", req);
    if (response is http:Response) {
        var payload = response.getTextPayload();
        if (payload is string) {
            io:print("Payload: " + payload + " Statuscode" + response.statusCode.toString());
        } else {
            io:println(<string>payload.detail()["message"]);
        }
    } else {
        io:println(<string>response.detail()["message"]);
    }
}

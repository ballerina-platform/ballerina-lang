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

service initiatingService on new http:Listener(9105) {
    resource function initiatingResource(http:Caller caller, http:Request request) {
        http:Client forwadingClient = new ("http://localhost:9106",
                                       {forwarded: "enable", httpVersion: "2.0",
                                        http2Settings: { http2PriorKnowledge: true }});
        var responseFromForwardBackend = forwadingClient->get(
                                        "/forwardedBackend/forwardedResource", <@untainted> request);
        if (responseFromForwardBackend is http:Response) {
            var resultSentToClient = caller->respond(responseFromForwardBackend);
        }
    }
}

service forwardedBackend on new http:Listener(9106, {httpVersion: "2.0"}) {
    resource function forwardedResource(http:Caller caller, http:Request request) {
        string header = request.getHeader("forwarded");
        io:println(header);
        http:Response response = new();
        response.setHeader("forwarded", header);
        response.setPayload("forward is working");
        var resultSentToInitClient = caller->respond(<@untianted> response);
    }
}

// Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

listener http:Listener ep1 = new(9230, { httpVersion: "2.0" });
listener http:Listener ep2 = new(9231, { httpVersion: "2.0" });

http:Client h2WithPriorKnowledge = new("http://localhost:9231", { httpVersion: "2.0", http2Settings: {
        http2PriorKnowledge: true }, poolConfig: {} });

http:Client h1Client = new("http://localhost:9231", { httpVersion: "1.1", poolConfig: {}});

@http:ServiceConfig {
    basePath: "/test"
}
service globalClientTest on ep1 {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/h1"
    }
    resource function testH1Client(http:Caller caller, http:Request req) {
        var response = h1Client->post("/backend", "HTTP/1.1 request");
        if (response is http:Response) {
            checkpanic caller->respond(<@untainted> response);
        } else {
            checkpanic caller->respond("Error in client post - HTTP/1.1");
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/h2"
    }
    resource function testH2Client(http:Caller caller, http:Request req) {
        var response = h2WithPriorKnowledge->post("/backend", "HTTP/2 with prior knowledge");
        if (response is http:Response) {
            checkpanic caller->respond(<@untainted> response);
        } else {
            checkpanic caller->respond("Error in client post - HTTP/2");
        }
    }
}

@http:ServiceConfig {
    basePath: "/backend"
}
service testBackEnd on ep2 {

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/"
    }
    resource function test(http:Caller caller, http:Request req) {
        string outboundResponse = "";
        if (req.hasHeader("connection") && req.hasHeader("upgrade")) {
            string[] connHeaders = req.getHeaders("connection");
            outboundResponse = connHeaders[1];
            outboundResponse = outboundResponse + "--" + req.getHeader("upgrade");
        } else {
            outboundResponse = "Connection and upgrade headers are not present";
        }
        outboundResponse = outboundResponse + "--" + checkpanic req.getTextPayload() + "--" + req.httpVersion;
        checkpanic caller->respond(<@untainted> outboundResponse);
    }
}

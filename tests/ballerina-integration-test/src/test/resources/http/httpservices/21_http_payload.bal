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

import ballerina/http;

endpoint http:Client clientEP19 {
    url: "http://localhost:9119/"
};

@http:ServiceConfig {
    basePath: "/test"
}
service<http:Service> testService16 bind { port: 9118 } {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    getPayload(endpoint caller, http:Request request) {
        http:Response res = check clientEP19->get("/payloadTest", message = ());
        //First get the payload as a byte array, then take it as an xml
        byte[] binaryPayload = check res.getBinaryPayload();
        xml payload = check res.getXmlPayload();
        xml descendants = payload.selectDescendants("title");
        _ = caller->respond(untaint descendants.getTextValue());
    }
}

@http:ServiceConfig {
    basePath: "/payloadTest"
}
service<http:Service> testPayload17 bind { port: 9119 } {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    sendXml(endpoint caller, http:Request req) {
        xml xmlPayload = xml `<xml version="1.0">
                                <channel>
                                    <title>W3Schools Home Page</title>
                                    <link>https://www.w3schools.com</link>
                                      <description>Free web building tutorials</description>
                                      <item>
                                        <title>RSS Tutorial</title>
                                        <link>https://www.w3schools.com/xml/xml_rss.asp</link>
                                        <description>New RSS tutorial on W3Schools</description>
                                      </item>
                                </channel>
                              </xml>`;
        _ = caller->respond(untaint xmlPayload);
    }
}

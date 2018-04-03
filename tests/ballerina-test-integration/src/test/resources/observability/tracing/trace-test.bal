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
import ballerina/testing;

endpoint http:ServiceEndpoint ep1 {
    port : 9090
};

@http:ServiceConfig {
    basePath:"/echoService"
}
service<http:Service> echoService bind ep1 {
    resourceOne (endpoint outboundEP, http:Request clientRequest) {
        http:Response outResponse = {};
        http:Request request = {};
        var response = callNextResource();
        outResponse.setStringPayload("Hello, World!");
        _ = outboundEP -> respond(response);
    }

    resourceTwo (endpoint outboundEP, http:Request clientRequest) {
        http:Response res = {};
        res.setStringPayload("Hello, World 2!");
        _ = outboundEP -> respond(res);
    }

    getFinishedSpansCount(endpoint outboundEP, http:Request clientRequest) {
        http:Response res = {};
        string returnString = testing:getFinishedSpansCount();
        res.setStringPayload(returnString);
        _ = outboundEP -> respond(res);
    }
}

function callNextResource() returns (http:Response) {
    endpoint http:ClientEndpoint httpEndpoint {
        targets : [{url: "http://localhost:9090/echoService"}]
    };

    http:Request request = {};
    var resp = httpEndpoint -> get("/resourceTwo", request);
    match resp {
        http:HttpConnectorError err => return {};
        http:Response response => return response;
    }
}

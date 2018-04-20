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

endpoint http:Listener listener {
    port : 9090
};

@http:ServiceConfig {
    basePath:"/echoService"
}
service echoService bind listener {
    resourceOne (endpoint caller, http:Request clientRequest) {
        http:Response outResponse = new;
        http:Request request = new;
        http:Response | () response = callNextResource();
        outResponse.setStringPayload("Hello, World!");
        match response {
            http:Response res => _ = caller -> respond(res);
            () => _ = caller -> respond(new http:Response());
        }
    }

    resourceTwo (endpoint caller, http:Request clientRequest) {
        http:Response res = new;
        res.setStringPayload("Hello, World 2!");
        _ = caller -> respond(res);
    }

    getFinishedSpansCount(endpoint caller, http:Request clientRequest) {
        http:Response res = new;
        string returnString = testing:getFinishedSpansCount();
        res.setStringPayload(returnString);
        _ = caller -> respond(res);
    }
}

function callNextResource() returns (http:Response | ()) {
    endpoint http:Client httpEndpoint {
        url: "http://localhost:9090/echoService"
    };
    http:Request request = new;
    var resp = httpEndpoint -> get("/resourceTwo", request);
    match resp {
        http:HttpConnectorError err => return ();
        http:Response response => return response;
    }
}

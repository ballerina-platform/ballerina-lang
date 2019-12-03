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
import ballerina/testobserve;

@http:ServiceConfig {
    basePath:"/echoService"
}
service echoService4 on new http:Listener(9094) {
     @http:ResourceConfig {
        methods: ["GET"],
        path: "/resourceOne/{id}"
    }
    resource function resourceOne (http:Caller caller, http:Request clientRequest, int id) {
        http:Response outResponse = new;
        if id == 0 {
            error e = error("Custom Panic in Service 1");
            panic e;
        }
        var response = callNextResource4(<@untainted> id);
        if (response is http:Response) {
            outResponse.setTextPayload("Hello, World!");
            checkpanic caller->respond(outResponse);
        } else {
            error err = error ("error occurred");
            panic err;
        }
    }

     @http:ResourceConfig {
        methods: ["GET"],
        path: "/resourceTwo/{id}"
    }
    resource function resourceTwo (http:Caller caller, http:Request clientRequest, int id) {
        http:Response res = new;
        match id {
            1 => {
                error e = error("Custom Panic in Service 2");
                panic e;
            }
            2 => {
                res.statusCode = 501;
                checkpanic caller->respond(res);
                return;
            }
        }
        res.setTextPayload("Hello, World 2!");
        checkpanic caller->respond(res);
    }

    resource function getMockTracers(http:Caller caller, http:Request clientRequest) {
        http:Response res = new;
        json returnString = testobserve:getMockTracers();
        res.setJsonPayload(returnString);
        checkpanic caller->respond(res);
    }
}

function callNextResource4(int id) returns (http:Response | error) {
    http:Client httpEndpoint = new("http://localhost:9094/echoService", {
            cache: {
                enabled: false
            }
        });
    http:Response resp = check httpEndpoint -> get("/resourceTwo/" + id.toString());
    return resp;
}

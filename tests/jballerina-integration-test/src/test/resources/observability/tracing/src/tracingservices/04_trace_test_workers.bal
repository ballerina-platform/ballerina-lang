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
import ballerina/runtime;
import ballerina/testobserve;

@http:ServiceConfig {
    basePath:"/echoService"
}
service echoService3 on new http:Listener(9093) {
    resource function resourceOne (http:Caller caller, http:Request clientRequest) {
        http:Response outResponse = new;
        test(13);
        var response = callNextResource3();
        if (response is http:Response) {
            test(13);
            outResponse.setTextPayload("Hello, World!");
            checkpanic caller->respond(outResponse);
        } else {
            error err = error ("error occurred");
            panic err;
        }
    }

    resource function resourceTwo (http:Caller caller, http:Request clientRequest) {
        http:Response res = new;
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

function test(int c) {
    worker w1 {
        int a = c;
        if (c == 11) {
            runtime:sleep(20);
        }
        a -> w2;
    }
    worker w2 {
        if (c == 12) {
            runtime:sleep(20);
        }
        int b = <- w1;
    }
}

function callNextResource3() returns (http:Response | error) {
    http:Client httpEndpoint = new("http://localhost:9093/echoService", {
            cache: {
                enabled: false
            }
        });
    http:Response resp = check httpEndpoint -> get("/resourceTwo");
    return resp;
}

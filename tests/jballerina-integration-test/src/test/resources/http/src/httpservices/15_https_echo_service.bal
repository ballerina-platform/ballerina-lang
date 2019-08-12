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

listener http:Listener echoDummyEP = new(9109);

listener http:Listener echoHttpEP = new(9110);

http:ListenerConfiguration echoEP2Config = {
    secureSocket: {
        keyStore: {
            path:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password:"ballerina"
        }
    }
};

listener http:Listener echoEP2 = new(9111, echoEP2Config);

@http:ServiceConfig {
    basePath:"/echo"
}

service echo2 on echoEP2 {
    @http:ResourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource function echo2(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload("hello world");
        checkpanic caller->respond(res);
    }
}

@http:ServiceConfig  {
    basePath:"/echoOne"
}
service echoOne1 on echoEP2, echoHttpEP {
    @http:ResourceConfig {
        methods:["POST"],
        path:"/abc"
    }
    resource function echoAbc(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload("hello world");
        checkpanic caller->respond(res);
    }
}

@http:ServiceConfig {
    basePath:"/echoDummy"
}
service echoDummy1 on echoDummyEP {

    @http:ResourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource function echoDummy1(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload("hello world");
        checkpanic caller->respond(res);
    }
}

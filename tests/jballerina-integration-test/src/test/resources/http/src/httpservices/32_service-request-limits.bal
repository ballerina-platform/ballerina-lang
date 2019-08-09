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

http:ListenerConfiguration urlLimitConfig = {
    http1Settings: {
        maxUriLength: 1024
    }
};

http:ListenerConfiguration lowUrlLimitConfig = {
    http1Settings: {
        maxUriLength: 2
    }
};

http:ListenerConfiguration lowHeaderConfig = {
    http1Settings: {
        maxHeaderSize: 30
    }
};

http:ListenerConfiguration midSizeHeaderConfig = {
    http1Settings: {
        maxHeaderSize: 100
    }
};

listener http:Listener normalRequestLimitEP = new(9234, urlLimitConfig);
listener http:Listener lowRequestLimitEP = new(9235, lowUrlLimitConfig);
listener http:Listener lowHeaderLimitEP = new(9236, lowHeaderConfig);
listener http:Listener midHeaderLimitEP = new(9237, midSizeHeaderConfig);

@http:ServiceConfig {basePath:"/requestUriLimit"}
service urlLimitService on normalRequestLimitEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/validUrl"
    }
    resource function mediumUrl(http:Caller caller, http:Request req) {
        checkpanic caller->respond("Hello World!!!");
    }
}

@http:ServiceConfig {basePath:"/lowRequestUriLimit"}
service lessUrlLimitService on lowRequestLimitEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/invalidUrl"
    }
    resource function lessUrlLength(http:Caller caller, http:Request req) {
        checkpanic caller->respond("Hello World!!!");
    }
}

@http:ServiceConfig {basePath:"/lowRequestHeaderLimit"}
service lessHeaderLimitService on lowHeaderLimitEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/invalidHeaderSize"
    }
    resource function invalidHeader(http:Caller caller, http:Request req) {
        checkpanic caller->respond("Hello World!!!");
    }
}

@http:ServiceConfig {basePath:"/requestHeaderLimit"}
service headerLimitService on midHeaderLimitEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/validHeaderSize"
    }
    resource function validHeader(http:Caller caller, http:Request req) {
        checkpanic caller->respond("Hello World!!!");
    }
}

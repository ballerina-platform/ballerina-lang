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

import ballerina/config;
import ballerina/http;

http:ListenerConfiguration mutualSslCertServiceConf = {
    secureSocket: {
        keyFile: config:getAsString("certificate.key"),
        certFile: config:getAsString("public.cert"),
        trustedCertFile: config:getAsString("public.cert"),
        sslVerifyClient: "require"
    }
};

listener http:Listener mutualSSLListener = new(9217, mutualSslCertServiceConf);

@http:ServiceConfig {
    basePath:"/echo"
}

service mutualSSLService on mutualSSLListener {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource function sayHello(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload("Response received");
        checkpanic caller->respond( res);
    }
}


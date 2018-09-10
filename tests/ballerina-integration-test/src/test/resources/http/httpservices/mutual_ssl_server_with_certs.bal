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
import ballerina/io;

endpoint http:Listener mutualSSLListener {
    port:9217,
    secureSocket: {
        keyFile: config:getAsString("certificate.key"),
        certFile: config:getAsString("public.cert"),
        trustedCertFile: config:getAsString("public.cert"),
        sslVerifyClient: "require"
    }
};

@http:ServiceConfig {
    basePath:"/echo"
}

service<http:Service> mutualSSLService bind mutualSSLListener {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    sayHello (endpoint conn, http:Request req) {
        http:Response res = new;
        res.setTextPayload("Response received");
        _ = conn->respond( res);
    }
}


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

import ballerina/io;
import ballerina/http;

endpoint http:Listener server {
    port:9218
};

@http:ServiceConfig {
    basePath:"/proxy"
}
service<http:Service> serverService bind server {

    @http:ResourceConfig {
        methods:["POST"],
        path:"/server"
    }
    sayHello (endpoint conn, http:Request req) {
        http:Response res = new;
        res.setTextPayload("Backend server sent response");
        _ = conn -> respond(res);
    }
}

endpoint http:Listener proxy {
    port:9219
};

@http:ServiceConfig {
    basePath:"/*"
}
service<http:Service> proxyService bind proxy {

    @http:ResourceConfig {
        path:"/*"
    }
    sayHello (endpoint conn, http:Request req) {
        string url = untaint req.rawPath;
        sendRequest(url, req, conn);
    }
}

function defineEndpointWithProxy (string url) returns http:Client {
    endpoint http:Client httpEndpoint {
        url: url
    };
    return httpEndpoint;
}

function sendRequest(string url, http:Request req, http:Listener conn) {
    endpoint http:Client clientEP = defineEndpointWithProxy(url);
    endpoint http:Listener listenerEP = conn;
    var response = clientEP->forward("", req);
    match response {
        http:Response httpResponse => {
            _ = listenerEP->respond(httpResponse);
        }
        http:error err => {
            http:Response errorResponse = new;
            errorResponse.setTextPayload(err.message);
            _ = listenerEP->respond(errorResponse);
        }
    }
}


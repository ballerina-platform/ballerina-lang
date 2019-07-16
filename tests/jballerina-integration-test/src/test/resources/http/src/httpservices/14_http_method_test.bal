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

listener http:Listener serviceEndpoint4 = new(9108);
http:Client endPoint = new("http://localhost:9108");

@http:ServiceConfig {
    basePath:"/headQuote"
}
service headQuoteService on serviceEndpoint4 {

    @http:ResourceConfig {
        path:"/default"
    }
    resource function defaultResource (http:Caller caller, http:Request req) {
        string method = req.method;
        http:Request clientRequest = new;

        var response = endPoint -> execute(<@untainted> method, "/getQuote/stocks", clientRequest);
        if (response is http:Response) {
            checkpanic caller->respond(response);
        } else {
            json errMsg = {"error":"error occurred while invoking the service"};
           checkpanic caller->respond(errMsg);
        }
    }

    @http:ResourceConfig {
        path:"/forward11"
    }
    resource function forwardRes11 (http:Caller caller, http:Request req) {
        var response = endPoint -> forward("/getQuote/stocks", req);
        if (response is http:Response) {
            checkpanic caller->respond(response);
        } else {
            json errMsg = {"error":"error occurred while invoking the service"};
           checkpanic caller->respond(errMsg);
        }
    }

    @http:ResourceConfig {
        path:"/forward22"
    }
    resource function forwardRes22 (http:Caller caller, http:Request req) {
        var response = endPoint -> forward("/getQuote/stocks", req);
        if (response is http:Response) {
            checkpanic caller->respond(response);
        } else {
            json errMsg = {"error":"error occurred while invoking the service"};
           checkpanic caller->respond(errMsg);
        }
    }

    @http:ResourceConfig {
        path:"/getStock/{method}"
    }
    resource function commonResource (http:Caller caller, http:Request req, string method) {
        http:Request clientRequest = new;
        var response = endPoint -> execute(<@untainted> method, "/getQuote/stocks", clientRequest);
        if (response is http:Response) {
            checkpanic caller->respond(response);
        } else {
            json errMsg = {"error":"error occurred while invoking the service"};
           checkpanic caller->respond(errMsg);
        }
    }
}

@http:ServiceConfig {
    basePath:"/sampleHead"
}
service testClientConHEAD on serviceEndpoint4 {

    @http:ResourceConfig {
        methods:["HEAD"],
        path:"/"
    }
    resource function passthrough (http:Caller caller, http:Request req) {
        http:Request clientRequest = new;
        var response = endPoint -> get("/getQuote/stocks", clientRequest);
        if (response is http:Response) {
            checkpanic caller->respond(response);
        } else {
            json errMsg = {"error":"error occurred while invoking the service"};
           checkpanic caller->respond(errMsg);
        }
    }
}

@http:ServiceConfig {
    basePath:"/getQuote"
}
service quoteService2 on serviceEndpoint4 {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/stocks"
    }
    resource function company (http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload("wso2");
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/stocks"
    }
    resource function product (http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload("ballerina");
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/stocks"
    }
    resource function defaultStock (http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setHeader("Method", "any");
        res.setTextPayload("default");
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["POST"],
        body:"person"
    }
    resource function employee (http:Caller caller, http:Request req, json person) {
        http:Response res = new;
        res.setJsonPayload(<@untainted> person);
        checkpanic caller->respond(res);
    }
}

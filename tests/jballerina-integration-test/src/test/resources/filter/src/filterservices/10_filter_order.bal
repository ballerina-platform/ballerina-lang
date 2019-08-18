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

public type ReqFilter1 object {
    *http:RequestFilter;
    public function filterRequest(http:Caller caller, http:Request request, http:FilterContext context)
                        returns boolean {
        request.setPayload("RequestFilter1 ");
        return true;
    }
};
public type RespFilter1 object {
    *http:ResponseFilter;
    public function filterResponse(http:Response response, http:FilterContext context) returns boolean {
        var payload = response.getTextPayload();
        if (payload is string) {
            payload += "ResponseFilter1 ";
            response.setPayload(<@untainted>payload);
        }
        return true;
    }
};

public type ReqFilter2 object {
    *http:RequestFilter;
    public function filterRequest(http:Caller caller, http:Request request, http:FilterContext context)
                        returns boolean {
        var payload = request.getTextPayload();
        if (payload is string) {
            payload += "RequestFilter2 ";
            request.setPayload(<@untainted>payload);
        }
        return true;
    }
};
public type RespFilter2 object {
    *http:ResponseFilter;
    public function filterResponse(http:Response response, http:FilterContext context) returns boolean {
        var payload = response.getTextPayload();
        if (payload is string) {
            payload += "ResponseFilter2 ";
            response.setPayload(<@untainted>payload);
        }
        return true;
    }
};

public type CommonFilter object {
    *http:RequestFilter;
    *http:ResponseFilter;
    public function filterRequest(http:Caller caller, http:Request request, http:FilterContext context)
                        returns boolean {
        var payload = request.getTextPayload();
        if (payload is string) {
            payload += "CommonFilterReq ";
            request.setPayload(<@untainted>payload);
        }
        return true;
    }

    public function filterResponse(http:Response response, http:FilterContext context) returns boolean {
        var payload = response.getTextPayload();
        if (payload is string) {
            payload += "CommonFilterResp ";
            response.setPayload(<@untainted>payload);
        }
        return true;
    }
};

ReqFilter1 reqFilter1 = new;
RespFilter1  respFilter1 = new;
ReqFilter2 reqFilter2 = new;
RespFilter2 respFilter2= new;
CommonFilter commFilter = new;

listener http:Listener filterOrderListener = new(9099,
    { filters: [reqFilter1, respFilter1, reqFilter2, respFilter2, commFilter] });

@http:ServiceConfig {
    basePath: "/filter"
}
service FilterOrder on filterOrderListener {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/order"
    }
    resource function filterOrder(http:Caller caller, http:Request req) {
        var payload = req.getTextPayload();
        http:Response res = new;
        if (payload is string) {
            res.setPayload(<@untainted> payload);
        }
        checkpanic caller->respond(res);
    }
}

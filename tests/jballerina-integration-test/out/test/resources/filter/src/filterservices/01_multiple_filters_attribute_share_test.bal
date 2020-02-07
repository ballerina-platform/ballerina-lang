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

import ballerina/log;

// Filter1
FilterDto dto = { authenticated: true, username: "abcde" };

public type Filter1 object {
    public function filterRequest(http:Caller caller, http:Request request, http:FilterContext context)
                        returns boolean {
        log:printInfo("Intercepting request for filter 1");
        context.attributes["attribute1"] = "attribute1";
        context.attributes["attribute2"] = dto;
        return true;
    }

    public function filterResponse(http:Response response, http:FilterContext context) returns boolean {
        return true;
    }
};

Filter1 filter1 = new;

// Filter2

public type Filter2 object {
    public function filterRequest(http:Caller caller, http:Request request, http:FilterContext context) returns boolean {
        log:printInfo("Intercepting request for filter 2");
        boolean status = true;
        if (context.attributes.hasKey("attribute1")){
            if (<string>context.attributes["attribute1"] == "attribute1"){
                status = status && true;
            } else {
                status = status && false;
            }
        } else {
            status = status && false;
        }
        if (context.attributes.hasKey("attribute2")){
            FilterDto returnedDto = <FilterDto>context.attributes["attribute2"];
            if (returnedDto.authenticated == dto.authenticated && returnedDto.username == dto.username){
                status = status && true;
            } else {
                status = status && false;
            }
        } else {
            status = status && false;
        }
        if (status){
            return true;
        } else {
            http:Response response = new;
            response.statusCode = 401;
            response.setTextPayload("attribute missing in context");
            checkpanic caller->respond(response);
            return false;
        }
    }

    public function filterResponse(http:Response response, http:FilterContext context) returns boolean {
        return true;
    }
};

Filter2 filter2 = new;

listener http:Listener echoEP = new(9090, { filters: [filter1, filter2] });

@http:ServiceConfig {
    basePath: "/echo"
}
service echo on echoEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/test"
    }
    resource function echo(http:Caller caller, http:Request req) {
        http:Response res = new;
        checkpanic caller->respond(res);
    }
}

public type FilterDto record {
    boolean authenticated;
    string username;
};
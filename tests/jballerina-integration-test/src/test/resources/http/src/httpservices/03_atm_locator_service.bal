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

listener http:Listener serviceEnpoint = new(9092);

http:Client bankInfoService = new("http://localhost:9092/bankinfo/product");

http:Client branchLocatorService = new("http://localhost:9092/branchlocator/product");

@http:ServiceConfig {
    basePath:"/ABCBank"
}
service ATMLocator on serviceEnpoint {
    @http:ResourceConfig {
        methods:["POST"]
    }
    resource function locator(http:Caller caller, http:Request req) {

        http:Request backendServiceReq = new;
        var jsonLocatorReq = req.getJsonPayload();
        if (jsonLocatorReq is json) {
            string zipCode = jsonLocatorReq.ATMLocator.ZipCode.toString();
            io:println("Zip Code " + zipCode);
            map<map<json>> branchLocatorReq = {"BranchLocator":{"ZipCode":""}};
            branchLocatorReq["BranchLocator"]["ZipCode"] = zipCode;
            backendServiceReq.setPayload(<@untainted> branchLocatorReq);
        } else {
            io:println("Error occurred while reading ATM locator request");
        }

        http:Response locatorResponse = new;
        var locatorRes = branchLocatorService -> post("", backendServiceReq);
        if (locatorRes is http:Response) {
            locatorResponse = locatorRes;
        } else {
            io:println("Error occurred while reading locator response");
        }

        var branchLocatorRes = locatorResponse.getJsonPayload();
        if (branchLocatorRes is json) {
            string branchCode = branchLocatorRes.ABCBank.BranchCode.toString();
            io:println("Branch Code " + branchCode);
            map<map<json>> bankInfoReq = {"BranchInfo":{"BranchCode":""}};
            bankInfoReq["BranchInfo"]["BranchCode"] = branchCode;
            backendServiceReq.setJsonPayload(<@untainted> bankInfoReq);
        } else {
            io:println("Error occurred while reading branch locator response");
        }

        http:Response infomationResponse = new;
        var infoRes = bankInfoService -> post("", backendServiceReq);
        if (infoRes is http:Response) {
            infomationResponse = infoRes;
        } else {
            io:println("Error occurred while writing info response");
        }
        checkpanic caller->respond(infomationResponse);
    }
}

@http:ServiceConfig {
    basePath:"/bankinfo"
}
service Bankinfo on serviceEnpoint {

    @http:ResourceConfig {
        methods:["POST"]
    }
    resource function product(http:Caller caller, http:Request req) {
        http:Response res = new;
        var jsonRequest = req.getJsonPayload();
        if (jsonRequest is json) {
            string branchCode = jsonRequest.BranchInfo.BranchCode.toString();
            json payload = {};
            if (branchCode == "123") {
                payload = {"ABC Bank":{"Address":"111 River Oaks Pkwy, San Jose, CA 95999"}};
            } else {
                payload = {"ABC Bank":{"error":"No branches found."}};
            }
            res.setPayload(payload);
        } else {
            io:println("Error occurred while reading bank info request");
        }

        checkpanic caller->respond(res);
    }
}

@http:ServiceConfig {
    basePath:"/branchlocator"
}
service Banklocator on serviceEnpoint {

    @http:ResourceConfig {
        methods:["POST"]
    }
    resource function product(http:Caller caller, http:Request req) {
        http:Response res = new;
        var jsonRequest = req.getJsonPayload();
        if (jsonRequest is json) {
            string zipCode = jsonRequest.BranchLocator.ZipCode.toString();
            json payload = {};
            if (zipCode == "95999") {
                payload = {"ABCBank":{"BranchCode":"123"}};
            } else {
                payload = {"ABCBank":{"BranchCode":"-1"}};
            }
            res.setPayload(payload);
        } else {
            io:println("Error occurred while reading bank locator request");
        }

        checkpanic caller->respond(res);
    }
}


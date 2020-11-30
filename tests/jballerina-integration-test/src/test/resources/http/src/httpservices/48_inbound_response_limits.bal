// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

http:ClientConfiguration statusLineLimitConfig = {
    http1Settings: {
        maxStatusLineLength: 1024
    }
};

http:ClientConfiguration headerLimitConfig = {
    http1Settings: {
        maxHeaderSize: 1024
    }
};

http:ClientConfiguration  entityBodyLimitConfig = {
    http1Settings: {
        maxEntityBodySize: 1024
    }
};

http:Client statusLimitClient = new("http://localhost:9262/backend/statustest", statusLineLimitConfig);
http:Client headerLimitClient = new("http://localhost:9263/backend/headertest", headerLimitConfig);
http:Client entityBodyLimitClient = new("http://localhost:9264/backend/entitybodytest", entityBodyLimitConfig);


@http:ServiceConfig {basePath:"/responseLimit"}
service passthruLimitService on new http:Listener(9261) {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/{clientType}"
    }
    resource function testStatusline(http:Caller caller, http:Request req, string clientType) {
        http:Client clientEP = entityBodyLimitClient;
        if (clientType == "statusline") {
            clientEP = statusLimitClient;
        } else if (clientType == "header") {
            clientEP = headerLimitClient;
        }

        var clientResponse = clientEP->forward("/", <@untainted>req);
        if (clientResponse is http:Response) {
            var result = caller->respond(<@untainted>clientResponse);
            if (result is error) {
                log:printError("Error sending passthru response", result);
            }
        } else {
            http:Response res = new;
            res.statusCode = 500;
            res.setPayload(clientResponse.toString());
            var result = caller->respond(res);
            if (result is error) {
                log:printError("Error sending error response", result);
            }
        }
    }
}

@http:ServiceConfig {basePath:"/backend"}
service statusBackendService on new http:Listener(9262) {

    resource function statustest(http:Caller caller, http:Request req) {
        http:Response res = new;
        string testType = req.getHeader("x-test-type");
        if (testType == "error") {
            res.reasonPhrase = getStringLengthOf(1200);
        } else {
            res.reasonPhrase = "HELLO";
        }
        res.setTextPayload("Hello World!!!");
        sendResponse(caller, res);
    }

}
@http:ServiceConfig {basePath:"/backend"}
service headertBackendService on new http:Listener(9263) {
    resource function headertest(http:Caller caller, http:Request req) {
        http:Response res = new;
        string testType = req.getHeader("x-test-type");
        if (testType == "error") {
            res.setHeader("x-header", getStringLengthOf(2048));
        } else {
            res.setHeader("x-header", "Validated");
        }
        res.setTextPayload("Hello World!!!");
        sendResponse(caller, res);
    }
}
@http:ServiceConfig {basePath:"/backend"}
service entitybodyBackendService on new http:Listener(9264) {
    resource function entitybodytest(http:Caller caller, http:Request req) {
        http:Response res = new;
        string testType = req.getHeader("x-test-type");
        if (testType == "error") {
            res.setTextPayload(getStringLengthOf(2048));
        } else {
            res.setTextPayload("Small payload");
        }
        sendResponse(caller, res);
    }
}

function getStringLengthOf(int length) returns string {
    string builder = "";
    int i = 0;
    while (i < length) {
        builder = builder + "a";
        i = i + 1;
    }
    return builder;
}

function sendResponse(http:Caller caller, http:Response res) {
    var result = caller->respond(res);
    if (result is error) {
        log:printError("Error sending backend response", result);
    }
}

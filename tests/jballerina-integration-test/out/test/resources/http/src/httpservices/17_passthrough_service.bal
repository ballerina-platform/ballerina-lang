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
import ballerina/mime;

listener http:Listener passthroughEP1 = new(9113);

@http:ServiceConfig { basePath: "/passthrough" }
service passthroughService on passthroughEP1 {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function passthrough(http:Caller caller, http:Request clientRequest) {
        http:Client nyseEP1 = new("http://localhost:9113");
        var response = nyseEP1->get("/nyseStock/stocks", <@untainted> clientRequest);
        if (response is http:Response) {
            checkpanic caller->respond(response);
        } else {
            checkpanic caller->respond({ "error": "error occurred while invoking the service" });
        }
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/forwardMultipart"
    }
    resource function forwardMultipart(http:Caller caller, http:Request clientRequest) {
        http:Client nyseEP1 = new("http://localhost:9113");
        var response = nyseEP1->forward("/nyseStock/stocksAsMultiparts", clientRequest);
        if (response is http:Response) {
            checkpanic caller->respond(response);
        } else {
            checkpanic caller->respond({ "error": "error occurred while invoking the service" });
        }
    }
}

@http:ServiceConfig { basePath: "/nyseStock" }
service nyseStockQuote1 on passthroughEP1 {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/stocks"
    }
    resource function stocks(http:Caller caller, http:Request clientRequest) {
        checkpanic caller->respond({ "exchange": "nyse", "name": "IBM", "value": "127.50" });
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/stocksAsMultiparts"
    }
    resource function stocksAsMultiparts(http:Caller caller, http:Request clientRequest) {
        var bodyParts = clientRequest.getBodyParts();
        if (bodyParts is mime:Entity[]) {
            checkpanic caller->respond(<@untainted> bodyParts);
        } else {
            error err = bodyParts;
            checkpanic caller->respond(<@untainted> err.reason());
        }
    }
}

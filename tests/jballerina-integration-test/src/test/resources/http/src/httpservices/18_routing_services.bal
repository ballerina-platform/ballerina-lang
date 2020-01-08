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
import ballerina/io;

listener http:Listener serviceEP = new(9114);

http:Client nasdaqEP = new("http://localhost:9114/nasdaqStocks");

http:Client nyseEP2 = new("http://localhost:9114/nyseStocks");

@http:ServiceConfig {basePath:"/cbr"}
service contentBasedRouting on serviceEP {

    @http:ResourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource function cbrResource(http:Caller conn, http:Request req) {
        string nyseString = "nyse";
        var jsonMsg = req.getJsonPayload();
        string nameString = "";
        if (jsonMsg is json) {
            nameString = jsonMsg.name.toString();
        } else {
            io:println("Error getting payload");
        }
        http:Request clientRequest = new;
        http:Response clientResponse = new;
        if (nameString == nyseString) {
            var result = nyseEP2 -> post("/stocks", clientRequest);
            if (result is http:Response) {
                checkpanic conn->respond(<@untainted> result);
            } else  {
                clientResponse.statusCode = 500;
                clientResponse.setPayload("Error sending request");
                checkpanic conn->respond(clientResponse);
            }
        } else {
            var result = nasdaqEP -> post("/stocks", clientRequest);
            if (result is http:Response) {
                checkpanic conn->respond(<@untainted> result);
            } else {
                clientResponse.statusCode = 500;
                clientResponse.setPayload("Error sending request");
                checkpanic conn->respond(clientResponse);
            }
        }
    }
}

@http:ServiceConfig {basePath:"/hbr"}
service headerBasedRouting on serviceEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource function hbrResource(http:Caller caller, http:Request req) {
        string nyseString = "nyse";
        var nameString = req.getHeader("name");

        http:Request clientRequest = new;
        http:Response clientResponse = new;
        if (nameString == nyseString) {
            var result = nyseEP2 -> post("/stocks", clientRequest);
            if (result is http:Response) {
                checkpanic caller->respond(<@untainted> result);
            } else {
                clientResponse.statusCode = 500;
                clientResponse.setPayload("Error sending request");
                checkpanic caller->respond(clientResponse);
            }
        } else {
            var result = nasdaqEP -> post("/stocks", clientRequest);
            if (result is http:Response) {
                checkpanic caller->respond(<@untainted> result);
            } else {
                clientResponse.statusCode = 500;
                clientResponse.setPayload("Error sending request");
                checkpanic caller->respond(clientResponse);
            }
        }
    }
}

@http:ServiceConfig {basePath:"/nasdaqStocks"}
service nasdaqStocksQuote on serviceEP {

    @http:ResourceConfig {
        methods:["POST"]
    }
    resource function stocks(http:Caller caller, http:Request req) {
        json payload = {"exchange":"nasdaq", "name":"IBM", "value":"127.50"};
        http:Response res = new;
        res.setJsonPayload(payload);
        checkpanic caller->respond(res);
    }
}

@http:ServiceConfig {basePath:"/nyseStocks"}
service nyseStockQuote2 on serviceEP {

    @http:ResourceConfig {
        methods:["POST"]
    }
    resource function stocks(http:Caller caller, http:Request req) {
        json payload = {"exchange":"nyse", "name":"IBM", "value":"127.50"};
        http:Response res = new;
        res.setJsonPayload(payload);
        checkpanic caller->respond(res);
    }
}


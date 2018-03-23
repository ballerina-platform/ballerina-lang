// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/net.http;
import ballerina/io;

endpoint http:ServiceEndpoint initiatorEP {
    port:8888
};

@http:ServiceConfig {
    basePath:"/"
}
service<http:Service> InitiatorService bind initiatorEP {

    @http:ResourceConfig {
        path:"/"
    }
    member (endpoint conn, http:Request req) {
        endpoint http:ClientEndpoint ep {
            targets: [{uri: "http://localhost:8889/participant1"}]
        };
        transaction {
            http:Request newReq = {};
            var getResult = ep -> get("/", newReq);
            match getResult {
                http:HttpConnectorError err => {
                    sendErrorResponseToCaller(conn);
                    abort;
                }
                http:Response participant1Res => {
                    var fwdResult = conn -> forward(participant1Res); 
                    match fwdResult {
                        http:HttpConnectorError err => {
                            io:print("Could not forward response to caller:");
                            io:println(err);
                        }
                    }
                }
            }
        } onretry {
            io:println("Intiator failed");
        }
    }
}

function sendErrorResponseToCaller(http:ServiceEndpoint conn) {
    endpoint http:ServiceEndpoint conn2 = conn;
    http:Response errRes = {statusCode: 500};
    var respondResult = conn2 -> respond(errRes);
    match respondResult {
        http:HttpConnectorError respondErr => {
            io:print("Could not send error response to caller:");
            io:println(respondErr);
        }
    }
}

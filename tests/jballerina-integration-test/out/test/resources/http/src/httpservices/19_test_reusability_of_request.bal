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
import ballerina/log;
import ballerina/mime;

listener http:Listener testEP = new(9115);

http:Client clientEP1 = new("http://localhost:9115/test");

@http:ServiceConfig {basePath:"/reuseObj"}
service testService_1 on testEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/request_without_entity"
    }
    resource function getWithoutEntity(http:Caller caller, http:Request clientRequest) {
        http:Request clientReq = new;
        string firstVal = "";
        string secondVal = "";

        var firstResponse = clientEP1 -> get("", clientReq);
        if (firstResponse is http:Response) {
            var result = <@untainted> firstResponse.getTextPayload();
            if (result is string) {
                firstVal = result;
            } else {
                error err = result;
                firstVal = err.reason();
            }
        } else  {
            error err = firstResponse;
            firstVal = err.reason();
        }

        var secondResponse = clientEP1 -> get("", clientReq);
        if (secondResponse is http:Response) {
            var result = <@untainted> secondResponse.getTextPayload();
            if (result is string) {
                secondVal = result;
            } else {
                error err = result;
                secondVal = err.reason();
            }
        } else {
            error err = secondResponse;
            secondVal = err.reason();
        }
        http:Response testResponse = new;
        testResponse.setPayload(firstVal + secondVal);
        checkpanic caller->respond(testResponse);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/request_with_empty_entity"
    }
    resource function getWithEmptyEntity(http:Caller caller, http:Request clientRequest) {
        http:Request clientReq = new;
        mime:Entity entity = new;
        clientReq.setEntity(entity);

        string firstVal = "";
        string secondVal = "";

        var firstResponse = clientEP1 -> get("", clientReq);
        if (firstResponse is http:Response) {
            var result = <@untainted> firstResponse.getTextPayload();
            if (result is string) {
                firstVal = result;
            } else {
                error err = result;
                firstVal = err.reason();
            }
        } else {
            error err = firstResponse;
            firstVal = err.reason();
        }

        var secondResponse = clientEP1 -> get("", clientReq);
        if (secondResponse is http:Response) {
            var result = <@untainted> secondResponse.getTextPayload();
            if (result is string) {
                secondVal = result;
            } else {
                error err = result;
                secondVal = err.reason();
            }
        } else {
            error err = secondResponse;
            secondVal = err.reason();
        }
        http:Response testResponse = new;
        testResponse.setPayload(firstVal + secondVal);
        checkpanic caller->respond(testResponse);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/two_request_same_entity"
    }
    resource function getWithEntity(http:Caller caller, http:Request clientRequest) {
        http:Request clientReq = new;
        clientReq.setHeader("test1", "value1");
        http:Request newRequest = new;
        string firstVal = "";
        string secondVal = "";
        http:Response testResponse = new;

        var entity = clientReq.getEntity();
        if (entity is mime:Entity) {
            newRequest.setEntity(entity);
            var firstResponse = clientEP1 -> get("", clientReq);
            if (firstResponse is http:Response) {
                newRequest.setHeader("test2", "value2");
                var secondResponse = clientEP1 -> get("", newRequest);
                if (secondResponse is http:Response) {
                    var result1 = <@untainted> firstResponse.getTextPayload();
                    if (result1 is string) {
                        firstVal = result1;
                    } else {
                        error err = result1;
                        firstVal = err.reason();
                    }

                    var result2 = <@untainted> secondResponse.getTextPayload();
                    if (result2 is string) {
                        secondVal = result2;
                    } else {
                        error err = result2;
                        secondVal = err.reason();
                    }
                } else {
                    error err = secondResponse;
                    log:printError(err.reason(), err);
                }
            } else {
                error err = firstResponse;
                log:printError(err.reason(), err);
            }
        } else {
            error err = entity;
            log:printError(err.reason(), err);
        }
        testResponse.setTextPayload(firstVal + secondVal);
        checkpanic caller->respond(testResponse);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/request_with_datasource"
    }
    resource function postWithEntity(http:Caller caller, http:Request clientRequest) {
        http:Request clientReq = new;
        clientReq.setTextPayload("String datasource");

        string firstVal;
        string secondVal;
        var firstResponse = clientEP1 -> post("/datasource", clientReq);
        if (firstResponse is http:Response) {
            var result = <@untainted> firstResponse.getTextPayload();
            if (result is string) {
                firstVal = result;
            } else {
                error err = result;
                firstVal = err.reason();
            }
        } else {
            error err = firstResponse;
            firstVal = err.reason();
        }

        var secondResponse = clientEP1 -> post("/datasource", clientReq);
        if (secondResponse is http:Response) {
            var result = <@untainted> secondResponse.getTextPayload();
            if (result is string) {
                secondVal = result;
            } else {
                error err = result;
                secondVal = err.reason();
            }
        } else {
            error err = secondResponse;
            secondVal = err.reason();
        }
        http:Response testResponse = new;
        testResponse.setPayload(firstVal + secondVal);
        checkpanic caller->respond(testResponse);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/request_with_bytechannel"
    }
    resource function postWithByteChannel(http:Caller caller, http:Request clientRequest) {
        http:Request clientReq = new;
        var byteChannel = clientRequest.getByteChannel();
        if (byteChannel is io:ReadableByteChannel) {
            clientReq.setByteChannel(byteChannel, "text/plain");
            var firstResponse = clientEP1 -> post("/consumeChannel", clientReq);
            if (firstResponse is http:Response) {
                var secondResponse = clientEP1 -> post("/consumeChannel", clientReq);
                http:Response testResponse = new;
                string firstVal;
                string secondVal;
                if (secondResponse is http:Response) {
                    var result1 = secondResponse.getTextPayload();
                    if  (result1 is string) {
                        secondVal = result1;
                    } else {
                        secondVal = "Error in parsing payload";
                    }
                } else {
                    error err = secondResponse;
                    secondVal = <string> err.detail()["message"];
                }

                var result2 = firstResponse.getTextPayload();
                if (result2 is string) {
                    firstVal = result2;
                } else {
                    error err = result2;
                    firstVal = err.reason();
                }

                testResponse.setTextPayload(<@untainted> firstVal + <@untainted> secondVal);
                checkpanic caller->respond(testResponse);
            } else {
                error err = firstResponse;
                log:printError(err.reason(), err);
            }
        } else {
            error err = byteChannel;
            log:printError(err.reason(), err);
        }
    }
}

@http:ServiceConfig {basePath:"/test"}
service testService_2 on testEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource function testForGet(http:Caller caller, http:Request clientRequest) {
        http:Response response = new;
        response.setTextPayload("Hello from GET!");
        checkpanic caller->respond(response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/datasource"
    }
    resource function testForPost(http:Caller caller, http:Request clientRequest) {
        http:Response response = new;
        response.setTextPayload("Hello from POST!");
        checkpanic caller->respond(response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/consumeChannel"
    }
    resource function testRequestBody(http:Caller caller, http:Request clientRequest) {
        http:Response response = new;
        var stringPayload = clientRequest.getTextPayload();
        if (stringPayload is string) {
            response.setPayload(<@untainted> stringPayload);
        } else  {
            error err = stringPayload;
            string errMsg = <string> err.detail()["message"];
            response.setPayload(<@untainted> errMsg);
        }
        checkpanic caller->respond(response);
    }
}

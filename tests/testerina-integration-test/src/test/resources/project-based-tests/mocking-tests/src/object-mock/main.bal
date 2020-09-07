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

import ballerina/io;
import ballerina/http;
//import ballerina/email;

// main bal

http:Client clientEndpoint = new("http://postman-echo.com");
//email:SmtpClient smtpClient = new ("localhost", "admin","admin");

function doGet() returns http:Response {
    http:Request req = new;
    req.addHeader("Sample-Name", "http-client-connector");

    http:Response|error result = clientEndpoint->get("/get?test=123", req);
    http:Response response = <http:Response>result;
    io:println(response.statusCode);
    return response;
}

function doGetBasic() returns http:Response {

    http:Response|error result = clientEndpoint->get("/get?test=1234");
    http:Response response = <http:Response>result;
    io:println(response.statusCode);
    return response;
}

function doGetRepeat() returns http:Response {
    http:Response|error result = clientEndpoint->get("/healthz");
    http:Response response = <http:Response>result;
    if(response.statusCode == 200) {
        result = clientEndpoint->get("/get?test=1234");
        response = <http:Response>result;
        io:println(response.statusCode);
    }
    return response;
}


//function sendNotification(string[] emailIds) returns error? {
//    email:Email msg = {
//        'from: "builder@abc.com",
//        subject: "Error Alert ...",
//        to: emailIds,
//        body: ""
//    };
//    email:Error? response = smtpClient->send(msg);
//
//    if (response is error) {
//      io:println("error while sending the email: " + response.message());
//      return response;
//    }
//}

function getClientUrl() returns string {
    return clientEndpoint.url;
}

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

import ballerina/auth;
import ballerina/http;

function testCanProcessHttpBearerAuthWithoutHeader() returns @tainted boolean {
    CustomAuthProvider customAuthProvider = new;
    http:BearerAuthHandler handler = new(customAuthProvider);
    http:Request inRequest = createRequest();
    string bearerAuthHeaderValue = "123Bearer xxxxxx";
    inRequest.setHeader("123Authorization", bearerAuthHeaderValue);
    return handler.canProcess(inRequest);
}

function testCanProcessHttpBearerAuth() returns boolean {
    CustomAuthProvider customAuthProvider = new;
    http:BearerAuthHandler handler = new(customAuthProvider);
    http:Request inRequest = createRequest();
    string bearerAuthHeaderValue = "Bearer xxxxxx";
    inRequest.setHeader("Authorization", bearerAuthHeaderValue);
    return <@untainted> handler.canProcess(inRequest);
}

function testHandleHttpBearerAuthFailure() returns boolean|http:AuthenticationError {
    CustomAuthProvider customAuthProvider = new;
    http:BearerAuthHandler handler = new(customAuthProvider);
    http:Request inRequest = createRequest();
    string bearerAuthHeaderValue = "Bearer YW1pbGE6cHFy";
    inRequest.setHeader("Authorization", bearerAuthHeaderValue);
    return handler.process(inRequest);
}

function testHandleHttpBearerAuth() returns boolean|http:AuthenticationError {
    CustomAuthProvider customAuthProvider = new;
    http:BearerAuthHandler handler = new(customAuthProvider);
    http:Request inRequest = createRequest();
    string bearerAuthHeaderValue = "Bearer aXN1cnU6eHh4";
    inRequest.setHeader("Authorization", bearerAuthHeaderValue);
    return handler.process(inRequest);
}

function createRequest() returns http:Request {
    http:Request inRequest = new;
    inRequest.rawPath = "/helloWorld/sayHello";
    inRequest.method = "GET";
    inRequest.httpVersion = "1.1";
    return inRequest;
}

public class CustomAuthProvider {

    *auth:InboundAuthProvider;

    public function authenticate(string credential) returns boolean|auth:Error {
        return credential == "aXN1cnU6eHh4";
    }
}

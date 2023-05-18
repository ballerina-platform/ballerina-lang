// Copyright (c) 2022 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/http as http2;
import project.http as http;

service / on new http2:Listener(8090) {
    isolated resource function get cases/[string shortCountryName]() returns json|error|int {
        int value = 1;
        int answer = 0;
        if (value >= 10) {
            http:Client clientEP = check new ("https://postman-echo.com/post");
            json clientResponse = check clientEP->get("/");
            return clientResponse;
        }

        else {
            http:Client clientEP = check new ("https://postman-echo.com/post");
            json clientResponse = check clientEP->get("/");
            answer = <int>clientResponse;
        }
        return answer;
    }

    isolated resource function get cases2/[string shortCountryName]() returns json|error|int {
        int value = 1;
        int answer = 0;
        if (value >= 10) {
            http:Client clientEP = check new ("https://postman-echo.com/post2");
            json clientResponse = check clientEP->get("/");
        }

        else {
            http:Client clientEP = check new ("https://postman-echo.com/post2");
            json clientResponse = check clientEP->get("/");
            answer = <int>clientResponse;
            return clientResponse;
        }
        return answer;
    }

    isolated resource function get cases3/[string shortCountryName]() returns json|error|int {
        int value = 1;
        int answer = 0;
        if (value >= 10) {
            http:Client clientEP = check new ("https://postman-echo.com/post3");
            json clientResponse = check clientEP->get("/");
            return clientResponse;
        }

        else {
            http:Client clientEP = check new ("https://postman-echo.com/post3");
            json clientResponse = check clientEP->get("/");
            answer = <int>clientResponse;
            return clientResponse;
        }

    }

    isolated resource function get cases4/[string shortCountryName]() returns int|error{
        int value=1;
        int value2=10;
        decimal totalCases=0;
        int population=0;
        int answer=0;
        if (value >= 10) {
            if (value2 >=10){
                http:Client clientEP = check new ("https://postman-echo.com/post4");
                json clientResponse = check clientEP->get("/");
                return 4;
            }
            else {
                http:Client clientEP = check new ("https://postman-echo.com/post4");
                json clientResponse = check clientEP->get("/");
                answer = 4;
            }
            return answer;
        }
        return answer;
    }
}


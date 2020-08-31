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
import ballerina/observe;
import ballerina/testobserve;

@http:ServiceConfig {
    basePath:"/mock-tracer"
}
service mockTracer on new http:Listener(9090) {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/spans/{serviceName}"
    }
    resource function getMockTracers(http:Caller caller, http:Request clientRequest, string serviceName) {
        json spans = testobserve:getFinishedSpans(serviceName);
        http:Response res = new;
        res.setJsonPayload(spans);
        checkpanic caller->respond(res);
    }
}

@observe:Observable
function calculateSumWithObservableFunction(int a, int b) returns int {
    var sum = a + b;
    return a + b;
}

type AbstractObservableAdder abstract object {
    @observe:Observable
    function getSum() returns int;
};

type ObservableAdder object {
    private int firstNumber;
    private int secondNumber;

    function init(int a, int b) {
        self.firstNumber = a;
        self.secondNumber = b;
    }

    function getSum() returns int {
        return self.firstNumber + self.secondNumber;
    }
};

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

import ballerina/observe;
import ballerina/observe.mockextension;
import ballerina/testobserve;
import intg_tests/tracing_tests.utils as utils;

utils:MockClient testClient = new();

@display { label: "mockTracer" }
service /mockTracer on new testobserve:Listener(19090) {
    resource function post getMockTraces(testobserve:Caller caller, string serviceName) {
        mockextension:Span[] spans = mockextension:getFinishedSpans(serviceName);
        json spansJson = checkpanic spans.cloneWithType(json);
        checkpanic caller->respond(spansJson.toJsonString());
    }
}

function panicAfterCalculatingSum(int a) {
    var sum = 0;
    foreach var i in 1 ... a {
        sum = sum + i;
    }
    error e = error("Test Error. Sum: " + sum.toString());
    panic e;
}

function calculateSum(int a, int b) returns int {
    var sum = a + b;
    return a + b;
}

@observe:Observable
function calculateSumWithObservability(int a, int b) returns int {
    var sum = a + b;
    return a + b;
}

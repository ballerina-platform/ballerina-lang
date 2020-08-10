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
import ballerina/runtime;

@http:ServiceConfig {
    basePath:"/test-service"
}
service testServiceThree on new http:Listener(9093) {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/resource-1"
    }
    resource function resourceOne(http:Caller caller, http:Request clientRequest) {
        testWorkerInteractions(11);
        testWorkerInteractions(12);
        testWorkerInteractions(13);
        testWorkerOnAnyStrand();
        testForkWorkers();

        http:Response outResponse = new;
        outResponse.setTextPayload("Hello, World! from resource one");
        checkpanic caller->respond(outResponse);
    }
}

function testWorkerInteractions(int c) {
    worker w1 {
        int a = c + 3;
        if (c == 11) {
            runtime:sleep(100);  // Sleep to make the workers not finish together
        }
        a -> w2;
    }
    worker w2 {
        if (c == 12) {
            runtime:sleep(100);  // Sleep to make the workers not finish together
        }
        int b = <- w1;
        if (b != (c + 3)) {
            error err = error("worker interaction failed");
            panic err;
        }
    }
    wait w2;
}

function testWorkerOnAnyStrand() {
    int n = 10000;
    int expectedSum = getSumOfNumbers(n);
    worker w3 {
        int actualSum = getSumOfNumbers(n);
        if (actualSum != expectedSum) {
            error err = error("failed to find the sum of first " + n.toString() + " numbers. actualSum: "
                + actualSum.toString() + " expectedSum: " + expectedSum.toString());
            panic err;
        }
    }
    @strand {
        thread: "any"
    }
    wait w3;
}

function testForkWorkers() {
    int n = 100;
    fork {
        worker w4 returns int {
            int result = 0;
            foreach int i in 1...10 {
                result = result + n;
            }
            runtime:sleep(100);  // Sleep to make the workers not finish together
            return result;
        }
        worker w5 returns int {
            int result = n * 10;
            return result;
        }
    }
    var expectedResult = 1000;
    record {int w4; int w5;} actualResults = wait {w4, w5};
    if (actualResults.w4 != expectedResult && actualResults.w5 != expectedResult) {
        error err = error("failed to find the sum of first " + n.toString() + " numbers. w4 result: "
            + actualResults.w4.toString() + " w5 result: " + actualResults.w5.toString() + " expectedResult: "
            + expectedResult.toString());
        panic err;
    }
}

function getSumOfNumbers(int n) returns int {
    int sum = 0;
    foreach var i in 1 ... n {
        sum += i;
    }
    return sum;
}

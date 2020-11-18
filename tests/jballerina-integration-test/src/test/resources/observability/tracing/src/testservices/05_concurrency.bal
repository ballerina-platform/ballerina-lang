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

import ballerina/testobserve;

service testServiceFour on new testobserve:Listener(9094) {
    # Resource function for testing async remote call wait
    resource function resourceOne(testobserve:Caller caller) {
        future<int> futureSum = start testClient->calculateSum(6, 17);
        var sum = wait futureSum;
        if (sum != 23) {    // Check for validating if normal execution is intact from instrumentation
            error err = error("failed to find the sum of 6 and 17. expected: 23 received: " + sum.toString());
            panic err;
        }
        checkpanic caller->respond("Invocation Successful");
    }

    # Resource function for testing async observable call wait
    resource function resourceTwo(testobserve:Caller caller) {
        future<int> futureSum = start calculateSumWithObservability(18, 31);
        var sum = wait futureSum;
        if (sum != 49) {    // Check for validating if normal execution is intact from instrumentation
            error err = error("failed to find the sum of 18 and 31. expected: 49 received: " + sum.toString());
            panic err;
        }
        checkpanic caller->respond("Invocation Successful");
    }

    # Resource function for testing async observable call wait
    resource function resourceThree(testobserve:Caller caller) {
        ObservableAdderClass adder = new ObservableAdder(61, 23);
        future<int> futureSum = start adder.getSum();
        var sum = wait futureSum;
        if (sum != 84) {    // Check for validating if normal execution is intact from instrumentation
            error err = error("failed to find the sum of 61 and 23. expected: 84 received: " + sum.toString());
            panic err;
        }
        checkpanic caller->respond("Invocation Successful");
    }

    # Resource function for testing worker interactions with no delays in worker completions
    resource function resourceFour(testobserve:Caller caller) {
        testWorkerInteractions(10);     // "10" adds no delays
        checkpanic caller->respond("Invocation Successful");
    }

    # Resource function for testing worker interactions with delayed writing
    resource function resourceFive(testobserve:Caller caller) {
        testWorkerInteractions(11);     // "11" adds delayed writing
        checkpanic caller->respond("Invocation Successful");
    }

    # Resource function for testing worker interactions with delayed reading
    resource function resourceSix(testobserve:Caller caller) {
        testWorkerInteractions(12);     // "12" adds delayed reading
        checkpanic caller->respond("Invocation Successful");
    }

    # Resource function for testing forked workers
    resource function resourceSeven(testobserve:Caller caller) {
        int n = 100;
        fork {
            worker w3 returns int {
                int result = 0;
                foreach int i in 1...10 {
                    result = result + n;
                }
                testobserve:sleep(100);  // Sleep to make the workers not finish together
                return result;
            }
            worker w4 returns int {
                int result = n * 10;
                return result;
            }
        }
        var expectedResult = 1000;
        record {int w3; int w4;} actualResults = wait {w3, w4};
        if (actualResults.w3 != expectedResult && actualResults.w4 != expectedResult) {
            error err = error("failed to find the sum of first " + n.toString() + " numbers. w3 result: "
                + actualResults.w3.toString() + " w4 result: " + actualResults.w4.toString() + " expectedResult: "
                + expectedResult.toString());
            panic err;
        }
        checkpanic caller->respond("Invocation Successful");
    }
}

function testWorkerInteractions(int c) {
    worker w1 {
        int a = c + 3;
        if (c == 11) {
            testobserve:sleep(1000);  // Sleep to make the workers not finish together
        }
        a -> w2;
    }
    worker w2 {
        if (c == 12) {
            testobserve:sleep(1000);  // Sleep to make the workers not finish together
        }
        int b = <- w1;
        if (b != (c + 3)) {
            error err = error("worker interaction failed");
            panic err;
        }
    }
    wait w2;
    wait w1;
}

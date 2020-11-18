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

service testServiceOne on new testobserve:Listener(9091) {
    # Resource function for testing whether no return functions are instrumented properly.
    resource function resourceOne(testobserve:Caller caller, string body) {
        int numberCount = checkpanic 'int:fromString(body);
        var sum = 0;
        foreach var i in 1 ... numberCount {
            sum = sum + i;
        }
        checkpanic caller->respond("Sum of numbers: " + sum.toString());
    }

    # Resource function for testing whether optional error return functions are instrumented properly.
    resource function resourceTwo(testobserve:Caller caller, string body) returns error? {
        int numberCount = checkpanic 'int:fromString(body);
        var sum = 0;
        foreach var i in 1 ... numberCount {
            sum = sum + i;
        }
        checkpanic caller->respond("Sum of numbers: " + sum.toString());
    }

    # Resource function for testing whether returning errors from the resource functions are handled properly.
    resource function resourceThree(testobserve:Caller caller) returns error? {
        var sum = 0;
        foreach var i in 1 ... 10 {
            sum = sum + i;
        }
        var expectedSum = 55;
        if (sum != expectedSum) {   // Check for validating if normal execution is intact from instrumentation
            error err = error("failed to find the sum of 10 numbers. expected: " + expectedSum.toString()
                + ", but found: " + sum.toString());
            panic err;
        }
        error e = error("Test Error 1");
        return e;
    }

    # Resource function for testing whether panicking from within resource function body is handled properly.
    resource function resourceFour(testobserve:Caller caller) returns error? {
        var sum = 0;
        foreach var i in 1 ... 10 {
            sum = sum + i;
        }
        var expectedSum = 55;
        if (sum != expectedSum) {   // Check for validating if normal execution is intact from instrumentation
            error err = error("failed to find the sum of 10 numbers. expected: " + expectedSum.toString()
                + ", but found: " + sum.toString());
            panic err;
        } else {
            error e = error("Test Error 2");
            panic e;
        }
    }

    # Resource function for testing whether panicking from within a function call is handled properly.
    resource function resourceFive(testobserve:Caller caller) returns error? {
        var sum = panicAfterCalculatingSum(13);
        checkpanic caller->respond("Sum of numbers: " + sum.toString());
    }

    # Resource function for testing whether panicking from within a function pointer is handled properly.
    resource function resourceSix(testobserve:Caller caller) returns error? {
        var calFunc = panicAfterCalculatingSum;
        var sum = calFunc(17);
        checkpanic caller->respond("Sum of numbers: " + sum.toString());
    }

    # Resource function for testing calling an observable function from within resource
    resource function resourceSeven(testobserve:Caller caller) {
        MockClient testClient1 = new();
        var ret = checkpanic testClient1->callWithReturn(5, 7);
        var expectedSum = 12;
        if (ret != expectedSum) {   // Check for validating if normal execution is intact from instrumentation
            error err = error("failed to find the sum of 10 numbers. expected: " + expectedSum.toString()
                + ", but found: " + ret.toString());
            panic err;
        }
        checkpanic caller->respond("Sum of numbers: " + ret.toString());
    }

    # Resource function for testing early return is handled properly.
    resource function resourceEight(testobserve:Caller caller) returns error? {
        var sum = trap testClient->callWithReturn(3, 13);
        if (sum is int && sum == 16) {
            checkpanic caller->respond("Successfully executed");
            return;
        } else {
            checkpanic caller->respond("Unexpected return value");
        }
        testClient->callWithNoReturn(5, 17, 22);
    }
}

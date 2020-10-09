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

service testServiceTwo on new testobserve:Listener(9092) {
    # Resource function for testing remote call which calls another remote call
    resource function resourceOne(testobserve:Caller caller) {
        testClient->callAnotherRemoteFunction();
        checkpanic caller->respond("Invocation Successful");
    }

    # Resource function for testing check on error return
    resource function resourceTwo(testobserve:Caller caller) returns error? {
        check testClient->callWithErrorReturn();
        checkpanic caller->respond("Invocation Successful");
    }

    # Resource function for testing checkpanic on error return
    resource function resourceThree(testobserve:Caller caller) returns error? {
        checkpanic testClient->callWithErrorReturn();
        checkpanic caller->respond("Invocation Successful");
    }

    # Resource function for testing ignored error return
    resource function resourceFour(testobserve:Caller caller) returns error? {
        var ret = testClient->callWithErrorReturn();
        checkpanic caller->respond("Invocation Successful");
    }

    # Resource function for testing whether trapping a panic from a function is handled properly.
    resource function resourceFive(testobserve:Caller caller) returns error? {
        var sum = trap testClient->callWithPanic();
        if (sum is error) {
            checkpanic caller->respond("Successfully trapped panic: " + sum.message());
        } else {
            checkpanic caller->respond("Sum of numbers: " + sum.toString());
        }
    }
}

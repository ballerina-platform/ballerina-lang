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

service testServiceThree on new testobserve:Listener(9093) {
    # Resource function for testing function call with observable annotation
    resource function resourceOne(testobserve:Caller caller) {
        var sum = calculateSumWithObservability(10, 51);
        if (sum != 61) {    // Check for validating if normal execution is intact from instrumentation
            error err = error("failed to find the sum of 10 and 51. expected: 61 received: " + sum.toString());
            panic err;
        }
        checkpanic caller->respond("Invocation Successful");
    }

    # Resource function for testing attached function call with observable annotation
    resource function resourceTwo(testobserve:Caller caller) {
        ObservableAdderClass adder = new ObservableAdder(20, 34);
        var sum = adder.getSum();
        if (sum != 54) {    // Check for validating if normal execution is intact from instrumentation
            error err = error("failed to find the sum of 20 and 34. expected: 54 received: " + sum.toString());
            panic err;
        }
        checkpanic caller->respond("Invocation Successful");
    }
}

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
    # Resource function for testing async call wait
    resource function resourceOne(testobserve:Caller caller) {
        future<int> futureSum = start testClient->calculateSum(6, 17);
        var sum = wait futureSum;
        if (sum != 23) {
            error err = error("failed to find the sum of 6 and 17. expected: 23 received: " + sum.toString());
            panic err;
        }
        checkpanic caller->respond("Invocation Successful");
    }
}

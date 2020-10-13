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
import ballerina/lang.'int;

service testServiceOne on new testobserve:Listener(9091) {
    resource function resourceOne(testobserve:Caller caller) {
        var ret = caller->respond("Hello from Resource One");
    }

    resource function resourceTwo(testobserve:Caller caller, string body) returns error? {
        int numberCount = check 'int:fromString(body);
        var sum = 0;
        foreach var i in 1 ... numberCount {
            sum = sum + i;
        }
        var ret = caller->respond("Sum of numbers: " + sum.toString());
    }

    resource function resourceThree(testobserve:Caller caller) returns error? {
        error e = error("Test Error");
        panic e;
    }
}

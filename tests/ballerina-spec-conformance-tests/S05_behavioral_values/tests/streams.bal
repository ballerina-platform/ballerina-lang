// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/runtime;
import ballerina/test;

const EXPECTED_SUBSCRIBER_TO_UPDATE_VALUE_FAILURE_MESSAGE =
                                "expected value to be updated via subscriber function invocation";

// stream-type-descriptor := stream < type-descriptor >

// A value of type stream<T> is a distributor for values of type T: 
// when a value v of type T is put into the stream, 
// a function will be called for each subscriber to the stream with v as an argument. 
// T must be a pure type.
const int CONST_INT = 1000;

int globalVarOne = 0;
int globalVarTwo = 0;

@test:Config {}
function testStreams() {
    stream<int> s = new;
    s.subscribe(subscriberOneFunc);
    s.subscribe(subscriberTwoFunc);
    s.publish(CONST_INT);

    int counter = 20;
    while ((globalVarOne == 0 || globalVarTwo == 0) && counter >= 0) {
        runtime:sleep(100);
        counter -= 1;
    }

    test:assertEquals(globalVarOne, CONST_INT,  msg = EXPECTED_SUBSCRIBER_TO_UPDATE_VALUE_FAILURE_MESSAGE);
    test:assertEquals(globalVarTwo, CONST_INT,  msg = EXPECTED_SUBSCRIBER_TO_UPDATE_VALUE_FAILURE_MESSAGE);
}

function subscriberOneFunc(int i) {
    globalVarOne = i;
}

function subscriberTwoFunc(int i) {
    globalVarTwo = i;
}

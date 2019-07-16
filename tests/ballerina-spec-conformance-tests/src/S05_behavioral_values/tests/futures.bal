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

import ballerina/test;

const EXPECTED_FUTURE_TO_WAIT_FAILURE_MESSAGE = "expected future to wait for named worker to return an int";

// future-type-descriptor := future < type-descriptor >
// A future value represents a value to be returned by a named worker.
// A future value belongs to a type future<T> if the value to be returned belongs to T.
@test:Config {}
function testFutures() {
    worker w1 returns int {
        return 200;
    }
    future<int> sampleFuture = w1;
    any result = wait sampleFuture;
    any futureVariable = sampleFuture;
    test:assertTrue(result is int, msg = "expected future return value to belong to type int");
    test:assertTrue(futureVariable is future<int>, msg = "expected future that returns int to belong to future<int>");
    test:assertEquals(result, 200, msg = "expected future to wait for named worker to return an int");
}

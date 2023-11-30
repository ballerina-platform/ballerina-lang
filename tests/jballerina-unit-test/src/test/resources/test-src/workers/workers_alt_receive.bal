// Copyright (c) 2023 WSO2 LLC.
//
// WSO2 LLC. licenses this file to you under the Apache License,
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
import ballerina/lang.runtime;

function workerAlternateReceiveTest() {
    worker w1 {
        2 -> w2;
    }

    worker w2 returns int {
        int result = <- w1 | w3;
        return result;
    }

    worker w3 {
        runtime:sleep(2);
        3 -> w2;
    }

    worker w4 {
        runtime:sleep(2);
        2 -> w5;
    }

    worker w5 returns int {
        int result = <- w4 | w6;
        return result;
    }

    worker w6 {
        3 -> w5;
    }

    map<int> results = wait {a: w2, b: w5};
    test:assertEquals(results["a"], 2, "Invalid int result");
    test:assertEquals(results["b"], 3, "Invalid int result");
}

function workerAlternateReceiveTest2() {
    worker w1 {
        runtime:sleep(2);
        1 -> w5;
    }

    worker w2 {
        runtime:sleep(3);
        2 -> w5;
    }

    worker w3 {
        3 -> w5;
    }

    worker w4 {
        runtime:sleep(4);
        4 -> w5;
    }

    worker w5 returns int {
        int result = <- w1 | w2 | w3 | w4;
        return result;
    }

    int result = wait w5;
    test:assertEquals(result, 3, "Invalid int result");
}

function alternateReceiveWithPanic() {
    worker w1 {
        int value = 10;
        if value == 10 {
            panic error("Error in worker 1");
        }
        value -> w2;
    }

    worker w2 returns int {
        int a = <- w1 | w3;
        return a;
    }

    worker w3 {
        runtime:sleep(2);
        3 -> w2;
    }

    int|error unionResult = trap wait w2;
    test:assertTrue(unionResult is error, "Expected error result");
    error e = <error>unionResult;
    test:assertEquals(e.message(), "Error in worker 1", "Invalid error message");
}

function alternateReceiveWithMultiplePanic() {
    worker w1 {
        int value = 10;
        if value == 10 {
            panic error("Error in worker 1");
        }
        value -> w2;
    }

    worker w2 returns int {
        int a = <- w1 | w3;
        return a;
    }

    worker w3 {
        int value = 10;
        if value == 10 {
            panic error("Error in worker 3");
        }
        3 -> w2;
    }

    int|error unionResult = trap wait w2;
    test:assertTrue(unionResult is error, "Expected error result");
    error e = <error>unionResult;
    boolean validError = e.message() == "Error in worker 1" || e.message() == "Error in worker 3";
    test:assertTrue(validError, "Invalid error message");
}

function alternateReceiveWithError() {
    worker w1 returns error? {
        int value = 10;
        if value == 10 {
            return error("Error in worker 1");
        }
        value -> w2;
    }

    worker w2 returns int|error {
        int|error a = <- w1 | w3;
        return a;
    }

    worker w3 {
        runtime:sleep(2);
        3 -> w2;
    }

    int|error unionResult = wait w2;
    test:assertTrue(unionResult is int, "Expected int result");
    test:assertEquals(unionResult, 3, "Invalid int result");
}

function alternateReceiveWithMultipleError() {
    worker w1 returns error? {
        int value = 10;
        if value == 10 {
            return error("Error in worker 1");
        }
        value -> w2;
    }

    worker w2 returns int|error {
        int|error a = <- w1 | w3;
        return a;
    }

    worker w3 returns error? {
        int value = 10;
        runtime:sleep(2);
        if value == 10 {
            return error("Error in worker 3");
        }
        value -> w2;
    }

    int|error unionResult = wait w2;
    test:assertTrue(unionResult is error, "Expected error result");
    error e = <error>unionResult;
    test:assertEquals(e.message(), "Error in worker 3", "Invalid error message");
}



function alternateReceiveWithPanicAndError() {
    worker w1 {
        int value = 10;
        if value == 10 {
            panic error("Error in worker 1");
        }
        value -> w2;
    }

    worker w2 returns int|error {
        int|error a = <- w1 | w3;
        return a;
    }

    worker w3 returns error? {
        int value = 10;
        if value == 10 {
            return error("Error in worker 3");
        }
        value -> w2;
    }

    int|error unionResult = trap wait w2;
    test:assertTrue(unionResult is error, "Expected error result");
    error e = <error>unionResult;
    test:assertEquals(e.message(), "Error in worker 1", "Invalid error message");
}

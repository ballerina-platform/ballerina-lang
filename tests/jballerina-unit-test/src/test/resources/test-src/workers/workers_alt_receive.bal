// Copyright (c) 2024 WSO2 Inc. (http://www.wso2.com).
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

function alternateReceiveWithSenderPanic() {
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

function alternateReceiveWithSenderError() {
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

function alternateReceiveWithReceiverPanic() {
    worker w1 {
        10 -> w2;
    }

    worker w2 returns int {
        int value = 10;
        if value == 10 {
            panic error("Error in worker 2");
        }
        int a = <- w1 | w3;
        return a;
    }

    worker w3 {
        3 -> w2;
    }

    int|error unionResult = trap wait w2;
    test:assertTrue(unionResult is error, "Expected error result");
    error e = <error>unionResult;
    test:assertEquals(e.message(), "Error in worker 2", "Invalid error message");
}

function alternateReceiveWithReceiverError() {
    worker w1 {
        10 -> w2;
    }

    worker w2 returns int|error {
        int value = 10;
        if value == 10 {
            return error("Error in worker 2");
        }
        int a = <- w1 | w3;
        return a;
    }

    worker w3 {
        3 -> w2;
    }

    int|error unionResult = wait w2;
    test:assertTrue(unionResult is error, "Expected error result");
    error e = <error>unionResult;
    test:assertEquals(e.message(), "Error in worker 2", "Invalid error message");
}

function alternateReceiveWithSameWorkerSend() {
    worker w1 {
        1 -> w2;
        2 -> w2;
    }

    worker w2 returns int {
        int x = <- w1 | w1;
        return x;
    }

    int intResult = wait w2;
    test:assertEquals(intResult, 1, "Invalid int result");
}

function alternateReceiveWithSameWorkerSendError1() {
    worker w1 {
        int|error x = int:fromString("invalid");
        x -> w2;
        2 -> w2;
    }

    worker w2 returns int|error {
        int|error x = <- w1 | w1;
        return x;
    }

    int|error intResult = wait w2;
    test:assertEquals(intResult, 2, "Invalid int result");
}


function alternateReceiveWithSameWorkerSendError2() {
    worker w1 {
        int|error x = int:fromString("invalid");
        x -> w2;
        x -> w2;
    }

    worker w2 returns int|error {
        int|error x = <- w1 | w1;
        return x;
    }

    int|error intResult = wait w2;
    test:assertTrue(intResult is error, "Expected error result");
    error e = <error>intResult;
    test:assertEquals(e.message(), "{ballerina/lang.int}NumberParsingError", "Invalid error result");
    test:assertEquals(e.detail().toString(), "{\"message\":\"'string' value 'invalid' cannot be converted to 'int'\"}",
     "Invalid error result");
}

function alternateReceiveWithSameWorkerSendPanic() {
    worker w1 {
        int x = checkpanic int:fromString("invalid");
        x -> w2;
        2 -> w2;
    }

    worker w2 returns int {
        int x = <- w1 | w1;
        return x;
    }

    int|error intResult = trap wait w2;
    test:assertTrue(intResult is error, "Expected error result");
    error e = <error>intResult;
    test:assertEquals(e.message(), "{ballerina/lang.int}NumberParsingError", "Invalid error result");
    test:assertEquals(e.detail().toString(), "{\"message\":\"'string' value 'invalid' cannot be converted to 'int'\"}",
     "Invalid error result");
    }

    function multilpleAlternateReceive1() {
        worker w1 {
            1 -> w2;
            2 -> w2;
        }

        worker w2 returns [int, int] {
            int x = <- w1 | w3;
            int y = <- w1;
            return [x, y];
        }

        worker w3 {
            runtime:sleep(2);
            3 -> w2;
        }

        [int, int] [x, y] = wait w2;
        test:assertEquals(x, 1, "Invalid int result");
        test:assertEquals(y, 2, "Invalid int result");
    }

    function multilpleAlternateReceive2() {
        worker w1 {
            runtime:sleep(2);
            1 -> w2;
            2 -> w2;
        }

        worker w2 returns [int, int] {
            int x = <- w1 | w3;
            int y = <- w1;
            return [x, y];
        }

        worker w3 {
            3 -> w2;
        }

        [int, int] [x, y] = wait w2;
        test:assertEquals(x, 3, "Invalid int result");
        test:assertEquals(y, 2, "Invalid int result");
    }

function workerAlternateReceiveWithConditionalSend() returns error? {
    worker w1 {
      boolean b1 = true;
      boolean b2 = true;
      boolean b3 = false;

      if b1 {
         1 -> function;
      } else if b2 {
         2 -> function;
      } else if b3 {
         3 -> function;
      } else {
         4 -> function;
      }
    }

    int n = check <- w1|w1|w1|w1;
    test:assertEquals(n, 1);
}

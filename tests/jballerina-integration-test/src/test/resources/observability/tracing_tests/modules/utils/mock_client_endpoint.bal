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

public client class MockClient {
    remote function callWithNoReturn(int a, int b, int expectedSum) {
        var sum = a + b;
        if (sum != expectedSum) {   // Check for validating if normal execution is intact from instrumentation
            error e = error("Sum is " + sum.toString() + ". Expected :" + expectedSum.toString());
            panic e;
        }
    }

    remote function calculateSum(int a, int b) returns int {
        var sum = a + b;
        return a + b;
    }

    remote function callAnotherRemoteFunction() {
        self->callWithNoReturn(13, 9, 22);
    }

    remote function callWithReturn(int a, int b) returns int|error {
        return a + b;
    }

    remote function callWithErrorReturn() returns error? {
        var sum = 3 + 7;
        var expectedSum = 10;
        if (sum != expectedSum) {    // Check for validating if normal execution is intact from instrumentation
            error e = error("Sum is " + sum.toString() + ". Expected " + expectedSum.toString());
            panic e;
        } else {
            error e = error("Test Error");
            return e;
        }
    }

    remote function callWithPanic() {
        var sum = 5 + 2;
        var expectedSum = 7;
        if (sum != expectedSum) {    // Check for validating if normal execution is intact from instrumentation
            error e = error("Sum is " + sum.toString() + ". Expected " + expectedSum.toString());
            panic e;
        }
        error e = error("Test Error");
        panic e;
    }
}

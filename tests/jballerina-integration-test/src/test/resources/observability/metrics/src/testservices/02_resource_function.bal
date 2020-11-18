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

MockClient testClient = new();

service testServiceOne on new testobserve:Listener(10091) {
    # Resource function for testing resource function and remote calls
    resource function resourceOne(testobserve:Caller caller, string body) {
        var ret = trap testClient->callWithPanic();
        if (!(ret is error)) {
            error e = error("Unexpected Error");
            panic e;
        }
        checkpanic testClient->callWithErrorReturn();
        checkpanic caller->respond("Executed Successfully");
    }

    # Resource function for testing worker interactions
    resource function resourceTwo(testobserve:Caller caller) {
        testWorkerInteractions(11);
        checkpanic caller->respond("Invocation Successful");
    }
}

public client class MockClient {
    public remote function callWithErrorReturn() returns error? {
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

    public remote function callWithPanic() {
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

function testWorkerInteractions(int c) {
    worker w1 {
        int a = c + 3;
        testobserve:sleep(1000);  // Sleep to make the workers not finish together
        a -> w2;
    }
    worker w2 {
        int b = <- w1;
        if (b != (c + 3)) {
            error err = error("worker interaction failed");
            panic err;
        }
    }
    wait w2;
    wait w1;
}

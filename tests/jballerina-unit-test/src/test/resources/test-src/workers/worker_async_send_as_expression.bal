// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function testAsyncSendAsExpressionReturnType() {
    worker w1 {
        () a = 5 -> w2;
        assertValueEquality(a, ());

        var b = "Foo" -> w2;
        assertValueEquality(b, ());
    }

    worker w2 {
        int x = <- w1;
        assertValueEquality(5, x);

        string y = <- w1;
        assertValueEquality("Foo", y);
    }

    _ = wait {w1, w2};
}

function testAsyncSendAsExpressionWithPanic() {
    worker w1 {
        () a = 5 -> w2;
        () b = 15 -> w2;
        assertValueEquality(b, ());
    }

    worker w2 returns error? {
        int x = <- w1;
        if (x == 5) {
            return error("This is an error");
        }
        x = <- w1;
    }

     map<error??> mapResult = wait {w1, w2};
}

type AssertionError distinct error;
const ASSERTION_ERROR_REASON = "AssertionError";

function assertValueEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}

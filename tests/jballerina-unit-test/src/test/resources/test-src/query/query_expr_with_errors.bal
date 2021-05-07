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

import ballerina/lang.'value;

type FooDetail record {
    int code;
    string message?;
    error cause?;
};

type FooError error<FooDetail>;

class ErrorGenerator {
    int i = 0;
    public isolated function next() returns record {| FooError value; |}? {
        if (self.i < 10) {
            self.i += 1;
            int i = self.i;
            string si = i.toString();
            FooError e = error FooError("CE", error("CE" + si, message = "error messge #" + si), code = i);
            return { value: e };
        }
    }
}

public function queryAnErrorStream() {
    ErrorGenerator errGen = new();
    stream<FooError> errStream = new(errGen);
    string[] causeErrorMsgs =
            from var err in errStream
            let map<value:Cloneable> detail = err.detail(), error cause = <error>err.cause()
            where <int> checkpanic detail.get("code") % 2 == 0
            limit 4
            select <string> checkpanic cause.detail().get("message");

    assertEquality("error messge #2", causeErrorMsgs[0]);
    assertEquality("error messge #4", causeErrorMsgs[1]);
    assertEquality("error messge #6", causeErrorMsgs[2]);
    assertEquality("error messge #8", causeErrorMsgs[3]);
}

//---------------------------------------------------------------------------------------------------------
const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON,
                            message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}

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

class IterableWithError {
    *object:Iterable;
    public function iterator() returns object {
        public isolated function next() returns record {|int value;|}|error?;
    } {
        return object {
            int[] integers = [12, 34, 56, 34, 78];
            int cursorIndex = 0;
            public isolated function next() returns record {|int value;|}|error? {
                self.cursorIndex += 1;
                if (self.cursorIndex == 3) {
                    return error("Custom error thrown.");
                }
                else if (self.cursorIndex <= 5) {
                    return {
                        value: self.integers[self.cursorIndex - 1]
                    };
                } else {
                    return ();
                }
            }
        };
    }
}

// Normal Queries

public function queryWithoutErrors() {
    // Normally, the result of evaluating a query expression is a single value (i.e int[]).
    var intArr = from var item in [1, 2, 3]
                 select item;
    assertEquality(true, intArr is int[]);
    assertEquality(3, intArr.length());
    assertEquality(1, intArr[0]);
    assertEquality(2, intArr[1]);
    assertEquality(3, intArr[2]);
}

public function queryWithAnError() {
    // The execution of a clause may complete early with an error value,
    // in which case this error value is the result of the query (i.e int[]|error).
    IterableWithError p = new IterableWithError();
    var intArr = from var item in p
                     select item;
    assertEquality(true, intArr is int[]|error);
    assertEquality(error("Custom error thrown."), intArr);
}

public function queryWithACheckFailEncl() {
    // Wrapper to handle checked error of queryWithACheckFail().
    var intArr = queryWithACheckFail();
    assertEquality(error("Verify Check."), intArr);
}

public function queryWithACheckFail() returns int[]|error {
    // If the evaluation of an expression within the query-expr completing abruptly with a check-fail,
    // an error will get propagated to the query result level.
    int[] intArr = from var item in [1, 2, 3]
                 select check verifyCheck(item);
    assertEquality(true, false); // this shouldn't be reachable.
    return intArr;
}

public function queryWithAPanicEncl() {
    // Wrapper to handle checked error of queryWithAPanic().
    error? e = trap queryWithAPanic();
    assertEquality(error("Verify Panic."), e);
}

public function queryWithAPanic() {
    // If there's an expression within the query-expr completing abruptly with a panic,
    // evaluation of the whole query-expr can complete abruptly with panic.
    int[] intArr = from var item in [1, 2, 3]
             select verifyPanic(item);
}

// Query to streams

public function streamFromQueryWithoutErrors() {
    stream<int> intStream = stream from var item in [1, 2, 3]
                            select item;
    assertEquality({value:1}, intStream.next());
    assertEquality({value:2}, intStream.next());
    assertEquality({value:3}, intStream.next());
    assertEquality((), intStream.next());
}

public function streamFromQueryWithAnError() {
    // If a next operation causes the execution of a clause that completes early with an error value,
    // then the error value is returned by the next operation.
    IterableWithError p = new IterableWithError();
    var intStream = stream from var item in p
                    select item;
    assertEquality({value:12}, intStream.next());
    assertEquality({value:34}, intStream.next());
    assertEquality(error("Custom error thrown."), intStream.next());
}

public function streamFromQueryWithACheckFail() returns error? {
    // If the next operation results in the evaluation of an expression within the query-expr
    // completing abruptly with a check-fail, the associated error value will be returned as
    // the result of the next operation.
    var intStream = stream from var item in [1, 2, 3]
                    select check verifyCheck(item);
    assertEquality(error("Verify Check."), intStream.next());
}

public function streamFromQueryWithAPanicEncl() {
    // Wrapper to handle checked error of streamFromQueryWithAPanic().
    error? e = trap streamFromQueryWithAPanic();
    assertEquality(error("Verify Panic."), e);
}

public function streamFromQueryWithAPanic() {
    // If the next operation results in the evaluation of an expression within the query-expr
    // completely abruptly with panic, then the next operation will complete abruptly with a panic.
    var intStream = stream from var item in [1, 2, 3]
                    select verifyPanic(item);
    var val = intStream.next(); // this should panic.
}

// Utils ---------------------------------------------------------------------------------------------------------

public function verifyCheck(int i) returns int|error {
    return error("Verify Check.");
}

public function verifyPanic(int i) returns int {
    panic error("Verify Panic.");
}

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
    if expectedValAsString == actualValAsString {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                            message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}

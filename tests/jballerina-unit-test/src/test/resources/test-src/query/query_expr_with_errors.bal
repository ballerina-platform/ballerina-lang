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
    assertEquality({value: 1}, intStream.next());
    assertEquality({value: 2}, intStream.next());
    assertEquality({value: 3}, intStream.next());
    assertEquality((), intStream.next());
}

public function streamFromQueryWithAnError() {
    // If a next operation causes the execution of a clause that completes early with an error value,
    // then the error value is returned by the next operation.
    IterableWithError p = new IterableWithError();
    var intStream = stream from var item in p
        select item;
    assertEquality({value: 12}, intStream.next());
    assertEquality({value: 34}, intStream.next());
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

type SemanticError error<SemanticDiagnostic>;

type UnreachableError distinct error<SemanticDiagnostic>;

public type SemanticDiagnostic record {|
    string message;
|};

function testDistinctErrorReturn() {
    SemanticError|int[] val1 = getIntArrayOrSemanticError();
    assertTrue(val1 is SemanticError);

    UnreachableError|int[] val2 = getIntArrayOrUnreachableError();
    assertTrue(val2 is UnreachableError);
}

function getIntArrayOrSemanticError() returns int[]|SemanticError {
    int[] val = from var _ in [1, 2, 3]
        select check throwSemanticError();
    return val;
}

function getIntArrayOrUnreachableError() returns int[]|UnreachableError {
    int[] val = from var _ in [1, 2, 3]
        select check throwUnreachableError();
    return val;
}

function throwSemanticError() returns int|SemanticError {
    return error SemanticError("intersection must not be empty", message = "GFGF");
}

function throwUnreachableError() returns int|UnreachableError {
    return error UnreachableError("intersection must not be empty", message = "GFGF");
}

function testCatchingErrorAtOnFail() {
    error? res1 = ();
    do {
        _ = from int v in 1 ... 3
            select check verifyCheck(v);
    } on fail error err {
        res1 = err;
    }
    assertTrue(res1 is error);

    error? res2 = ();
    do {
        do {
            _ = from int v in 1 ... 3
                select check verifyCheck(v);
        } on fail error err {
            _ = from int v in 1 ... 3
                select check verifyCheck(v);
        }
    } on fail error err {
        res2 = err;
    }
    assertTrue(res2 is error);

    error? res3 = ();
    do {
        _ = from int v in 1 ... 3
            let int intVal = check verifyCheck(v)
            select 1;
    } on fail error err {
        res3 = err;
    }
    assertTrue(res3 is error);

    error? res4 = ();
    do {
        _ = from int a in (from int v in [1, 2]
                let int m = check verifyCheck(v)
                select m)
            select a;
    } on fail error err {
        res4 = err;
    }
    assertTrue(res4 is error);

    error? res5 = ();
    do {
        _ = from int a in (from int v in [1, 2]
                select check verifyCheck(v))
            select a;
    } on fail error err {
        res5 = err;
    }
    assertTrue(res5 is error);

    error? res6 = ();
    do {
        _ = from int a in (from int v in check verifyCheckArr()
                select v)
            select a;
    } on fail error err {
        res6 = err;
    }
    assertTrue(res6 is error);

    error? res7 = ();
    do {
        _ = from int a in from int v in (from int i in 1 ... 3
                    select check verifyCheck(i))
                select v
            select a;
    } on fail error err {
        res7 = err;
    }
    assertTrue(res7 is error);

    error? res8 = ();
    do {
        _ = from int v in 1 ... 3
            where check verifyCheck(v) == 1
            select v;
    } on fail error err {
        res8 = err;
    }
    assertTrue(res8 is error);

    error? res9 = ();
    do {
        _ = from int v in (from int i in 1 ... 3
                where check verifyCheck(i) == 1
                select i)
            select v;
    } on fail error err {
        res9 = err;
    }
    assertTrue(res9 is error);

    error? res10 = ();
    do {
        _ = from int i in 1 ... 3
            join int j in (from int jj in 1 ... 3
                select check verifyCheck(jj))
            on i equals j
            select i;
    } on fail error err {
        res10 = err;
    }
    assertTrue(res10 is error);

    error? res11 = ();
    do {
        _ = from int i in 1 ... 3
            join int j in 1 ... 3
            on check verifyCheck(i) equals j
            select i;
    } on fail error err {
        res11 = err;
    }
    assertTrue(res11 is error);

    error? res12 = ();
    do {
        _ = from int i in 1 ... 3
            join int j in 1 ... 3
            on i equals check verifyCheck(j)
            select i;
    } on fail error err {
        res12 = err;
    }
    assertTrue(res12 is error);

    error? res13 = ();
    do {
        _ = from int i in 1 ... 3
           order by check verifyCheck(i)
           select i;
    } on fail error err {
        res13 = err;
    }
    assertTrue(res13 is error);

    error? res14 = ();
    do {
        _ = from int i in 1 ... 3
            outer join int j in (from int jj in 1 ... 3
                select check verifyCheck(jj))
            on i equals j
            select i;
    } on fail error err {
        res14 = err;
    }
    assertTrue(res14 is error);
}

function testErrorReturnedFromSelect() {
    assertTrue(checkErrorAtSelect() is error);
}

function checkErrorAtSelect() returns error? {
    _ = from int v in 1 ... 3
        select check verifyCheck(v);
}

function testErrorReturnedFromWhereClause() {
    assertTrue(checkErrorAtWhere1() is error);
    assertTrue(checkErrorAtWhere2() is error);
}

function checkErrorAtWhere1() returns error? {
    _ = from int v in 1 ... 3
        where check verifyCheck(v) == 1
        select v;
}

function checkErrorAtWhere2() returns error? {
    _ = from int v in (from int i in 1 ... 3
            where check verifyCheck(i) == 1
            select i)
        select v;
}

function testErrorReturnedFromLetClause() {
    assertTrue(checkErrorAtLet1() is error);
    assertTrue(checkErrorAtLet2() is error);
}

function checkErrorAtLet1() returns error? {
    _ = from int v in 1 ... 3
        let int newVar = check verifyCheck(v)
        select v;
}

function checkErrorAtLet2() returns error? {
    _ = from int v in (from int i in 1 ... 3
            let int newVar = check verifyCheck(i)
            select i)
        select v;
}

function testErrorReturnedFromLimitClause() {
    assertTrue(checkErrorAtLimitClause1() is error);
    assertTrue(checkErrorAtLimitClause2() is error);
}

function checkErrorAtLimitClause1() returns error? {
    _ = from int i in 1 ... 3
        limit check verifyCheck(i)
        select i;
}

function checkErrorAtLimitClause2() returns error? {
    _ = from int v in (from int i in 1 ... 3
            limit check verifyCheck(i)
            select i)
        select v;
}

function testErrorReturnedFromJoinClause() {
    assertTrue(checkErrorAtJoinClause() is error);
    assertTrue(checkErrorAtOnEqualLHS() is error);
    assertTrue(checkErrorAtOnEqualRHS() is error);
    assertTrue(checkErrorAtOuterJoin() is error);
    assertTrue(checkErrorAtOuterJoinOnEqualLHS() is error);
    assertTrue(checkErrorAtOuterJoinOnEqualRHS() is error);
}

function checkErrorAtJoinClause() returns error? {
    _ = from int i in 1 ... 3
        join int j in (from int jj in 1 ... 3
            select check verifyCheck(jj))
        on i equals j
        select i;
}

function checkErrorAtOnEqualLHS() returns error? {
    _ = from int i in 1 ... 3
        join int j in 1 ... 3
        on check verifyCheck(i) equals j
        select i;
}

function checkErrorAtOnEqualRHS() returns error? {
    _ = from int i in 1 ... 3
        join int j in 1 ... 3
        on i equals check verifyCheck(j)
        select i;
}

function checkErrorAtOuterJoin() returns error? {
    _ = from int i in 1 ... 3
        outer join int j in (from int jj in 1 ... 3
            select check verifyCheck(jj))
        on i equals j
        select i;
}

function checkErrorAtOuterJoinOnEqualLHS() returns error? {
    _ = from int i in 1 ... 3
        outer join int j in 1 ... 3
        on check verifyCheck(i) equals j
        select i;
}

function checkErrorAtOuterJoinOnEqualRHS() returns error? {
    _ = from int i in 1 ... 3
        outer join int j in 1 ... 3
        on i equals check verifyCheck(j)
        select i;
}

function testErrorReturnedFromOrderByClause() {
    assertTrue(checkErrorAtOrderBy() is error);
}

function checkErrorAtOrderBy() returns error? {
    _ = from int i in 1...3
       order by check verifyCheck(i)
       select i;
}

// Utils ---------------------------------------------------------------------------------------------------------

public function verifyCheck(int i) returns int|error {
    return error("Verify Check.");
}

public function verifyPanic(int i) returns int {
    panic error("Verify Panic.");
}

public function verifyCheckArr() returns int[]|error {
    return error("Verify Check.");
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(any|error actual) {
    assertEquality(true, actual);
}

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

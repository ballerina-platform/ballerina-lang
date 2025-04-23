// Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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
// KIND, either express or implied. See the License for the
// specific language governing permissions and limitations
// under the License.

type A [int, string];

type B record {|int a;|};

function testSameTypeDesc() {
    assertEquality(A, A);

    A a1 = [1, "a"];
    A a2 = [2, "b"];
    assertEquality(typeof a1, typeof a2);

    var a3 = a1;
    a1 = [3, "c"];
    assertEquality(typeof a1, typeof a3);

    [int, string] a4 = [1, "a"];
    var a5 = a4;
    a4 = [2, "b"];
    assertEquality(typeof a4, typeof a5);

    typedesc td1 = A;
    assertEquality(typeof td1, typeof A);

    assertEquality(B, B);

    B r1 = {a: 1};
    var r2 = r1;
    r1 = {a: 2};
    assertEquality(typeof r1, typeof r2);

    record {|int a;|} r3 = {a: 1};
    var r4 = r3;
    r3 = {a: 2};
    assertEquality(typeof r3, typeof r4);

    typedesc td2 = B;
    assertEquality(typeof td2, typeof B);
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
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}

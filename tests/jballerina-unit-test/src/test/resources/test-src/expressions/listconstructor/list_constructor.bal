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

function testListConstructorExpr() returns boolean {
    var fooList = [
        ["AAA", 111],
        ["BBB", 222]
    ];

    foreach var foo in fooList {}

    var barList = ["CCC", 333];

    (int|string)[] fooArr = ["DDD", 444];

    return fooList[0][0] == "AAA"
        && fooList[0][1] == 111
        && fooList[1][0] == "BBB"
        && fooList[1][1] == 222
        && barList[0] == "CCC"
        && barList[1] == 333
        && fooArr[0] == "DDD"
        && fooArr[1] == 444;
}

function testListConstructorAutoFillExpr() {
    int[8] arrOfEightInts = [1, 2, 3];
    int sum = 0;
    int i = 0;

    while (i < 8) {
        sum += arrOfEightInts[i];
        i = i + 1;
    }

    if (sum != 6) {
        panic error("Invalid sum of int array");
    }
}

const TYPEDESC_ARRAY_ALL = "typedesc (any|error)[]";

function testListConstructorWithAnyACET() {
    any a = [1, 2];
    typedesc<any> ta = typeof a;
    assertEquality(TYPEDESC_ARRAY_ALL, ta.toString());

    any|((any|error)[]) b = [];
    ta = typeof b;
    assertEquality(TYPEDESC_ARRAY_ALL, ta.toString());
}

const TYPEDESC_ARRAY_ANYDATA = "typedesc anydata[]";

function testListConstructorWithAnydataACET() {
    anydata a = [];
    typedesc<any> ta = typeof a;
    assertEquality(TYPEDESC_ARRAY_ANYDATA, ta.toString());

    anydata|string[]|anydata[] b = ["hi", 1, 2.0];
    ta = typeof b;
    assertEquality(TYPEDESC_ARRAY_ANYDATA, ta.toString());
}

const TYPEDESC_ARRAY_JSON = "typedesc json[]";

function testListConstructorWithJsonACET() {
    json a = [true, 1, 2.0d];
    typedesc<any> ta = typeof a;
    assertEquality(TYPEDESC_ARRAY_JSON, ta.toString());

    json|string b = ["hello", 1, 2.0, false];
    ta = typeof b;
    assertEquality(TYPEDESC_ARRAY_JSON, ta.toString());
}

function testTypeWithReadOnlyInUnionCET() {
    [int, string] tp = [1, "ballerina"];
    readonly|(int|[int, string])[] val = [1, [1, "foo"], tp, tp];

    assertEquality(true, <any> checkpanic val is (int|[int, string])[]);
    assertEquality(false, val is (int|[int, string])[] & readonly);

    (int|[int, string])[] arr = <(int|[int, string])[]> checkpanic val;

    assertEquality(1, arr[0]);
    assertEquality(true, arr[1] is [int, string]);
    assertEquality(<[int, string]> [1, "foo"], arr[1]);
    assertEquality(tp, arr[2]);
    assertEquality(tp, arr[3]);

    // Updates should be allowed.
    arr[0] = tp;
    [int, string] tempTup = <[int, string]> arr[1];
    tempTup[0] = 2;
    tempTup = <[int, string]> arr[2];
    tempTup[0] = 3;

    assertEquality(<[int, string]> [3, "ballerina"], arr[0]);
    assertEquality(<[int, string]> [3, "ballerina"], arr[2]);
    assertEquality(<[int, string]> [3, "ballerina"], arr[3]);
    assertEquality(<[int, string]> [2, "foo"], arr[1]);
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

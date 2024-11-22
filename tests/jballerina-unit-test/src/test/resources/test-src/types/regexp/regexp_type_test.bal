// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/lang.regexp;

function testBasicRegExp() {
    string:RegExp _ = re `AB*|[^c-d]{1,5}`;
}

function testAssignabilityWithRegExp() {
    string:RegExp x1 = re `AB*|[^c-d]{1,5}`;

    anydata x2 = x1;
    assertEquality(true, x2 is string:RegExp);

    any x3 = x1;
    assertEquality(true, x2 is string:RegExp);

    readonly x4 = x1;
    assertEquality(true, x4 is string:RegExp);

    anydata & readonly x5 = x1;
    assertEquality(true, x5 is string:RegExp);

    any & readonly x6 = x1;
    assertEquality(true, x6 is string:RegExp);
}

function testSubtypingWithRegExp() {
    string:RegExp x1 = re `AB*|[^c-d]{1,5}`;
    assertEquality(true, x1 is any);
    assertEquality(true, x1 is anydata);
    assertEquality(true, x1 is readonly);

    any x2 = re `AB*|[^c-d]{1,5}`;
    assertEquality(true, x2 is string:RegExp);

    readonly x3 = re `AB*|[^c-d]{1,5}`;
    assertEquality(true, x3 is string:RegExp);

    any & readonly x4 = re `AB*|[^c-d]{1,5}`;
    assertEquality(true, x4 is string:RegExp);

    anydata x5 = re `AB*|[^c-d]{1,5}`;
    assertEquality(true, x5 is string:RegExp);

    anydata & readonly x6 = re `AB*|[^c-d]{1,5}`;
    assertEquality(true, x6 is string:RegExp);
}

function testRegExpWithVar() {
    var x1 = re `AB*|[^c-d]{1,5}`;

    string:RegExp x2 = x1;
    assertEquality(re `AB*|[^c-d]{1,5}`, x2);

    anydata x3 = x1;
    assertEquality(re `AB*|[^c-d]{1,5}`, x3);

    any x4 = x1;
    assertEquality(re `AB*|[^c-d]{1,5}`, x4);

    readonly x5 = x1;
    assertEquality(re `AB*|[^c-d]{1,5}`, x5);

    anydata & readonly x6 = x1;
    assertEquality(re `AB*|[^c-d]{1,5}`, x5);

    any & readonly x7 = x1;
    assertEquality(re `AB*|[^c-d]{1,5}`, x7);
}

type UserType string:RegExp;

function testRegExpWithUserDefinedType() {
    UserType x1 = re `AB*|[^c-d]{1,5}`;

    string:RegExp x2 = x1;
    assertEquality(re `AB*|[^c-d]{1,5}`, x2);

    anydata x3 = x1;
    assertEquality(re `AB*|[^c-d]{1,5}`, x3);

    any x4 = x1;
    assertEquality(re `AB*|[^c-d]{1,5}`, x4);

    readonly x5 = x1;
    assertEquality(re `AB*|[^c-d]{1,5}`, x5);

    anydata & readonly x6 = x1;
    assertEquality(re `AB*|[^c-d]{1,5}`, x5);

    any & readonly x7 = x1;
    assertEquality(re `AB*|[^c-d]{1,5}`, x7);
}

type T1 string:RegExp & readonly;
type T2 regexp:RegExp & readonly;

type Foo record {|
    int e;
    readonly regexp:RegExp f;
|};

Foo & readonly rf = {e: 1, f: re `test`};

function testRegExpReadonlyLocalVars() {
    string:RegExp & readonly r1 = re `test`;
    assertEquality(true, r1 is readonly);

    T1 & readonly r2 = re `test`;
    assertEquality(true, r2 is readonly);

    (T2 & readonly) & string:RegExp r3 = re `test`;
    assertEquality(true, r3 is readonly);
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

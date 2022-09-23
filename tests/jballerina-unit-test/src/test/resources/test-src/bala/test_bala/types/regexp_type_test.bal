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

import testorg/regexp as regexp1;
import ballerina/lang.regexp;

function testBasicRegExp() {
    regexp1:RegExpType x1 = re `AB*|[^c-d]{1,5}`;
    assertEquality(true, x1 is regexp:RegExp);
}

function testAssignabilityWithRegExp() {
    regexp1:RegExpType x1 = re `AB*|[^c-d]{1,5}`;
    assertEquality("AB*|[^c-d]{1,5}", x1.toString());

    regexp:RegExp _ = x1;

    anydata x2 = x1;
    assertEquality(true, x2 is regexp:RegExp);

    any x3 = x1;
    assertEquality(true, x2 is regexp:RegExp);

    readonly x4 = x1;
    assertEquality(true, x4 is regexp:RegExp);

    anydata & readonly x5 = x1;
    assertEquality(true, x5 is regexp:RegExp);

    any & readonly x6 = x1;
    assertEquality(true, x6 is regexp:RegExp);
}

function testSubtypingWithRegExp() {
    regexp1:RegExpType x1 = re `AB*|[^c-d]{1,5}`;
    assertEquality(true, x1 is any);
    assertEquality(true, x1 is anydata);
    assertEquality(true, x1 is readonly);

    any x2 = re `AB*|[^c-d]{1,5}`;
    assertEquality(true, x2 is regexp1:RegExpType);

    readonly x3 = re `AB*|[^c-d]{1,5}`;
    assertEquality(true, x3 is regexp1:RegExpType);

    any & readonly x4 = re `AB*|[^c-d]{1,5}`;
    assertEquality(true, x4 is regexp1:RegExpType);

    anydata x5 = re `AB*|[^c-d]{1,5}`;
    assertEquality(true, x5 is regexp1:RegExpType);

    anydata & readonly x6 = re `AB*|[^c-d]{1,5}`;
    assertEquality(true, x6 is regexp1:RegExpType);
}

function testRegExpWithVar() {
    var x1 = re `AB*|[^c-d]{1,5}`;

    regexp1:RegExpType x2 = x1;
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

type UserType regexp1:RegExpType2;

function testRegExpWithUserDefinedType() {
    UserType x1 = re `AB*|[^c-d]{1,5}`;
    assertEquality("AB*|[^c-d]{1,5}", x1.toString());

    regexp:RegExp x2 = x1;
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

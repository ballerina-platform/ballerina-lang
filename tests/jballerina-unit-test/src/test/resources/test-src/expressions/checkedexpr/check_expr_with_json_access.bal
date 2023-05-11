// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

type Null null;
type IntMax int:MAX_VALUE;
type FloatValue 12.1;
type BooleanOrStringValue true|"abc";

function testJsonAccessWithCheckWithSubTypesOfSimpleBasicTypesCET() returns error? {
    json j = {i: int:MAX_VALUE, f: 12.1, t: true, s: "abc", n: null, d: 1234d};

    IntMax a = check j.i;
    assertEquality(int:MAX_VALUE, a);

    FloatValue b = check j.f;
    assertEquality(12.1, b);

    BooleanOrStringValue c = check j.t;
    assertEquality(true, c);

    true|"abc" d = check j.s;
    assertEquality("abc", d);

    Null e = check j.n;
    assertEquality((), e);

    11f|1234d f = check j.d;
    assertEquality(1234d, f);

    var f1 = function () returns IntMax|error {
        IntMax a2 = check j.i2;
        return a2;
    };
    IntMax|error f1Res = f1();
    assertTrue(f1Res is error);
    error f1Err = <error> f1Res;
    assertEquality("{ballerina/lang.map}KeyNotFound", f1Err.message());
    assertEquality("key 'i2' not found in JSON mapping", check f1Err.detail()["message"].ensureType());

    var f2 = function () returns BooleanOrStringValue|error {
        BooleanOrStringValue c2 = check j.i;
        return c2;
    };
    BooleanOrStringValue|error f2Res = f2();
    assertTrue(f2Res is error);
    error f2Err = <error> f2Res;
    assertEquality("{ballerina}TypeCastError", f2Err.message());
    assertEquality("incompatible types: 'int' cannot be cast to 'BooleanOrStringValue'",
                   check f2Err.detail()["message"].ensureType());
}

function assertTrue(anydata actual) {
    assertEquality(true, actual);
}

function assertEquality(anydata expected, anydata actual) {
    if (expected == actual) {
        return;
    }

    panic error(string `expected '${expected.toBalString()}', found '${actual.toBalString()}'`);
}

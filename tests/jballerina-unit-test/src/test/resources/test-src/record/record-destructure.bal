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

type Topt1 record {
    int x?;
    int y?;
};

function testRecordDestructure1() {
    int? xx;
    int? yy;

    Topt1 topt = {x: 2, y: 3};
    {x: xx, y: yy} = topt;
    assertEquality(2, xx);
    assertEquality(3, yy);

    topt = {y: 4};
    {x: xx, y: yy} = topt;
    assertTrue(xx is ());
    assertEquality(4, yy);

    int? x;
    int? y;

    topt = {x: 5, y: 6};
    {x, y} = topt;
    assertEquality(5, x);
    assertEquality(6, y);

    topt = {y: 7};
    {x, y} = topt;
    assertTrue(x is ());
    assertEquality(7, y);

    topt = {};
    {x, y} = topt;
    assertTrue(x is () && y is ());
}

type Topt2 record {
    int a;
    record {
        int b?;
    }[1] c;
};

function testRecordDestructure2() {
    int? aa;
    int? bb;

    Topt2 topt = {a: 4, c: [{b: 5}]};
    {a: aa, c: [{b: bb}]} = topt;
    assertEquality(4, aa);
    assertEquality(5, bb);
}

function testRecordDestructure3() {
    int? a;
    int? b;

    Topt2 topt = {a: 4, c: [{b: 5}]};
    {a, c: [{b}]} = topt;
    assertEquality(4, a);
    assertEquality(5, b);

    record {int b?;}[1] c;
    {a, c} = topt;
    assertEquality(5, c[0].b);
}

type Topt3 record {
    int a;
    record {
        int b?;
    }[1] c?;
};

function testRecordDestructure4() {
    int? a;
    int? b;

    Topt3 topt = {a: 4, c: [{b: 5}]};
    {a, c: [{b}]} = topt;
    assertEquality(4, a);
    assertEquality(5, b);

    record {int b?;}[1]? c;
    topt = {a: 4};
    {a, c} = topt;
    assertTrue(c is ());
}

type Topt4 record {
    int x?;
    int? y?;
};

function testRecordDestructure5() {
    int? xx;
    int? yy;

    Topt4 topt = {x: 2, y: 3};
    {x: xx, y: yy} = topt;
    assertEquality(2, xx);
    assertEquality(3, yy);
}

const ASSERTION_ERR_REASON = "AssertionError";

function assertTrue(boolean actual) {
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
    panic error(ASSERTION_ERR_REASON,
                message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}

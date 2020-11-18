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

const ASSERTION_ERROR_REASON = "AssertionError";

function testTupleFillMemberSimpleTypes() {
    [int, int, string, boolean, float, decimal, ()] tuple = [100];

    assertEquality(7, tuple.length());
    var expectedTuple = [100, 0, "", false, 0.0f, 0.0d, ()];
    assertEquality(expectedTuple, tuple);
    assertEquality(100, tuple[0]);
    assertEquality(0, tuple[1]);
    assertEquality("", tuple[2]);
    assertEquality(false, tuple[3]);
    assertEquality(0.0f, tuple[4]);
    assertEquality(0.0d, tuple[5]);
    assertEquality((), tuple[6]);

    tuple[3] = true;
    assertEquality(7, tuple.length());
    var expectedTuple2 = [100, 0, "", true, 0.0f, 0.0d, ()];
    assertEquality(expectedTuple2, tuple);
    assertEquality(100, tuple[0]);
    assertEquality(0, tuple[1]);
    assertEquality("", tuple[2]);
    assertEquality(true, tuple[3]);
    assertEquality(0.0f, tuple[4]);
    assertEquality(0.0d, tuple[5]);
    assertEquality((), tuple[6]);
}

function testTupleFillMemberSimpleTypesWithRest() {
    [int, int, string, boolean, float, decimal, (), boolean...] tuple = [100];

    assertEquality(7, tuple.length());
    var expectedTuple = [100, 0, "", false, 0.0f, 0.0d, ()];
    assertEquality(expectedTuple, tuple);
    assertEquality(100, tuple[0]);
    assertEquality(0, tuple[1]);
    assertEquality("", tuple[2]);
    assertEquality(false, tuple[3]);
    assertEquality(0.0f, tuple[4]);
    assertEquality(0.0d, tuple[5]);
    assertEquality((), tuple[6]);

    tuple[3] = true;
    tuple.push(false);
    var expectedTuple2 = [100, 0, "", true, 0.0f, 0.0d, (), false];
    assertEquality(8, tuple.length());
    assertEquality(expectedTuple2, tuple);
    assertEquality(100, tuple[0]);
    assertEquality(0, tuple[1]);
    assertEquality("", tuple[2]);
    assertEquality(true, tuple[3]);
    assertEquality(0.0f, tuple[4]);
    assertEquality(0.0d, tuple[5]);
    assertEquality((), tuple[6]);
    assertEquality(false, tuple[7]);
}

function testTupleFillMemberStructuredTypes() {
    [Foo, Foo, int[3], Bar[1][2], Bar] tuple = [{s: "customS", i: 200, b: true}];

    assertEquality(5, tuple.length());

    assertEquality("customS", tuple[0].s);
    assertEquality(200, tuple[0].i);
    assertEquality(true, tuple[0]?.b);

    assertEquality("foo", tuple[1].s);
    assertEquality(100, tuple[1].i);
    assertEquality((), tuple[1]?.b);

    assertEquality(3, tuple[2].length());
    assertEquality(0, tuple[2][0]);
    assertEquality(0, tuple[2][1]);
    assertEquality(0, tuple[2][2]);

    assertEquality(1, tuple[3].length());
    assertEquality(11, tuple[3][0][0].b);
    assertEquality(11, tuple[3][0][1].b);

    assertEquality(11, tuple[4].b);
}

function testTupleFillMemberStructuredTypesWithRest() {
    [Foo, Foo, int[3], Bar[1][2], Bar, Foo...] tuple = [{s: "customS", i: 200, b: true}];

    assertEquality(5, tuple.length());

    assertEquality("customS", tuple[0].s);
    assertEquality(200, tuple[0].i);
    assertEquality(true, tuple[0]?.b);

    assertEquality("foo", tuple[1].s);
    assertEquality(100, tuple[1].i);
    assertEquality((), tuple[1]?.b);

    assertEquality(3, tuple[2].length());
    assertEquality(0, tuple[2][0]);
    assertEquality(0, tuple[2][1]);
    assertEquality(0, tuple[2][2]);

    assertEquality(1, tuple[3].length());
    assertEquality(11, tuple[3][0][0].b);
    assertEquality(11, tuple[3][0][1].b);

    assertEquality(11, tuple[4].b);

    tuple[2] = [6, 7, 8];
    tuple[5] = {s: "customS2", i: 400};

    assertEquality(6, tuple.length());

    assertEquality("customS", tuple[0].s);
    assertEquality(200, tuple[0].i);
    assertEquality(true, tuple[0]?.b);

    assertEquality("foo", tuple[1].s);
    assertEquality(100, tuple[1].i);
    assertEquality((), tuple[1]?.b);

    assertEquality(3, tuple[2].length());
    assertEquality(6, tuple[2][0]);
    assertEquality(7, tuple[2][1]);
    assertEquality(8, tuple[2][2]);

    assertEquality(1, tuple[3].length());
    assertEquality(11, tuple[3][0][0].b);
    assertEquality(11, tuple[3][0][1].b);

    assertEquality(11, tuple[4].b);

    assertEquality("customS2", tuple[5].s);
    assertEquality(400, tuple[5].i);
    assertEquality((), tuple[5]?.b);
}

type Foo record {
    string s = "foo";
    int i = 100;
    boolean b?;
};

class Bar {
    int b;

    public function init() {
        self.b = 11;
    }
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}

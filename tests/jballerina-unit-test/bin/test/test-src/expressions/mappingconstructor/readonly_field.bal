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

//////////////////////////////////// Records ////////////////////////////////////

type Employee record {|
    Details details;
    readonly string department;
|};

type Details record {|
    string name;
    int id;
|};

function testBasicReadOnlyField1() {
    Details details = {
        readonly name: "May",
        id: 1234
    };
    assertTrue(details is record {|readonly string name; int id;|});
    assertEquality("May", details.name);
    assertEquality(1234, details.id);

    // Valid update since `id` is not readonly.
    details.id = 2345;
    assertEquality(2345, details.id);

    // Invalid update since `name` is readonly.
    var fn = function () {
        details.name = "Jo";
    };
    error? res = trap fn();
    assertTrue(res is error);

    error err = <error> res;
    string expected = "cannot update 'readonly' field 'name' in record of type " +
                   "'record {| readonly string name; int id; |}'";
    var actual = <string> checkpanic err.detail()["message"];
    assertEquality(expected, actual);
    assertEquality("May", details.name);
}

function testBasicReadOnlyField2() {
    int id = 1234;
    Details details = {
        readonly name: "May",
        readonly id
    };

    assertTrue(details is record {|readonly string name; readonly int id;|});
    // `details` is now immutable since all fields are `readonly`.
    assertTrue(details.isReadOnly());
    assertEquality("May", details.name);
    assertEquality(1234, details.id);

    // Invalid updates since `details` is readonly.
    var fn = function () {
        details.name = "Jo";
    };
    error? res = trap fn();
    assertTrue(res is error);

    error err = <error> res;
    string expected = "cannot update 'readonly' field 'name' in record of type " +
                   "'record {| readonly string name; readonly int id; |} & readonly'";
    var actual = <string> checkpanic err.detail()["message"];
    assertEquality(expected, actual);
    assertEquality("May", details.name);

    fn = function () {
        details.id = 4567;
    };
    res = trap fn();
    assertTrue(res is error);

    err = <error> res;
    expected = "cannot update 'readonly' field 'id' in record of type " +
               "'record {| readonly string name; readonly int id; |} & readonly'";
    actual = <string> checkpanic err.detail()["message"];
    assertEquality(expected, actual);
    assertEquality(1234, details.id);
}

function testComplexReadOnlyField() {
    Details & readonly details = {
        name: "May",
        id: 1234
    };

    Employee emp = {
        readonly details,
        department: "IT"
    };

    assertTrue(emp is record {|readonly Details details; readonly string department;|});
    assertTrue(emp.isReadOnly());

    // Invalid updates since `emp` is readonly.
    var fn = function () {
        emp.details = {
            name: "Jo",
            id: 1234
        };
    };
    error? res = trap fn();
    assertTrue(res is error);

    error err = <error> res;

    string expected = "cannot update 'readonly' field 'details' in record of type " +
                   "'record {| readonly (Details & readonly) details; readonly string department; |} & readonly'";
    var actual = <string> checkpanic err.detail()["message"];
    assertEquality(expected, actual);
    assertEquality("May", details.name);

    fn = function () {
        emp.details.name = "Jo";
    };
    res = trap fn();
    assertTrue(res is error);

    err = <error> res;
    assertEquality("cannot update 'readonly' field 'name' in record of type '(Details & readonly)'",
                   err.detail()["message"]);
    assertEquality(1234, details.id);
}

type Open record {
    int i;
};

type Strings record {|
    string a;
    readonly string b;
    string...;
|};

function testInferredTypeReadOnlynessWithReadOnlyFields() {
    Details d1 = {
        readonly name: "May",
        id: 1234
    };
    // `d1` is not `readonly` since non-readonly fields are present.
    assertEquality(false, d1.isReadOnly());

    string name = "Jo";
    Details d2 = {
        readonly name,
        readonly id: 1234
    };
    // `d2` is `readonly` since all the fields are `readonly` and `Details` is closed.
    assertTrue(d2.isReadOnly());

    Open op = {
        readonly i: 1,
        readonly "j": "str"
    };
    // `op` is not `readonly` since `Open` is open.
    assertEquality(false, op.isReadOnly());

    Strings st = {
        readonly a: "a",
        b: "b",
        readonly "c": "c"
    };
    // `st` is not `readonly` since `Strings` is open.
    assertEquality(false, st.isReadOnly());

    Details details = {
        name: "May",
        id: 1234
    };

    Employee emp = {
        details,
        readonly department: "IT"
    };
    // `emp` is not `readonly` since non-readonly fields are present.
    assertEquality(false, emp.isReadOnly());
}

function testReadOnlyBehaviourWithRecordACETInUnionCET() {
    Details & readonly details = {
        name: "May",
        id: 1234
    };

    map<int>|Employee|Details rec1 = {
        readonly details,
        department: "IT"
    };
    assertTrue(rec1 is record {|readonly Details details; readonly string department;|});
    assertTrue(rec1.isReadOnly());

    map<int>|Employee|Details rec2 = {
        details,
        department: "IT"
    };
    assertTrue(rec2 is record {|Details details; readonly string department;|});
    assertEquality(false, rec2.isReadOnly());
}

//////////////////////////////////// Maps ////////////////////////////////////

function testReadOnlyFieldsWithSimpleMapCET() {
    int c = 3;
    map<int> mp = {
        "a": 1,
        readonly b: 2,
        readonly c
    };
    assertTrue(mp is record {|readonly int b; readonly int c; int...;|});
    // `mp` is not `readonly` since non-readonly fields may be present.
    assertEquality(false, mp.isReadOnly());
    assertEquality(1, mp["a"]);
    assertEquality(2, mp["b"]);
    assertEquality(3, mp["c"]);

    // Valid update since `a` is not readonly.
    mp["a"] = 4;
    assertEquality(4, mp["a"]);

    // Invalid updates since `b` is readonly.
    var fn = function () {
        mp["b"] = 5;
    };
    error? res = trap fn();
    assertTrue(res is error);

    error err = <error> res;
    string expected = "cannot update 'readonly' field 'b' in record of type " +
                   "'record {| readonly int b; readonly int c; int...; |}'";
    var actual = <string> checkpanic err.detail()["message"];
    assertEquality(expected, actual);
    assertEquality(2, mp["b"]);

    map<int> mp2 = {
        readonly b: 2,
        readonly c
    };
    assertTrue(mp2 is record {|readonly int b; readonly int c; int...;|});
    // `mp` is not `readonly` since non-readonly fields may be present.
    assertEquality(false, mp.isReadOnly());
    assertEquality(2, mp2.length());
    assertEquality(2, mp2["b"]);
    assertEquality(3, mp["c"]);

    // Valid update since `a` is not readonly.
    mp["a"] = 4;
    assertEquality(4, mp["a"]);

    // Invalid updates since `b` is readonly.
    fn = function () {
        mp["b"] = 5;
    };
    res = trap fn();
    assertTrue(res is error);

    err = <error> res;
    expected = "cannot update 'readonly' field 'b' in record of type " +
                   "'record {| readonly int b; readonly int c; int...; |}'";
    actual = <string> checkpanic err.detail()["message"];
    assertEquality(expected, actual);
    assertEquality(2, mp["b"]);
}

function testReadOnlyBehaviourWithMapACETInUnionCET() {
    int a = 1;
    map<int>|Employee|Details mp1 = {
        readonly a,
        "b": 2
    };
    assertTrue(mp1 is record {|readonly int a; int...;|});
    assertEquality(false, mp1.isReadOnly());
    // Valid update since `mp1` is mutable.
    mp1["c"] = 3;
    assertEquality(3, mp1["c"]);

    map<int>|Employee|Details mp2 = {
        readonly a,
        readonly b: 2
    };
    assertTrue(mp2 is record {|readonly int a; readonly int b; int...;|});
    assertEquality(false, mp2.isReadOnly());
    // Valid update since `mp1` is mutable.
    mp2["c"] = 3;
    assertEquality(3, mp2["c"]);
}

type Person record {|
    readonly Details details;
    boolean employed;
|};

function testReadOnlyFieldForAlreadyReadOnlyField() {
    Person p = {
        readonly details: {
            name: "Maryam",
            id: 123
        },
        employed: true
    };

    record {} rec = p;
    assertTrue(rec is record {|readonly Details details; boolean employed;|});
    assertTrue(p.details.isReadOnly());
}

function testReadOnlyFieldWithInferredType() {
    Details & readonly d1 = {
        name: "May",
        id: 1234
    };
    int i = 1;
    string s = "d5";

    var val = {
        readonly d1,
        readonly d2: d1,
        d3: {
            str: "hello"
        },
        readonly d4: {
            "str": "world",
            readonly count: 1234
        },
        [s]: i
    };

    record {|any|error...;|} rec = val;
    assertTrue(rec is record {|
        readonly Details d1;
        readonly readonly & Details d2; // same as `readonly Details`
        record {|string str;|} d3;
        record {|string str; readonly int count;|} d4;
        int...;
    |});

    // Invalid updates since `d1` is readonly.
    var fn = function() {
        rec["d1"] = 1;
    };
    error? res = trap fn();
    assertTrue(res is error);

    error err = <error> res;
    assertEquality((<string> checkpanic err.detail()["message"]),
                    "cannot update 'readonly' field 'd1' in record of type 'record {| readonly (Details & readonly) d1;" +
                    " readonly (Details & readonly) d2; record {| string str; |} d3; readonly record {| string str;" +
                    " readonly int count; |} & readonly d4; int...; |}'");
    assertEquality(d1, rec["d1"]);
}

function testInferredTypeWithAllReadOnlyFields() {
    Details & readonly d1 = {
        name: "May",
        id: 1234
    };
    int i = 1;
    string s = "d5";

    var val = {
        readonly d1,
        readonly d2: d1,
        readonly d3: {
            str: "hello"
        },
        readonly "d4": {
            "str": "world",
            readonly count: 1234
        }
    };

    readonly rd = val; // Allowed since all the fields are readonly.
    assertTrue(rd is readonly & record {|
        readonly Details d1;
        readonly readonly & Details d2; // same as `readonly Details`
        readonly record {|string str;|} d3;
        readonly record {|string str; readonly int count;|} d4;
    |});
}

function testIdentifierKeysInConstructorWithReadOnlyFieldsForMap() {
    int k = 3;
    map<int> mp1 = {
        readonly i: 1,
        j: 2,
        k
    };

    assertTrue(mp1 is record {|readonly int i; int...;|});

    var rec1 = <record {|readonly int i; int...;|}> mp1;
    assertEquality(1, rec1.i);
    assertEquality(2, rec1["j"]);
    assertEquality(3, rec1["k"]);

    // Valid updates.
    rec1["k"] = 4;
    mp1["l"] = 5;
    assertEquality(4, rec1["k"]);
    assertEquality(5, rec1["l"]);

    map<string>|map<json> mp2 = {
        readonly i: "foo",
        j: 2,
        k
    };

    assertTrue(mp2 is record {|readonly string i; json...;|});

    var rec2 = <record {|readonly string i; json...;|}> mp2;
    assertEquality("foo", rec2.i);
    assertEquality(2, rec2["j"]);
    assertEquality(3, rec2["k"]);

    // Valid updates.
    rec2["k"] = 4.0;
    rec2["l"] = true;
    assertEquality(4.0, rec2["k"]);
    assertTrue(rec2["l"]);
}

type DetailsTwo record {|
    string name;
    string|int id;
|};

function testFieldTypeNarrowing() {
    DetailsTwo nonReadOnlyId = {
        name: "Jo",
        id: 1234
    };

    any a = nonReadOnlyId;
    assertFalse(a is Details);
    assertTrue(a is DetailsTwo);

    DetailsTwo readOnlyId = {
        name: "Jo",
        readonly id: 1234
    };

    any b = readOnlyId;
    assertTrue(b is Details);
    assertTrue(b is DetailsTwo);

    int id = 2345;
    DetailsTwo readOnlyId2 = {
        name: "Jo",
        readonly id
    };

    any c = readOnlyId2;
    assertTrue(c is Details);
    assertTrue(c is DetailsTwo);
}

function assertTrue(any|error actual) {
    assertEquality(true, actual);
}

function assertFalse(any|error actual) {
    assertEquality(false, actual);
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
    panic error("expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}

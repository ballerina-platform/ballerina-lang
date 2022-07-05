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

const INHERENT_TYPE_VIOLATION_REASON = "{ballerina/lang.map}InherentTypeViolation";

type Student record {
    readonly string name;
    readonly int id?;
};

function testRecordWithSimpleReadonlyFields() {
    Student st = {
        name: "Maryam"
    };
    assertEquality("Maryam", st.name);
    assertEquality((), st?.id);
}

function testInvalidRecordSimpleReadonlyFieldUpdate() {
    Student st1 = {
        name: "Maryam"
    };

    // Invalid updates.
    var fn1 = function () {
        record {} rec = st1;
        string str = "name";
        rec[str] = "Jo";
    };
    error? res = trap fn1();
    assertTrue(res is error);

    error err = <error> res;
    assertEquality(INHERENT_TYPE_VIOLATION_REASON, err.message());
    assertEquality("cannot update 'readonly' field 'name' in record of type 'Student'", err.detail()["message"]);

    var fn2 = function () {
        record {} rec = st1;
        rec["id"] = 4567;
    };
    res = trap fn2();
    assertTrue(res is error);

    err = <error> res;
    assertEquality(INHERENT_TYPE_VIOLATION_REASON, err.message());
    assertEquality("cannot update 'readonly' field 'id' in record of type 'Student'", err.detail()["message"]);
}

type Employee record {
    readonly Details details;
    string department;
};

type ReadonlyName record {
    readonly string name;
};

type Details record {
    string name;
    int id;
};

function testValidUpdateOfPossiblyReadonlyFieldInUnion() {
    Details d = {
        name: "Jo",
        id: 1234
    };

    Student|Details sd = d;
    sd.name = "May";
    sd.id = 4567;

    assertEquality("May", sd.name);
    assertEquality(4567, sd?.id);

    ReadonlyName|Details rnd = d;
    rnd.name = "Sue";
    rnd["id"] = 2525;

    assertEquality("Sue", rnd.name);
    assertEquality(2525, rnd?.id);

}

function testInvalidUpdateOfPossiblyReadonlyFieldInUnion() {
    Student s = {
        name: "Jo",
        id: 1234
    };

    Student|Details sd = s;

    var fn1 = function () {
        sd["name"] = "May";
    };
    error? res = trap fn1();
    assertTrue(res is error);

    error err = <error> res;
    assertEquality(INHERENT_TYPE_VIOLATION_REASON, err.message());
    assertEquality("cannot update 'readonly' field 'name' in record of type 'Student'", err.detail()["message"]);

    var fn2 = function () {
        sd.id = 4567;
    };
    res = trap fn2();
    assertTrue(res is error);

    err = <error> res;
    assertEquality(INHERENT_TYPE_VIOLATION_REASON, err.message());
    assertEquality("cannot update 'readonly' field 'id' in record of type 'Student'", err.detail()["message"]);
}

function testRecordWithStructuredReadonlyFields() {
    Details & readonly details = {
        name: "Maryam",
        id: 1234
    };

    Employee emp = {
        details,
        department: "finance"
    };

    assertEquality("Maryam", emp.details.name);
    assertEquality(1234, emp.details.id);
    assertEquality("finance", emp.department);

    Employee emp2 = {
        details: {
            name: "Ziyad",
            id: 8910
        },
        department: "IT"
    };

    assertEquality("Ziyad", emp2.details.name);
    assertEquality(8910, emp2.details.id);
    assertEquality("IT", emp2.department);

    var fn = function () {
        record {} empVal = emp;
        empVal["details"] = {
            name: "Jo",
            id: 3456
        };
    };
    error? res = trap fn();
    assertTrue(res is error);

    error err = <error> res;
    assertEquality(INHERENT_TYPE_VIOLATION_REASON, err.message());
    assertEquality("cannot update 'readonly' field 'details' in record of type 'Employee'", err.detail()["message"]);
}

type Identifier record {|
    readonly string id = "Identifier";
    string code;
|};

type Foo2 record {|
    function () f;
    int id;
    function (int, int) returns int f2;    
    function () returns string f3;
    function (int) f4;
|};

function sum(int a, int b) returns int {
    return a + b;
} 

function name() returns string {
    return "chirans";
}

function value(int a) {

}

function testRecordWithFunctionTypeField() {
    Foo2 & readonly x = {
        f: testRecordWithFunctionTypeField,
        id: 3456,
        f2: sum,
        f3: name,
        f4: value
    };
    
    assertEquality(3456, x.id);
    function (int, int) returns int sum = x.f2;
    function () returns string name = x.f3;
    function (int) value = x.f4;
    assertEquality(25, sum(10, 15));
    assertEquality("chirans", name());
    assertEquality((), value(10));
}

function testReadOnlyFieldWithDefaultValue() {
    string k = "id";

    Identifier i1 = {code: "QWE", id: "new id"};

    assertEquality("new id", i1.id);
    assertEquality("QWE", i1.code);

    Identifier i2 = {code: "QWE"};
    assertEquality("Identifier", i2.id);
    assertEquality("QWE", i2.code);

    var fn1 = function () {
        i2[k] = "new identifier";
    };
    error? res = trap fn1();
    assertTrue(res is error);

    error err = <error> res;
    assertEquality(INHERENT_TYPE_VIOLATION_REASON, err.message());
    assertEquality("cannot update 'readonly' field 'id' in record of type 'Identifier'", err.detail()["message"]);
}

type Foo record {|
    string name;
    int id;
    float...;
|};

type Bar record {|
    readonly string name;
    readonly int id;
|};

type EmptyClosedRecord record {|
|};

function testTypeReadOnlyFlagForAllReadOnlyFields() {
    Bar st = {
        name: "Maryam",
        id: 1234
    };

    Foo & readonly pr = st;
    assertTrue(pr is Bar);
    assertTrue(pr is Bar & readonly);
    assertEquality("Maryam", pr.name);
    assertEquality(1234, pr.id);

    readonly rd = st;
    assertTrue(rd is Bar);
    assertTrue(rd is Bar & readonly);

    EmptyClosedRecord ecr = {};
    readonly rd2 = ecr;
    assertTrue(rd2 is EmptyClosedRecord);
    assertTrue(rd2 is EmptyClosedRecord & readonly);
    assertTrue(rd2 is record {} & readonly);
}

record {|
    readonly int x = 1;
|} modAnonRecord = {x: 2};

function testTypeReadOnlyFlagForAllReadOnlyFieldsInAnonymousRecord() {
    readonly rd = modAnonRecord;
    assertTrue(<any|error> rd is record { int x; });
    assertTrue(rd is record { int x; } & readonly);
    record { int x; } rec = <record { int x; } & readonly> checkpanic rd;
    assertEquality(2, rec.x);

    record {|
        readonly int x = 1;
        readonly Bar y;
    |} localAnonRecord = {y: {name: "Amy", id: 1001}};
    readonly rd2 = localAnonRecord;
    assertTrue(<any|error> rd2 is record {| int x; Bar y; |});
    assertTrue(rd2 is record { int x; Bar y; } & readonly);
    var rec2 = <record { int x; Bar y; } & readonly> checkpanic rd2;
    assertEquality(1, rec2.x);
    assertEquality("Amy", rec2.y.name);
    assertEquality(1001, rec2.y.id);
}

type Person record {|
    readonly Particulars particulars;
    int id;
|};

type Undergraduate record {|
    Particulars & readonly particulars;
    int id;
|};

type Graduate record {|
    Particulars particulars;
    int id;
|};

type Particulars record {|
    string name;
|};

type OptionalId record {|
    readonly map<int>|boolean id?;
    map<int>|boolean...;
|};

function testSubTypingWithReadOnlyFields() {
    Person p1 = {
        particulars: {
            name: "Jo"
        },
        id: 1234
    };
    Undergraduate u = p1;
    assertTrue(u is Person);

    var fn1 = function () {
        u.particulars = {name: "May"};
    };
    error? res = trap fn1();
    assertTrue(res is error);
    error err = <error> res;
    assertEquality("cannot update 'readonly' field 'particulars' in record of type 'Person'", err.detail()["message"]);

    Person p2 = {
        particulars: {
          name: "Amy"
        },
        id: 1121
    };
    Graduate g = p2;

    var fn2 = function () {
        u.particulars = {name: "May"};
    };
    res = trap fn2();
    assertTrue(res is error);
    err = <error> res;
    assertEquality("cannot update 'readonly' field 'particulars' in record of type 'Person'", err.detail()["message"]);

    assertTrue(g is Person);
    var fn3 = function () {
        g.particulars.name = "Anne";
    };
    res = trap fn3();
    assertTrue(res is error);
    err = <error> res;
    assertEquality("cannot update 'readonly' field 'name' in record of type '(Particulars & readonly)'",
                   err.detail()["message"]);

    map<map<int>|boolean> & readonly mp = {
        a: true,
        b: {
            x: 1,
            y: 2
        }
    };
    OptionalId opId = mp;
    assertEquality((), opId?.id);
    assertEquality(<map<int>> {x: 1, y: 2}, opId["b"]);
}

function testSubTypingWithReadOnlyFieldsViaReadOnlyType() {
    Undergraduate & readonly u = {
        particulars: {
            name: "Jo"
        },
        id: 1234
    };
    Person p1 = u;
    assertTrue(p1 is Undergraduate & readonly);

    Graduate & readonly g = {
        particulars: {
          name: "Amy"
        },
        id: 1121
    };
    Person p2 = g;
    assertTrue(p2 is Graduate & readonly);
}

function testSubTypingWithReadOnlyFieldsNegative() {
    Undergraduate u = {
        particulars: {
            name: "Jo"
        },
        id: 1234
    };
    any undergrad = u;

    Graduate g = {
        particulars: {
          name: "Amy"
        },
        id: 1121
    };
    any grad = g;

    assertTrue(undergrad is Undergraduate);
    assertTrue(grad is Graduate);
    assertFalse(undergrad is Person);
    assertFalse(grad is Person);
}

const HUNDRED = 100;

type Baz record {|
    HUNDRED i;
    float|string f;
    boolean b;
    object {} a1;
    json a2;
    decimal d?;
    () ad;
    map<int>|boolean[] u;
    readonly r;
    Quuz q;
    string|int...;
|};

type Qux record {|
    readonly string|int i;
    float f;
    readonly boolean b?;
    readonly int|Quux a1;
    readonly any a2;
    readonly int[] d?;
    readonly anydata ad;
    readonly map<any> u;
    readonly int[] r;
    readonly anydata|object {} q;
    readonly json z;
    string...;
|};

type Quux object {
    map<string> m;

    function getMap() returns map<string>;
};

type Quuz record {|
    int i;
    float f;
|};

readonly class ReadonlyQuux {
    map<string> & readonly m;

    function init(map<string> & readonly m) {
        self.m = m;
    }

    function getMap() returns map<string> & readonly {
        return self.m;
    }
}

function testSubTypingWithReadOnlyFieldsPositiveComposite() {
    int[] & readonly arr = [1, 2];
    readonly & record {|json|xml...;|} rec = {
        "i": 123,
        "f": 988.42
    };

    Qux b = {
        i: 100,
        b: true,
        f: 12.0,
        a1: new ReadonlyQuux({a: "hello", b: "world"}),
        a2: "anydata value",
        ad: (),
        u: {
            a: 1,
            b: 2
        },
        r: arr,
        q: rec,
        z: 1111
    };

    any a = b;
    assertTrue(a is Qux);
    assertTrue(a is Baz);

    Baz f = <Baz> a;
    assertEquality(HUNDRED, f.i);
    assertEquality(12.0, f.f);
    assertTrue(f.b);
    assertTrue(f["a1"] is ReadonlyQuux);
    assertEquality(<map<string>> {a: "hello", b: "world"}, (<ReadonlyQuux> f["a1"]).getMap());
    assertEquality("anydata value", f["a2"]);
    assertEquality((), f?.d);
    assertEquality((), f.ad);
    assertTrue(f.u is map<int> & readonly);
    assertEquality(<map<int>> {a: 1, b: 2}, f.u);
    assertTrue(f.r is int[] & readonly);
    assertEquality(arr, f.r);
    assertEquality(rec, f.q);
    assertEquality(<Quuz> {i: 123, f: 988.42}, f.q);
    assertEquality((), f["y"]);
    assertEquality(1111, f["z"]);
}

function testSubTypingWithReadOnlyFieldsNegativeComposite() {
    int[] & readonly arr = [1, 2];
    readonly & record {|json|xml...;|} rec = {
        "i": 123,
        "f": 988.42
    };

    Qux b1 = {
        i: 101, // doesn't match, expected `HUNDRED`
        b: true,
        f: 12.0,
        a1: new ReadonlyQuux({a: "hello", b: "world"}),
        a2: "anydata value",
        ad: (),
        u: {
            a: 1,
            b: 2
        },
        r: arr,
        q: rec,
        z: 1111
    };

    any a = b1;
    assertTrue(a is Qux);
    assertFalse(a is Baz);

    Qux b2 = {
        i: 100,
         // doesn't match because no `b`
        f: 12.0,
        a1: new ReadonlyQuux({a: "hello", b: "world"}),
        a2: "anydata value",
        ad: (),
        u: {
            a: 1,
            b: 2
        },
        r: arr,
        q: rec,
        z: 1111
    };

    a = b2;
    assertTrue(a is Qux);
    assertFalse(a is Baz);

    Qux b3 = {
        i: 100,
        b: true,
        f: 12.0,
        a1: 123, // doesn't match, expected `object {}`
        a2: "anydata value",
        ad: (),
        u: {
            a: 1,
            b: 2
        },
        r: arr,
        q: rec,
        z: 1111
    };

    a = b3;
    assertTrue(a is Qux);
    assertFalse(a is Baz);

    Qux b4 = {
        i: 100,
        b: true,
        f: 12.0,
        a1: new ReadonlyQuux({a: "hello", b: "world"}),
        a2: xml `<foo>FOO</foo>`, // doesn't match, expected `json`
        ad: (),
        u: {
            a: 1,
            b: 2
        },
        r: arr,
        q: rec,
        z: 1111
    };

    a = b4;
    assertTrue(a is Qux);
    assertFalse(a is Baz);

    Qux b5 = {
        i: 100,
        b: true,
        f: 12.0,
        a1: new ReadonlyQuux({a: "hello", b: "world"}),
        a2: "anydata value",
        ad: 1.2, // doesn't match, expected `()`
        u: {
            a: 1,
            b: 2
        },
        r: arr,
        q: rec,
        z: 1111
    };

    a = b5;
    assertTrue(a is Qux);
    assertFalse(a is Baz);

    Qux b6 = {
        i: 100,
        b: true,
        f: 12.0,
        a1: new ReadonlyQuux({a: "hello", b: "world"}),
        a2: "anydata value",
        ad: (),
        u: {  // doesn't match, expected `map<int>`
            a: 1,
            b: "str"
        },
        r: arr,
        q: rec,
        z: 1111
    };

    a = b6;
    assertTrue(a is Qux);
    assertFalse(a is Baz);

    Qux b7 = {
        i: 100,
        b: true,
        f: 12.0,
        a1: new ReadonlyQuux({a: "hello", b: "world"}),
        a2: "anydata value",
        ad: (),
        u: {
            a: 1,
            b: 2
        },
        r: arr,
        q: {i: 112},  // doesn't match, expected `Quuz`
        z: 1111
    };

    a = b7;
    assertTrue(a is Qux);
    assertFalse(a is Baz);

    Qux b8 = {
        i: 100,
        b: true,
        f: 12.0,
        a1: new ReadonlyQuux({a: "hello", b: "world"}),
        a2: "anydata value",
        ad: (),
        u: {
            a: 1,
            b: 2
        },
        r: arr,
        q: rec,
        z: 1.0 // doesn't match, expected `int|string`
    };

    a = b8;
    assertTrue(a is Qux);
    assertFalse(a is Baz);
}

type StudentParticulars record {|
    readonly string name;
    int id;
|};

type StudentParticularsWithMarks record {|
    readonly string name;
    float id?;
    float...;
|};

function testSubTypingMapAsRecordWithReadOnlyFields() {
    map<string|int> mutableMap = {
        name: "Maryam",
        id: 1234
    };

    map<string|int> & readonly immutableMap = {
        name: "Maryam",
        id: 1234
    };

    map<string|float> & readonly immutableMapWithFloatId = {
        name: "Maryam",
        id: 123.4
    };

    map<string|int|float> & readonly immutableMapWithMarks = {
        name: "Maryam",
        id: 123.4,
        physics: 85.0
    };

    assertFalse(<any> mutableMap is StudentParticulars);
    assertFalse(<any> mutableMap is StudentParticularsWithMarks);

    assertTrue(<any> immutableMap is StudentParticulars);
    assertFalse(<any> immutableMap is StudentParticularsWithMarks);

    assertFalse(<any> immutableMapWithFloatId is StudentParticulars);
    assertTrue(<any> immutableMapWithFloatId is StudentParticularsWithMarks);

    assertFalse(<any> immutableMapWithMarks is StudentParticulars);
    assertTrue(<any> immutableMapWithMarks is StudentParticularsWithMarks);
}

class NonReadOnlyClass {
    int i = 1;
}

readonly class ReadOnlyClass {
    int i;

    isolated function init() {
        self.i = 2;
    }
}

readonly class AnotherReadOnlyClass {
    int i;

    function init(int i) {
        self.i = i;
    }
}

readonly class TheOtherReadOnlyClass {
    final int i;

    isolated function init() {
        self.i = 212;
    }
}

type RecordWithReadOnlyFields record {|
    readonly NonReadOnlyClass a;
    readonly ReadOnlyClass & readonly b = new ReadOnlyClass();
    readonly TheOtherReadOnlyClass c;
|};

type ONE 1;

function testReadOnlyFieldsOfClassTypes() {
    int[] arr = [];

    record {
        readonly NonReadOnlyClass a;
        readonly ReadOnlyClass b;
        readonly int c = 1;
    } rec1 = {
        a: object {
            final int i = 1234;
        },
        b: new AnotherReadOnlyClass(2345)
    };
    assertTrue(<any> rec1 is record {
        object { int i; } a;
        AnotherReadOnlyClass b;
        ONE c;
    });
    assertEquality(1234, rec1.a.i);
    assertEquality(2345, rec1.b.i);
    assertEquality(1, rec1.c);

    RecordWithReadOnlyFields rec2 = {a: new AnotherReadOnlyClass(4567), c: new};
    assertTrue(<any> rec2 is record {
        AnotherReadOnlyClass a;
        ReadOnlyClass b;
        TheOtherReadOnlyClass c;
    });
    assertEquality(4567, rec2.a.i);
    assertEquality(2, rec2.b.i);
    assertEquality(212, rec2.c.i);
}

type CommonResponse record {|
    string mediaType?;
    map<string|string[]> headers?;
    anydata body?;
|};
public type Forbidden record {|
    *CommonResponse;
|};

public type Unauthorized record {|
    *CommonResponse;
    readonly byte[] body;
    readonly boolean valid;
|};

function testTypeReadOnlynessNegativeWithNonReadOnlyFieldsViaInclusion() {
    Unauthorized|Forbidden? x = tryAuthenticate(true);
    assertFalse(<any> x is readonly);
    Unauthorized u = {headers: {"h1": "v1"}, body: [1, 2, 3], valid: false};
    assertEquality(u, x);

    Forbidden f = {headers: {"h1": "v1", "h2": "v2"}, body: "invalid"};
    assertFalse(<any> f is readonly);
    var y = tryAuthenticate(false);
    assertEquality(f, y);
    x = y;
    assertEquality(f, x);
    y = x;
    assertEquality(f, y);
}

function tryAuthenticate(boolean b) returns Unauthorized|Forbidden? {
    if b {
        return {headers: {"h1": "v1"}, body: [1, 2, 3], valid: false};
    }
    return {headers: {"h1": "v1", "h2": "v2"}, body: "invalid"};
}

type OpenRecordWithAllReadOnlyFields record {
    readonly int i = 1;
    readonly int j;
};

type ClosedRecordWithAllReadOnlyFields record {|
    readonly int k;
|};

type IncludingRec1 record {|
    *OpenRecordWithAllReadOnlyFields;
    *ClosedRecordWithAllReadOnlyFields;
    never...;
|};

type IncludingRec2 record {|
    *ClosedRecordWithAllReadOnlyFields;
    readonly int l;
|};

function testTypeReadOnlynessWithReadOnlyFieldsViaInclusion() {
    IncludingRec1|IncludingRec2 w = {i: 1, j: 2, k: 3};
    assertTrue(<any> w is readonly);
    readonly r = w;
    assertEquality(w, r);

    IncludingRec1|IncludingRec2? x = getRecord(true);
    assertEquality(w, x);

    IncludingRec2 v = {k: 1, l: 2};
    assertTrue(<any> v is readonly);
    var y = getRecord(false);
    assertEquality(v, y);
    x = y;
    assertEquality(v, x);
    y = x;
    assertEquality(v, y);
}

function getRecord(boolean b) returns IncludingRec1|IncludingRec2? {
    if b {
        return {i: 1, j: 2, k: 3};
    }
    return {k: 1, l: 2};
}

type Corge record {|
    string a = "hello";
    string b = "world";
    string[] c = <readonly> ["x", "y"];
|};

function testDefaultValueFromCETBeingUsedWithReadOnlyFieldsInTheMappingConstructor() {
    record {int a; string b = "default";} a = {readonly a: 2};
    assertEquality(<anydata> {a: 2, b: "default"}, a);
    assertTrue(a is record {readonly int a; string b;});
    assertFalse(a is record {readonly int a; readonly string b;});

    record {int a; string b = "default";} b = {readonly a: 3, b: "val"};
    assertEquality(<anydata> {a: 3, b: "val"}, b);
    assertTrue(b is record {readonly int a; string b;});
    assertFalse(b is record {readonly int a; readonly string b;});

    record {int a; string b = "default";} c = {readonly a: 3, readonly b: "val"};
    assertEquality(<anydata> {a: 3, b: "val"}, c);
    assertTrue(c is record {readonly int a; string b;});
    assertTrue(c is record {readonly int a; readonly string b;});

    Corge d = {readonly b: "ballerina"};
    string[] & readonly e = ["x", "y"];
    assertEquality(<anydata> {a: "hello", b: "ballerina", c: e}, d);
    assertTrue(d is record {|string a; readonly string b; string[] c;|});
    assertFalse(d is record {|string a; readonly string b; readonly string[] c;|});

    Corge f = {readonly b: "val", readonly c: ["a", "b", "c"]};
    assertEquality(<anydata> {a: "hello", b: "val", c: <readonly> ["a", "b", "c"]}, f);
    assertTrue(f is record {|string a; readonly string b; string[] c;|});
    assertTrue(f is record {|string a; readonly string b; readonly string[] c;|});
    assertFalse(f is record {|readonly string a; readonly string b; readonly string[] c;|});
}

const ASSERTION_ERROR_REASON = "AssertionError";

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
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}

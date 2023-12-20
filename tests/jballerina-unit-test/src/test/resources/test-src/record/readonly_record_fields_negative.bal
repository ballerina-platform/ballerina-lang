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

type Student record {
    readonly string name;
    readonly int id?;
};

function testInvalidUpdateOfRecordWithSimpleReadonlyFields() {
    Student st = {
        name: "Maryam"
    };

    st.name = "Mary";
    st["name"] = "Jo";

    // Should fail at runtime.
    string str = "name";
    st[str] = "Amy";
}

type Employee record {
    readonly Details details;
    string department;
};

type Details record {
    string name;
    int id;
};

function testRecordWithStructuredReadonlyFields() {
    Details details = {
        name: "Kim",
        id: 1000
    };

    Employee e = {
        details,
        department: "finance"
    };

    e.details = details;
    e.details.name = "Jo";
    e.details["id"] = 400;

    // Should fail at runtime.
    string str = "details";
    e[str] = details;
}

type Customer record {
    readonly string name;
    int id;
};

function testInvalidUpdateOfReadonlyFieldInUnion() {
    Customer customer = {
        name: "Jo",
        id: 1234
    };

    Student|Customer sd = customer;
    sd.name = "May"; // invalid
    sd.id = 4567; // valid
}

type Foo record {|
    int[] arr;
    map<string> mp;
|};

type Bar record { // inclusive-type-desc
    readonly int[] arr;
    readonly map<string> mp;
};

type Baz record {| // not all readonly
    readonly int[] arr;
    map<string> mp;
|};

type Qux record {| // only readonly typed
    readonly & int[] arr;
    map<string> & readonly mp;
|};

function testInvalidImmutableTypeAssignmentForNotAllReadOnlyFields() {
    Bar bar = {arr: [1, 2], mp: {a: "a"}};
    Baz baz = {arr: [1, 2], mp: {a: "a"}};
    Qux qux = {arr: [1, 2], mp: {a: "a"}};

    Foo & readonly f1 = bar;
    Foo & readonly f2 = baz;
    Foo & readonly f3 = qux;
}

type Person record {|
    readonly Particulars particulars;
    int id;
|};

type Undergraduate record {|
    readonly & Particulars particulars;
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

function testSubTypingWithReadOnlyFieldsNegative() {
    Undergraduate u = {
        particulars: {
            name: "Jo"
        },
        id: 1234
    };
    Person p1 = u;

    Graduate g = {
        particulars: {
          name: "Amy"
        },
        id: 1121
    };
    Person p2 = g;

    map<map<int>|boolean> mp = {
        a: true,
        b: {
            x: 1,
            y: 2
        }
    };
    OptionalId opId = mp;
}

type Quux record {|
    int id;
    readonly string code?;
|};

function testInvalidOptionalFieldUpdate() {
    Quux q = {id: 100};
    q.code = "quux";
}

function testReadOnlyModifierInStringRepresentation() {
    record {|
        int i;
        string s;
        boolean b;
    |} a = {i: 1, s: "hello", b: false};

    record {|
        readonly int i;
        string s;
        readonly boolean b;
    |} x = a;
}

function testReadOnlyFieldOfNeverReadOnlyType() {
    int[] arr = [];

    record {
        readonly NonReadOnlyClass a;
        readonly stream<int> b;
    } rec = {a: new, b: arr.toStream()};
}

class NonReadOnlyClass {
    int i = 1;
}

readonly class ReadOnlyClass {
    int i = 1;
}

type RecordWithReadOnlyFieldsWithInvalidInitialization record {|
    readonly NonReadOnlyClass a;
    readonly ReadOnlyClass & readonly b = new;
    readonly stream<int> c;
|};

RecordWithReadOnlyFieldsWithInvalidInitialization invalidInit = {a: new, b: new, c: (<int[]> [1, 2]).toStream()};

type OpenRecordWithNoFieldDescriptors record {

};

type OpenIntRecordWithNoFieldDescriptors record {|
    int...;
|};

type OpenRecordWithFieldDescriptors record {
    readonly int x = 100;
};

record {
    readonly int x;
} modLevelRecord = {x: 100};

readonly rd = modLevelRecord;

function testNonReadOnlynessOfOpenRecordWithAllReadOnlyFields() {
    OpenRecordWithNoFieldDescriptors r1 = {};
    OpenRecordWithNoFieldDescriptors r2 = {x: 1};
    OpenIntRecordWithNoFieldDescriptors r3 = {};
    OpenIntRecordWithNoFieldDescriptors r4 = {x: 1};
    OpenRecordWithFieldDescriptors r5 = {};
    OpenRecordWithFieldDescriptors r6 = {"a": 200};

    record {|
        readonly int x = 1;
        int...;
    |} localRecord = {"a": 2, "b": 3};

    readonly rd = modLevelRecord;
    readonly rd1 = r1;
    readonly rd2 = r2;
    readonly rd3 = r3;
    readonly rd4 = r4;
    readonly rd5 = r5;
    readonly rd6 = r6;
    readonly rd7 = localRecord;
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
    Unauthorized|Forbidden? x = ();
    readonly r1 = x;

    Unauthorized? y = tryAuthenticate();
    readonly r2 = y;
    readonly z = tryAuthenticate();
}

function tryAuthenticate() returns Unauthorized? {
    return {headers: {"h1": "v1"}, body: [1, 2, 3], valid: false};
}

type RecordWithReadOnlyFields record {|
    readonly int[] x;
    readonly string y;
|};

RecordWithReadOnlyFields & readonly r1 = {x: []};
readonly & RecordWithReadOnlyFields & readonly r2 = 1;

type R1 record {
    never x?;
};

type R2 record {|
    never x?;
    string y;
|};

function testRecordReadonlynessWithNeverFieldsNegative() {
    R1 r1 = {};
    readonly x = r1;

    R2 r2 = {y: "hello"};
    readonly y = r2;

    record {
        int x;
        never y?;
    } r3 = {x: 1};
    readonly z = r3;
}

type Foo1 record {|
    any[] x = [1, 2];
|};

class Test {
}

type Foo2 record {
    Test y = new();
};

function testRecordReadonlyIntersection() {
    Foo1 & readonly _ = {};
    Foo2 & readonly _ = {};
}

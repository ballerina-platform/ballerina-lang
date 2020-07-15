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

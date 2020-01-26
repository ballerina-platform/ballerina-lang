// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type Person record {|
    int id;
    int age;
    float salary;
    string name;
    boolean married = false;
|};

type Employee object {

    public int id = 1;
    public int age = 10;
    public string name = "sample name";


    function __init(int id, int age, string name) {
        self.id  = id;
        self.age = age;
        self.name = name;
    }
};

type test table<Employee>;

function testTableLiteralDataAndAdd2() returns (int) {
    Person p4 = { id: 4, age: 30, salary: 300.50, name: "john", married: true };
    Person p5 = { id: 5, age: 30, salary: 300.50, name: "mary", married: true };

    table<Person> t1 = table {
        { key id, salary, key name, age, married2 },
        [{ 1, 300.5, "jane",  30, true },
        { 2, 302.5, "anne",  23, false },
        { 3, 320.5, "john",  33, true }
        ]
    };

    checkpanic t1.add(p4);
    checkpanic t1.add(p5);

    int count = 0;
    foreach var v in t1 {
        count = count + 1;
    }
    return count;
}

function testTableLiteralDataWithInit() returns (int) {
    table<Person> t1 = table {
        { key id, salary, key name, age, married },
        [1, 1]
    };

    int count = 0;
    foreach var v in t1 {
        count = count + 1;
    }
    return count;
}

function testTableLiteralDataAndAddWithObject() returns (int) {
    Employee p4 = new Employee(4, 24, "Paul");
    Employee p5 = new Employee(5, 30, "mary");

    //Object types cannot be included in the literal
    table<Employee> t1 = table {
        { key id, name, age }
    };

    checkpanic t1.add(p4);
    checkpanic t1.add(p5);

    int count = 0;
    foreach var v in t1 {
        count = count + 1;
    }
    return count;
}

function testUnknownTableType() {
    table<Student> t1 = table {};
}

function testTableRemoveInvalidFunctionPointer() returns [int, json] | error {
    Person p1 = { id: 1, age: 35, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 40, salary: 200.50, name: "martin", married: true };
    Person p3 = { id: 3, age: 42, salary: 100.50, name: "john", married: false };

    table<Person> dt = table{};
    checkpanic dt.add(p1);
    checkpanic dt.add(p2);
    checkpanic dt.add(p3);

    var res = dt.remove(isBelow35Invalid);
    int count = -1;
    if (res is int) {
        count = res;
    }
    json j = check typedesc<json>.constructFrom(dt);

    return [count, j];
}

type SubjectInvalid record {
    float name;
    int moduleCount;
};

type SubjectInvalid2 record {
    json name;
    int moduleCount;
};

function testTableFloatPrimaryKey() {
    table<SubjectInvalid> t1 = table {
        { key name, moduleCount }
    };
}

function testTableJsonPrimaryKey() {
    table<SubjectInvalid2> t1 = table {
        { key name, moduleCount }
    };
}

type UnionRecord record {
    int id;
    float|int salary;
};

type RecordInRecord record {
    int id;
    Bar bar;
};

type ObjectInRecord record {
    int id;
    Foo foo;
};

type ErrorInRecord record {
    int id;
    error bar;
};

type Bar record {
    int a;
};

type Foo object {
    public int age = 0;
};

function addInvalidUnionData() {
    table<UnionRecord> t = table {
        { key id, salary },
        [{1, 300.5}
        ]
    };
}

function addInvalidRecordData() {
    Bar bar1 = { a: 10 };
    table<RecordInRecord> t = table {
        { key id, bar },
        [{1, bar1}
        ]
    };
}

function addInvalidObjectData() {
    Foo foo1 = new();
    table<ObjectInRecord> t = table {
        { key id, foo },
        [{1, foo1}
        ]
    };
}

function addInvalidErrorData() {
    table<ErrorInRecord> t1 = table {
        { key id, bar }
    };

    error e = error("response error");
    ErrorInRecord d1 = { id: 10, bar: e };
    checkpanic t1.add(d1);
}

type ArrayRecord record {
    int id;
    xml[] xArr;
    error?[] eArr;
};

function addInvalidArrayData() {
    table<ArrayRecord> t1 = table {
        { key id, xArr, eArr }
    };
}

function isBelow35Invalid(Person p) {
    p.age = 10;
}

function testTableLiteralWithVar() {

    var t1 = table {
        { key id, salary, key name, age, married2 },
        [{ 1, 300.5, "jane",  30, true },
        { 2, 302.5, "anne",  23, false },
        { 3, 320.5, "john",  33, true }
        ]
    };
}

public function testNonRecordAsTableConstraint(string... args) {
    table<any> t = table {
                { key id, name, salary },
                [ { 1, "Mary",  300.5 },
                  { 2, "John",  200.5 },
                  { 3, "Jim", 330.5 }
                ]
            };
}

type Teacher record {
    int id;
    int age;
    string name;
};

table<Teacher> t1 = table {
    { key id, age, name },
    [
        { 1, 29 }
    ]
};

type Qux record {|
    string s;
    float f;
|};

type Baz record {|
    int i;
|};

function testInvalidAssignment() {
    table<Qux> t = table {
        {key s, f},
        [
            {"a", 1.0},
            {"b", 2.0}
        ]
    };
    table<Baz> t2 = t;
}

function testInvalidTypeguard() {
    table<record {}> t = table {};
    table<record {}>|error t2 = t;

    if t2 is table<Qux> {
        // table<Qux>
    } else {
        error e = t2;
    }
}

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

type Person record {
    int id;
    int age;
    float salary;
    string name;
    boolean married;
    !...
};

type Employee object {

    public int id = 1;
    public int age = 10;
    public string name = "sample name";


    new(id, age, name) {
    }
};

type test table<Employee>;

function testTableLiteralDataAndAdd2() returns (int) {
    Person p4 = { id: 4, age: 30, salary: 300.50, name: "john", married: true };
    Person p5 = { id: 5, age: 30, salary: 300.50, name: "mary", married: true };

    table<Person> t1 = table {
        { key id, key salary, name, age, married2 },
        [{ 1, 300.5, "jane",  30, true },
        { 2, 302.5, "anne",  23, false },
        { 3, 320.5, "john",  33, true }
        ]
    };

    _ = t1.add(p4);
    _ = t1.add(p5);

    int count = t1.count();
    return count;
}

function testTableLiteralDataWithInit() returns (int) {
    table<Person> t1 = table {
        { key id, key salary, name, age, married },
        [1, 1]
    };

    int count = t1.count();
    return count;
}

function testTableLiteralDataAndAddWithObject() returns (int) {
    Employee p4 = new Employee(4, 24, "Paul");
    Employee p5 = new Employee(5, 30, "mary");

    //Object types cannot be included in the literal
    table<Employee> t1 = table {
        { key id, name, age }
    };

    _ = t1.add(p4);
    _ = t1.add(p5);

    int count = t1.count();
    return count;
}

function testEmptyTableCreateInvalid() {
    table t1 = table{};
}

function testUnknownTableType() {
    table<Student> t1 = table {};
}

function testTableRemoveInvalidFunctionPointer() returns (int, json) {
    Person p1 = { id: 1, age: 35, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 40, salary: 200.50, name: "martin", married: true };
    Person p3 = { id: 3, age: 42, salary: 100.50, name: "john", married: false };

    table<Person> dt = table{};
    _ = dt.add(p1);
    _ = dt.add(p2);
    _ = dt.add(p3);

    int count = check dt.remove(isBelow35Invalid);
    json j = check <json>dt;

    return (count, j);
}

function isBelow35Invalid(Person p) {
    p.age = 10;
}

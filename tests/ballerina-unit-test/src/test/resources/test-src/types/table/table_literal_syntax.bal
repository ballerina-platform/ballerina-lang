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
};

type Person2 record {
    int id;
    int age;
    string key;
    string name;
    boolean married;
};

type Subject record {
   string name;
   int moduleCount;
};

type Data record {
    int id;
    decimal salary;
    json address;
    xml role;
};

type ArrayData record {
    int id;
    int[] intArr;
    string[] strArr;
    float[] floatArr;
    boolean[] boolArr;
    byte[] byteArr;
    decimal[] decimalArr;
};

table<Person> tGlobal = table{};

function testTableDefaultValueForLocalVariable() returns (int) {
    table<Person> t1 = table {};
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    _ = t1.add(p1);
    int count = t1.count();
    return count;
}

function testTableDefaultValueForGlobalVariable() returns (int) {
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    _ = tGlobal.add(p1);
    int count = tGlobal.count();
    return count;
}

function testTableAddOnUnconstrainedTable() returns (int) {
    table<Person> t1 = table {};
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    _ = t1.add(p1);
    int count = t1.count();
    return count;
}

function testTableAddOnConstrainedTable() returns (int) {
    table<Person> t1 = table {
        { key id, salary, key name, age, married }
    };

    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 30, salary: 300.50, name: "jane", married: true };
    _ = t1.add(p1);
    _ = t1.add(p2);
    int count = t1.count();
    return count;
}

function testValidTableVariable() returns (int) {
    table<record {}> t1;
    table<Person> t2;
    return 0;
}

function testTableLiteralData() returns (int) {
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 30, salary: 300.50, name: "anne", married: true };
    Person p3 = { id: 3, age: 30, salary: 300.50, name: "peter", married: true };

    table<Person> t1 = table {
        { key id, salary, key name, age, married },
        [p1, p2, p3]
    };

    int count = t1.count();
    return count;
}

function testTableLiteralDataAndAdd() returns (int) {
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 30, salary: 300.50, name: "anne", married: true };
    Person p3 = { id: 3, age: 30, salary: 300.50, name: "peter", married: true };
    Person p4 = { id: 4, age: 30, salary: 300.50, name: "john", married: true };
    Person p5 = { id: 5, age: 30, salary: 300.50, name: "mary", married: true };

    table<Person> t1 = table {
        { key id, salary, name, age, married },
        [p1, p2, p3]
    };

    _ = t1.add(p4);
    _ = t1.add(p5);

    int count = t1.count();
    return count;
}

function testTableLiteralDataAndAdd2() returns (int) {
    Person p4 = { id: 4, age: 30, salary: 300.50, name: "john", married: true };
    Person p5 = { id: 5, age: 30, salary: 300.50, name: "mary", married: true };

    table<Person> t1 = table {
        { key id, salary, key name, age, married },
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

function testTableAddOnConstrainedTableWithViolation() returns (int) {
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };

    table<Person> t1 = table {
        { key id, salary, name, age, married },
        [p1, p2]
    };

    int count = t1.count();
    return count;
}

function testTableAddOnConstrainedTableWithViolation2() returns (string) {
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 30, salary: 300.50, name: "jane", married: true };
    Person p3 = { id: 2, age: 30, salary: 300.50, name: "jane", married: true };

    table<Person> t1 = table {
        { key id, salary, name, age, married },
        [p1, p2]
    };

    var ret = t1.add(p3);
    string s = ret is error ? <string>ret.detail().message : "nil";
    return s;
}

function testTableLiteralDataAndAddWithKey() returns (int) {
    Person2 p4 = { id: 4, age: 30, key: "test", name: "john", married: true };
    Person2 p5 = { id: 5, age: 30, key: "test", name: "mary", married: true };

    string key = "test2";
    table<Person2> t1 = table {
        { key id, key key, name, age, married },
        [{ 1, "test3", "jane", 30, true },
         { 2, key, "anne", 23, false },
         { 3, "test4", "peter", 33, true }
        ]
    };

    _ = t1.add(p4);
    _ = t1.add(p5);

    int count = t1.count();
    return count;
}

function testTableAddWhileIterating() returns (int, int) {
    table<Person> t1 = table {
        { key id, salary, key name, age, married }
    };

    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 30, salary: 350.50, name: "jane", married: true };
    _ = t1.add(p1);

    int loopVariable = 0;

    while(t1.hasNext()) {
        loopVariable = loopVariable + 1;
        _ = t1.add(p2);
        any data = t1.getNext();
    }
    int count = t1.count();
    return (loopVariable, count);
}

function testTableStringPrimaryKey() returns int {
    table<Subject> t1 = table {
        { key name, moduleCount }
    };

    Subject s1 = { name: "Maths", moduleCount: 10 };
    Subject s2 = { name: "Science", moduleCount: 5 };
    _ = t1.add(s1);
    _ = t1.add(s2);

    int count = t1.count();
    return t1.count();
}

function testTableWithDifferentDataTypes() returns (int, int, decimal, xml, json) {
    table<Data> t1 = table {
        { key id, salary, address, role }
    };

    json j = { city: "London", country: "UK" };
    xml x = xml `<role>Manager</role>`;
    Data d1 = { id: 10, salary: 1000.45, address: j, role: x  };
    _ = t1.add(d1);

    int i = 0;
    decimal d = 0;
    xml xRet = xml `<book>Invalid Role</book>`;
    json jRet = {};
    foreach var v in t1 {
        i = v.id;
        d = v.salary;
        jRet = v.address;
        xRet = v.role;
    }

    int count = t1.count();
    return (count, i, d, xRet, jRet);
}

function testArrayData() returns (int, int[], string[], float[], boolean[], byte[], decimal[]) {
    int[] iArr = [1, 2, 3];
    string[] sArr = ["test1", "test2"];
    float[] fArr = [1.1, 2.2];
    boolean[] bArr = [true, false];
    byte[] byteArrVal = base64 `aGVsbG8gYmFsbGVyaW5hICEhIQ==`;
    decimal[] dArr = [11.11, 22.22];

    table<ArrayData> t1 = table {
        { key id, intArr, strArr, floatArr, boolArr, byteArr, decimalArr}
    };

    ArrayData d1 = { id: 10, intArr: iArr, strArr: sArr, floatArr: fArr, boolArr: bArr, byteArr: byteArrVal,
                     decimalArr: dArr };
    _ = t1.add(d1);

    int i = 0;
    int[] retiArr;
    string[] retsArr;
    float[] retfArr;
    boolean[] retbArr;
    byte[] retbyteArr;
    decimal[] retdArr;
    foreach var v in t1 {
        i = v.id;
        retiArr = v.intArr;
        retsArr = v.strArr;
        retfArr = v.floatArr;
        retbArr = v.boolArr;
        retbyteArr = v.byteArr;
        retdArr = v.decimalArr;
    }
    return (i, retiArr, retsArr, retfArr, retbArr, retbyteArr, retdArr);
}

function isBelow35(Person p) returns (boolean) {
    return p.age < 35;
}

// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/io;
import ballerina/xmlutils;
import ballerina/jsonutils;

type Person record {
    int id;
    int age;
    float salary;
    string name;
    boolean married;
};

type DefaultablePerson record {
    int id = 0;
    int age = 0;
    float salary = 0.0;
    string name = "empty";
    boolean married = false;
};

type Company record {
    int id;
    string name;
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

type BlobTypeTest record {
    int id;
    byte[] blobData;
};

type TypeTest record {
    int id;
    json jsonData;
    xml xmlData;
};

type ArraTypeTest record {
    int id;
    int[] intArrData;
    float[] floatArrData;
    string[] stringArrData;
    boolean[] booleanArrData;
};

type Order record {
    int id;
    string name;
};

table<Person> tGlobal = table{};

function testTableDefaultValueForLocalVariable() returns (int) {
    table<Person> t1 = table {};
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    checkpanic t1.add(p1);
    int count = 0;
    foreach var v in t1 {
        count = count + 1;
    }
    return count;
}

function testTableDefaultValueForGlobalVariable() returns (int) {
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    checkpanic tGlobal.add(p1);
    int count = 0;
    foreach var v in tGlobal {
        count = count + 1;
    }
    return count;
}

function testTableAddOnUnconstrainedTable() returns (int) {
    table<Person> t1 = table {};
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    checkpanic t1.add(p1);
    int count = 0;
    foreach var v in t1 {
        count = count + 1;
    }
    return count;
}

function testTableAddOnConstrainedTable() returns (int) {
    table<Person> t1 = table {
        { key id, salary, key name, age, married }
    };

    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 30, salary: 300.50, name: "jane", married: true };
    checkpanic t1.add(p1);
    checkpanic t1.add(p2);
    int count = 0;
    foreach var v in t1 {
        count = count + 1;
    }
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

    int count = 0;
    foreach var v in t1 {
        count = count + 1;
    }
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

    checkpanic t1.add(p4);
    checkpanic t1.add(p5);

    int count = 0;
    foreach var v in t1 {
        count = count + 1;
    }
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

    checkpanic t1.add(p4);
    checkpanic t1.add(p5);

    int count = 0;
    foreach var v in t1 {
        count = count + 1;
    }
    return count;
}

function testTableAddOnConstrainedTableWithViolation() returns (int) {
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };

    table<Person> t1 = table {
        { key id, salary, name, age, married },
        [p1, p2]
    };

    int count = 0;
    foreach var v in t1 {
        count = count + 1;
    }
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
    string s = ret is error ? <string>ret.detail()["message"] : "nil";
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

    checkpanic t1.add(p4);
    checkpanic t1.add(p5);

    int count = 0;
    foreach var v in t1 {
        count = count + 1;
    }
    return count;
}

function testTableAddWhileIterating() returns [int, int] {
    table<Person> t1 = table {
        { key id, salary, key name, age, married }
    };

    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 30, salary: 350.50, name: "jane", married: true };
    checkpanic t1.add(p1);

    int loopVariable = 0;

    while(t1.hasNext()) {
        loopVariable = loopVariable + 1;
        error? e = t1.add(p2);
        any data = t1.getNext();
    }
    int count = 0;
    foreach var v in t1 {
        count = count + 1;
    }
    return [loopVariable, count];
}

function testTableStringPrimaryKey() returns int {
    table<Subject> t1 = table {
        { key name, moduleCount }
    };

    Subject s1 = { name: "Maths", moduleCount: 10 };
    Subject s2 = { name: "Science", moduleCount: 5 };
    checkpanic t1.add(s1);
    checkpanic t1.add(s2);

    int count = 0;
    foreach var v in t1 {
        count = count + 1;
    }
    return count;
}

function testTableWithDifferentDataTypes() returns [int, int, decimal, xml, json] {
    table<Data> t1 = table {
        { key id, salary, address, role }
    };

    json j = { city: "London", country: "UK" };
    xml x = xml `<role>Manager</role>`;
    Data d1 = { id: 10, salary: 1000.45, address: j, role: x  };
    checkpanic t1.add(d1);

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

    int count = 0;
    foreach var v in t1 {
        count = count + 1;
    }
    return [count, i, d, xRet, jRet];
}

function testArrayData() returns [int, int[], string[], float[], boolean[], byte[], decimal[]] {
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
    checkpanic t1.add(d1);

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
    return [i, retiArr, retsArr, retfArr, retbArr, retbyteArr, retdArr];
}

function testAddData() returns [int, int, int, int[], int[], int[]] {
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 20, salary: 200.50, name: "martin", married: true };
    Person p3 = { id: 3, age: 32, salary: 100.50, name: "john", married: false };

    Company c1 = { id: 100, name: "ABC" };

    table<Person> dt3 = table{};
    table<Person> dt4 = table{};
    table<Company> ct1 = table{};

    checkpanic dt3.add(p1);
    checkpanic dt3.add(p2);

    checkpanic dt4.add(p3);

    checkpanic ct1.add(c1);

    int count1 = 0;
    foreach var v in dt3 {
        count1 = count1 + 1;
    }

    int[] dt1data = [];
    int i = 0;
    while (dt3.hasNext()) {
        var p = dt3.getNext();
        dt1data[i] = p.id;
        i = i + 1;
    }

    int count2 = 0;
    foreach var v in dt4 {
        count2 = count2 + 1;
    }

    int[] dt2data = [];
    i = 0;
    while (dt4.hasNext()) {
        var p = dt4.getNext();
        dt2data[i] = p.id;
        i = i + 1;
    }

    int count3 = 0;
    foreach var v in ct1 {
        count3 = count3 + 1;
    }

    int[] ct1data = [];
    i = 0;
    while (ct1.hasNext()) {
        var p = ct1.getNext();
        ct1data[i] = p.id;
        i = i + 1;
    }
    return [count1, count2, count3, dt1data, dt2data, ct1data];
}

function testMultipleAccess() returns [int, int, int[], int[]] {
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 20, salary: 200.50, name: "martin", married: true };
    Person p3 = { id: 3, age: 32, salary: 100.50, name: "john", married: false };

    table<Person> dt3 = table{};
    checkpanic dt3.add(p1);
    checkpanic dt3.add(p2);
    checkpanic dt3.add(p3);

    int count1 = 0;
    foreach var v in dt3 {
        count1 = count1 + 1;
    }
    int[] dtdata1 = [];
    int i = 0;
    while (dt3.hasNext()) {
        var p = dt3.getNext();
        dtdata1[i] = p.id;
        i = i + 1;
    }

    int count2 = 0;
    foreach var v in dt3 {
        count2 = count2 + 1;
    }
    int[] dtdata2 = [];
    i = 0;
    while (dt3.hasNext()) {
        var p = dt3.getNext();
        dtdata2[i] = p.id;
        i = i + 1;
    }
    return [count1, count2, dtdata1, dtdata2];
}


function testLoopingTable() returns (string) {
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 20, salary: 200.50, name: "martin", married: true };
    Person p3 = { id: 3, age: 32, salary: 100.50, name: "john", married: false };

    table<Person> dt = table{};
    checkpanic dt.add(p1);
    checkpanic dt.add(p2);
    checkpanic dt.add(p3);

    string names = "";

    while (dt.hasNext()) {
        var p = dt.getNext();
        names = names + p.name + "_";
    }
    return names;
}

function testTableWithAllDataToStruct() returns [json, xml]|error {
    json j1 = { name: "apple", color: "red", price: 30.3 };
    xml x1 = xml `<book>The Lost World</book>`;
    TypeTest t1 = { id: 1, jsonData: j1, xmlData: x1 };

    table<TypeTest> dt3 = table{};
    checkpanic dt3.add(t1);

    json jData = {};
    xml xData = xml ` `;
    while (dt3.hasNext()) {
        var x = dt3.getNext();
        jData = x.jsonData;
        xData = x.xmlData;
    }
    return [jData, xData];
}

function testTableWithBlobDataToStruct() returns (byte[]|error) {
    string text = "Sample Text";
    byte[] content = text.toBytes();
    BlobTypeTest t1 = { id: 1, blobData: content };

    table<BlobTypeTest> dt3 = table{};
    checkpanic dt3.add(t1);

    byte[] bData = [];
    while (dt3.hasNext()) {
        var x = dt3.getNext();
        bData = x.blobData;
    }
    return bData;
}

function testStructWithDefaultDataToStruct() returns [int, float, string, boolean]|error {
    DefaultablePerson p1 = { id: 1 };

    table<DefaultablePerson> dt3 = table{};
    checkpanic dt3.add(p1);

    int iData = -1;
    float fData = -1;
    string sData = "";
    boolean bData = true;

    while (dt3.hasNext()) {
        var x = <DefaultablePerson>dt3.getNext();
        iData = x.age;
        fData = x.salary;
        sData = x.name;
        bData = x.married;
    }
    return [iData, fData, sData, bData];
}

function testTableWithArrayDataToStruct() returns [int[], float[], string[], boolean[]]|error {
    int[] intArray = [1, 2, 3];
    float[] floatArray = [11.1, 22.2, 33.3];
    string[] stringArray = ["Hello", "World"];
    boolean[] boolArray = [true, false, true];
    ArraTypeTest t1 = { id: 1, intArrData: intArray, floatArrData: floatArray, stringArrData: stringArray,
        booleanArrData: boolArray };

    table<ArraTypeTest> dt3 = table{};
    checkpanic dt3.add(t1);

    int[] intArr = [];
    float[] floatArr = [];
    string[] stringArr = [];
    boolean[] boolArr = [];

    while (dt3.hasNext()) {
        var x = dt3.getNext();
        intArr = x.intArrData;
        floatArr = x.floatArrData;
        stringArr = x.stringArrData;
        boolArr = x.booleanArrData;
    }
    return [intArr, floatArr, stringArr, boolArr];
}

function testTableWithAllDataToXml() returns (xml|error) {
    json j1 = { name: "apple", color: "red", price: 30.3 };
    xml x1 = xml `<book>The Lost World</book>`;
    TypeTest t1 = { id: 1, jsonData: j1, xmlData: x1 };
    TypeTest t2 = { id: 2, jsonData: j1, xmlData: x1 };

    table<TypeTest> dt3 = table{};
    checkpanic dt3.add(t1);
    checkpanic dt3.add(t2);

    xml x = xmlutils:fromTable(dt3);
    return x;
}

function testTableWithArrayDataToXml() returns (xml|error) {
    int[] intArray = [1, 2, 3];
    float[] floatArray = [11.1, 22.2, 33.3];
    string[] stringArray = ["Hello", "World"];
    boolean[] boolArray = [true, false, true];
    ArraTypeTest t1 = { id: 1, intArrData: intArray, floatArrData: floatArray, stringArrData: stringArray,
        booleanArrData: boolArray };

    int[] intArray2 = [10, 20, 30];
    float[] floatArray2 = [111.1, 222.2, 333.3];
    string[] stringArray2 = ["Hello", "World", "test"];
    boolean[] boolArray2 = [false, false, true];
    ArraTypeTest t2 = { id: 2, intArrData: intArray2, floatArrData: floatArray2, stringArrData: stringArray2,
        booleanArrData: boolArray2 };

    table<ArraTypeTest> dt3 = table{};
    checkpanic dt3.add(t1);
    checkpanic dt3.add(t2);

    xml x = xmlutils:fromTable(dt3);
    return x;
}

function testToJson() returns (json|error) {
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 20, salary: 200.50, name: "martin", married: true };
    Person p3 = { id: 3, age: 32, salary: 100.50, name: "john", married: false };

    table<Person> dt = table{};
    checkpanic dt.add(p1);
    checkpanic dt.add(p2);
    checkpanic dt.add(p3);

    json j = jsonutils:fromTable(dt);
    return j;
}

function testToXML() returns (xml|error) {
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 20, salary: 200.50, name: "martin", married: true };
    Person p3 = { id: 3, age: 32, salary: 100.50, name: "john", married: false };

    table<Person> dt = table{};
    checkpanic dt.add(p1);
    checkpanic dt.add(p2);
    checkpanic dt.add(p3);

    xml x = xmlutils:fromTable(dt);
    return x;
}

function testTableWithAllDataToJson() returns (json|error) {
    json j1 = { name: "apple", color: "red", price: 30.3 };
    xml x1 = xml `<book>The Lost World</book>`;
    TypeTest t1 = { id: 1, jsonData: j1, xmlData: x1 };
    TypeTest t2 = { id: 2, jsonData: j1, xmlData: x1 };

    table<TypeTest> dt3 = table{};
    checkpanic dt3.add(t1);
    checkpanic dt3.add(t2);

    json j = jsonutils:fromTable(dt3);
    return j;
}

function testTableWithBlobDataToJson() returns (json|error) {
    string text = "Sample Text";
    byte[] content = text.toBytes();
    BlobTypeTest t1 = { id: 1, blobData: content };

    table<BlobTypeTest> dt3 = table{};
    checkpanic dt3.add(t1);

    json j = jsonutils:fromTable(dt3);
    return j;
}

function testTableWithBlobDataToXml() returns (xml|error) {
    string text = "Sample Text";
    byte[] content = text.toBytes();
    BlobTypeTest t1 = { id: 1, blobData: content };

    table<BlobTypeTest> dt3 = table{};
    checkpanic dt3.add(t1);

    xml x = xmlutils:fromTable(dt3);
    return x;
}

function testTableLiteralWithDefaultableRecord() returns (json|error) {
    table<DefaultablePerson> t1 = table {
        { key id, age, salary, name, married },
        [{ 1, 23, 340.50, "John" },
         { 2, 34, 345.32 }
        ]
    };

    json j = jsonutils:fromTable(t1);
    return j;
}

function testStructWithDefaultDataToJson() returns (json|error) {
    DefaultablePerson p1 = { id: 1 };

    table<DefaultablePerson> dt3 = table{};
    checkpanic dt3.add(p1);

    json j = jsonutils:fromTable(dt3);
    return j;
}

function testStructWithDefaultDataToXml() returns (xml|error) {
    DefaultablePerson p1 = { id: 1 };

    table<DefaultablePerson> dt3 = table{};
    checkpanic dt3.add(p1);

    xml x = xmlutils:fromTable(dt3);
    return x;
}

function testTableWithArrayDataToJson() returns (json|error) {
    int[] intArray = [1, 2, 3];
    float[] floatArray = [11.1, 22.2, 33.3];
    string[] stringArray = ["Hello", "World"];
    boolean[] boolArray = [true, false, true];
    ArraTypeTest t1 = { id: 1, intArrData: intArray, floatArrData: floatArray, stringArrData: stringArray,
        booleanArrData: boolArray };

    int[] intArray2 = [10, 20, 30];
    float[] floatArray2 = [111.1, 222.2, 333.3];
    string[] stringArray2 = ["Hello", "World", "test"];
    boolean[] boolArray2 = [false, false, true];
    ArraTypeTest t2 = { id: 2, intArrData: intArray2, floatArrData: floatArray2, stringArrData: stringArray2,
        booleanArrData: boolArray2 };

    table<ArraTypeTest> dt3 = table{};
    checkpanic dt3.add(t1);
    checkpanic dt3.add(t2);

    json j = jsonutils:fromTable(dt3);
    return j;
}

function testPrintData() {
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 20, salary: 200.50, name: "martin", married: true };
    Person p3 = { id: 3, age: 32, salary: 100.50, name: "john", married: false };

    table<Person> dt = table{
        { key id, key age,  salary, name, married }
    };
    checkpanic dt.add(p1);
    checkpanic dt.add(p2);
    checkpanic dt.add(p3);

   io:println(dt);
}

function testTableAddAndAccess() returns [string, string]|error {
    Person p1 = { id: 1, age: 35, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 40, salary: 200.50, name: "martin", married: true };
    Person p3 = { id: 3, age: 42, salary: 100.50, name: "john", married: false };


    table<Person> dt = table{};
    checkpanic dt.add(p1);
    checkpanic dt.add(p2);

    json j1 = jsonutils:fromTable(dt);
    string s1 = j1.toJsonString();

    checkpanic dt.add(p3);
    json j2 = jsonutils:fromTable(dt);
    string s2 = j2.toJsonString();

    return [s1, s2];
}

function testPrintDataEmptyTable() {
    table<Person> dt = table{};
    io:println(dt);
}

function testTableRemoveSuccess() returns [int, json]|error {
    Person p1 = { id: 1, age: 35, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 20, salary: 200.50, name: "martin", married: true };
    Person p3 = { id: 3, age: 32, salary: 100.50, name: "john", married: false };

    table<Person> dt = table{};
    checkpanic dt.add(p1);
    checkpanic dt.add(p2);
    checkpanic dt.add(p3);

    int count = check dt.remove(isBelow35);
    json j = jsonutils:fromTable(dt);

    return [count, j];
}

function testTableRemoveSuccessMultipleMatch() returns [int, json]|error {
    Person p1 = { id: 1, age: 35, salary: 300.50, name: "john", married: true };
    Person p2 = { id: 2, age: 20, salary: 200.50, name: "martin", married: true };
    Person p3 = { id: 3, age: 32, salary: 100.50, name: "john", married: false };

    table<Person> dt = table{};
    checkpanic dt.add(p1);
    checkpanic dt.add(p2);
    checkpanic dt.add(p3);

    int count = check dt.remove(isJohn);
    json j = jsonutils:fromTable(dt);

    return [count, j];
}

function testTableRemoveFailed() returns [int, json]|error {
    Person p1 = { id: 1, age: 35, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 40, salary: 200.50, name: "martin", married: true };
    Person p3 = { id: 3, age: 42, salary: 100.50, name: "john", married: false };


    table<Person> dt = table{};
    checkpanic dt.add(p1);
    checkpanic dt.add(p2);
    checkpanic dt.add(p3);

    int count = check dt.remove(isBelow35);
    json j = jsonutils:fromTable(dt);

    return [count, j];
}

function testRemoveWithInvalidRecordType() returns string {
    Person p1 = { id: 1, age: 35, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 40, salary: 200.50, name: "martin", married: true };
    Person p3 = { id: 3, age: 42, salary: 100.50, name: "john", married: false };

    table<Person> dt = table{};
    checkpanic dt.add(p1);
    checkpanic dt.add(p2);
    checkpanic dt.add(p3);

    string returnStr = "";
    var ret = dt.remove(isBelow35Invalid);
    if (ret is int) {
        returnStr = ret.toString();
    } else {
        returnStr = <string>ret.detail()["message"];
    }
    return returnStr;
}

function testRemoveOpAnonymousFilter() returns table<Order> {
    table<Order> orderTable = table {
        { id, name },
        [
            {1, "BTC"},
            {2, "LTC"}
        ]
    };

    int invalid = 0;
    var s = orderTable.remove(function (Order ord) returns boolean {
                        return ord.id > invalid;
                    });
    return orderTable;
}

function testTableAddOnConstrainedTableWithViolation3() {
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };

    table<Person> t1 = table {
        { key id, salary, name, age, married },
        [p1, p2]
    };
}

//function testEmptyTableCreate() returns [int, int] {
//    table<Person> dt3 = table{};
//    table<Person> dt4 = table{};
//    table<Company> dt5 = table{};
//    table<Person> dt6;
//    table<Company> dt7;
//    table<record {}> dt8;
//    int count1 = checkTableCount("TABLE_PERSON_%");
//    int count2 = checkTableCount("TABLE_COMPANY_%");
//    return [count1, count2];
//}

//function checkTableCount(string tablePrefix) returns (int) {
//    h2:Client testDB = new(<h2:InMemoryConfig>{
//        name: "TABLEDB",
//        username: "SA",
//        password: "",
//        poolOptions: { maximumPoolSize: 1 }
//    });
//
//    sql:Parameter p1 = { sqlType: sql:TYPE_VARCHAR, value: tablePrefix };
//
//    int count = 0;
//    var dt = testDB->select("SELECT count(*) as count FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME like
//         ?", ResultCount, p1);
//    if (dt is table<ResultCount>) {
//        while (dt.hasNext()) {
//            var ret = dt.getNext();
//            if (ret is ResultCount) {
//                count = ret.COUNTVAL;
//            } else {
//                count = -1;
//            }
//        }
//    }
//    checkpanic testDB.stop();
//    return count;
//}

function testTableDrop() {
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };

    table<Person> dt = table{};
    checkpanic dt.add(p1);
}

function isBelow35(Person p) returns (boolean) {
    return p.age < 35;
}

function isJohn(Person p) returns (boolean) {
    return p.name == "john";
}

function isBelow35Invalid(Company p) returns (boolean) {
    return p.id < 35;
}
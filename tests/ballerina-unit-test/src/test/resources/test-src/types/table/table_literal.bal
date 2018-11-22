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

import ballerina/h2;
import ballerina/io;
import ballerina/sql;

type Person record {
    int id = 0;
    int age = 0;
    float salary = 0.0;
    string name = "";
    boolean married = false;
};

type Company record {
    int id;
    string name;
};

type TypeTest record {
    int id;
    json jsonData;
    xml xmlData;
};

type BlobTypeTest record {
    int id;
    byte[] blobData;
};

type AnyTypeTest record {
    int id;
    any anyData;
};

type ArraTypeTest record {
    int id;
    int[] intArrData;
    float[] floatArrData;
    string[] stringArrData;
    boolean[] booleanArrData;
};

type ResultCount record {
    int COUNTVAL;
};

table<Person> dt1 = table{};
table<Company> dt2 = table{};

function testEmptyTableCreate() returns (int, int) {
    table<Person> dt3 = table{};
    table<Person> dt4 = table{};
    table<Company> dt5 = table{};
    table<Person> dt6;
    table<Company> dt7;
    table dt8;
    int count1 = checkTableCount("TABLE_PERSON_%");
    int count2 = checkTableCount("TABLE_COMPANY_%");
    return (count1, count2);
}

function checkTableCount(string tablePrefix) returns (int) {
    endpoint h2:Client testDB {
        name: "TABLEDB",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    sql:Parameter p1 = { sqlType: sql:TYPE_VARCHAR, value: tablePrefix };

    int count = 0;
    var dt = testDB->select("SELECT count(*) as count FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME like
         ?", ResultCount, p1);
    if (dt is table) {
        while (dt.hasNext()) {
            var ret = <ResultCount>dt.getNext();
            if (ret is ResultCount) {
                count = ret.COUNTVAL;
            } else if (ret is error) {
                count = -1;
            }
        }
    }
    testDB.stop();
    return count;
}

function testAddData() returns (int, int, int, int[], int[], int[]) {
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 20, salary: 200.50, name: "martin", married: true };
    Person p3 = { id: 3, age: 32, salary: 100.50, name: "john", married: false };

    Company c1 = { id: 100, name: "ABC" };

    table<Person> dt3 = table{};
    table<Person> dt4 = table{};
    table<Company> ct1 = table{};

    _ = dt3.add(p1);
    _ = dt3.add(p2);

    _ = dt4.add(p3);

    _ = ct1.add(c1);

    int count1 = dt3.count();
    int[] dt1data = [];
    int i = 0;
    while (dt3.hasNext()) {
        var p = <Person>dt3.getNext();
        if (p is Person) {
            dt1data[i] = p.id;
        }
        i = i + 1;
    }

    int count2 = dt4.count();
    int[] dt2data = [];
    i = 0;
    while (dt4.hasNext()) {
        var p = <Person>dt4.getNext();
        if (p is Person) {
            dt2data[i] = p.id;
        }
        i = i + 1;
    }

    int count3 = ct1.count();
    int[] ct1data = [];
    i = 0;
    while (ct1.hasNext()) {
        var p = <Company>ct1.getNext();
        if (p is Company) {
            ct1data[i] = p.id;
        }
        i = i + 1;
    }
    return (count1, count2, count3, dt1data, dt2data, ct1data);
}

function testTableAddInvalid() returns string {
    Company c1 = { id: 100, name: "ABC" };

    table<Person> dt3 = table{};
    var ret = dt3.add(c1);
    string s = ret is error ? <string>ret.detail().message : "nil";
    return s;
}

function testMultipleAccess() returns (int, int, int[], int[]) {
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 20, salary: 200.50, name: "martin", married: true };
    Person p3 = { id: 3, age: 32, salary: 100.50, name: "john", married: false };

    table<Person> dt3 = table{};
    _ = dt3.add(p1);
    _ = dt3.add(p2);
    _ = dt3.add(p3);

    int count1 = dt3.count();
    int[] dtdata1 = [];
    int i = 0;
    while (dt3.hasNext()) {
        var p = <Person>dt3.getNext();
        if (p is Person) {
            dtdata1[i] = p.id;
        }
        i = i + 1;
    }

    int count2 = dt3.count();
    int[] dtdata2 = [];
    i = 0;
    while (dt3.hasNext()) {
        var p = <Person>dt3.getNext();
        if (p is Person) {
            dtdata2[i] = p.id;
        }
        i = i + 1;
    }
    return (count1, count2, dtdata1, dtdata2);
}

function testLoopingTable() returns (string) {
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 20, salary: 200.50, name: "martin", married: true };
    Person p3 = { id: 3, age: 32, salary: 100.50, name: "john", married: false };

    table<Person> dt = table{};
    _ = dt.add(p1);
    _ = dt.add(p2);
    _ = dt.add(p3);

    string names = "";

    while (dt.hasNext()) {
        var p = <Person>dt.getNext();
        if (p is Person) {
            names = names + p.name + "_";
        }
    }
    return names;
}

function testToJson() returns (json|error) {
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 20, salary: 200.50, name: "martin", married: true };
    Person p3 = { id: 3, age: 32, salary: 100.50, name: "john", married: false };

    table<Person> dt = table{};
    _ = dt.add(p1);
    _ = dt.add(p2);
    _ = dt.add(p3);

    json j = check <json>dt;
    return j;
}

function testToXML() returns (xml|error) {
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 20, salary: 200.50, name: "martin", married: true };
    Person p3 = { id: 3, age: 32, salary: 100.50, name: "john", married: false };

    table<Person> dt = table{};
    _ = dt.add(p1);
    _ = dt.add(p2);
    _ = dt.add(p3);

    xml x = check <xml>dt;
    return x;
}

function testPrintData() {
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 20, salary: 200.50, name: "martin", married: true };
    Person p3 = { id: 3, age: 32, salary: 100.50, name: "john", married: false };

    table<Person> dt = table{
        { key id, key age,  salary, name, married }
    };
    _ = dt.add(p1);
    _ = dt.add(p2);
    _ = dt.add(p3);

    io:println(dt);
}

function testPrintDataEmptyTable() {
    table<Person> dt = table{};
    io:println(dt);
}

function testTableDrop() {
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };

    table<Person> dt = table{};
    _ = dt.add(p1);
}

function testTableWithAllDataToJson() returns (json|error) {
    json j1 = { name: "apple", color: "red", price: 30.3 };
    xml x1 = xml `<book>The Lost World</book>`;
    TypeTest t1 = { id: 1, jsonData: j1, xmlData: x1 };
    TypeTest t2 = { id: 2, jsonData: j1, xmlData: x1 };

    table<TypeTest> dt3 = table{};
    _ = dt3.add(t1);
    _ = dt3.add(t2);

    json j = check <json>dt3;
    return j;
}

function testTableWithAllDataToXml() returns (xml|error) {
    json j1 = { name: "apple", color: "red", price: 30.3 };
    xml x1 = xml `<book>The Lost World</book>`;
    TypeTest t1 = { id: 1, jsonData: j1, xmlData: x1 };
    TypeTest t2 = { id: 2, jsonData: j1, xmlData: x1 };

    table<TypeTest> dt3 = table{};
    _ = dt3.add(t1);
    _ = dt3.add(t2);

    xml x = check <xml>dt3;
    return x;
}


function testTableWithAllDataToStruct() returns (json, xml)|error {
    json j1 = { name: "apple", color: "red", price: 30.3 };
    xml x1 = xml `<book>The Lost World</book>`;
    TypeTest t1 = { id: 1, jsonData: j1, xmlData: x1 };

    table<TypeTest> dt3 = table{};
    _ = dt3.add(t1);

    json jData;
    xml xData;
    while (dt3.hasNext()) {
        TypeTest x = check <TypeTest>dt3.getNext();
        jData = x.jsonData;
        xData = x.xmlData;
    }
    return (jData, xData);
}

function testTableWithBlobDataToJson() returns (json|error) {
    string text = "Sample Text";
    byte[] content = text.toByteArray("UTF-8");
    BlobTypeTest t1 = { id: 1, blobData: content };

    table<BlobTypeTest> dt3 = table{};
    _ = dt3.add(t1);

    json j = check <json>dt3;
    return j;
}

function testTableWithBlobDataToXml() returns (xml|error) {
    string text = "Sample Text";
    byte[] content = text.toByteArray("UTF-8");
    BlobTypeTest t1 = { id: 1, blobData: content };

    table<BlobTypeTest> dt3 = table{};
    _ = dt3.add(t1);

    xml x = check <xml>dt3;
    return x;
}

function testTableWithBlobDataToStruct() returns (byte[]|error) {
    string text = "Sample Text";
    byte[] content = text.toByteArray("UTF-8");
    BlobTypeTest t1 = { id: 1, blobData: content };

    table<BlobTypeTest> dt3 = table{};
    _ = dt3.add(t1);

    byte[] bData;
    while (dt3.hasNext()) {
        BlobTypeTest x = check <BlobTypeTest>dt3.getNext();
        bData = x.blobData;
    }
    return bData;
}

function testTableWithAnyDataToJson() returns (json|error) {
    AnyTypeTest t1 = { id: 1, anyData: "Sample Text" };

    table<AnyTypeTest> dt3 = table{};
    _ = dt3.add(t1);

    json j = check <json>dt3;
    return j;
}

function testStructWithDefaultDataToJson() returns (json|error) {
    Person p1 = { id: 1 };

    table<Person> dt3 = table{};
    _ = dt3.add(p1);

    json j = check <json>dt3;
    return j;
}

function testStructWithDefaultDataToXml() returns (xml|error) {
    Person p1 = { id: 1 };

    table<Person> dt3 = table{};
    _ = dt3.add(p1);

    xml x = check <xml>dt3;
    return x;
}


function testStructWithDefaultDataToStruct() returns (int, float, string, boolean)|error {
    Person p1 = { id: 1 };

    table<Person> dt3 = table{};
    _ = dt3.add(p1);

    int iData;
    float fData;
    string sData;
    boolean bData;

    while (dt3.hasNext()) {
        Person x = check <Person>dt3.getNext();
        iData = x.age;
        fData = x.salary;
        sData = x.name;
        bData = x.married;
    }
    return (iData, fData, sData, bData);
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
    _ = dt3.add(t1);
    _ = dt3.add(t2);

    json j = check <json>dt3;
    return j;
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
    _ = dt3.add(t1);
    _ = dt3.add(t2);

    xml x = check <xml>dt3;
    return x;
}

function testTableWithArrayDataToStruct() returns (int[], float[], string[], boolean[])|error {
    int[] intArray = [1, 2, 3];
    float[] floatArray = [11.1, 22.2, 33.3];
    string[] stringArray = ["Hello", "World"];
    boolean[] boolArray = [true, false, true];
    ArraTypeTest t1 = { id: 1, intArrData: intArray, floatArrData: floatArray, stringArrData: stringArray,
        booleanArrData: boolArray };

    table<ArraTypeTest> dt3 = table{};
    _ = dt3.add(t1);

    int[] intArr;
    float[] floatArr;
    string[] stringArr;
    boolean[] boolArr;

    while (dt3.hasNext()) {
        ArraTypeTest x = check <ArraTypeTest>dt3.getNext();
        intArr = x.intArrData;
        floatArr = x.floatArrData;
        stringArr = x.stringArrData;
        boolArr = x.booleanArrData;
    }
    return (intArr, floatArr, stringArr, boolArr);
}

function testTableRemoveSuccess() returns (int, json)|error {
    Person p1 = { id: 1, age: 35, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 20, salary: 200.50, name: "martin", married: true };
    Person p3 = { id: 3, age: 32, salary: 100.50, name: "john", married: false };

    table<Person> dt = table{};
    _ = dt.add(p1);
    _ = dt.add(p2);
    _ = dt.add(p3);

    int count = check dt.remove(isBelow35);
    json j = check <json>dt;

    return (count, j);
}

function testTableRemoveSuccessMultipleMatch() returns (int, json)|error {
    Person p1 = { id: 1, age: 35, salary: 300.50, name: "john", married: true };
    Person p2 = { id: 2, age: 20, salary: 200.50, name: "martin", married: true };
    Person p3 = { id: 3, age: 32, salary: 100.50, name: "john", married: false };

    table<Person> dt = table{};
    _ = dt.add(p1);
    _ = dt.add(p2);
    _ = dt.add(p3);

    int count = check dt.remove(isJohn);
    json j = check <json>dt;

    return (count, j);
}

function testTableRemoveFailed() returns (int, json)|error {
    Person p1 = { id: 1, age: 35, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 40, salary: 200.50, name: "martin", married: true };
    Person p3 = { id: 3, age: 42, salary: 100.50, name: "john", married: false };


    table<Person> dt = table{};
    _ = dt.add(p1);
    _ = dt.add(p2);
    _ = dt.add(p3);

    int count = check dt.remove(isBelow35);
    json j = check <json>dt;

    return (count, j);
}

function testTableAddAndAccess() returns (string, string)|error {
    Person p1 = { id: 1, age: 35, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 40, salary: 200.50, name: "martin", married: true };
    Person p3 = { id: 3, age: 42, salary: 100.50, name: "john", married: false };


    table<Person> dt = table{};
    _ = dt.add(p1);
    _ = dt.add(p2);

    json j1 = check <json>dt;
    string s1 = j1.toString();

    _ = dt.add(p3);
    json j2 = check <json>dt;
    string s2 = j2.toString();

    return (s1, s2);
}

function testRemoveWithInvalidRecordType() returns string {
    Person p1 = { id: 1, age: 35, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 40, salary: 200.50, name: "martin", married: true };
    Person p3 = { id: 3, age: 42, salary: 100.50, name: "john", married: false };

    table<Person> dt = table{};
    _ = dt.add(p1);
    _ = dt.add(p2);
    _ = dt.add(p3);

    string returnStr = "";
    var ret = dt.remove(isBelow35Invalid);
    if (ret is int) {
        returnStr = <string>ret;
    } else if (ret is error) {
        returnStr = <string>ret.detail().message;
    }
    return returnStr;
}

function testRemoveWithInvalidParamType() returns string {
    Person p1 = { id: 1, age: 35, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 40, salary: 200.50, name: "martin", married: true };
    Person p3 = { id: 3, age: 42, salary: 100.50, name: "john", married: false };


    table<Person> dt = table{};
    _ = dt.add(p1);
    _ = dt.add(p2);
    _ = dt.add(p3);

    string returnStr = "";
    var ret = dt.remove(isBelow35InvalidParam);
    if (ret is int) {
        returnStr = <string>ret;
    } else if (ret is error) {
        returnStr = <string>ret.detail().message;
    }
    return returnStr;
}


function getPersonId(Person p) returns (int) {
    return p.id;
}

function getCompanyId(Company p) returns (int) {
    return p.id;
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

function isBelow35InvalidParam(int p) returns (boolean) {
    return true;
}

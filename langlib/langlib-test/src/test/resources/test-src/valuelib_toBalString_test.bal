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

import ballerina/lang.'string as strings;
import ballerina/lang.'int as ints;

type Address record {
    string country;
    string city;
    string street;
};

type Person record {
    string name;
    Address address;
    int age;

};

type Employee record {
    readonly int id;
    int age;
    decimal salary;
    string name;
    boolean married;
};

type UndergradStudent record {|
    readonly int id;
    readonly string name;
    int grade;
|};

public type AnotherDetail record {
    string message;
    error cause?;
};

public const REASON_1 = "Reason1";

public type FirstError distinct error<AnotherDetail>;

public class Student {
    string name;
    string school;

    public function init(string name, string school) {
        self.name = name;
        self.school = school;
    }

    public function getDetails() returns string {
        return self.name + " from " + self.school;
    }
}

public class Teacher {

    string name;
    string school;

    public function init(string name, string school) {
        self.name = name;
        self.school = school;
    }

    public function getDetails() returns string {
        return self.name + " from " + self.school;
    }

    public function toString() returns string {
        return self.getDetails();
    }
}

function testIntValueToBalString() {
    int x1 = 12;
    ints:Signed32 x2 = 2147483647;
    ints:Signed16 x3 = -32768;
    ints:Signed8 x4 = 100;
    ints:Unsigned32 x5 = 4294967295;
    ints:Unsigned16 x6 = 450;
    ints:Unsigned8 x7 = 221;
    byte x8 = 10;

    assert(x1.toBalString(), "12");
    assert(x2.toBalString(), "2147483647");
    assert(x3.toBalString(), "-32768");
    assert(x4.toBalString(), "100");
    assert(x5.toBalString(), "4294967295");
    assert(x6.toBalString(), "450");
    assert(x7.toBalString(), "221");
    assert(x8.toBalString(), "10");
}

function testStringValueToBalString() {
    string a = "Anne";
    strings:Char b = "m";

    assert(a.toBalString(), "\"Anne\"");
    assert(b.toBalString(), "\"m\"");
}

function testFloatingPointNumbersToBalString() {
    float x1 = 12.342;
    float x2 = 0.0/0.0;
    float x3 = 4.0/0.0;
    decimal x4 = 345.2425341;
    decimal x5 = 1;

    assert(x1.toBalString(), "12.342");
    assert(x2.toBalString(), "float:NaN");
    assert(x3.toBalString(), "float:Infinity");
    assert(x4.toBalString(), "345.2425341d");
    assert(x5.toBalString(), "1d");
}

function testAnyAnydataNilToBalString() {
    anydata a = 99;
    any b = a;
    () c = ();

    assert(a.toBalString(), "99");
    assert(b.toBalString(), "99");
    assert(c.toBalString(), "()");
}

function testTableToBalString() {
    table<UndergradStudent> underGradTable1 = table key(id,name) [
            { id: 1, name: "Mary", grade: 12 },
            { id: 2, name: "John", grade: 13 }
    ];
    table<UndergradStudent> underGradTable2 = table [
                { id: 1, name: "Mary", grade: 12 },
                { id: 2, name: "John", grade: 13 }
    ];

    assert(underGradTable1.toBalString(), "table key(id,name) [{\"id\":1,\"name\":\"Mary\",\"grade\":12}," +
                                          "{\"id\":2,\"name\":\"John\",\"grade\":13}]");
    assert(underGradTable2.toBalString(), "table key() [{\"id\":1,\"name\":\"Mary\",\"grade\":12}," +
                                              "{\"id\":2,\"name\":\"John\",\"grade\":13}]");
}

function testErrorToBalString() {
    error err1 = error("Failed to get account balance", details = true, val1 = (0.0/0.0), val2 = "This Error",
           val3 = {"x":"AA","y":(1.0/0.0)});
    FirstError err2 = FirstError(REASON_1, message = "Test passing error union to a function");

    assert(err1.toBalString(), "error error (\"Failed to get account balance\",details=true,val1=float:NaN," +
          "val2=\"This Error\",val3={\"x\":\"AA\",\"y\":float:Infinity})");
    assert(err2.toBalString(), "error FirstError (\"Reason1\",message=\"Test passing error union to a function\")");
}

function testMapToBalString() {
    map<string> mapVal1 = {"name":"ABC", "school":"City College"};
    map<any|error> mapVal2 = {"1":12, "2":"James", "3":{"x":"AA","y":(1.0/0.0)}, "4":(),
            "5":error("Failed to get account balance", details = true)};

    assert(mapVal1.toBalString(), "{\"name\":\"ABC\",\"school\":\"City College\"}");
    assert(mapVal2.toBalString(), "{\"1\":12,\"2\":\"James\",\"3\":{\"x\":\"AA\",\"y\":float:Infinity},\"4\":()," +
         "\"5\":error error (\"Failed to get account balance\",details=true)}");
}

function testArrayToBalString() {
    decimal x1 = 345.2425341;
    table<UndergradStudent> underGradTable = table key(id,name) [
            { id: 1, name: "Mary", grade: 12 },
            { id: 2, name: "John", grade: 13 }
    ];
    error err = error("Failed to get account balance", details = true, val1 = (0.0/0.0), val2 = "This Error",
           val3 = {"x":"AA","y":(1.0/0.0)});
    xml xmlVal = xml `<CATALOG><CD><TITLE>Empire Burlesque</TITLE><ARTIST>Bob Dylan</ARTIST></CD></CATALOG>`;

    int[] arr1 = [1, 2, 3, 4, 5];
    float[] arr2 = [12, 12.34, (0.0/0.0), (1.0/0.0)];
    boolean[] arr3 = [true, false, true, true];
    byte[] arr4 = [12, 10, 9, 8];
    string[] arr5 = ["ABC", "XYZ", "LMN"];
    decimal[] arr6 = [12.65, 1, 2, 90.0];
    (any|error)[] arr7 = ["str", 23, 23.4, true, {"x":"AA","y":(1.0/0.0),"z":1.23}, x1, ["X", (0.0/0.0),
    x1], underGradTable, err, xmlVal];

    assert(arr1.toBalString(), "[1,2,3,4,5]");
    assert(arr2.toBalString(), "[12.0,12.34,float:NaN,float:Infinity]");
    assert(arr2.toBalString(), "[12.0,12.34,float:NaN,float:Infinity]");
    assert(arr3.toBalString(), "[true,false,true,true]");
    assert(arr4.toBalString(), "[12,10,9,8]");
    assert(arr5.toBalString(), "[\"ABC\",\"XYZ\",\"LMN\"]");
    assert(arr6.toBalString(), "[12.65d,1d,2d,90.0d]");
    assert(arr7.toBalString(), "[\"str\",23,23.4,true,{\"x\":\"AA\",\"y\":float:Infinity,\"z\":1.23},345.2425341d," +
    "[\"X\",float:NaN,345.2425341d],table key(id,name) [{\"id\":1,\"name\":\"Mary\",\"grade\":12}," +
    "{\"id\":2,\"name\":\"John\",\"grade\":13}],error error (\"Failed to get account balance\",details=true," +
    "val1=float:NaN,val2=\"This Error\",val3={\"x\":\"AA\",\"y\":float:Infinity}),xml`<CATALOG><CD>" +
    "<TITLE>Empire Burlesque</TITLE><ARTIST>Bob Dylan</ARTIST></CD></CATALOG>`]");
}

function testTupleToBalString() {
    [string, int, decimal, float] tupleVal = ["TOM", 10, 90.12, (0.0/0.0)];

    assert(tupleVal.toBalString(), "\"TOM\" 10 90.12d float:NaN");
}

function testJsonToBalString() {
    json jsonVal = {a: "STRING", b: 12, c: 12.4, d: true, e: {x:"x", y: ()}};

    assert(jsonVal.toBalString(), "{\"a\":\"STRING\",\"b\":12,\"c\":12.4,\"d\":true," +
                                    "\"e\":{\"x\":\"x\",\"y\":()}}");

}

function testXmlToBalString() {
    xml xmlVal = xml `<CATALOG><CD><TITLE>Empire Burlesque</TITLE><ARTIST>Bob Dylan</ARTIST></CD></CATALOG>`;

    assert(xmlVal.toBalString(), "xml`<CATALOG><CD><TITLE>Empire Burlesque</TITLE>" +
         "<ARTIST>Bob Dylan</ARTIST></CD></CATALOG>`");
    assert((xmlVal/*).toBalString(), "xml`<CD><TITLE>Empire Burlesque</TITLE><ARTIST>Bob Dylan</ARTIST></CD>`");

}

function testObjectToString() {
    Student obj1 = new("Alaa", "MMV");
    Student obj2 = new("Alaa", "MMV");
    Student obj3 = obj1;
    Teacher obj4 = new("Rola", "MMV");

    assert(obj1.toBalString() === obj2.toBalString(), false);
    assert(obj1.toBalString() === obj3.toBalString(), true);
    assert(strings:startsWith(obj1.toBalString(), "object Student"), true);
    assert(strings:startsWith(obj2.toBalString(), "object Student"), true);
    assert(strings:startsWith(obj3.toBalString(), "object Student"), true);
    assert(obj4.toBalString(), "object Rola from MMV");
}

function testToBalStringOnCycles() {
     map<anydata> x = {"ee" : 3};
     map<anydata> y = {"qq" : 5};
     map<anydata> z = {"mm" : 5};
     anydata[] arr = [2 , 3, 5];
     x["1"] = z;
     x["2"] = y;
     x["3"] = x;
     y["1"] = arr;
     y["2"] = x;
     y["3"] = y;
     y["4"] = z;
     z["1"] = x;
     z["2"] = y;
     arr.push(x);

     assert(x.toBalString(), "{\"ee\":3,\"1\":{\"mm\":5,\"1\":...[1],\"2\":{\"qq\":5,\"1\":[2,3,5,...[1]]," +
     "\"2\":...[1],\"3\":...[1],\"4\":...[1]}},\"2\":{\"qq\":5,\"1\":[2,3,5,...[1]],\"2\":...[1],\"3\":...[1]," +
     "\"4\":{\"mm\":5,\"1\":...[1],\"2\":...[1]}},\"3\":...[1]}");
}

function assert(anydata actual, anydata expected) {
    if (expected != actual) {
        typedesc<anydata> expT = typeof expected;
        typedesc<anydata> actT = typeof actual;
        string reason = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
        error e = error(reason);
        panic e;
    }
}

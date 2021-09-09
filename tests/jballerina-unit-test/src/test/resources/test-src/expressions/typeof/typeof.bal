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
import ballerina/test;

type RecType0 record {
    string name;
};

function typeDescOfARecord() {
    RecType0 i = { name: "theName"};
    typedesc<any> td0 = typeof i;
    test:assertEquals(td0.toString(), "typedesc RecType0");
}

class Obj0 {
    string a;
    int b;

    function init(string a, int b) {
        self.a = a;
        self.b = b;
    }
}

function typeDescOrAObject() {
    Obj0 o = new("name", 42);
    test:assertEquals((typeof o).toString(), "typedesc Obj0");
}

function typeDescOfLiterals() {
    checkTypeDescLiteral(1, "1");
    checkTypeDescLiteral(2.0d, "2.0");
    checkTypeDescLiteral(2.1, "2.1");
    checkTypeDescLiteral("str-literal", "str-literal");
    checkTypeDescLiteral(true, "true");
    checkTypeDescLiteral(false, "false");
    checkTypeDescLiteral((), "()");    
}

function checkTypeDescLiteral(any value, string expected) {
    var valueType = typeof value;
    test:assertEquals(valueType.toString(), "typedesc " + expected);
}

function typeDescOfExpressionsOfLiterals() {
    int i = 0;
    int j = 4;
    int k = 4;
    float f = 0.0;
    float ff = 22.0;
    var r1 = typeof (i + j * k);
    test:assertEquals(r1.toString(), "typedesc 16");
    var r2 = typeof (f * ff);
    test:assertEquals(r2.toString(), "typedesc 0.0");
}

function takesATypedescParam(typedesc<any> param) returns typedesc<any> {
    return param;
}

function passTypeofToAFunction() {
    typedesc<any> t = typeof 22;
    var t1 = takesATypedescParam(t);
    var t2 = takesATypedescParam(typeof 33);
    test:assertEquals(t1.toString(), "typedesc 22");
    test:assertEquals(t2.toString(), "typedesc 33");
}

function takeTypeofAsRestParams(typedesc<any>... xs) returns typedesc<any>[] {
    return xs;
}

function passTypeofAsRestParams() {
    typedesc<any>[] result = takeTypeofAsRestParams(typeof 22, typeof 33, typeof 33.33);
    test:assertEquals(result[0].toString(), "typedesc 22");
    test:assertEquals(result[1].toString(), "typedesc 33");
    test:assertEquals(result[2].toString(), "typedesc 33.33");
}

function returnTypeOfInt() returns typedesc<any> {
    return typeof (5 + 1);
}

function compareTypeOfValues() {

    // Basic Simple Type 
    int i = 34;
    test:assertFalse(typeof i === typeof i);

    // xml Value
    xml xmlValue = xml`<data>test</data>`;
    test:assertTrue(typeof xmlValue === typeof xmlValue);

    // Structural types - Array, Tuple
    int[] arr = [1, 2, 3];
    test:assertTrue(typeof arr === typeof arr);

    [string, int] tuple = ["ballerina", 123];
    test:assertTrue(typeof tuple === typeof tuple);

    // Structural types - Records, Maps
    RecType0 rec = {name: "test"};
    test:assertTrue(typeof rec === typeof rec);
    test:assertTrue(typeof rec === RecType0);

    map<string> mapVal = {s: "test"};
    test:assertTrue(typeof mapVal === typeof mapVal);

    // Structural types -tables 
    table<map<string>> tableVal = table [{s: "test"}];
    test:assertTrue(typeof tableVal === typeof tableVal);

    // Behavioral types - Object
    Obj0 obj = new ("abc", 123);
    test:assertTrue(typeof obj === typeof obj);

    // Behavioral types - Error
    error err = error("This is an error!");
    test:assertTrue(typeof err === typeof err);
}

function typeOfImmutableStructuralValues() {
    // xml Value
    xml & readonly xmlValue = xml `<data>test</data>`;
    test:assertTrue(typeof xmlValue === typeof xmlValue);
    test:assertEquals((typeof xmlValue).toString(), "typedesc <data></data>");

    // Structural types - Array, Tuple
    int[] & readonly arr = [1, 2, 3];
    test:assertTrue(typeof arr === typeof arr);
    test:assertEquals((typeof arr).toString(), "typedesc [1,2,3]");

    [string, int] & readonly tuple = ["ballerina", 123];
    test:assertTrue(typeof tuple === typeof tuple);
    test:assertEquals((typeof tuple).toString(), "typedesc [\"ballerina\",123]");

    // Structural types - Records, Maps
    RecType0 & readonly rec = {name: "test"};
    test:assertTrue(typeof rec === typeof rec);
    test:assertTrue(typeof rec !== RecType0);
    test:assertEquals((typeof rec).toString(), "typedesc {\"name\":\"test\"}");

    map<string> & readonly mapVal = {s: "test"};
    test:assertTrue(typeof mapVal === typeof mapVal);
    test:assertEquals((typeof mapVal).toString(), "typedesc {\"s\":\"test\"}");

    // Structural types -tables 
    table<map<string>> & readonly tableVal = table [{s: "test"}];
    test:assertTrue(typeof tableVal === typeof tableVal);
    test:assertEquals((typeof tableVal).toString(), "typedesc [{\"s\":\"test\"}]");
}

function typeOfWithCloneReadOnly() {

    // xml Value
    xml & readonly xmlValue = xml `<?xml-stylesheet href="mystyle.css" type="text/css"?>`;
    any val = xmlValue.cloneReadOnly();
    test:assertTrue(typeof val === typeof val);
    test:assertEquals((typeof val).toString(), "typedesc <?xml-stylesheet href=\"mystyle.css\" " +
    "type=\"text/css\"?>");

    // Structural types - Array, Tuple
    int[] arr = [1, 2, 3];
    val = arr.cloneReadOnly();
    test:assertTrue(typeof val === typeof val);
    test:assertEquals((typeof val).toString(), "typedesc [1,2,3]");

    [string, int] tuple = ["ballerina", 123];
    val = tuple.cloneReadOnly();
    test:assertTrue(typeof val === typeof val);
    test:assertEquals((typeof val).toString(), "typedesc [\"ballerina\",123]");

    // Structural types - Records, Maps
    RecType0 rec = {name: "test"};
    val = rec.cloneReadOnly();
    test:assertTrue(typeof val === typeof val);
    test:assertTrue(typeof val !== RecType0);
    test:assertEquals((typeof val).toString(), "typedesc {\"name\":\"test\"}");

    map<string> mapVal = {s: "test"};
    val = mapVal.cloneReadOnly();
    test:assertTrue(typeof val === typeof val);
    test:assertEquals((typeof val).toString(), "typedesc {\"s\":\"test\"}");

    // Structural types - Tables
    table<map<string>> tableVal = table [{s: "test"}];
    val = tableVal.cloneReadOnly();
    test:assertTrue(typeof val === typeof val);
    test:assertEquals((typeof val).toString(), "typedesc [{\"s\":\"test\"}]");
}

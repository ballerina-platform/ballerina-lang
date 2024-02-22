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

import ballerina/lang.test;
import ballerina/lang.'value as value;

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

type Address1 record {|
    string country;
    string city;
    json...;
|};

function testToJsonString() returns map<string> {
    json aNil = ();
    json aString = "aString";
    json aNumber = 10;
    json aFloatNumber = 10.5;
    json anArray = ["hello", "world"];
    map<string> aStringMap = {name: "anObject", value: "10", sub: "Science"};
    map<()|int|float|boolean|string|map<json>> anotherMap = {
        name: "anObject",
        value: "10",
        sub: "Science",
        intVal: 2324,
        boolVal: true,
        floatVal: 45.4,
        nestedMap: {xx: "XXStr", n: 343, nilVal: ()}
    };
    json anObject = {name: "anObject", value: 10, sub: {subName: "subObject", subValue: 10}};
    map<()|int|float|boolean|string|map<json>>[] aArr =
        [
        {
            name: "anObject",
            value: "10",
            sub: "Science",
            intVal: 2324,
            boolVal: true,
            floatVal: 45.4,
            nestedMap:
        {xx: "XXStr", n: 343, nilVal: ()}
        },
        {name: "anObject", value: "10", sub: "Science"}
    ];
    Address1 addr1 = {country: "x", city: "y", "street": "z", "no": 3};
    map<string> result = {};
    byte[] bArr = [0, 1, 255];
    int[] iArr = bArr;

    result["aNil"] = aNil.toJsonString();
    result["aString"] = aString.toJsonString();
    result["aNumber"] = aNumber.toJsonString();
    result["aFloatNumber"] = aFloatNumber.toJsonString();
    result["anArray"] = anArray.toJsonString();
    result["anObject"] = anObject.toJsonString();
    result["aStringMap"] = aStringMap.toJsonString();
    result["anotherMap"] = anotherMap.toJsonString();
    result["aArr"] = aArr.toJsonString();
    result["iArr"] = iArr.toJsonString();
    result["arr1"] = addr1.toJsonString();
    return result;
}

type Pooh record {
    int id;
    xml x;
};

function testToJsonStringForNonJsonTypes() {
    () aNil = ();
    string aString = "aString";
    int aNumber = 10;
    float aFloatNumber = 10.5;
    string[] anStringArray = ["hello", "world"];
    int[] anIntArray = [1, 2];
    Address aRecord = {country: "A", city: "Aa", street: "Aaa"};
    Pooh aRecordWithXML = {id: 1, x: xml `<book>DJ</book>`};
    anydata anAnyData = 10.23;
    map<string> result = {};

    result["aNil"] = aNil.toJsonString();
    result["aString"] = aString.toJsonString();
    result["aNumber"] = aNumber.toJsonString();
    result["aFloatNumber"] = aFloatNumber.toJsonString();
    result["anStringArray"] = anStringArray.toJsonString();
    result["anIntArray"] = anIntArray.toJsonString();
    result["aRecord"] = aRecord.toJsonString();
    result["aRecordWithXML"] = aRecordWithXML.toJsonString();
    result["anAnyData"] = anAnyData.toJsonString();

    assert(result["aNil"], "null");
    assert(result["aString"], "\"aString\"");
    assert(result["aNumber"], "10");
    assert(result["aFloatNumber"], "10.5");
    assert(result["anStringArray"], "[\"hello\", \"world\"]");
    assert(result["anIntArray"], "[1, 2]");
    assert(result["aRecord"], "{\"country\":\"A\", \"city\":\"Aa\", \"street\":\"Aaa\"}");
    assert(result["aRecordWithXML"], "{\"id\":1, \"x\":\"<book>DJ</book>\"}");
    assert(result["anAnyData"], "10.23");
}

function testFromJsonString() returns map<json|error> {
    string aNil = "()";
    string aNull = "null";
    string aString = "\"aString\"";
    string aNumber = "10";
    string aFloatNumber = "10.5";
    string positiveZero = "0";
    string negativeZero = "-0";
    string negativeNumber = "-25";
    string negativeFloatNumber = "-10.5";
    string anArray = "[\"hello\", \"world\"]";
    string anObject = "{\"name\":\"anObject\", \"value\":10, \"sub\":{\"subName\":\"subObject\", \"subValue\":10}}";
    string anInvalid = "{\"name\":\"anObject\",";
    map<json|error> result = {};

    result["aNil"] = aNil.fromJsonString();
    result["aNull"] = aNull.fromJsonString();
    result["aString"] = aString.fromJsonString();
    result["aNumber"] = aNumber.fromJsonString();
    result["aFloatNumber"] = aFloatNumber.fromJsonString();
    result["positiveZero"] = positiveZero.fromJsonString();
    result["negativeZero"] = negativeZero.fromJsonString();
    result["negativeNumber"] = negativeNumber.fromJsonString();
    result["negativeFloatNumber"] = negativeFloatNumber.fromJsonString();
    result["anArray"] = anArray.fromJsonString();
    result["anObject"] = anObject.fromJsonString();
    result["anInvalid"] = anInvalid.fromJsonString();

    assert(result["aNumber"] is int, true);
    assert(result["aFloatNumber"] is decimal, true);
    assert(result["positiveZero"] is int, true);
    assert(result["negativeZero"] is float, true);
    assert(result["negativeNumber"] is int, true);
    assert(result["negativeFloatNumber"] is decimal, true);

    return result;
}

function testFromJsonStringNegative() {
    string s1 = "{\"fruits\":[\"apple', 'orange\", 'grapes']}";
    json|error j1 = s1.fromJsonString();
    error err = <error>j1;
    assert(<string>checkpanic err.detail()["message"], "unrecognized token ''grapes'' at line: 1 column: 40");
    assert(err.message(), "{ballerina/lang.value}FromJsonStringError");

    string s2 = "{'fruits':['apple', 'orange', \"grapes\"]}";
    json|error j2 = s2.fromJsonString();
    err = <error>j2;
    assert(<string>checkpanic err.detail()["message"], "expected '\"' or '}' at line: 1 column: 2");
    assert(err.message(), "{ballerina/lang.value}FromJsonStringError");
}

function testFromJsonFloatString() returns map<json|error> {
    string aNil = "()";
    string aNull = "null";
    string aString = "\"aString\"";
    string aNumber = "10";
    string aFloatNumber = "10.5";
    string positiveZero = "0";
    string negativeZero = "-0";
    string negativeNumber = "-25";
    string negativeFloatNumber = "-10.5";
    string anArray = "[\"hello\", \"world\"]";
    string anObject = "{\"name\":\"anObject\", \"value\":10, \"sub\":{\"subName\":\"subObject\", \"subValue\":10}}";
    string anInvalid = "{\"name\":\"anObject\",";
    map<json|error> result = {};

    result["aNil"] = aNil.fromJsonFloatString();
    result["aNull"] = aNull.fromJsonFloatString();
    result["aString"] = aString.fromJsonFloatString();
    result["aNumber"] = aNumber.fromJsonFloatString();
    result["aFloatNumber"] = aFloatNumber.fromJsonFloatString();
    result["positiveZero"] = positiveZero.fromJsonFloatString();
    result["negativeZero"] = negativeZero.fromJsonFloatString();
    result["negativeNumber"] = negativeNumber.fromJsonFloatString();
    result["negativeFloatNumber"] = negativeFloatNumber.fromJsonFloatString();
    result["anArray"] = anArray.fromJsonFloatString();
    result["anObject"] = anObject.fromJsonFloatString();
    result["anInvalid"] = anInvalid.fromJsonFloatString();

    assert(result["aNumber"] is float, true);
    assert(result["aFloatNumber"] is float, true);
    assert(result["positiveZero"] is float, true);
    assert(result["negativeZero"] is float, true);
    assert(result["negativeNumber"] is float, true);
    assert(result["negativeFloatNumber"] is float, true);

    return result;
}

function testFromJsonDecimalString() returns map<json|error> {
    string aNil = "()";
    string aNull = "null";
    string aString = "\"aString\"";
    string aNumber = "10";
    string aFloatNumber = "10.5";
    string positiveZero = "0";
    string negativeZero = "-0";
    string negativeNumber = "-25";
    string negativeFloatNumber = "-10.5";
    string anArray = "[\"hello\", \"world\"]";
    string anObject = "{\"name\":\"anObject\", \"value\":10, \"sub\":{\"subName\":\"subObject\", \"subValue\":10}}";
    string anInvalid = "{\"name\":\"anObject\",";
    map<json|error> result = {};

    result["aNil"] = aNil.fromJsonDecimalString();
    result["aNull"] = aNull.fromJsonDecimalString();
    result["aString"] = aString.fromJsonDecimalString();
    result["aNumber"] = aNumber.fromJsonDecimalString();
    result["aFloatNumber"] = aFloatNumber.fromJsonDecimalString();
    result["positiveZero"] = positiveZero.fromJsonDecimalString();
    result["negativeZero"] = negativeZero.fromJsonDecimalString();
    result["negativeNumber"] = negativeNumber.fromJsonDecimalString();
    result["negativeFloatNumber"] = negativeFloatNumber.fromJsonDecimalString();
    result["anArray"] = anArray.fromJsonDecimalString();
    result["anObject"] = anObject.fromJsonDecimalString();
    result["anInvalid"] = anInvalid.fromJsonDecimalString();

    assert(result["aNumber"] is decimal, true);
    assert(result["aFloatNumber"] is decimal, true);
    assert(result["positiveZero"] is decimal, true);
    assert(result["negativeZero"] is decimal, true);
    assert(result["negativeNumber"] is decimal, true);
    assert(result["negativeFloatNumber"] is decimal, true);

    return result;
}

function testToStringMethod() {
    int a = 4;
    anydata b = a;
    any c = b;
    var d = c.toString();
    error err1 = error("Failed to get account balance", details = true, val1 = (0.0 / 0.0), val2 = "This Error",
                val3 = {"x": "AA", "y": (1.0 / 0.0)});
    FirstError err2 = error FirstError(REASON_1, message = "Test passing error union to a function");
    error err3 = error("first error", detail = (1.0 / 0.0));
    error err4 = error("second error", err3);

    assertEquality("4", a.toString());
    assertEquality("4", b.toString());
    assertEquality("4", c.toString());
    assertEquality("4", d);
    assertEquality("error(\"Failed to get account balance\",details=true,val1=NaN,val2=\"This Error\","
                                                            + "val3={\"x\":\"AA\",\"y\":Infinity})", err1.toString());
    assertEquality("error FirstError (\"Reason1\",message=\"Test passing error union to a function\")",
                                                                                                    err2.toString());
    assertEquality("error(\"second error\",error(\"first error\",detail=Infinity))", err4.toString());
    assertEquality("function isolated function ((boolean|float),int?,(lang.int:Signed16|lang.string:Char)...) " +
    "returns (string?)", dummyFunc.toString());
    assertEquality("function isolated function () returns (())", testFromJsonStringNegative.toString());
}

function dummyFunc(boolean|float firstarg, int? secondarg = 0, (int:Signed16|string:Char)... thirdarg) returns string? {
    return "val";
}

/////////////////////////// Tests for `mergeJson()` ///////////////////////////
const MERGE_JSON_ERROR_REASON = "{ballerina/lang.value}MergeJsonError";
const MESSAGE = "message";

function testNilAndNonNilJsonMerge() returns boolean {
    json j1 = ();
    json j2 = {one: "hello", two: 2};
    json j3 = 5;

    json|error mj1 = j1.mergeJson(j2);
    json|error mj2 = j2.mergeJson(j1);

    json|error mj3 = j1.mergeJson(j3);
    json|error mj4 = j3.mergeJson(j1);

    return mj1 is json && mj1 === j2 && mj2 is json && mj2 === j2 && mj3 is json && mj3 == j3 &&
        mj4 is json && mj4 == j3;
}

function testNonNilNonMappingJsonMerge() returns boolean {
    json j1 = 34.5;
    json j2 = ["hello", "world"];

    json|error mj = j1.mergeJson(j2);
    return mj is error && mj.message() == MERGE_JSON_ERROR_REASON &&
        mj.detail()[MESSAGE] === "cannot merge JSON values of types 'float' and 'json[]'";
}

function testMappingJsonAndNonMappingJsonMerge1() returns boolean {
    json j1 = {one: "hello", two: "world", three: 1};
    json j2 = "string value";

    json|error mj = j1.mergeJson(j2);
    return mj is error && mj.message() == MERGE_JSON_ERROR_REASON &&
        mj.detail()[MESSAGE] === "cannot merge JSON values of types 'map<json>' and 'string'";
}

function testMappingJsonAndNonMappingJsonMerge2() returns boolean {
    json j1 = {one: "hello", two: "world", three: 1};
    json[] j2 = [1, 2];

    json|error mj = j1.mergeJson(j2);
    return mj is error && mj.message() == MERGE_JSON_ERROR_REASON &&
        mj.detail()[MESSAGE] === "cannot merge JSON values of types 'map<json>' and 'json[]'";
}

function testMappingJsonNoIntersectionMergeSuccess() returns boolean {
    json j1 = {one: "hello", two: "world", three: 1};
    map<json> j2 = {x: 12.0, y: "test value"};

    json|error mje = j1.mergeJson(j2);

    if (!(mje is map<json>) || mje.length() != 5) {
        return false;
    }

    json mj = <json>checkpanic mje;
    return mj.one === "hello" && mj.two === "world" && mj.three === 1 && mj.x === 12.0 && mj.y === "test value";
}

function testMappingJsonWithIntersectionMergeFailure1() returns boolean {
    json j1 = {one: "hello", two: "world", three: 1};
    json j2 = {two: 1, y: "test value"};

    json j1Clone = j1.clone();
    json j2Clone = j2.clone();

    json|error mj = j1.mergeJson(j2);

    if (!(mj is error) || mj.message() != MERGE_JSON_ERROR_REASON ||
            mj.detail()[MESSAGE] !== "JSON Merge failed for key 'two'") {
        return false;
    }

    error err = <error>mj;
    error cause = <error>err.detail()["cause"];
    return cause.message() == MERGE_JSON_ERROR_REASON &&
            cause.detail()[MESSAGE] === "cannot merge JSON values of types 'string' and 'int'" &&
            j1 == j1Clone && j2 == j2Clone;
}

function testMappingJsonWithIntersectionMergeFailure2() returns boolean {
    json j1 = {one: {a: "one", b: 2}, three: 1};
    json j2 = {two: "test value", one: true};

    json j1Clone = j1.clone();
    json j2Clone = j2.clone();

    json|error mj = j1.mergeJson(j2);

    if (!(mj is error) || mj.message() != MERGE_JSON_ERROR_REASON ||
            mj.detail()[MESSAGE] !== "JSON Merge failed for key 'one'") {
        return false;
    }

    error err = <error>mj;
    error cause = <error>err.detail()["cause"];
    return cause.message() == MERGE_JSON_ERROR_REASON &&
            cause.detail()[MESSAGE] === "cannot merge JSON values of types 'map<json>' and 'boolean'" &&
            j1 == j1Clone && j2 == j2Clone;
}

function testMappingJsonWithIntersectionMergeSuccess() returns boolean {
    json j1 = {one: "hello", two: (), three: 1, four: {a: (), b: "test", c: "value"}};
    json j2 = {four: {a: "strings", b: ()}, one: (), two: "world", five: 5};

    json j2Clone = j2.clone();
    json|error mj = j1.mergeJson(j2);

    if (mj is error) {
        return false;
    } else {
        map<json> expMap = {a: "strings", b: "test", c: "value"};
        map<json> mj4 = <map<json>>(checkpanic mj.four);
        return mj === j1 && mj.one === "hello" && mj.two === "world" && mj.three === 1 &&
            mj4 == expMap && mj.five === 5 && j2 == j2Clone;
    }
}

function testMergeJsonSuccessForValuesWithNonIntersectingCyclicRefererences() returns boolean {
    map<json> j1 = {x: {a: 1}};
    j1["z"] = j1;
    map<json> j2 = {x: {b: 2}};
    j2["p"] = j2;
    var result = j1.mergeJson(j2);
    return result === j1 && (checkpanic j1.x) == <json>{a: 1, b: 2} && j1.z === j1 && j2.p === j2;
}

function testMergeJsonFailureForValuesWithIntersectingCyclicRefererences() returns boolean {
    map<json> j1 = {x: {a: 1}};
    j1["z"] = j1;
    map<json> j2 = {x: {b: 2}};
    j2["z"] = j2;

    var result = j1.mergeJson(j2);
    if (result is json || result.detail()["message"] !== "JSON Merge failed for key 'z'") {
        return false;
    } else {
        error? cause = <error?>result.detail()["cause"]; // incompatible types: '(anydata|readonly)' cannot be cast to 'error?'
        if (cause is () || cause.detail()["message"] !== "Cannot merge JSON values with cyclic references") {
            return false;
        }
    }
    return (checkpanic j1.x) == <json>{a: 1} && (checkpanic j2.x) == <json>{b: 2}
        && (checkpanic j1.z) == j1 && (checkpanic j2.z) == j2;
}

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

function testToString() returns string[] {
    int varInt = 6;
    float varFloat = 6.0;
    string varStr = "toString";
    () varNil = ();
    Address addr = {country: "Sri Lanka", city: "Colombo", street: "Palm Grove"};
    Person p = {name: "Gima", address: addr, age: 12};
    boolean varBool = true;
    decimal varDecimal = 345.2425341;
    map<any|error> varMap = {};
    json varJson = {a: "STRING", b: 12, c: 12.4, d: true, e: {x: "x", y: ()}};
    any[] varArr = ["str", 23, 23.4, true];
    error err = error("ExampleError");
    FirstError varErr = error FirstError(REASON_1, err, message = "Test passing error union to a function");
    Student varObj = new ("Alaa", "MMV");
    Teacher varObj2 = new ("Rola", "MMV");
    any[] varObjArr = [varObj, varObj2];
    xml varXml = xml `<CATALOG><CD><TITLE>Empire Burlesque</TITLE><ARTIST>Bob Dylan</ARTIST></CD><CD><TITLE>Hide your heart</TITLE><ARTIST>Bonnie Tyler</ARTIST></CD><CD><TITLE>Greatest Hits</TITLE><ARTIST>Dolly Parton</ARTIST></CD></CATALOG>`;

    varMap["varInt"] = varInt;
    varMap["varFloat"] = varFloat;
    varMap["varStr"] = varStr;
    varMap["varNil"] = varNil;
    varMap["varBool"] = varBool;
    varMap["varDecimal"] = varDecimal;
    varMap["varjson"] = varJson;
    varMap["varXml"] = varXml;
    varMap["varArr"] = varArr;
    varMap["varErr"] = varErr;
    varMap["varObj"] = varObj;
    varMap["varObj2"] = varObj2;
    varMap["varObjArr"] = varObjArr;
    varMap["varRecord"] = p;

    return [
        varInt.toString(),
        varFloat.toString(),
        varStr.toString(),
        varNil.toString(),
        varBool.toString(),
        varDecimal.toString(),
        varJson.toString(),
        varXml.toString(),
        varArr.toString(),
        varErr.toString(),
        value:toString(varObj),
        varObj2.toString(),
        varObjArr.toString(),
        p.toString(),
        varMap.toString()
    ];
}

function testToStringMethodForTable() {
    table<Employee> employeeTable = table key(id) [
        {id: 1, age: 30, salary: 300.5, name: "Mary", married: true},
        {id: 2, age: 20, salary: 300.5, name: "John", married: true}
    ];

    assertEquality("[{\"id\":1,\"age\":30,\"salary\":300.5,\"name\":\"Mary\",\"married\":true},"
    + "{\"id\":2,\"age\":20,\"salary\":300.5,\"name\":\"John\",\"married\":true}]", employeeTable.toString());
}

public function xmlSequenceFragmentToString() returns string {
    xml x = xml `<abc><def>DEF</def><ghi>1</ghi></abc>`;

    return (x/*).toString();
}

type Person2 record {
    string name;
    int age;
};

type A [int, string|xml, A...];

function testCloneWithTypeTupleToJSON() {
    [string, string, string] tupleValue1 = ["Mohan", "single", "LK2014"];
    json|error jsonValue = tupleValue1.cloneWithType();

    assert(jsonValue is error, false);
    assert(jsonValue is json[], true);

    [string, string, xml] tupleValue2 = ["Mohan", "single"];
    jsonValue = tupleValue2.cloneWithType();
    assert(jsonValue is error, true);
    error err = <error>jsonValue;
    assert(err.message(), "{ballerina/lang.value}ConversionError");
    assert(<string>checkpanic err.detail()["message"], "'[string,string,xml<(lang.xml:Element|lang.xml:Comment|" +
        "lang.xml:ProcessingInstruction|lang.xml:Text)>]' value cannot be converted to 'json'");

    [string, xml|int] tupleValue3 = ["text1", 1];
    jsonValue = tupleValue3.cloneWithType();
    assert(jsonValue is error, false);
    assert(jsonValue is json[], true);

    [string, anydata...] tupleValue4 = [""];
    jsonValue = tupleValue4.cloneWithType();
    assert(jsonValue is error, false);
    assert(jsonValue is json[], true);

    [string, int|xml...] tupleValue5 = ["text"];
    jsonValue = tupleValue5.cloneWithType();
    assert(jsonValue is error, false);
    assert(jsonValue is json[], true);

    [string, int|xml...] tupleValue6 = ["text", xml `text`, 1];
    jsonValue = tupleValue6.cloneWithType();
    assert(jsonValue is error, true);
    err = <error>jsonValue;
    assert(err.message(), "{ballerina/lang.value}ConversionError");
    assert(<string>checkpanic err.detail()["message"], "'[string,(int|xml<(lang.xml:Element|lang.xml:Comment|" +
        "lang.xml:ProcessingInstruction|lang.xml:Text)>)...]' value cannot be converted to 'json'");

    [string, anydata...] tupleValue7 = ["", xml `text`, true];
    jsonValue = tupleValue7.cloneWithType();
    assert(jsonValue is error, true);
    err = <error>jsonValue;
    assert(err.message(), "{ballerina/lang.value}ConversionError");
    assert(<string>checkpanic err.detail()["message"], "'[string,anydata...]' value cannot be converted to 'json'");

    [string, xml|int] tupleValue8 = ["text1", xml `<elem></elem>`];
    jsonValue = tupleValue8.cloneWithType();
    assert(jsonValue is error, true);
    err = <error>jsonValue;
    assert(err.message(), "{ballerina/lang.value}ConversionError");
    assert(<string>checkpanic err.detail()["message"], "'[string,(xml<(lang.xml:Element|lang.xml:Comment|" +
        "lang.xml:ProcessingInstruction|lang.xml:Text)>|int)]' value cannot be converted to 'json'");

    A tupleValue9 = [1, ""];
    jsonValue = tupleValue9.cloneWithType();
    assert(jsonValue is error, false);
    assert(jsonValue is json[], true);

    A tupleValue10 = [1, xml `<elem></elem>`];
    jsonValue = tupleValue10.cloneWithType();
    assert(jsonValue is error, true);
    err = <error>jsonValue;
    assert(err.message(), "{ballerina/lang.value}ConversionError");
    assert(<string>checkpanic err.detail()["message"], "'A' value cannot be converted to 'json'");
}

function testCloneWithTypeJsonRec1() {
    Person2 p = {name: "N", age: 3};
    json|error ss = p.cloneWithType(json);
    assert(ss is json, true);

    assert((checkpanic ss).toJsonString(), "{\"name\":\"N\", \"age\":3}");
}

function testCloneWithTypeJsonRec2() {
    json pj = {name: "tom", age: 2};
    Person2 pe = checkpanic pj.cloneWithType(Person2);
    assert(pe.name, "tom");
    assert(pe.age, 2);

    Person2 s = {name: "bob", age: 4};
    json|error ss = s.cloneWithType(json);
    assert(ss is json, true);
    assert((checkpanic ss).toJsonString(), "{\"name\":\"bob\", \"age\":4}");
}

type BRec record {
    int i;
};

type CRec record {
    int i?;
};

public function testCloneWithTypeOptionalFieldToMandotoryField() {
    CRec c = {};
    var b = c.cloneWithType(BRec);
    assert(b is error, true);

    error bbe = <error>b;
    var message = bbe.detail()["message"];
    string messageString = message is error ? message.toString() : message.toString();
    assert(bbe.message(), "{ballerina/lang.value}ConversionError");
    assert(messageString, "'CRec' value cannot be converted to 'BRec': " +
    "\n\t\tmissing required field 'i' of type 'int' in record 'BRec'");
}

type Foo record {
    string s;
};

type Bar record {|
    float f?;
    string s;
|};

type Baz record {|
    int i?;
    string s;
|};

type BarOrBaz typedesc<Bar|Baz>;

function testCloneWithTypeAmbiguousTargetType() {
    Foo f = {s: "test string"};
    Bar|Baz|error bb = f.cloneWithType(BarOrBaz);
    assert(bb is Bar, true);
    assert(bb is Baz, false);
    assert(bb is error, false);
    assert(bb is Bar|Baz, true);
    if (bb is Bar|Baz) {
        assert(<Bar|Baz>bb, {s: "test string"});
    }
}

type StringOrNull string?;

function testCloneWithTypeForNilPositive() {
    anydata a = ();
    string|error? c1 = a.cloneWithType(StringOrNull);
    json|error c2 = a.cloneWithType(json);
    assert(c1 is (), true);
    assert(c2 is (), true);
}

type FooOrBar typedesc<Foo|Bar>;

type StringOrInt string|int;

function testCloneWithTypeForNilNegative() {
    anydata a = ();
    string|int|error c1 = a.cloneWithType(StringOrInt);
    Foo|Bar|error c2 = a.cloneWithType(FooOrBar);
    assert(c1 is error, true);
    assert(c2 is error, true);

    error c1e = <error>c1;
    var message = c1e.detail()["message"];
    string messageString = message is error ? message.toString() : message.toString();
    assert(messageString, "cannot convert '()' to type 'StringOrInt'");
}

function testCloneWithTypeNumeric1() {
    int a = 1234;
    float|error b = a.cloneWithType(float);
    assert(b is float, true);
    assert(checkpanic b, 1234.0);
}

function testCloneWithTypeNumeric2() {
    anydata a = 1234.6;
    int|error b = a.cloneWithType(int);
    assert(b is int, true);
    assert(checkpanic b, 1235);
}

type X record {
    int a;
    string b;
    float c;
};

type Y record {|
    float a;
    string b;
    decimal...;
|};

function testCloneWithTypeNumeric3() {
    X x = {a: 21, b: "Alice", c: 1000.5};
    Y|error ye = x.cloneWithType(Y);
    assert(ye is Y, true);

    Y y = checkpanic ye;
    assert(y.a, 21.0);
    assert(y.b, "Alice");
    assert(y["c"], <decimal>1000.5);
}

function testCloneWithTypeNumeric4() {
    json j = {a: 21.3, b: "Alice", c: 1000};
    X|error xe = j.cloneWithType(X);
    assert(xe is X, true);

    X x = checkpanic xe;
    assert(x.a, 21);
    assert(x.b, "Alice");
    assert(x.c, 1000.0);
}

type FloatArray float[];

type FloatOrBooleanArray (float|boolean)[];

function testCloneWithTypeNumeric5() {
    int[] i = [1, 2];
    float[]|error je = i.cloneWithType(FloatArray);
    (float|boolean)[] j2 = checkpanic i.cloneWithType(FloatOrBooleanArray);
    assert(je is float[], true);

    float[] j = checkpanic je;
    assert(j.length(), i.length());
    assert(j[0], 1.0);
    assert(j[1], 2.0);
    assert(j, <(float|boolean)[]>j2);
}

function testCloneWithTypeToArrayOfRecord() {
    X x1 = {a: 21, b: "Alice", c: 1000.5};
    X x2 = {a: 25, b: "Michel", c: 1020.5};
    X[] x = [x1, x2];
    Y[] y = checkpanic x.cloneWithType();
    assert(y, <Y[]>[{"a": 21.0, "b": "Alice", "c": 1000.5}, {"a": 25.0, "b": "Michel", "c": 1020.5}]);
}

function testCloneWithTypeToArrayOfMap() {
    map<float> m1 = {a: 1.2, b: 2.7};
    map<float> m2 = {a: 1.8, b: 2.3};
    map<float>[] m = [m1, m2];
    map<string|int>[]|error res = m.cloneWithType();
    assert(res is error, false);
    assert(checkpanic res, [{"a": 1, "b": 3}, {"a": 2, "b": 2}]);
}

type IntMap map<int>;

type IntOrStringMap map<string|int>;

function testCloneWithTypeNumeric6() {
    map<float> m = {a: 1.2, b: 2.7};
    map<int>|error m1e = m.cloneWithType(IntMap);
    map<string|int> m3 = checkpanic m.cloneWithType(IntOrStringMap);
    assert(m1e is map<int>, true);

    map<int> m1 = checkpanic m1e;
    assert(m1.length(), m.length());
    assert(m1["a"], 1);
    assert(m1["b"], 3);
    assert(m1, <map<string|int>>m3);
}

type DecimalArray decimal[];

function testCloneWithTypeNumeric7() {
    int[] a1 = [1, 2, 3];
    decimal[]|error a2e = a1.cloneWithType(DecimalArray);
    assert(a2e is decimal[], true);

    decimal[] a2 = checkpanic a2e;
    assert(a2.length(), a1.length());
    assert(a2[0], <decimal>1);
    assert(a2[1], <decimal>2);
    assert(a2[2], <decimal>3);
}

type ByteArray byte[];

type IntArray int[];

function testCloneWithTypeDecimalToInt() {
    decimal a = 12.3456;
    int|error result = a.cloneWithType(int);
    assert(result is int, true);
    assert(checkpanic result, 12);

    decimal[] a1 = [1.23, 2.34, 3.45];
    int[]|error a2e = a1.cloneWithType(IntArray);
    assert(a2e is int[], true);

    int[] a2 = checkpanic a2e;
    assert(a2.length(), a1.length());
    assert(a2[0], 1);
    assert(a2[1], 2);
    assert(a2[2], 3);
}

function testCloneWithTypeDecimalToByte() {
    decimal a = 12.3456;
    byte|error result = a.cloneWithType(byte);
    assert(result is byte, true);
    assert(checkpanic result, 12);

    decimal[] a1 = [1.23, 2.34, 3.45];
    byte[]|error a2e = a1.cloneWithType(ByteArray);
    assert(a2e is byte[], true);

    byte[] a2 = checkpanic a2e;
    assert(a2.length(), a1.length());
    assert(a2[0], 1);
    assert(a2[1], 2);
    assert(a2[2], 3);
}

function testCloneWithTypeDecimalToIntSubType() {
    decimal a = 12.3456;
    int:Signed32|error result = a.cloneWithType(int:Signed32);
    assert(result is int:Signed32, true);
    assert(checkpanic result, 12);
}

function testCloneWithTypeDecimalToIntNegative() {
    decimal a = 9223372036854775807.5;
    int|error result = a.cloneWithType(int);
    assert(result is error, true);
    error err = <error>result;
    var message = err.detail()["message"];
    string messageString = message is error ? message.toString() : message.toString();
    assert(err.message(), "{ballerina/lang.value}ConversionError");
    assert(messageString, "'decimal' value cannot be converted to 'int'");

    decimal[] a1 = [9223372036854775807.5, -9223372036854775807.6];
    int[]|error a2e = a1.cloneWithType(IntArray);
    assert(a2e is error, true);
    err = <error>a2e;
    message = err.detail()["message"];
    messageString = message is error ? message.toString() : message.toString();
    assert(err.message(), "{ballerina/lang.value}ConversionError");
    assert(messageString, "'decimal' value cannot be converted to 'int'");
}

type IntSubtypeArray1 int:Signed32[];

type IntSubtypeArray2 int:Unsigned16[];

function testCloneWithTypeIntSubTypeArray() {
    float[] a = [1.25, 3.46, 7.52];
    int:Signed32[]|error b = a.cloneWithType(IntSubtypeArray1);
    assert(b is error, false);
    assert(checkpanic b, [1, 3, 8]);

    decimal[] d = [12.763445, 23.4578923, 31.674895374];
    int:Unsigned16[]|error e = d.cloneWithType(IntSubtypeArray2);
    assert(e is error, false);
    assert(checkpanic e, [13, 23, 32]);
}

function testCloneWithTypeIntArrayToUnionArray() {
    int[] x = [1, 2, 3];

    (int|float)[]|error a = x.cloneWithType();
    assert(a is error, false);
    assert(checkpanic a, [1, 2, 3]);

    (float|string)[]|error c = x.cloneWithType();
    assert(c is error, false);
    assert(checkpanic c, [1.0, 2.0, 3.0]);

    (float|int:Signed16)[]|error d = x.cloneWithType();
    assert(d is error, false);
    assert(checkpanic d, [1, 2, 3]);

    (byte|float)[]|error e = x.cloneWithType();
    assert(e is error, false);
    assert(checkpanic e, [1, 2, 3]);

    float[] y = [10, 20];

    (int|byte)[]|error f = y.cloneWithType();
    assert(f is error, false);
    assert(checkpanic f, [10, 20]);

    (byte|int:Signed8|int:Unsigned8)[]|error g = y.cloneWithType();
    assert(g is error, false);
    assert(checkpanic g, [10, 20]);

    (int:Signed32|int:Unsigned32|int:Unsigned16)[]|error h = y.cloneWithType();
    assert(h is error, false);
    assert(checkpanic h, [10, 20]);

    (int:Signed32|int:Unsigned16)[]|error i = y.cloneWithType();
    assert(i is error, false);
    assert(checkpanic i, [10, 20]);

    (int:Signed16|int:Unsigned8)[]|error j = y.cloneWithType();
    assert(j is error, false);
    assert(checkpanic j, [10, 20]);

    (int:Signed8|byte)[]|error k = y.cloneWithType();
    assert(k is error, false);
    assert(checkpanic k, [10, 20]);

    (int:Signed16|int:Unsigned8|decimal)[]|error m = y.cloneWithType();
    assert(m is int:Signed16[], false);
    assert(m is int:Unsigned8[], false);
    assert(m is decimal[], false);
    assert(m is error, false);
    assert(m is (int:Signed16|int:Unsigned8|decimal)[], true);
    assert(checkpanic m, [10, 20]);

    byte[] z = [1, 2, 3];

    (int|float)[]|error l = z.cloneWithType();
    assert(l is error, false);
    assert(checkpanic l, [1, 2, 3]);

    (int|decimal|int:Unsigned8|int:Signed32)[]|error n = z.cloneWithType();
    assert(n is error, false);
    assert(checkpanic n, [1, 2, 3]);

    int:Signed32[] w = [1, 2, 3];

    (int|float)[]|error p = w.cloneWithType();
    assert(p is error, false);
    assert(checkpanic p, [1, 2, 3]);

    (byte|float)[]|error p1 = w.cloneWithType();
    assert(p1 is error, false);
    assert(checkpanic p1, [1, 2, 3]);

    (byte|decimal|int:Unsigned8|int:Signed8)[]|error q = w.cloneWithType();
    assert(q is error, false);
    assert(checkpanic q, [1, 2, 3]);

    int[] x1 = [5, 500, 65000];

    (byte|int:Signed32)[]|error r = x1.cloneWithType();
    assert(r is error, false);
    assert(checkpanic r, [5, 500, 65000]);

    (byte|int:Unsigned16|float)[]|error s = x1.cloneWithType();
    assert(s is error, false);
    assert(checkpanic s, [5, 500, 65000]);

    (byte|int:Signed16|float)[]|error t = x1.cloneWithType();
    assert(t is error, false);
    assert(checkpanic t, [5, 500, 65000.0]);

    (byte|int:Signed16)[]|error u = x1.cloneWithType();
    assert(u is error, true);
    error err = <error>u;
    var message = err.detail()["message"];
    string messageString = message is error ? message.toString() : message.toString();
    string errMsg = "'int[]' value cannot be converted to '(byte|lang.int:Signed16)[]': " +
                "\n\t\tarray element '[2]' should be of type '(byte|lang.int:Signed16)', found '65000'";
    assert(err.message(), "{ballerina/lang.value}ConversionError");
    assert(messageString, errMsg);

    (byte|float|decimal)[]|error v = x1.cloneWithType();
    assert(v is byte[], false);
    assert(v is float[], false);
    assert(v is decimal[], false);
    assert(v is error, false);
    assert(v is (byte|float|decimal)[], true);
    assert(checkpanic v, [5, 500.0, 65000.0]);

    (int:Signed16|float|decimal)[]|error v1 = x1.cloneWithType();
    assert(v1 is int:Signed16[], false);
    assert(v1 is float[], false);
    assert(v1 is decimal[], false);
    assert(v1 is error, false);
    assert(v1 is (int:Signed16|float|decimal)[], true);
    assert(checkpanic v1, [5, 500, 65000.0]);
}

function testCloneWithTypeArrayToTupleWithRestType() {
    int[] arr = [1, 2, 3];
    [decimal, byte...]|error a = arr.cloneWithType();
    assert(checkpanic a, <[decimal, byte...]>[1.0, 2, 3]);
}

function testCloneWithTypeArrayToTupleWithRestTypeUnionType() {
    int[] arr = [1, 128, 255];
    [int|decimal, byte|int:Unsigned8...]|error b = arr.cloneWithType();
    assert(checkpanic b, [1, 128, 255]);
}

function testCloneWithTypeArrayToUnionTupleNegative() {
    int[] arr = [1, 2, 3];
    [int|decimal, byte|int:Unsigned8]|error c = arr.cloneWithType();
    assert(c is error, true);
    error err = <error>c;
    var message = err.detail()["message"];
    string messageString = message is error ? message.toString() : message.toString();
    assert(err.message(), "{ballerina/lang.value}ConversionError");
    assert(messageString, "'int[]' value cannot be converted to '[(int|decimal),(byte|lang.int:Unsigned8)]'");
}

function testCloneWithTypeArrayToTupleWithMoreTargetTypes() {
    int[] arr = [1, 2];
    [int, float, decimal, byte]|error d = arr.cloneWithType();
    assert(d is error, true);
    error err = <error>d;
    var message = err.detail()["message"];
    string messageString = message is error ? message.toString() : message.toString();
    assert(err.message(), "{ballerina/lang.value}ConversionError");
    assert(messageString, "'int[]' value cannot be converted to '[int,float,decimal,byte]'");
}

function testCloneWithTypeArrayToTupleWithUnionRestTypeNegative() {
    int[] arr1 = [1, 2, 3];
    [float|decimal, int|byte...]|error e = arr1.cloneWithType();
    assert(e is [float|decimal, int|byte...], true);
    assert(e is error, false);
    assert(checkpanic e, [1.0, 2, 3]);

    float[] arr2 = [1, 1.2, 1.5, 2.1, 2.2, 2.3, 2.5, 2.7, 3.4, 4.1, 5, 1.2, 1.5, 2.1, 2.2, 2.3, 2.5, 2.7, 3.4, 4.1, 5, 7, 10];
    [byte|decimal, string|int, int|decimal...]|error f = arr2.cloneWithType();
    assert(f is [byte|decimal, string|int, int|decimal...], true);
    assert(f is error, false);
    assert(checkpanic f, [1, 1, 2, 2, 2, 2, 2, 3, 3, 4, 5, 1, 2, 2, 2, 2, 2, 3, 3, 4, 5, 7, 10]);
}

function testCloneWithTypeArrayToTupleNegative() {
    float[] arr = [1, 2.3, 3.5];
    [string, string:Char, string|string:Char]|error f = arr.cloneWithType();
    assert(f is error, true);
    error err = <error>f;
    var message = err.detail()["message"];
    string messageString = message is error ? message.toString() : message.toString();
    assert(err.message(), "{ballerina/lang.value}ConversionError");
    assert(messageString, "'float[]' value cannot be converted to '[string,lang.string:Char,(string|lang.string:Char)]': "
    + "\n\t\ttuple element '[0]' should be of type 'string', found '1.0'"
    + "\n\t\ttuple element '[1]' should be of type 'lang.string:Char', found '2.3'"
    + "\n\t\ttuple element '[2]' should be of type '(string|lang.string:Char)', found '3.5'");
}

function testCloneWithTypeArrayToTupleWithStructureRestTypeNegative() {
    int[] arr = [10, 20, 30];
    [map<int>, [string, int]...]|error g = arr.cloneWithType();
    assert(g is error, true);
    error err = <error>g;
    var message = err.detail()["message"];
    string messageString = message is error ? message.toString() : message.toString();
    assert(err.message(), "{ballerina/lang.value}ConversionError");
    assert(messageString, "'int[]' value cannot be converted to '[map<int>,[string,int]...]': " +
    "\n\t\ttuple element '[0]' should be of type 'map<int>', found '10'" +
    "\n\t\ttuple element '[1]' should be of type '[string,int]', found '20'" +
    "\n\t\ttuple element '[2]' should be of type '[string,int]', found '30'");
}

function testCloneWithTypeTupleRestType() {
    [int, float, int|float...] t = [1, 2.5, 3, 5.2];

    [float...]|error a = t.cloneWithType();
    assert(checkpanic a, [1.0, 2.5, 3.0, 5.2]);
}

function testCloneWithTypeUnionTuple() {
    [int, float, int|decimal...] t = [1, 2.5, 3, 4.2];

    [byte|int:Unsigned8, int|float, int|byte, byte|int:Unsigned32]|error b = t.cloneWithType();
    assert(checkpanic b, [1, 2.5, 3, 4]);
}

function testCloneWithTypeTupleRestTypeNegative() {
    [int, float, int|float...] t = [1, 2.5, 3, 5.2];

    [string...]|error c = t.cloneWithType();
    assert(c is error, true);
    error err = <error>c;
    var message = err.detail()["message"];
    string messageString = message is error ? message.toString() : message.toString();
    assert(err.message(), "{ballerina/lang.value}ConversionError");
    assert(messageString, "'[int,float,(int|float)...]' value cannot be converted to '[string...]': " +
    "\n\t\ttuple element '[0]' should be of type 'string', found '1'" +
    "\n\t\ttuple element '[1]' should be of type 'string', found '2.5'" +
    "\n\t\ttuple element '[2]' should be of type 'string', found '3'" +
    "\n\t\ttuple element '[3]' should be of type 'string', found '5.2'");
}

function testCloneWithTypeUnionTupleRestTypeNegative() {
    [int, float, int|float...] t = [1, 2.5, 3, 5.2];
    [int|float, decimal|int...]|error d = t.cloneWithType();
    assert(d is [int|float, decimal|int...], true);
    assert(d is [int, decimal|int...], false);
    assert(d is [float, decimal|int...], false);
    assert(d is error, false);
    assert(checkpanic d, <[int|float, decimal|int...]>[1, 2.5, 3, 5.2]);
}

function testCloneWithTypeToTupleTypeWithFiniteTypesNegative() {
    [1|"1"|"2", 2|"2"|"3"] tupleVal = [1, "2"];
    ["1", 2]|error ans = trap tupleVal.cloneWithType();
    assert(ans is error, true);
    error err = <error>ans;
    string message = <string>checkpanic err.detail()["message"];
    assert(err.message(), "{ballerina/lang.value}ConversionError");
    assert(message, "'[(1|\"1\"|\"2\"),(2|\"2\"|\"3\")]' value cannot be converted to '[\"1\",2]': "
    + "\n\t\ttuple element '[0]' should be of type '\"1\"', found '1'"
    + "\n\t\ttuple element '[1]' should be of type '2', found '\"2\"'");
}

const CONST_TWO = 2;

type Foo1 1|2;

type Bar1 0|4;

type Foo2 "1"|"2";

type Bar2 "0"|"";

type nil ();

type integer int;

type sym1 112|string|int[3];

type Foo3 1|2|sym1|3|[Bar3, sym1];

type Bar3 7|int|integer;

type sym2 Foo3|Bar3|boolean[];

type sym3 7|Bar3|byte;

type sym4 11|0;

type sym5 11|sym4|12;

type WithFillerValSym Bar3|20|sym3;

type NoFillerValSym sym2|20|sym3;

type finiteUnionType false|byte|0|0.0f|0d|""|["a"]|string:Char|int:Unsigned16;

type LiteralConstAndIntType int|CONST_TWO;

type NoFillerValue 1|"foo";

type finiteA 1|2|3;

type finiteB 0|-100;

type unionA finiteA|finiteB|int;

type unionB finiteA|4|5;

type finiteDec 1.0d|0d|4d;

type finiteFloat 1.2|1.4|0.0|10.0;

type finiteBoolean true|false;

type emptyString "";

function testCloneWithTypeTupleConsideringFillerValues() {

    [NoFillerValue, NoFillerValue] tuple1 = [1, "foo"];
    NoFillerValue[5]|error result1 = tuple1.cloneWithType();

    assert(result1 is error, true);
    error err = <error>result1;
    var message = err.detail()["message"];
    string messageString = message is error ? message.toString() : message.toString();
    assert(err.message(), "{ballerina/lang.value}ConversionError");
    assert(messageString, "'[NoFillerValue,NoFillerValue]' value cannot be converted to 'NoFillerValue[5]': "
    + "\n\t\tarray cannot be expanded to size '5' because, the target type 'NoFillerValue[5]' does not " +
    "have a filler value");

    [1|"2", 2|"3"] tuple2 = [1, 2];
    (1|2|"2")[4]|error result2 = tuple2.cloneWithType();
    assert(result2 is error, true);
    err = <error>result2;
    message = err.detail()["message"];
    messageString = message is error ? message.toString() : message.toString();
    assert(err.message(), "{ballerina/lang.value}ConversionError");
    assert(messageString, "'[(1|\"2\"),(2|\"3\")]' value cannot be converted to '(1|2|\"2\")[4]': "
    + "\n\t\tarray cannot be expanded to size '4' because, the target type '(1|2|\"2\")[4]' does not have a filler value");

    [string, string] tuple3 = ["test", "string"];
    string[5]|error result3 = tuple3.cloneWithType();
    assert(result3 is string[], true);
    if (result3 is string[]) {
        assert(result3, ["test", "string", "", "", ""]);
    }

    [int, byte, 3] tuple4 = [1, 2, 3];
    unionA[4] result4 = checkpanic tuple4.cloneWithType();
    assert(result4, [1, 2, 3, 0]);

    unionB[4]|error result5 = trap tuple4.cloneWithType();
    assert(result5 is error, true);
    err = <error>result5;
    message = err.detail()["message"];
    messageString = message is error ? message.toString() : message.toString();
    assert(err.message(), "{ballerina/lang.value}ConversionError");
    assert(messageString, "'[int,byte,3]' value cannot be converted to 'unionB[4]': "
    + "\n\t\tarray cannot be expanded to size '4' because, the target type 'unionB[4]' does not have a filler value");

    [decimal, 1.0d, 4d] tuple5 = [1.0, 1.0d, 4d];
    finiteDec[4] resultTuple5 = checkpanic tuple5.cloneWithType();
    assert(resultTuple5, [1.0d, 1.0d, 4d, 0d]);

    [float, 1.2, 1.4] tuple6 = [10, 1.2, 1.4];
    finiteFloat[4] result6 = checkpanic tuple6.cloneWithType();
    assert(result6, [10.0, 1.2, 1.4, 0.0]);

    [true, boolean, false] tuple7 = [true, false, false];
    finiteBoolean[4] result7 = checkpanic tuple7.cloneWithType();
    assert(result7, [true, false, false, false]);

    [2] tuple8 = [2];
    LiteralConstAndIntType[2] result8 = checkpanic tuple8.cloneWithType();
    assert(result8, [2, 0]);

    (Foo1|Bar1)[2] result9 = checkpanic tuple8.cloneWithType();
    assert(result9, [2, 0]);

    ["1"] tuple10 = ["1"];
    (Foo2|Bar2)[2] result10 = checkpanic tuple10.cloneWithType();
    assert(result10, ["1", ""]);

    (nil|int)[2] result11 = checkpanic tuple8.cloneWithType();
    assert(result11, [2, ()]);

    (Foo1|integer)[2] result12 = checkpanic tuple8.cloneWithType();
    assert(result12, [2, 0]);

    [100] tuple12 = [100];
    WithFillerValSym[2] result13 = checkpanic tuple12.cloneWithType();
    assert(result13, [100, 0]);

    (Bar3|20|sym5)[2] result14 = checkpanic tuple12.cloneWithType();
    assert(result14, [100, 0]);

    NoFillerValSym[2]|error result15 = tuple12.cloneWithType();
    assert(result15 is error, true);
    err = <error>result15;
    message = err.detail()["message"];
    messageString = message is error ? message.toString() : message.toString();
    assert(err.message(), "{ballerina/lang.value}ConversionError");
    assert(messageString, "'[100]' value cannot be converted to 'NoFillerValSym[2]': "
    + "\n\t\tarray cannot be expanded to size '2' because, the target type 'NoFillerValSym[2]' does not have a filler value");

    finiteUnionType[2]|error result16 = tuple12.cloneWithType();
    assert(result16 is error, true);
    err = <error>result16;
    message = err.detail()["message"];
    messageString = message is error ? message.toString() : message.toString();
    assert(err.message(), "{ballerina/lang.value}ConversionError");
    assert(messageString, "'[100]' value cannot be converted to 'finiteUnionType[2]': "
    + "\n\t\tarray cannot be expanded to size '2' because, the target type 'finiteUnionType[2]' does not have a filler value");

    [0] tuple17 = [0];
    (false|0|0.0f|0d|"")[2]|error result17 = tuple17.cloneWithType();
    assert(result17 is error, true);
    err = <error>result17;
    message = err.detail()["message"];
    messageString = message is error ? message.toString() : message.toString();
    assert(err.message(), "{ballerina/lang.value}ConversionError");
    assert(messageString, "'[0]' value cannot be converted to '(false|0|0.0f|0d|\"\")[2]': "
    + "\n\t\tarray cannot be expanded to size '2' because, the target type '(false|0|0.0f|0d|\"\")[2]' does not have a filler value");

    ["0"] tuple18 = ["0"];
    (string|string:Char|finiteA)[2]|error result18 = tuple18.cloneWithType();
    assert(result18 is error, true);
    err = <error>result18;
    message = err.detail()["message"];
    messageString = message is error ? message.toString() : message.toString();
    assert(err.message(), "{ballerina/lang.value}ConversionError");
    assert(messageString, "'[\"0\"]' value cannot be converted to '(string|lang.string:Char|finiteA)[2]': "
    + "\n\t\tarray cannot be expanded to size '2' because, the target type '(string|lang.string:Char|finiteA)[2]' does not have a filler value");

    [1] tuple19 = [1];
    (xml:Text|xml:Comment|finiteA)[2]|error result19 = tuple19.cloneWithType();
    assert(result19 is error, true);
    err = <error>result19;
    message = err.detail()["message"];
    messageString = message is error ? message.toString() : message.toString();
    assert(err.message(), "{ballerina/lang.value}ConversionError");
    assert(messageString, "'[1]' value cannot be converted to '(lang.xml:Text|lang.xml:Comment|finiteA)[2]': "
    + "\n\t\tarray cannot be expanded to size '2' because, the target type '(lang.xml:Text|lang.xml:Comment|finiteA)[2]' does not have a filler value");

    (string:Char|emptyString)[2] result20 = checkpanic tuple18.cloneWithType();
    assert(result20, ["0", ""]);

    (string:Char|"k")[2]|error result21 = tuple18.cloneWithType();
    assert(result21 is error, true);
    err = <error>result21;
    message = err.detail()["message"];
    assert(err.message(), "{ballerina/lang.value}ConversionError");
    messageString = message is error ? message.toString() : message.toString();
    assert(messageString, "'[\"0\"]' value cannot be converted to '(lang.string:Char|\"k\")[2]': "
    + "\n\t\tarray cannot be expanded to size '2' because, the target type '(lang.string:Char|\"k\")[2]' does not have a filler value");

    (string:Char|Bar2)[2] result22 = checkpanic tuple18.cloneWithType();
    assert(result22, ["0", ""]);
}

type Default record {|
    int id = 0;
    string name = "Tom";
|};

function testCloneWithTypeConsideringReadOnlyFillerValues() {
    anydata arr = [];

    int[2] & readonly arr1 = checkpanic arr.cloneWithType();
    int[4][2] & readonly arr2 = checkpanic arr.cloneWithType();
    [int, string][4] & readonly arr3 = checkpanic arr.cloneWithType();
    map<int>[2] & readonly arr4 = checkpanic arr.cloneWithType();
    Default[4] & readonly arr5 = checkpanic arr.cloneWithType();
    table<Default>[2] & readonly arr6 = checkpanic arr.cloneWithType();

    assertEquality("[0,0]", arr1.toString());
    assertEquality("[[0,0],[0,0],[0,0],[0,0]]", arr2.toString());
    assertEquality("[[0,\"\"],[0,\"\"],[0,\"\"],[0,\"\"]]", arr3.toString());
    assertEquality("[{},{}]", arr4.toString());
    assertEquality("[{\"id\":0,\"name\":\"Tom\"},{\"id\":0,\"name\":\"Tom\"},{\"id\":0,\"name\":\"Tom\"}," +
    "{\"id\":0,\"name\":\"Tom\"}]", arr5.toString());
    assertEquality("[[],[]]", arr6.toString());
}

type StringArray string[];

function testCloneWithTypeStringArray() {
    string anArray = "[\"hello\", \"world\"]";
    json j = <json>checkpanic anArray.fromJsonString();
    string[]|error cloned = j.cloneWithType(StringArray);
    assert(cloned is string[], true);

    string[] s = checkpanic cloned;
    assert(s[0], "hello");
    assert(s[1], "world");
}

function testCloneWithTypeWithInferredArgument() {
    Person2 a = {name: "N", age: 3};
    json|error b = a.cloneWithType();
    assert(b is json, true);
    assert((checkpanic b).toJsonString(), "{\"name\":\"N\", \"age\":3}");

    json c = {name: "tom", age: 2};
    Person2 d = checkpanic c.cloneWithType();
    assert(d.name, "tom");
    assert(d.age, 2);

    Person2 e = {name: "bob", age: 4};
    json|error f = e.cloneWithType();
    assert(f is json, true);
    assert((checkpanic f).toJsonString(), "{\"name\":\"bob\", \"age\":4}");

    CRec g = {};
    BRec|error h = g.cloneWithType();
    assert(h is error, true);
    error err = <error>h;
    var message = err.detail()["message"];
    string messageString = message is error ? message.toString() : message.toString();
    assert(err.message(), "{ballerina/lang.value}ConversionError");
    assert(messageString, "'CRec' value cannot be converted to 'BRec': " +
    "\n\t\tmissing required field 'i' of type 'int' in record 'BRec'");

    Foo i = {s: "test string"};
    Bar|Baz|error j = i.cloneWithType();
    assert(j is Bar, true);
    assert(j is Baz, false);
    assert(j is error, false);
    assert(j is Bar|Baz, true);
    if (j is Bar|Baz) {
        assert(<Bar|Baz>j, {s: "test string"});
    }

    anydata k = ();
    string|error? l = k.cloneWithType();
    json|error m = k.cloneWithType();
    assert(l is (), true);
    assert(m is (), true);

    anydata n = ();
    string|int|error o = n.cloneWithType();
    Foo|Bar|error p = n.cloneWithType();
    assert(o is error, true);
    assert(p is error, true);

    err = <error>o;
    message = err.detail()["message"];
    messageString = message is error ? message.toString() : message.toString();
    assert(messageString, "cannot convert '()' to type '(string|int)'");

    int q = 1234;
    float|error r = q.cloneWithType();
    assert(r is float, true);
    assert(checkpanic r, 1234.0);

    anydata s = 1234.6;
    int|error t = s.cloneWithType();
    assert(t is int, true);
    assert(checkpanic t, 1235);

    X u = {a: 21, b: "Alice", c: 1000.5};
    Y|error v = value:cloneWithType(u);
    assert(v is Y, true);

    Y w = checkpanic v;
    assert(w.a, 21.0);
    assert(w.b, "Alice");
    assert(w["c"], <decimal>1000.5);

    json x = {a: 21.3, b: "Alice", c: 1000};
    X|error y = x.cloneWithType();
    assert(y is X, true);

    X z = checkpanic y;
    assert(z.a, 21);
    assert(z.b, "Alice");
    assert(z.c, 1000.0);

    int[] a2 = [1, 2];
    float[]|error b2 = a2.cloneWithType();
    (float|boolean)[] o2 = checkpanic value:cloneWithType(a2);
    assert(b2 is float[], true);

    float[] c2 = checkpanic b2;
    assert(c2.length(), a2.length());
    assert(c2[0], 1.0);
    assert(c2[1], 2.0);
    assert(c2, <(float|boolean)[]>o2);

    map<float> d2 = {a: 1.2, b: 2.7};
    map<int>|error e2 = d2.cloneWithType();
    map<string|int> f2 = checkpanic d2.cloneWithType();
    assert(e2 is map<int>, true);

    map<int> g2 = checkpanic e2;
    assert(g2.length(), d2.length());
    assert(g2["a"], 1);
    assert(g2["b"], 3);
    assert(g2, <map<string|int>>f2);

    int[] h2 = [1, 2, 3];
    decimal[]|error i2 = value:cloneWithType(h2);
    assert(i2 is decimal[], true);

    decimal[] j2 = checkpanic i2;
    assert(j2.length(), h2.length());
    assert(j2[0], <decimal>1);
    assert(j2[1], <decimal>2);
    assert(j2[2], <decimal>3);

    string k2 = "[\"hello\", \"world\"]";
    json l2 = <json>checkpanic k2.fromJsonString();
    string[]|error m2 = l2.cloneWithType();
    assert(m2 is string[], true);

    string[] n2 = checkpanic m2;
    assert(n2[0], "hello");
    assert(n2[1], "world");
}

type Map map<anydata>;

function testCloneWithTypeWithImmutableTypes() {

    map<int> & readonly m = {a: 1, b: 2};
    map<map<int>> n = {m};
    var o = checkpanic n.cloneWithType(Map);
    assert(o["m"] === m, false);
    assert(o["m"].isReadOnly(), false);
    anydata p = o["m"];
    assert(p is map<anydata>, true);
    map<anydata> q = <map<anydata>>p;
    q["c"] = "foo";
    assert(q.length(), 3);
    assert(q["a"], 1);
    assert(q["b"], 2);
    assert(q["c"], "foo");

    int[] & readonly array = [3, 4];
    map<int[]> n2 = {array};
    var o2 = checkpanic n2.cloneWithType(Map);
    assert(o2["array"] === array, false);
    assert(o2["array"].isReadOnly(), false);
    anydata[] anyDataArray = <anydata[]>o2["array"];
    anyDataArray[2] = "foo";
    assert(anyDataArray.length(), 3);
    assert(anyDataArray[0], 3);
    assert(anyDataArray[1], 4);
    assert(anyDataArray[2], "foo");
}

type Person3 record {|
    string name = "abc";
    int id;
|};

type Person4 record {|
    readonly int id;
|};

function testCloneWithTypeImmutableStructuredTypes() {
    map<Person4> m = {a: {id: 11}};
    map<Person3 & readonly> & readonly personMap = checkpanic m.cloneWithType();
    Person3 person1 = <Person3>personMap["a"];
    assert(person1.id, 11);

    Person4[] m1 = [{id: 12}];
    (Person3 & readonly)[] arr = checkpanic m1.cloneWithType();
    Person3 person2 = arr[0];
    assert(person2.id, 12);

    table<Person4> m2 = table [{id: 13}];
    table<Person3 & readonly> tab = checkpanic m2.cloneWithType();
    record {|Person3 & readonly value;|} p = <record {|Person3 & readonly value;|}>tab.iterator().next();
    Person3 person3 = p["value"];
    assert(person3.id, 13);

    table<Person4> key(id) m3 = table [{id: 14}];
    table<Person3 & readonly> key(id) tab2 = checkpanic m3.cloneWithType();
    record {|Person3 & readonly value;|} p2 = <record {|Person3 & readonly value;|}>tab2.iterator().next();
    Person3 person4 = p2["value"];
    assert(person4.id, 14);
}

type IntOne 1;

type FloatOne 1.0;

type DecimalOne 1d;

type IntOneOrTwo 1|2;

type IntTwoOrThree 2|3;

type IntThreeOrFour 3|4;

type FloatOneOrTwo 1.0|2.0;

type FloatTwoOrThree 2.0|3.0;

type FloatThreeOrFour 3.0|4.0;

type IntOneOrFloatTwo 1|2.0;

type IntTwoOrFloatTwo 2|2.0;

type DecimalOneOrTwo 1d|2d;

function testCloneWithTypeWithFiniteType() {
    int x = 2;
    float y = 2.0;

    IntOneOrTwo|error a = x.cloneWithType();
    assert(a is IntOneOrTwo, true);
    IntOneOrTwo b = checkpanic a;
    assert(b, 2);

    FloatOneOrTwo|error c = x.cloneWithType();
    assert(c is error, false);
    FloatOneOrTwo d = checkpanic c;
    assert(d, 2.0);

    IntTwoOrFloatTwo|error e = x.cloneWithType();
    assert(e is error, false);
    IntTwoOrFloatTwo f = checkpanic e;
    assert(f, 2);

    IntTwoOrFloatTwo|error g = y.cloneWithType();
    assert(g is error, false);
    IntTwoOrFloatTwo h = checkpanic g;
    assert(h, 2.0);

    int z = 1;
    float w = 1.0;
    decimal v = 1d;

    DecimalOne|error i = z.cloneWithType();
    assert(checkpanic i, 1.0d);
    DecimalOneOrTwo|error j = z.cloneWithType();
    assert(checkpanic j, 1.0d);
    DecimalOne|error k = w.cloneWithType();
    assert(checkpanic k, 1.0d);
    DecimalOneOrTwo|error l = w.cloneWithType();
    assert(checkpanic l, 1.0d);

    IntOne|error m = v.cloneWithType();
    assert(checkpanic m, 1);
    FloatOne|error n = v.cloneWithType();
    assert(checkpanic n, 1.0);
    IntOneOrTwo|error p = v.cloneWithType();
    assert(checkpanic p, 1);

    DecimalOneOrTwo decimalOneOrTwo = 2d;
    IntTwoOrFloatTwo|error q = decimalOneOrTwo.cloneWithType();
    assert(q is IntTwoOrFloatTwo, true);
    assert(q is error, false);
    assert(checkpanic q, 2);
}

function testCloneWithTypeWithUnionOfFiniteType() {
    int x = 3;

    (IntTwoOrThree|IntThreeOrFour)|error a = x.cloneWithType();
    assert(a is error, false);
    IntTwoOrThree|IntThreeOrFour b = checkpanic a;
    assert(b, 3);

    (FloatTwoOrThree|FloatThreeOrFour)|error c = x.cloneWithType();
    assert(c is error, false);
    FloatTwoOrThree|FloatThreeOrFour d = checkpanic c;
    assert(d, 3.0);

    int y = 2;
    (IntOneOrFloatTwo|IntTwoOrThree)|error e = y.cloneWithType();
    assert(e is error, false);
    IntOneOrFloatTwo|IntTwoOrThree f = checkpanic e;
    assert(f, 2);

    (DecimalOneOrTwo|FloatThreeOrFour)|error g = y.cloneWithType();
    assert(checkpanic g, 2d);
}

function testCloneWithTypeWithFiniteArrayTypeFromIntArray() {
    int[] x = [1, 2];
    IntOneOrTwo[]|error a = x.cloneWithType();
    assert(a is IntOneOrTwo[], true);
    assert(a is error, false);

    IntOneOrTwo[] b = checkpanic a;
    assert(b[0], 1);
    assert(b[1], 2);

    int[] y = [3, 4];
    FloatThreeOrFour[]|error c = y.cloneWithType();
    assert(c is error, false);
    FloatThreeOrFour[] d = checkpanic c;
    assert(d, [3.0, 4.0]);
}

function testCloneWithTypeWithUnionOfFiniteTypeArraysFromIntArray() {
    int[] x = [1, 2, 3];
    (IntOneOrTwo|IntTwoOrThree)[]|error a = x.cloneWithType();
    assert(a is error, false);

    (IntOneOrTwo|IntTwoOrThree)[] b = checkpanic a;
    assert(b, [1, 2, 3]);

    float[] y = [3.0, 4.0];
    (IntTwoOrThree|FloatThreeOrFour)[]|error c = y.cloneWithType();
    assert(c is error, false);
    (IntTwoOrThree|FloatThreeOrFour)[] d = checkpanic c;
    assert(d, [3.0, 4.0]);
}

function testCloneWithTypeWithUnionTypeArrayFromIntArray() {
    int[] x = [1, 2, 3];

    (string|IntOneOrTwo|IntTwoOrThree)[]|error a = x.cloneWithType();
    assert(a is error, false);
    (string|IntOneOrTwo|IntTwoOrThree)[] b = checkpanic a;
    assert(b, [1, 2, 3]);

    int[] y = [3, 4];

    (float|FloatThreeOrFour)[]|error c = y.cloneWithType();
    assert(c is error, false);
    (float|FloatThreeOrFour)[] d = checkpanic c;
    assert(d, [3.0, 4.0]);

    (IntThreeOrFour|FloatThreeOrFour)[]|error e = y.cloneWithType();
    assert(e is error, false);
    (IntThreeOrFour|FloatThreeOrFour)[] f = checkpanic e;
    assert(f, [3, 4]);
}

function testCloneWithTypeWithFiniteTypeArrayFromIntArrayNegative() {
    int[] x = [1, 2, 3, 4];

    IntTwoOrThree[]|error a = x.cloneWithType();
    assert(a is error, true);

    error err = <error>a;
    var message = err.detail()["message"];
    string messageString = message is error ? message.toString() : message.toString();
    string errMsg = "'int[]' value cannot be converted to 'IntTwoOrThree[]': " +
    "\n\t\tarray element '[0]' should be of type 'IntTwoOrThree', found '1'" +
    "\n\t\tarray element '[3]' should be of type 'IntTwoOrThree', found '4'";
    assert(messageString, errMsg);

    (IntTwoOrThree|IntThreeOrFour)[]|error c = x.cloneWithType();
    assert(c is error, true);
    err = <error>c;
    message = err.detail()["message"];
    messageString = message is error ? message.toString() : message.toString();
    errMsg = "'int[]' value cannot be converted to '(IntTwoOrThree|IntThreeOrFour)[]': " +
    "\n\t\tarray element '[0]' should be of type '(IntTwoOrThree|IntThreeOrFour)', found '1'";
    assert(messageString, errMsg);

    int[] y = [3, 4];

    IntThreeOrFour[]|FloatThreeOrFour[]|error f = y.cloneWithType();
    assert(f is IntThreeOrFour[], true);
}

type Boss record {
    Person5 man;
    string department;
};

type Factory record {|
    Person5 man1;
    Person5 man2;
    Boss man3;
    float grade;
    boolean permanant = false;
    Student1 intern;
    boolean...;
|};

type Person5 record {|
    float value?;
    string name;
    int age;
|};

type Apple record {
    string color;
};

type Orange record {|
    string colour;
|};

type Mango record {
    string taste;
    int amount;
};

type Student1 record {|
    string name;
    Apple|Orange|Mango fruit;
|};

json jsonVal = {
    "man1": {
        "fname": "Jane",
        "age": "14"
    },
    "man2": {
        "name": 2,
        "aage": 14,
        "height": 67.5
    },
    "man3": {
        "man": {
            "namee": "Jane",
            "age": "14",
            "height": 67.5
        },
        "department": 4
    },
    "intern": {
        "name": 12,
        "fruit": {
            "color": 4,
            "amount": "five"
        }
    },
    "black": "color",
    "blue": 4,
    "white": true,
    "yellow": "color",
    "green": 4,
    "permanant": true
};

string errorMsgContent = "\n\t\tmissing required field 'grade' of type 'float' in record 'Factory'" +
        "\n\t\tmissing required field 'man1.name' of type 'string' in record 'Person5'" +
        "\n\t\tfield 'man1.fname' cannot be added to the closed record 'Person5'" +
        "\n\t\tfield 'man1.age' in record 'Person5' should be of type 'int', found '\"14\"'" +
        "\n\t\tmissing required field 'man2.age' of type 'int' in record 'Person5'" +
        "\n\t\tfield 'man2.name' in record 'Person5' should be of type 'string', found '2'" +
        "\n\t\tfield 'man2.aage' cannot be added to the closed record 'Person5'" +
        "\n\t\tfield 'man2.height' cannot be added to the closed record 'Person5'" +
        "\n\t\tmissing required field 'man3.man.name' of type 'string' in record 'Person5'" +
        "\n\t\tfield 'man3.man.namee' cannot be added to the closed record 'Person5'" +
        "\n\t\tfield 'man3.man.age' in record 'Person5' should be of type 'int', found '\"14\"'" +
        "\n\t\tfield 'man3.man.height' cannot be added to the closed record 'Person5'" +
        "\n\t\tfield 'man3.department' in record 'Boss' should be of type 'string', found '4'" +
        "\n\t\tfield 'intern.name' in record 'Student1' should be of type 'string', found '12'" +
        "\n\t\t{" +
        "\n\t\t  field 'intern.fruit.color' in record 'Apple' should be of type 'string', found '4'" +
        "\n\t\tor" +
        "\n\t\t  missing required field 'intern.fruit.colour' of type 'string' in record 'Orange'" +
        "\n\t\t  field 'intern.fruit.color' cannot be added to the closed record 'Orange'" +
        "\n\t\t  field 'intern.fruit.amount' cannot be added to the closed record 'Orange'" +
        "\n\t\t...";

type Organization record {
    string id;
    string login;
    string name;
};

type User record {
    string id;
    string login;
    string? name?;
};

type Repository record {
    string orgName;
};

type Bazz record {
    (User|Organization|Repository)[] x;
};

function testConvertJsonToNestedRecordsWithErrors() {

    Factory|error val = trap jsonVal.cloneWithType(Factory);

    error err = <error>val;
    string errorMsg = "'map<json>' value cannot be converted to 'Factory': " + errorMsgContent;
    assert(<string>checkpanic err.detail()["message"], errorMsg);
    assert(err.message(), "{ballerina/lang.value}ConversionError");

    json j = {
        x: [
            {id: "0", name: "a", login: "c"},
            {id: "1", name: "b", login: "d"},
            {height: 12.5, weight: "medium", login: 4}
        ]
    };

    Bazz|error result = trap j.cloneWithType();
    assert(result is error, true);
    err = <error>result;
    string errMsg = "'map<json>' value cannot be converted to 'Bazz': " +
    "\n\t\t{" +
    "\n\t\t  missing required field 'x[2].id' of type 'string' in record 'User'" +
    "\n\t\t  field 'x[2].login' in record 'User' should be of type 'string', found '4'" +
    "\n\t\tor" +
    "\n\t\t  missing required field 'x[2].name' of type 'string' in record 'Organization'" +
    "\n\t\t  missing required field 'x[2].id' of type 'string' in record 'Organization'" +
    "\n\t\t  field 'x[2].login' in record 'Organization' should be of type 'string', found '4'" +
    "\n\t\tor" +
    "\n\t\t  missing required field 'x[2].orgName' of type 'string' in record 'Repository'\n\t\t}";
    assert(<string>checkpanic err.detail()["message"], errMsg);
    assert(err.message(), "{ballerina/lang.value}ConversionError");
}

type NumberArray int[]|float[];

type StringCharArray string[]|string:Char[];

type NumberOrStringCharArray NumberArray|StringCharArray;

type RecordWithArrayAsAField record {|
    NumberOrStringCharArray arr;
|};

type RecordWithNumberArrayAsAField record {|
    NumberArray arr;
|};

function testUnionNestedTypeConversionErrors() {
    json j = {"arr": [1, 1.2, "a", "aa"]};
    RecordWithArrayAsAField|RecordWithNumberArrayAsAField|error result = j.cloneWithType();
    assert(result is error, true);
    error err = <error>result;
    string errMsg = "'map<json>' value cannot be converted to '(RecordWithArrayAsAField|RecordWithNumberArrayAsAField)': " +
    "\n\t\t{" +
    "\n\t\t  {" +
    "\n\t\t    array element 'arr[2]' should be of type 'int', found '\"a\"'" +
    "\n\t\t    array element 'arr[3]' should be of type 'int', found '\"aa\"'" +
    "\n\t\t  or" +
    "\n\t\t    array element 'arr[2]' should be of type 'float', found '\"a\"'" +
    "\n\t\t    array element 'arr[3]' should be of type 'float', found '\"aa\"'" +
    "\n\t\t  or" +
    "\n\t\t    array element 'arr[0]' should be of type 'string', found '1'" +
    "\n\t\t    array element 'arr[1]' should be of type 'string', found '1.2'" +
    "\n\t\t  or" +
    "\n\t\t    array element 'arr[0]' should be of type 'lang.string:Char', found '1'" +
    "\n\t\t    array element 'arr[1]' should be of type 'lang.string:Char', found '1.2'" +
    "\n\t\t    array element 'arr[3]' should be of type 'lang.string:Char', found '\"aa\"'" +
    "\n\t\t  }" +
    "\n\t\tor" +
    "\n\t\t  {" +
    "\n\t\t    array element 'arr[2]' should be of type 'int', found '\"a\"'" +
    "\n\t\t    array element 'arr[3]' should be of type 'int', found '\"aa\"'" +
    "\n\t\t  or" +
    "\n\t\t...";
    assert(<string>checkpanic err.detail()["message"], errMsg);
    assert(err.message(), "{ballerina/lang.value}ConversionError");
}

type Journey record {|
    map<int> destinations;
    boolean[] enjoyable;
    [string, decimal] rating;
|};

type tupleType [map<float>, [Journey, map<Journey>], ()[], int...];

function testCloneWithTypeNestedStructuredTypesNegative() {
    json j = [
        {
            "destinations": {
                "Bali": 2,
                "Hawaii": 3
            },
            "enjoyable": [
                true
            ],
            "rating": [
                "high",
                8.5
            ]
        },
        [
            12,
            {
                "first": {
                    "destinations": {
                        "Bali": true,
                        "Hawaii": "3"
                    },
                    "enjoyable": [
                        1
                    ],
                    "rating": [
                        10,
                        8.5
                    ]
                },
                "second": 2
            }
        ],
        [
            null,
            null,
            0
        ],
        "12345678901234567890123"
    ];
    tupleType|error val = trap j.cloneWithType(tupleType);

    error err = <error>val;
    string errorMsgContent2 =
        "\n\t\tmap field '[0].destinations' should be of type 'float', found '{\"Bali\":2,\"Hawaii\":3...'" +
        "\n\t\tmap field '[0].enjoyable' should be of type 'float', found '[true]'" +
        "\n\t\tmap field '[0].rating' should be of type 'float', found '[\"high\",8.5]'" +
        "\n\t\ttuple element '[1][0]' should be of type 'Journey', found '12'" +
        "\n\t\tmap field '[1][1].first.destinations.Bali' should be of type 'int', found 'true'" +
        "\n\t\tmap field '[1][1].first.destinations.Hawaii' should be of type 'int', found '\"3\"'" +
        "\n\t\tarray element '[1][1].first.enjoyable[0]' should be of type 'boolean', found '1'" +
        "\n\t\ttuple element '[1][1].first.rating[0]' should be of type 'string', found '10'" +
        "\n\t\tmap field '[1][1].second' should be of type 'Journey', found '2'" +
        "\n\t\tarray element '[2][2]' should be of type '()', found '0'" +
        "\n\t\ttuple element '[3]' should be of type 'int', found '\"1234567890123456789...'";
    string errorMsg = "'json[]' value cannot be converted to 'tupleType': " +
        errorMsgContent2;
    assert(<string>checkpanic err.detail()["message"], errorMsg);
    assert(err.message(), "{ballerina/lang.value}ConversionError");
}

type OpenRec record {
};

function testCloneWithTypeJsonToRecordRestField() {
    json jsonVal = {
        arrVal: [
            {
            }
        ],
        mapVal: {
        }
    };

    (OpenRec & readonly)|error result = jsonVal.cloneWithType();
    assert(result is error, false);
    OpenRec & readonly rec = checkpanic result;
    assert(rec, {"arrVal": [{}], "mapVal": {}});
}

type Movie record {
    string title;
    int[]|decimal[] year;
};

type Union int|decimal|string|Union[]|map<Union>;

type Finite 2|2d;

type Emp1 record {|
    int salary;
|};

type Emp2 record {|
    decimal salary;
|};

function testCloneWithTypeWithAmbiguousUnion() {
    // json
    json val = 23.0d;
    int|decimal unionVal = checkpanic val.cloneWithType();
    assertTrue(unionVal is decimal);
    assert(unionVal, 23.0d);

    json[] jsonArr = [23d];
    string[]|int[]|decimal[] arrayUnionVal = checkpanic jsonArr.cloneWithType();
    assert(arrayUnionVal, [23]);
    assertTrue(arrayUnionVal is int[]);

    json[] jsonArr1 = [23.0d, 24];
    int[]|[decimal, int] arrTupleUnionVal = checkpanic jsonArr1.cloneWithType();
    assert(arrTupleUnionVal, [23, 24]);
    assertTrue(arrTupleUnionVal is int[]);

    [json] jsonTuple = [23d];
    string[]|int[]|decimal[] arrayUnionVal1 = checkpanic jsonTuple.cloneWithType();
    assert(arrayUnionVal1, [23]);
    assertTrue(arrayUnionVal1 is int[]);

    [json, json] jsonTuple1 = [23.0d, 24];
    int[]|[decimal, int] arrTupleUnionVal1 = checkpanic jsonTuple1.cloneWithType();
    assert(arrTupleUnionVal1, [23, 24]);
    assertTrue(arrTupleUnionVal1 is int[]);

    [decimal, int...]|int[] arrTupleRestUnionVal1 = checkpanic jsonTuple1.cloneWithType();
    assert(arrTupleRestUnionVal1, [23.0d, 24]);
    assertTrue(arrTupleRestUnionVal1 is [decimal, int...]);

    val = [[1.2d, 2.3d]];
    int[][]|decimal[][] clone2D = checkpanic val.cloneWithType();
    assert(clone2D, [[1, 2]]);
    assertTrue(clone2D is int[][]);

    decimal[][]|int[][] clone2D2 = checkpanic val.cloneWithType();
    assert(clone2D2, [[1.2d, 2.3d]]);
    assertTrue(clone2D2 is decimal[][]);

    [[int, int...]]|[decimal, decimal...][] clone2DRest = checkpanic val.cloneWithType();
    assert(clone2DRest, [[1, 2]]);
    assertTrue(clone2DRest is [[int, int...]]);

    [decimal, decimal...][]|[[int, int...]] clone2DRest2 = checkpanic val.cloneWithType();
    assert(clone2DRest2, [[1.2d, 2.3d]]);
    assertTrue(clone2DRest2 is [decimal, decimal...][]);

    val = [[1.2d, "l"], 9];
    [[decimal, string], int]|int[][]|decimal[][] cloneTuple = checkpanic val.cloneWithType();
    assert(cloneTuple, [[1.2d, "l"], 9]);
    assertTrue(cloneTuple is [[decimal, string], int]);

    int[][]|decimal[][]|[[decimal, string], int] cloneTuple2 = checkpanic val.cloneWithType();
    assert(cloneTuple2, [[1.2d, "l"], 9]);
    assertTrue(cloneTuple2 is [[decimal, string], int]);

    [[decimal, string...], int...]|int[][]|decimal[][] cloneTupleRest = checkpanic val.cloneWithType();
    assert(cloneTupleRest, [[1.2d, "l"], 9]);
    assertTrue(cloneTupleRest is [[decimal, string...], int...]);

    json jsonMap = {
        title: "Inception",
        year: [2010.0d, 2011.0d]
    };

    Movie recordVal = checkpanic jsonMap.cloneWithType(Movie);
    assertTrue(recordVal.year is int[]);

    json jsonMap1 = {a: [1d, 2.03d], b: [2d, 3d, 4d]};
    map<int[]|decimal[]>|map<float[]> mapVal = checkpanic jsonMap1.cloneWithType();
    assertTrue(mapVal["b"] is int[]);

    json moviesJson = [
        {
            title: "Inception",
            year: [2010.0d, 2011.0d]
        },
        {
            title: "Alice in wonderland",
            year: [2010.0d, 2016.0d]
        },
        {
            title: "Coco",
            year: [2017.0d, 2018.0d]
        }
    ];

    table<map<json>> tableJson = table [
        {
            title: "Inception",
            year: [2010, 2011]
        },
        {
            title: "Alice in wonderland",
            year: [2010, 2016]
        },
        {
            title: "Coco",
            year: [2017, 2018]
        }
    ];

    Movie[]|table<map<anydata>> movieArr = checkpanic moviesJson.cloneWithType();
    assertTrue(movieArr is Movie[]);
    if (movieArr is Movie[]) {
        assertTrue(movieArr[0].year is int[]);
        assertTrue(movieArr[1].year is int[]);
        assertTrue(movieArr[2].year is int[]);
    }

    table<Movie> movieTab = checkpanic tableJson.cloneWithType();
    foreach Movie mov in movieTab {
        assertTrue(mov.year is int[]);
    }

    // json with other union combinations
    json m = {a: 1, b: 2.2};
    map<int>|map<decimal> clone = checkpanic m.cloneWithType();
    assertTrue(clone is map<int>);
    assertTrue(clone["a"] is int);
    assertTrue(clone["b"] is int);

    m = {salary: 1000d};
    Emp1|Emp2 clone1 = checkpanic m.cloneWithType();
    assertTrue(clone1 is Emp1);
    assertFalse(clone1 is Emp2);

    m = [m];
    map<int>[]|map<decimal>[] clone2 = checkpanic m.cloneWithType();
    assertTrue(clone2 is map<int>[]);

    Emp1[]|Emp2[] clone3 = checkpanic m.cloneWithType();
    assertTrue(clone3 is Emp1[]);

    // anydata

    anydata val1 = 23.0d;
    int|decimal unionVal1 = checkpanic val1.cloneWithType();
    assertTrue(unionVal1 is decimal);
    assert(unionVal1, 23.0d);

    anydata[] anydataArr = [23d];
    string[]|int[]|decimal[] arrayUnionVal2 = checkpanic anydataArr.cloneWithType();
    assert(arrayUnionVal2, [23]);
    assertTrue(arrayUnionVal2 is int[]);

    anydata[] anydataArr1 = [23.0d, 24];
    [decimal, int]|int[] arrTupleUnionVal2 = checkpanic anydataArr1.cloneWithType();
    assert(arrTupleUnionVal2, [23.0d, 24]);
    assertTrue(arrTupleUnionVal2 is [decimal, int]);

    [anydata] anydataTuple = [23d];
    string[]|decimal[]|int[]|() arrayUnionVal3 = checkpanic anydataTuple.cloneWithType();
    assert(arrayUnionVal3, [23d]);
    assertTrue(arrayUnionVal3 is decimal[]);

    [anydata, anydata] anydataTuple1 = [23.0d, 24];
    [decimal, int]|int[]|float[] arrTupleUnionVal3 = checkpanic anydataTuple1.cloneWithType();
    assert(arrTupleUnionVal3, [23.0d, 24]);
    assertTrue(arrTupleUnionVal3 is [decimal, int]);

    [decimal, int...]|int[]|[float...] arrTupleUnionVal3Rest = checkpanic anydataTuple1.cloneWithType();
    assert(arrTupleUnionVal3Rest, [23.0d, 24]);
    assertTrue(arrTupleUnionVal3Rest is [decimal, int...]);

    anydata anydataMap = {
        title: "Inception",
        year: [2010.0d, 2011.0d]
    };

    Movie recordVal1 = checkpanic anydataMap.cloneWithType(Movie);
    assertTrue(recordVal1.year is int[]);

    anydata anydataMap1 = {a: [1d, 2.03d], b: [2d, 3d, 4d]};
    map<int[]|decimal[]> mapVal1 = checkpanic anydataMap1.cloneWithType();
    assertTrue(mapVal1["b"] is int[]);

    anydata moviesAnydata = [
        {
            title: "Inception",
            year: [2010.0d, 2011.0d]
        },
        {
            title: "Alice in wonderland",
            year: [2010.0d, 2016.0d]
        },
        {
            title: "Coco",
            year: [2017.0d, 2018.0d]
        }
    ];

    table<map<anydata>> tableAnydata = table [
        {
            title: "Inception",
            year: [2010, 2011]
        },
        {
            title: "Alice in wonderland",
            year: [2010, 2016]
        },
        {
            title: "Coco",
            year: [2017, 2018]
        }
    ];

    Movie[] movieArr1 = checkpanic moviesAnydata.cloneWithType();
    assertTrue(movieArr1[0].year is int[]);
    assertTrue(movieArr1[1].year is int[]);
    assertTrue(movieArr1[2].year is int[]);

    table<Movie> movieTab1 = checkpanic tableAnydata.cloneWithType();
    foreach Movie mov in movieTab1 {
        assertTrue(mov.year is int[]);
    }

    // union
    Union val2 = 23.0d;
    int|decimal unionVal2 = checkpanic val2.cloneWithType();
    assertTrue(unionVal2 is decimal);
    assert(unionVal2, 23.0d);

    Union[] unionArr = [23d];
    string[]|int[]|decimal[] arrayUnionVal4 = checkpanic unionArr.cloneWithType();
    assert(arrayUnionVal4, [23]);
    assertTrue(arrayUnionVal4 is int[]);

    Union[] UnionArr2 = [23.0d, 24];
    [decimal, int]|int[] arrTupleUnionVal5 = checkpanic UnionArr2.cloneWithType();
    assert(arrTupleUnionVal5, [23.0d, 24]);
    assertTrue(arrTupleUnionVal5 is [decimal, int]);

    [Union] unionTuple = [23d];
    string[]|decimal[]|int[] arrayUnionVal5 = checkpanic unionTuple.cloneWithType();
    assert(arrayUnionVal5, [23d]);
    assertTrue(arrayUnionVal5 is decimal[]);

    [Union, Union] unionTuple1 = [23.0d, 24];
    [decimal, int]|int[] arrTupleUnionVal6 = checkpanic unionTuple1.cloneWithType();
    assert(arrTupleUnionVal6, [23.0d, 24]);
    assertTrue(arrTupleUnionVal6 is [decimal, int]);

    Union unionMap = {
        title: "Inception",
        year: [2010.0d, 2011.0d]
    };

    Movie recordVal2 = checkpanic unionMap.cloneWithType(Movie);
    assertTrue(recordVal2.year is int[]);

    Union unionMap1 = {a: [1d, 2.03d], b: [2d, 3d, 4d]};
    map<int[]|decimal[]> mapVal2 = checkpanic unionMap1.cloneWithType();
    assertTrue(mapVal2["b"] is int[]);

    Union moviesUnion = [
        {
            title: "Inception",
            year: [2010.0d, 2011.0d]
        },
        {
            title: "Alice in wonderland",
            year: [2010.0d, 2016.0d]
        },
        {
            title: "Coco",
            year: [2017.0d, 2018.0d]
        }
    ];

    table<map<Union>> tableUnion = table [
        {
            title: "Inception",
            year: [2010, 2011]
        },
        {
            title: "Alice in wonderland",
            year: [2010, 2016]
        },
        {
            title: "Coco",
            year: [2017, 2018]
        }
    ];

    Movie[]|table<map<anydata>> movieArr2 = checkpanic moviesUnion.cloneWithType();
    assertTrue(movieArr2 is Movie[]);
    if (movieArr2 is Movie[]) {
        assertTrue(movieArr2[0].year is int[]);
        assertTrue(movieArr2[1].year is int[]);
        assertTrue(movieArr2[2].year is int[]);
    }

    table<Movie> movieTab2 = checkpanic tableUnion.cloneWithType();
    foreach Movie mov in movieTab2 {
        assertTrue(mov.year is int[]);
    }

    // With Finite types
    jsonArr = [2];
    string[]|Finite[] arrayFiniteVal = checkpanic jsonArr.cloneWithType();
    assertFalse(arrayFiniteVal[0] is decimal);
    assertTrue(arrayFiniteVal[0] is int);

    jsonArr = [2.0d];
    string[]|Finite[] arrayFiniteVal1 = checkpanic jsonArr.cloneWithType();
    assertTrue(arrayFiniteVal1 is Finite[]);
    assertFalse(arrayFiniteVal1[0] is int);
    assertTrue(arrayFiniteVal1[0] is decimal);

    jsonArr = [2.0];
    string[]|Finite[]|error arrayFiniteVal2 = jsonArr.cloneWithType();
    assert(arrayFiniteVal2 is string[], false);
    assert(arrayFiniteVal2 is Finite[], true);
    assert(arrayFiniteVal2 is error, false);
    assert(checkpanic arrayFiniteVal2, [2]);

    json jVal = [23.0d, 24];
    int[]|[decimal, float]|error result1 = jVal.cloneWithType();
    assert(result1 is int[], true);
    assert(result1 is [decimal, float], false);
    assert(result1 is error, false);
    assert(checkpanic result1, [23, 24]);

    decimal[]|[decimal, decimal...]|[decimal, float...]|error result1Rest = jVal.cloneWithType();
    assert(result1Rest is decimal[], true);
    assert(result1Rest is [decimal, decimal...], false);
    assert(result1Rest is [decimal, float...], false);
    assert(result1Rest is error, false);
    assert(checkpanic result1Rest, [23.0d, 24d]);

    jVal = [23.0, 24];
    int[]|decimal[]|float[]|error result2 = jVal.cloneWithType();
    assert(result2 is int[], true);
    assert(result2 is decimal[], false);
    assert(result2 is float[], false);
    assert(result2 is error, false);
    assert(checkpanic result2, [23, 24]);

    jVal = [[1.2d, 2.3d]];
    [int...][]|[[int, decimal...]]|error result2Rest = jVal.cloneWithType();
    assert(result2Rest is [int...][], true);
    assert(result2Rest is [[int, decimal...]], false);
    assert(result2Rest is error, false);
    assert(checkpanic result2Rest, [[1, 2]]);

    jVal = {a: [1d, 2.03], b: [2, 3d, 4]};
    map<int[]|decimal[]>|error result3 = jVal.cloneWithType();
    assert(result3 is map<int[]|decimal[]>, true);
    assert(result3 is map<int[]>, false);
    assert(result3 is map<decimal[]>, false);
    assert(result3 is error, false);
    assert(checkpanic result3, {a: [1, 2], b: [2, 3, 4]});

    // Negative cases
    jVal = {a: ["aaa"], b: [3.2]};
    map<int|decimal>|error result4 = jVal.cloneWithType();
    error err = <error>result4;
    assertEquality("{ballerina/lang.value}ConversionError", err.message());
    assertEquality("'map<json>' value cannot be converted to 'map<(int|decimal)>': \n\t	map field 'a' should be of " +
    "type '(int|decimal)', found '[\"aaa\"]'\n\t	map field 'b' should be of type '(int|decimal)', found '[3.2]'",
    <string>checkpanic err.detail()["message"]);

    [int, string] tuple = [1, "aaa"];
    jVal = {a: tuple, b: [3.2]};
    result4 = jVal.cloneWithType();
    err = <error>result4;
    assertEquality("{ballerina/lang.value}ConversionError", err.message());
    assertEquality("'map<json>' value cannot be converted to 'map<(int|decimal)>': \n\t	map field 'a' should be of " +
    "type '(int|decimal)', found '[1,\"aaa\"]'\n\t	map field 'b' should be of type '(int|decimal)', found '[3.2]'",
    <string>checkpanic err.detail()["message"]);
}

public type CodingExtension record {|
    *Element;

    uri url;
    Coding valueCoding;
|};

public type Coding record {|
    *Element;

    string id?;
    Extension[] extension?;
    uri system?;
    string 'version?;
    code code?;
    string display?;
    boolean userSelected?;
|};

public type code string;

public type uri string;

public type ElementReadOnlyCyclic record {|
    string id?;
    Extension[] extension?;
    ((ElementReadOnlyCyclic & readonly)|())...;
|};

public type Element record {|
    string id?;
    Extension[] extension?;
    Element...;
|};

public type ExtensionExtension record {|
    *Element;

    uri url;
    Extension[] extension?;
|};

public type CodeableConceptExtension record {|
    *Element;

    uri url;
    CodeableConcept valueCodeableConcept;
|};

public type CodeableConcept record {|
    *Element;

    string id?;
    Extension[] extension?;
    Coding[] coding?;
    string text?;
|};

public type ExtensionExtension2 record {|
    *ElementReadOnlyCyclic;

    uri url;
    Extension[] extension?;
|};

public type CodeableConceptExtension2 record {|
    *ElementReadOnlyCyclic;

    uri url;
    CodeableConcept valueCodeableConcept;
|};

public type CodingExtension2 record {|
    *ElementReadOnlyCyclic;

    uri url;
    Coding valueCoding;
|};

public type Extension CodeableConceptExtension|ExtensionExtension|CodingExtension;

public type Extension2 CodeableConceptExtension2|ExtensionExtension2|CodingExtension2;

function testCloneWithTypeToUnion() {
    int|float|[string, string] unionVar = 2;
    float|decimal|[string, int]|error tupleValue = unionVar.cloneWithType(UnionTypedesc);
    assertEquality(tupleValue, 2.0);
    assertTrue(tupleValue is float);
    assertFalse(tupleValue is decimal);
    assertFalse(tupleValue is [string, int]);
    assertFalse(tupleValue is error);

    json extCoding = {
        "valueCoding": {
            "system": "http://loinc.org",
            "code": "LA29518-0",
            "display": "he/him/his/himself"
        },
        "url": "http://open.epic.com/FHIR/StructureDefinition/extension/calculated-pronouns-to-use-for-text"
    };

    Extension ext = checkpanic extCoding.cloneWithType();
    assertEquality(ext, <Extension>{
        "url":
    "http://open.epic.com/FHIR/StructureDefinition/extension/calculated-pronouns-to-use-for-text",
        "valueCoding": {"system": "http://loinc.org", "code": "LA29518-0", "display": "he/him/his/himself"}
    });
    assertEquality((typeof ext).toString(), "typedesc CodingExtension");

    Extension2 ext2 = checkpanic extCoding.cloneWithType();
    assertEquality(ext2, <Extension2>{
        "url":
    "http://open.epic.com/FHIR/StructureDefinition/extension/calculated-pronouns-to-use-for-text",
        "valueCoding": {"system": "http://loinc.org", "code": "LA29518-0", "display": "he/him/his/himself"}
    });
    assertEquality((typeof ext2).toString(), "typedesc CodingExtension2");
}

type UnionTypedesc typedesc<float|decimal|[string, int]>;

public type Array ["array", 1];

public type Mapping ["mapping", 2];

function testCloneWithTypeWithTuples() returns error? {
    Array|Mapping x = check (["mapping", 2]).cloneWithType();
    assertTrue(x is Mapping);
    assertFalse(x is Array);
}

type FooTab2 record {|
    int x2 = 1;
    int y3 = 2;
|};

type FooTab3 record {
    string x3;
    int y3;
};

type FooTab4 record {
    string x3;
    int y3 = 1;
};

type FooTab5 record {
    string x3;
    int y3?;
};

type FooTab6 record {
    string x3;
};

type TableFoo2 table<FooTab2>;

type TableFoo3 table<FooTab3>;

type TableFoo4 table<FooTab4>;

type TableFoo5 table<FooTab5>;

type TableFoo6 table<FooTab6>;

function testCloneWithTypeTable() {
    json jj = [
        {x3: "abc"},
        {x3: "abc"}
    ];

    table<FooTab2>|error tabJ2 = jj.cloneWithType(TableFoo2);
    assertEquality(tabJ2 is error, true);

    table<FooTab3>|error tabJ3 = jj.cloneWithType(TableFoo3);
    assertEquality(tabJ3 is error, true);

    table<FooTab4>|error tabJ4 = jj.cloneWithType(TableFoo4);
    assertEquality(tabJ4 is table<FooTab4>, true);

    table<FooTab5>|error tabJ5 = jj.cloneWithType(TableFoo5);
    assertEquality(tabJ5 is table<FooTab5>, true);

    table<FooTab6>|error tabJ6 = jj.cloneWithType(TableFoo6);
    assertEquality(tabJ6 is table<FooTab6>, true);
}

type RegExpType string:RegExp;

function testCloneWithTypeOnRegExp() {
    string s = "AB+C*D{1,4}";
    RegExpType|error x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `AB+C*D{1,4}` == x1, true);
    }

    s = "A?B+C*?D{1,4}";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `A?B+C*?D{1,4}` == x1, true);
    }

    s = "A\\sB\\WC\\Dd\\\\";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `A\sB\WC\Dd\\` == x1, true);
    }

    s = "\\s{1}\\p{sc=Braille}*";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `\s{1}\p{sc=Braille}*` == x1, true);
    }

    s = "AB+\\p{gc=Lu}{1,}";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `AB+\p{gc=Lu}{1,}` == x1, true);
    }

    s = "A\\p{Lu}??B+\\W\\(+?C*D{1,4}?";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `A\p{Lu}??B+\W\(+?C*D{1,4}?` == x1, true);
    }

    s = "\\p{sc=Latin}\\p{gc=Lu}\\p{Lt}\\tA+?\\)*";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `\p{sc=Latin}\p{gc=Lu}\p{Lt}\tA+?\)*` == x1, true);
    }

    s = "[\\r\\n\\^]";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `[\r\n\^]` == x1, true);
    }

    s = "[A\\sB\\WC\\Dd\\\\]";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `[A\sB\WC\Dd\\]` == x1, true);
    }

    s = "[\\p{sc=Latin}\\p{gc=Lu}\\p{Lt}\\tA\\)]??";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `[\p{sc=Latin}\p{gc=Lu}\p{Lt}\tA\)]??` == x1, true);
    }

    s = "[A\\sA-GB\\WC\\DJ-Kd\\\\]*";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `[A\sA-GB\WC\DJ-Kd\\]*` == x1, true);
    }

    s = "[\\sA-F\\p{sc=Braille}K-Mabc-d\\--]";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `[\sA-F\p{sc=Braille}K-Mabc-d\--]` == x1, true);
    }

    s = "[\\p{Lu}-\\w\\p{sc=Latin}\\p{gc=Lu}\\p{Lu}-\\w\\p{Lt}\\tA\\)\\p{Lu}-\\w]{12,32}?";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `[\p{Lu}-\w\p{sc=Latin}\p{gc=Lu}\p{Lu}-\w\p{Lt}\tA\)\p{Lu}-\w]{12,32}?` == x1, true);
    }

    s = "(?:ABC)";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `(?:ABC)` == x1, true);
    }

    s = "(?i:ABC)";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `(?i:ABC)` == x1, true);
    }

    s = "(?i-m:AB+C*)";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `(?i-m:AB+C*)` == x1, true);
    }

    s = "(?imxs:AB+C*D{1,4})";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `(?imxs:AB+C*D{1,4})` == x1, true);
    }

    s = "(?imx-s:A?B+C*?D{1,4})";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `(?imx-s:A?B+C*?D{1,4})` == x1, true);
    }

    s = "(?i-s:\\s{1}\\p{sc=Braille}*)";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `(?i-s:\s{1}\p{sc=Braille}*)` == x1, true);
    }

    s = "(?ims-x:\\p{sc=Latin}\\p{gc=Lu}\\p{Lt}\\tA+?\\)*)";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `(?ims-x:\p{sc=Latin}\p{gc=Lu}\p{Lt}\tA+?\)*)` == x1, true);
    }

    s = "(?im-sx:[A\\sA-GB\\WC\\DJ-Kd\\\\]*)";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `(?im-sx:[A\sA-GB\WC\DJ-Kd\\]*)` == x1, true);
    }

    s = "(?i-sxm:[\\p{Lu}-\\w\\p{sc=Latin}\\p{gc=Lu}\\p{Lu}-\\w\\p{Lt}\\tA\\)\\p{Lu}-\\w]{12,32}?)";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `(?i-sxm:[\p{Lu}-\w\p{sc=Latin}\p{gc=Lu}\p{Lu}-\w\p{Lt}\tA\)\p{Lu}-\w]{12,32}?)` == x1, true);
    }

    s = "(?:(?i-m:ab|cd)|aa|abcdef[a-zefg-ijk-]|ba|b|c{1,3}^)+|ef";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `(?:(?i-m:ab|cd)|aa|abcdef[a-zefg-ijk-]|ba|b|c{1,3}^)+|ef` == x1, true);
    }

    s = "(z)((a+)?(b+)?(c))*";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `(z)((a+)?(b+)?(c))*` == x1, true);
    }

    s = "^^^^^^^robot$$$$";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `^^^^^^^robot$$$$` == x1, true);
    }

    s = "cx{0,93}c";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `cx{0,93}c` == x1, true);
    }

    s = "[\\d]*[\\s]*bc.";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `[\d]*[\s]*bc.` == x1, true);
    }

    s = "\\??\\??\\??\\??\\??";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `\??\??\??\??\??` == x1, true);
    }

    s = ".?.?.?.?.?.?.?";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `.?.?.?.?.?.?.?` == x1, true);
    }

    s = "bc..[\\d]*[\\s]*";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `bc..[\d]*[\s]*` == x1, true);
    }

    s = "\\\\u123";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `\\u123` == x1, true);
    }

    s = "";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
}

function testCloneWithTypeOnRegExpNegative() {
    string s = "AB+^*";
    RegExpType|error x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is error, true);
    assertEquality("{ballerina/lang.value}ConversionError", (<error>x1).message());
    assertEquality("'string' value cannot be converted to 'RegExpType': Failed to parse regular expression: missing backslash before '*' token in 'AB+^*'",
        <string>checkpanic (<error>x1).detail()["message"]);

    s = "AB\\hCD";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is error, true);
    assertEquality("{ballerina/lang.value}ConversionError", (<error>x1).message());
    assertEquality("'string' value cannot be converted to 'RegExpType': Failed to parse regular expression: invalid character 'h' after backslash in 'AB\\hCD'",
        <string>checkpanic (<error>x1).detail()["message"]);

    s = "AB\\pCD";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is error, true);
    assertEquality("{ballerina/lang.value}ConversionError", (<error>x1).message());
    assertEquality("'string' value cannot be converted to 'RegExpType': Failed to parse regular expression: missing open brace '{' token in 'AB\\pCD'",
        <string>checkpanic (<error>x1).detail()["message"]);

    s = "AB\\uCD";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is error, true);
    assertEquality("{ballerina/lang.value}ConversionError", (<error>x1).message());
    assertEquality("'string' value cannot be converted to 'RegExpType': Failed to parse regular expression: invalid character 'u' after backslash in 'AB\\uCD'",
        <string>checkpanic (<error>x1).detail()["message"]);

    s = "AB\\u{001CD";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is error, true);
    assertEquality("{ballerina/lang.value}ConversionError", (<error>x1).message());
    assertEquality("'string' value cannot be converted to 'RegExpType': Failed to parse regular expression: missing close brace '}' token in 'AB\\u{001CD'",
        <string>checkpanic (<error>x1).detail()["message"]);

    s = "AB\\p{sc=Lu";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is error, true);
    assertEquality("{ballerina/lang.value}ConversionError", (<error>x1).message());
    assertEquality("'string' value cannot be converted to 'RegExpType': Failed to parse regular expression: missing close brace '}' token in 'AB\\p{sc=Lu'",
        <string>checkpanic (<error>x1).detail()["message"]);

    s = "[^abc";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is error, true);
    assertEquality("{ballerina/lang.value}ConversionError", (<error>x1).message());
    assertEquality("'string' value cannot be converted to 'RegExpType': Failed to parse regular expression: missing close bracket ']' token in '[^abc'",
        <string>checkpanic (<error>x1).detail()["message"]);

    s = "(abc";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is error, true);
    assertEquality("{ballerina/lang.value}ConversionError", (<error>x1).message());
    assertEquality("'string' value cannot be converted to 'RegExpType': Failed to parse regular expression: missing close parenthesis ')' token in '(abc'",
        <string>checkpanic (<error>x1).detail()["message"]);

    s = "(ab^*)";
    x1 = s.cloneWithType(RegExpType);
    assertEquality(x1 is error, true);
    assertEquality("{ballerina/lang.value}ConversionError", (<error>x1).message());
    assertEquality("'string' value cannot be converted to 'RegExpType': Failed to parse regular expression: missing backslash before '*' token in '(ab^*)'",
        <string>checkpanic (<error>x1).detail()["message"]);
}

function testCloneWithTypeWithXML() {
    string s1 = "<test>name</test>";
    xml|error xe = s1.cloneWithType(XmlType);
    assertEquality(xe is xml, true);

    xml x = checkpanic xe;
    json j = x.toJson();
    assertEquality(j, s1);

    xml wso2 = xml `<name>WSO2</name>`;
    xml bal = xml `<name>Ballerina</name>`;
    json mapXmlJson = {name: wso2.toJson(), team: bal.toJson()};
    map<xml> mapXml = checkpanic mapXmlJson.cloneWithType();
    test:assertValueEqual(mapXml, {"name": xml `<name>WSO2</name>`, "team": xml `<name>Ballerina</name>`});

    json[] tableMapXmlJson = [
        {id: wso2.toJson(), title: bal.toJson()}
    ];
    table<map<xml>> tableMapXml = checkpanic tableMapXmlJson.cloneWithType();
    test:assertValueEqual(tableMapXml, table [{"id": xml `<name>WSO2</name>`, "title": xml `<name>Ballerina</name>`}]);

    anydata xmlArrayAnydata = [xml `<name>WSO2</name>`, xml `<name>Ballerina</name>`];
    json xmlArrayJson = xmlArrayAnydata.toJson();
    xml[] xmlArray = checkpanic xmlArrayJson.cloneWithType();
    test:assertValueEqual(xmlArray, xmlArrayAnydata);
}

type MyRecord record {
    int a;
};

type FirstArr int[];

type SecondArr int[];

type ThirdArr FirstArr;

type FirstTuple [int, string];

type SecondTuple [int, string];

type ThirdTuple FirstTuple;

type FirstMap map<int>;

type SecondMap map<int>;

type ThirdMap FirstMap;

type FirstRecord MyRecord;

type SecondRecord MyRecord;

type ThirdRecord FirstRecord;

type FirstTable table<MyRecord>;

type SecondTable table<MyRecord>;

type ThirdTable FirstTable;

type FirstNull ();

type SecondNull ();

type ThirdNull FirstNull;

type FirstBoolean boolean;

type SecondBoolean boolean;

type ThirdBoolean FirstBoolean;

type FirstInt int;

type SecondInt int;

type ThirdInt FirstInt;

type FirstFloat float;

type SecondFloat float;

type ThirdFloat FirstFloat;

type FirstDecimal decimal;

type SecondDecimal decimal;

type ThirdDecimal FirstDecimal;

type FirstString string;

type SecondString string;

type ThirdString FirstString;

type FirstByte byte;

type SecondByte byte;

type ThirdByte FirstByte;

type FirstXML xml;

type SecondXML xml;

type ThirdXML FirstXML;

type FirstJSON json;

type SecondJSON json;

type ThirdJSON FirstJSON;

type FirstAnydata anydata;

type SecondAnydata anydata;

type ThirdAnydata FirstAnydata;

type FirstArrayUnion ThirdArr|SecondArr|FirstArr;

type SecondArrayUnion ThirdArr|SecondArr|FirstArr;

type ThirdArrayUnion FirstArrayUnion;

type FourthArrayUnion FirstArrayUnion|SecondArrayUnion;

type FifthArrayUnion FourthArrayUnion;

type SixthArrayUnion FifthArrayUnion|FourthArrayUnion;

type FirstNumberUnion int|int:Unsigned8|int:Signed16;

type SecondNumberUnion FirstInt|FirstByte;

type ThirdNumberUnion FirstNumberUnion;

type FourthNumberUnion ThirdNumberUnion|SecondNumberUnion;

type FifthNumberUnion FourthNumberUnion;

type SixthNumberUnion FifthNumberUnion|FourthNumberUnion;

function testConvertingToReferenceTypes() {
    int[] val0 = [1, 2, 3];
    FirstArr val1 = [1, 2, 3];
    SecondArr val2 = checkpanic val1.cloneWithType();
    ThirdArr val3 = checkpanic val2.cloneWithType();
    ThirdArr val4 = checkpanic val0.cloneWithType();
    test:assertValueEqual((typeof val2).toString(), "typedesc SecondArr");
    test:assertValueEqual((typeof val3).toString(), "typedesc ThirdArr");
    test:assertValueEqual((typeof val4).toString(), "typedesc ThirdArr");

    [int, string] val5 = [1, "hello"];
    FirstTuple val6 = [1, "hello"];
    SecondTuple val7 = checkpanic val6.cloneWithType();
    ThirdTuple val8 = checkpanic val7.cloneWithType();
    ThirdTuple val9 = checkpanic val5.cloneWithType();
    test:assertValueEqual((typeof val7).toString(), "typedesc SecondTuple");
    test:assertValueEqual((typeof val8).toString(), "typedesc ThirdTuple");
    test:assertValueEqual((typeof val9).toString(), "typedesc ThirdTuple");

    map<int> val10 = {a: 1, b: 2};
    FirstMap val11 = {a: 1, b: 2};
    SecondMap val12 = checkpanic val11.cloneWithType();
    ThirdMap val13 = checkpanic val12.cloneWithType();
    ThirdMap val14 = checkpanic val10.cloneWithType();
    test:assertValueEqual((typeof val12).toString(), "typedesc SecondMap");
    test:assertValueEqual((typeof val13).toString(), "typedesc ThirdMap");
    test:assertValueEqual((typeof val14).toString(), "typedesc ThirdMap");

    MyRecord val15 = {a: 1};
    FirstRecord val16 = {a: 1};
    SecondRecord val17 = checkpanic val16.cloneWithType();
    ThirdRecord val18 = checkpanic val17.cloneWithType();
    ThirdRecord val19 = checkpanic val15.cloneWithType();
    test:assertValueEqual((typeof val17).toString(), "typedesc SecondRecord");
    test:assertValueEqual((typeof val18).toString(), "typedesc ThirdRecord");
    test:assertValueEqual((typeof val19).toString(), "typedesc ThirdRecord");

    table<MyRecord> val20 = table [{a: 1}];
    FirstTable val21 = table [{a: 1}];
    SecondTable val22 = checkpanic val21.cloneWithType();
    ThirdTable val23 = checkpanic val22.cloneWithType();
    ThirdTable val24 = checkpanic val20.cloneWithType();
    test:assertValueEqual((typeof val22).toString(), "typedesc SecondTable");
    test:assertValueEqual((typeof val23).toString(), "typedesc ThirdTable");
    test:assertValueEqual((typeof val24).toString(), "typedesc ThirdTable");

    () val25 = ();
    FirstNull val26 = ();
    SecondNull val27 = checkpanic val26.cloneWithType();
    ThirdNull val28 = checkpanic val27.cloneWithType();
    ThirdNull val29 = checkpanic val25.cloneWithType();
    test:assertValueEqual((typeof val27).toString(), "typedesc ()");
    test:assertValueEqual((typeof val28).toString(), "typedesc ()");
    test:assertValueEqual((typeof val29).toString(), "typedesc ()");

    boolean val30 = true;
    FirstBoolean val31 = true;
    SecondBoolean val32 = checkpanic val31.cloneWithType();
    ThirdBoolean val33 = checkpanic val32.cloneWithType();
    ThirdBoolean val34 = checkpanic val30.cloneWithType();
    test:assertValueEqual((typeof val32).toString(), "typedesc true");
    test:assertValueEqual((typeof val33).toString(), "typedesc true");
    test:assertValueEqual((typeof val34).toString(), "typedesc true");

    int val35 = 1;
    FirstInt val36 = 1;
    SecondInt val37 = checkpanic val36.cloneWithType();
    ThirdInt val38 = checkpanic val37.cloneWithType();
    ThirdInt val39 = checkpanic val35.cloneWithType();
    test:assertValueEqual((typeof val37).toString(), "typedesc 1");
    test:assertValueEqual((typeof val38).toString(), "typedesc 1");
    test:assertValueEqual((typeof val39).toString(), "typedesc 1");

    float val40 = 1.0;
    FirstFloat val41 = 1.0;
    SecondFloat val42 = checkpanic val41.cloneWithType();
    ThirdFloat val43 = checkpanic val42.cloneWithType();
    ThirdFloat val44 = checkpanic val40.cloneWithType();
    test:assertValueEqual((typeof val42).toString(), "typedesc 1.0");
    test:assertValueEqual((typeof val43).toString(), "typedesc 1.0");
    test:assertValueEqual((typeof val44).toString(), "typedesc 1.0");

    decimal val45 = 1.0;
    FirstDecimal val46 = 1.0;
    SecondDecimal val47 = checkpanic val46.cloneWithType();
    ThirdDecimal val48 = checkpanic val47.cloneWithType();
    ThirdDecimal val49 = checkpanic val45.cloneWithType();
    test:assertValueEqual((typeof val47).toString(), "typedesc 1.0");
    test:assertValueEqual((typeof val48).toString(), "typedesc 1.0");
    test:assertValueEqual((typeof val49).toString(), "typedesc 1.0");

    string val50 = "hello";
    FirstString val51 = "hello";
    SecondString val52 = checkpanic val51.cloneWithType();
    ThirdString val53 = checkpanic val52.cloneWithType();
    ThirdString val54 = checkpanic val50.cloneWithType();
    test:assertValueEqual((typeof val52).toString(), "typedesc hello");
    test:assertValueEqual((typeof val53).toString(), "typedesc hello");
    test:assertValueEqual((typeof val54).toString(), "typedesc hello");

    byte val55 = 1;
    FirstByte val56 = 1;
    SecondByte val57 = checkpanic val56.cloneWithType();
    ThirdByte val58 = checkpanic val57.cloneWithType();
    ThirdByte val59 = checkpanic val55.cloneWithType();
    test:assertValueEqual((typeof val57).toString(), "typedesc 1");
    test:assertValueEqual((typeof val58).toString(), "typedesc 1");
    test:assertValueEqual((typeof val59).toString(), "typedesc 1");

    xml val60 = xml `<hello>world</hello>`;
    FirstXML val61 = xml `<hello>world</hello>`;
    SecondXML val62 = checkpanic val61.cloneWithType();
    ThirdXML val63 = checkpanic val62.cloneWithType();
    ThirdXML val64 = checkpanic val60.cloneWithType();
    test:assertValueEqual((typeof val62).toString(),
        "typedesc xml<(lang.xml:Element|lang.xml:Comment|lang.xml:ProcessingInstruction|lang.xml:Text)>");
    test:assertValueEqual((typeof val63).toString(),
        "typedesc xml<(lang.xml:Element|lang.xml:Comment|lang.xml:ProcessingInstruction|lang.xml:Text)>");
    test:assertValueEqual((typeof val64).toString(),
        "typedesc xml<(lang.xml:Element|lang.xml:Comment|lang.xml:ProcessingInstruction|lang.xml:Text)>");

    json val65 = {a: 1};
    FirstJSON val66 = {a: 1};
    SecondJSON val67 = checkpanic val66.cloneWithType();
    ThirdJSON val68 = checkpanic val67.cloneWithType();
    ThirdJSON val69 = checkpanic val65.cloneWithType();
    test:assertValueEqual((typeof val67).toString(), "typedesc map<json>");
    test:assertValueEqual((typeof val68).toString(), "typedesc map<json>");
    test:assertValueEqual((typeof val69).toString(), "typedesc map<json>");

    anydata val70 = 1;
    FirstAnydata val71 = 1;
    SecondAnydata val72 = checkpanic val71.cloneWithType();
    ThirdAnydata val73 = checkpanic val72.cloneWithType();
    ThirdAnydata val74 = checkpanic val70.cloneWithType();
    test:assertValueEqual((typeof val72).toString(), "typedesc 1");
    test:assertValueEqual((typeof val73).toString(), "typedesc 1");
    test:assertValueEqual((typeof val74).toString(), "typedesc 1");

    int[] val75 = [1, 2, 3];
    FirstFloat[] val76 = [1.0, 2.0, 3.0];
    SixthArrayUnion val77 = checkpanic val75.cloneWithType();
    SixthArrayUnion val78 = checkpanic val76.cloneWithType();
    test:assertValueEqual((typeof val77).toString(), "typedesc ThirdArr");
    test:assertValueEqual((typeof val78).toString(), "typedesc ThirdArr");

    decimal val79 = 1.0d;
    FirstDecimal val80 = 1.0d;
    SixthNumberUnion|decimal val81 = checkpanic val79.cloneWithType();
    SixthNumberUnion val82 = checkpanic val80.cloneWithType();
    test:assertValueEqual((typeof val81).toString(), "typedesc 1.0");
    test:assertValueEqual((typeof val82).toString(), "typedesc 1");
}

type union_with_int string|float|byte|int:Unsigned8|int:Signed8|int;

function testConvertToUnionWithAmbiguousMemberTypes() {
    json j = {x: 1};
    record {float x;}|record {int x;} v1 = checkpanic j.cloneWithType();
    test:assertValueEqual(v1, {"x": 1.0});

    j = {y: {x: 1}};
    record {record {float x;}|record {int x;} y;} v2 = checkpanic j.cloneWithType();
    test:assertValueEqual(v2, {"y": {"x": 1.0}});

    j = {x: 1, y: 2};
    record {float x; float|int y;}|record {int x; decimal y;} v3 = checkpanic j.cloneWithType();
    test:assertValueEqual(v3, {"x": 1.0, "y": 2});

    float x = 12.0;
    int|float y1 = checkpanic x.cloneWithType();
    test:assertValueEqual(y1, 12.0);

    j = {x: 6.0, y: 12};
    record {int|float x; float y;} y2 = checkpanic j.fromJsonWithType();
    test:assertValueEqual(y2, {"x": 6.0, "y": 12.0});

    j = {x: 6.0, y: 12};
    record {int|float x; int y;} y3 = checkpanic j.fromJsonWithType();
    test:assertValueEqual(y3, {"x": 6.0, "y": 12});

    -127 val1 = -127;
    union_with_int val1_ans = checkpanic val1.fromJsonWithType();
    test:assertValueEqual(val1_ans, -127);

    j = {x: 1};
    record {float|int x;}|record {float x;}|record {int x;} val2_ans = checkpanic j.cloneWithType();
    test:assertValueEqual(val2_ans, {x: 1});

    2 val3 = 2;
    1|2.0|2.0d|int|2 val3_ans = checkpanic val3.fromJsonWithType();
    test:assertValueEqual(val3_ans, 2);

    1|2.0|2.0d|2 val3_ans2 = checkpanic val3.fromJsonWithType();
    test:assertValueEqual(val3_ans2, 2);

    1|2.0d|2.0 val3_ans3 = checkpanic val3.fromJsonWithType();
    test:assertValueEqual(val3_ans3, 2.0d);

    1|2.0 val3_ans4 = checkpanic val3.cloneWithType();
    test:assertValueEqual(val3_ans4, 2.0);
}

type Student4 record {
    int id;
    xml x;
};

function testCloneWithTypeRecordWithXMLField() {
    Student4 student = {id: 1, x: xml `<book>DJ</book>`};
    json j = <json>student.toJson();
    Student4|error ss = j.cloneWithType(Student4);
    assertEquality(ss is Student4, true);
}

type ArrayOnRecordField record {
    anydata[] value;
};

type ReadOnlyAnydata anydata & readonly;

function testCloneWithTypeTableToAnydata() {
    table<record {readonly int 'key; string value;}> key('key) tableVal = table key('key) [
        {'key: 1, value: "foo"}
        ];

    anydata|(anydata & readonly) tableValCloned = checkpanic tableVal.cloneWithType(anydata);
    test:assertValueEqual(tableValCloned, table [{"key": 1, "value": "foo"}]);
    test:assertValueEqual(tableValCloned is readonly, false);

    anydata|(anydata & readonly) tableValCloned2 = checkpanic tableVal.cloneWithType(ReadOnlyAnydata);
    test:assertValueEqual(tableValCloned2, table [{"key": 1, "value": "foo"}]);
    test:assertValueEqual(tableValCloned2 is readonly, true);

    any result;
    table<record {int 'key; readonly string value;}> key(value) tableVal2 = checkpanic tableValCloned.cloneWithType();
    test:assertValueEqual(tableVal2, table key(value) [{"key": 1, "value": "foo"}]);
    result = tableVal2;
    test:assertValueEqual(result is readonly, false);

    table<record {int 'key; readonly string value;}> key(value) tableVal3 = checkpanic tableValCloned2.cloneWithType();
    test:assertValueEqual(tableVal3, table key(value) [{"key": 1, "value": "foo"}]);
    result = tableVal3;
    test:assertValueEqual(result is readonly, false);

    table<record {int 'key; readonly string value;}> key(value) & readonly tableVal4 =
        checkpanic tableValCloned.cloneWithType();
    test:assertValueEqual(tableVal4, table key(value) [{"key": 1, "value": "foo"}]);
    result = tableVal4;
    test:assertValueEqual(result is readonly, true);

    table<record {int 'key; readonly string value;}> key(value) & readonly tableVal5 =
        checkpanic tableValCloned2.cloneWithType();
    test:assertValueEqual(tableVal5, table key(value) [{"key": 1, "value": "foo"}]);
    result = tableVal5;
    test:assertValueEqual(result is readonly, true);

    anydata rec = {
        value: [
            (),
            true,
            123,
            123.45,
            123.45d,
            "s3cr3t",
            xml `<book>The Lost World</book>`,
            {foo: "bar"},
            table [
                {'key: 1, value: "foo"}
                ]
        ]
    };

    ArrayOnRecordField recCloned = checkpanic rec.cloneWithType();
    test:assertValueEqual(recCloned, <ArrayOnRecordField>{
        "value": [
            (),
            true,
            123,
            123.45,
            123.45d,
            "s3cr3t",
            xml `<book>The Lost World</book>`,
            {"foo": "bar"},
            table [{"key": 1, "value": "foo"}]
        ]
    });
    result = recCloned;
    test:assertValueEqual(result is readonly, false);

    ArrayOnRecordField & readonly recCloned2 = checkpanic rec.cloneWithType();
    test:assertValueEqual(recCloned2, <ArrayOnRecordField>{
        "value": [
            (),
            true,
            123,
            123.45,
            123.45d,
            "s3cr3t",
            xml `<book>The Lost World</book>`,
            {"foo": "bar"},
            table [{"key": 1, "value": "foo"}]
        ]
    });
    result = recCloned2;
    test:assertValueEqual(result is readonly, true);
}

function testCloneWithTypeToArrays() {
    string[2] lastNames = ["Alice", "Bob"];
    string[] firstNames = ["Alice", "Bob"];

    string[0]|error names = lastNames.cloneWithType();
    test:assertTrue(names is error);
    if (names is error) {
        test:assertValueEqual("{ballerina/lang.value}ConversionError", names.message());
        test:assertValueEqual("'string[2]' value cannot be converted to 'string[0]': \n\t\t" +
        "element count exceeds the target array size '0'", <string>checkpanic names.detail()["message"]);
    }

    names = firstNames.cloneWithType();
    test:assertTrue(names is error);
    if (names is error) {
        test:assertValueEqual("{ballerina/lang.value}ConversionError", names.message());
        test:assertValueEqual("'string[]' value cannot be converted to 'string[0]': \n\t\t" +
        "element count exceeds the target array size '0'", <string>checkpanic names.detail()["message"]);
    }
}

type xml1 xml:ProcessingInstruction|xml:Text;

type xml2 xml:Element & readonly;

type xml3 xml:Element;

type xml4 xml3;

type xml5 xml<xml:ProcessingInstruction|xml:Text|xml:Comment>;

type xml6 xml<xml:Element|(xml:ProcessingInstruction & readonly)|(xml:Comment & readonly)>;

type xml7 xml<xml5|xml6> & readonly;

type BigXml xml<xml1|xml2|xml4>;

type ReadOnlyXmlElement xml:Element & readonly;

type AnydataUnion int|anydata;

type JsonUnion xml|json;

type JsonUnion2 BigXml|json;

function testCloneWithTypeXmlToUnion() {
    xml z = xml `<a>1</a>`;
    xml:Element|json z0 = checkpanic z.cloneWithType();
    any result = z0;
    test:assertValueEqual(result is readonly, false);
    test:assertValueEqual(z0, xml `<a>1</a>`);

    xml7 z00 = checkpanic z.cloneWithType();
    result = z00;
    test:assertValueEqual(result is readonly, true);
    test:assertValueEqual(z00, xml `<a>1</a>`);

    xml|json z1 = checkpanic z.cloneWithType();
    result = z1;
    test:assertValueEqual(result is readonly, false);
    test:assertValueEqual(z1, xml `<a>1</a>`);

    JsonUnion & readonly z2 = checkpanic z.cloneWithType();
    result = z2;
    test:assertValueEqual(result is readonly, true);
    test:assertValueEqual(z2, xml `<a>1</a>`);

    JsonUnion z3 = checkpanic z.cloneWithType();
    result = z3;
    test:assertValueEqual(result is readonly, false);
    test:assertValueEqual(z3, xml `<a>1</a>`);

    JsonUnion2 z4 = checkpanic z.cloneWithType();
    result = z4;
    test:assertValueEqual(result is readonly, false);
    test:assertValueEqual(z4, xml `<a>1</a>`);

    JsonUnion2 & readonly z5 = checkpanic z.cloneWithType();
    result = z5;
    test:assertValueEqual(result is readonly, true);
    test:assertValueEqual(z5, xml `<a>1</a>`);

    xml:Comment|json|error z6 = z.cloneWithType();
    test:assertValueEqual(z6 is error, true);
    if (z6 is error) {
        test:assertValueEqual("{ballerina/lang.value}ConversionError", z6.message());
        test:assertValueEqual("'lang.xml:Element' value cannot be converted to '(lang.xml:Comment|json)'",
        <string>checkpanic z6.detail()["message"]);
    }

    xml xmlComment = xml `<!-- xml comment -->`;
    JsonUnion2|error z7 = xmlComment.cloneWithType();
    test:assertValueEqual(z7 is error, true);
    if (z7 is error) {
        test:assertValueEqual("{ballerina/lang.value}ConversionError", z7.message());
        test:assertValueEqual("'lang.xml:Comment' value cannot be converted to 'JsonUnion2'",
        <string>checkpanic z7.detail()["message"]);
    }

    xml x1 = xml `<book>The Lost World</book><!-- xml comment -->`;
    xml<xml:Comment|xml:Element> y1 = checkpanic x1.cloneWithType();
    result = y1;
    test:assertValueEqual(result is readonly, false);
    test:assertValueEqual(y1, x1);

    xml<xml:Comment|xml:Element> & readonly y2 = checkpanic x1.cloneWithType();
    result = y2;
    test:assertValueEqual(result is readonly, true);
    test:assertValueEqual(y2, x1);

    xml<xml:Comment|(xml:Element & readonly)> y3 = checkpanic x1.cloneWithType();
    result = y3;
    test:assertValueEqual(result is readonly, false);
    test:assertValueEqual(y3, x1);

    xml x2 = xml `<?xml version="1.0" encoding="UTF-8"?><book>The Lost World</book>`;
    xml<xml:Comment|xml:Element>|error y4 = x2.cloneWithType();
    test:assertValueEqual(y4 is error, true);
    if (y4 is error) {
        test:assertValueEqual("{ballerina/lang.value}ConversionError", y4.message());
        test:assertValueEqual("'xml<(lang.xml:Element|lang.xml:Comment|lang.xml:ProcessingInstruction|lang.xml:Text)>'"
        + " value cannot be converted to 'xml<(lang.xml:Comment|lang.xml:Element)>'",
        <string>checkpanic y4.detail()["message"]);
    }

    (xml<xml:Comment|xml:Element> & readonly)|error y5 = x2.cloneWithType();
    test:assertValueEqual(y5 is error, true);
    if (y5 is error) {
        test:assertValueEqual("{ballerina/lang.value}ConversionError", y5.message());
        test:assertValueEqual("'xml<(lang.xml:Element|lang.xml:Comment|lang.xml:ProcessingInstruction|lang.xml:Text)>'"
        + " value cannot be converted to '(xml<(lang.xml:Comment|lang.xml:Element)> & readonly)'",
        <string>checkpanic y5.detail()["message"]);
    }

    xml<xml:Comment|(xml:Element & readonly)>|error y6 = x2.cloneWithType();
    test:assertValueEqual(y6 is error, true);
    if (y6 is error) {
        test:assertValueEqual("{ballerina/lang.value}ConversionError", y6.message());
        test:assertValueEqual("'xml<(lang.xml:Element|lang.xml:Comment|lang.xml:ProcessingInstruction|lang.xml:Text)>'"
        + " value cannot be converted to 'xml<(lang.xml:Comment|(lang.xml:Element & readonly))>'",
        <string>checkpanic y6.detail()["message"]);
    }
}

function testConversionsBetweenXml() {
    // xml to xml
    xml:Element x1 = xml `<book>The Lost World</book>`;
    xml:Element x1_cloned1 = checkpanic x1.cloneWithType(xml:Element);
    test:assertValueEqual(x1_cloned1 == x1, true);
    test:assertValueEqual(x1_cloned1 === x1, false);
    xml child_x1 = xml `<author>Sir Arthur Conan Doyle</author>`;
    x1.setChildren(child_x1);
    test:assertValueEqual(x1, xml `<book><author>Sir Arthur Conan Doyle</author></book>`);
    test:assertValueEqual(x1_cloned1, xml `<book>The Lost World</book>`);
    anydata|(anydata & readonly) x1_cloned_readonly = checkpanic x1.cloneWithType(ReadOnlyXmlElement);
    test:assertValueEqual(x1_cloned_readonly, xml `<book><author>Sir Arthur Conan Doyle</author></book>`);
    test:assertValueEqual(x1_cloned_readonly is readonly, true);

    xml x2 = xml `<book>The Lost World</book>`;
    xml:Element x1_cloned5 = checkpanic x2.cloneWithType();
    test:assertValueEqual(x1_cloned5, xml `<book>The Lost World</book>`);

    xml:ProcessingInstruction|error x1_cloned6 = x2.cloneWithType();
    test:assertValueEqual(x1_cloned6 is error, true);
    if (x1_cloned6 is error) {
        test:assertValueEqual("{ballerina/lang.value}ConversionError", x1_cloned6.message());
        test:assertValueEqual("'lang.xml:Element' value cannot be converted to 'lang.xml:ProcessingInstruction'",
        <string>checkpanic x1_cloned6.detail()["message"]);
    }

    xml:Element & readonly x3 = xml `<foo>bar</foo>`;
    xml:Element x3_cloned1_val = checkpanic x3.cloneWithType();
    anydata x3_cloned1 = x3_cloned1_val;
    test:assertValueEqual(x3_cloned1 === x3, false);
    test:assertValueEqual(x3_cloned1 is xml:ProcessingInstruction, false);
    test:assertValueEqual(x3_cloned1 is xml:Comment, false);
    test:assertValueEqual(x3_cloned1 is xml:Text, false);

    xml:Element & readonly x3_cloned2_val = checkpanic x3_cloned1.cloneWithType();
    anydata|(anydata & readonly) x3_cloned2 = x3_cloned2_val;
    test:assertValueEqual(x3_cloned2 === x3 || x3_cloned2 === x3_cloned1, false);
    test:assertValueEqual(x3_cloned2 is readonly, true);
    test:assertValueEqual(x3_cloned2 is xml:ProcessingInstruction, false);
    test:assertValueEqual(x3_cloned2 is xml:Comment, false);
    test:assertValueEqual(x3_cloned2 is xml:Text, false);

    xml x4 = xml `<!--I am a comment-->`;
    xml:Comment x4_cloned1_val = checkpanic x4.cloneWithType();
    anydata|(anydata & readonly) x4_cloned1 = x4_cloned1_val;
    test:assertValueEqual(x4_cloned1 === x4, false);
    test:assertValueEqual(x4_cloned1 is xml:ProcessingInstruction, false);
    test:assertValueEqual(x4_cloned1 is xml:Element, false);
    test:assertValueEqual(x4_cloned1 is xml:Text, false);

    xml:ProcessingInstruction x5 = xml `<?target data?>`;
    xml x5_cloned1 = checkpanic x5.cloneWithType();
    anydata|(anydata & readonly) x5_cloned2 = x5_cloned1;
    test:assertValueEqual(x5_cloned1 === x5, false);
    test:assertValueEqual(x5_cloned1 is xml:Comment, false);
    test:assertValueEqual(x5_cloned1 is xml:Element, false);
    test:assertValueEqual(x5_cloned1 is xml:Text, false);
    test:assertValueEqual(x5_cloned1 is xml:ProcessingInstruction, true);
    test:assertValueEqual(x5_cloned2 is readonly, false);

    xml:Text x6 = xml `Hello, world!`;
    xml & readonly x6_cloned1_val = checkpanic x6.cloneWithType();
    anydata|(anydata & readonly) x6_cloned1 = x6_cloned1_val;
    test:assertValueEqual(x6_cloned1 === x6, true);
    test:assertValueEqual(x6_cloned1 is xml:Comment, false);
    test:assertValueEqual(x6_cloned1 is xml:Element, false);
    test:assertValueEqual(x6_cloned1 is xml:ProcessingInstruction, false);
    test:assertValueEqual(x6_cloned1 is xml:Text, true);
    test:assertValueEqual(x6_cloned1 is readonly, true);

    // xml with anydata
    xml a = xml `<a>1</a>`;
    AnydataUnion & readonly a1 = checkpanic a.cloneWithType();
    any result = a1;
    test:assertValueEqual(result is readonly, true);
    test:assertValueEqual(a1, xml `<a>1</a>`);
    AnydataUnion a2 = checkpanic a.cloneWithType();
    result = a2;
    test:assertValueEqual(result is readonly, false);
    test:assertValueEqual(a2, xml `<a>1</a>`);

    anydata & readonly x1_cloned2 = checkpanic x1.cloneWithType();
    result = x1_cloned2;
    test:assertValueEqual(result is readonly, true);
    anydata x1_cloned3 = checkpanic x1.cloneWithType();
    result = x1_cloned3;
    test:assertValueEqual(result is readonly, false);
    anydata x1_cloned4 = checkpanic x1_cloned2.cloneWithType();
    result = x1_cloned4;
    test:assertValueEqual(result is readonly, false);

    anydata val = xml `<book>The Lost World</book>`;
    anydata|(anydata & readonly) res;
    xml:Element & readonly val_element_readonly = checkpanic val.cloneWithType();
    res = val_element_readonly;
    test:assertValueEqual(res is readonly, true);
    test:assertValueEqual(val_element_readonly === val, false);
    (xml:ProcessingInstruction & readonly)|error val_pi_readonly = val.cloneWithType();
    test:assertValueEqual(val_pi_readonly is error, true);
    if (val_pi_readonly is error) {
        test:assertValueEqual("{ballerina/lang.value}ConversionError", val_pi_readonly.message());
        test:assertValueEqual("'lang.xml:Element' value cannot be converted to '(lang.xml:ProcessingInstruction " +
        "& readonly)'", <string>checkpanic val_pi_readonly.detail()["message"]);
    }

    anydata|(anydata & readonly) val2 = checkpanic val.cloneWithType(anydata);
    test:assertValueEqual(val2 is readonly, false);
    test:assertValueEqual(val2 === val, false);
    anydata & readonly val2_clone_readonly = checkpanic val.cloneWithType();
    res = val2_clone_readonly;
    test:assertValueEqual(res is readonly, true);
    test:assertValueEqual(val2_clone_readonly == val, true);
    anydata|(anydata & readonly) val2_2 = checkpanic val2_clone_readonly.cloneWithType(anydata);
    test:assertValueEqual(val2_2 is readonly, false);
    test:assertValueEqual(val2_2 === val2_clone_readonly, false);
    anydata & readonly val2_2_readonly = checkpanic val2_clone_readonly.cloneWithType();
    res = val2_2_readonly;
    test:assertValueEqual(res is readonly, true);
    test:assertValueEqual(res == val2_clone_readonly, true);

    xml:Element val3 = xml `<book>The Lost World</book>`;
    anydata val3_cloned1 = checkpanic val3.cloneWithType();
    res = val3_cloned1;
    test:assertValueEqual(res is readonly, false);
    test:assertValueEqual(val3 === val3_cloned1, false);
    test:assertValueEqual(val3 == val3_cloned1, true);

    xml:Element & readonly val4 = xml `<book>The Lost World</book>`;
    anydata val4_cloned1 = checkpanic val4.cloneWithType();
    res = val4_cloned1;
    test:assertValueEqual(res is readonly, false);
    test:assertValueEqual(val4 === val4_cloned1, false);
    test:assertValueEqual(val4 == val4_cloned1, true);

    // xml with strings
    string y1 = string `<book>The Lost World</book>`;
    string y2 = string `<?target data?>`;
    string y3 = string `<!--I am a comment-->`;
    string y4 = string `Hello, world!`;
    string y5 = "";

    xml y1_val0 = checkpanic y1.cloneWithType();
    test:assertValueEqual(y1_val0, xml `<book>The Lost World</book>`);
    test:assertValueEqual(y1_val0 is xml:Element, true);
    test:assertValueEqual(y1_val0 is xml:ProcessingInstruction, false);
    xml y2_val0 = checkpanic y2.cloneWithType();
    test:assertValueEqual(y2_val0, xml `<?target data?>`);
    test:assertValueEqual(y2_val0 is xml:Element, false);
    test:assertValueEqual(y2_val0 is xml:ProcessingInstruction, true);
    xml y3_val0 = checkpanic y3.cloneWithType();
    test:assertValueEqual(y3_val0, xml `<!--I am a comment-->`);
    test:assertValueEqual(y3_val0 is xml:Element, false);
    test:assertValueEqual(y3_val0 is xml:Comment, true);
    xml y4_val0 = checkpanic y4.cloneWithType();
    test:assertValueEqual(y4_val0, xml `Hello, world!`);
    test:assertValueEqual(y4_val0 is xml:Element, false);
    test:assertValueEqual(y4_val0 is xml:Text, true);
    xml y5_val0 = checkpanic y5.cloneWithType();
    test:assertValueEqual(y5_val0, xml ``);
    test:assertValueEqual(y5_val0 is xml:Element, false);
    test:assertValueEqual(y5_val0 is xml:Text, true);

    xml:Element y1_val1 = checkpanic y1.cloneWithType();
    test:assertValueEqual(y1_val1, xml `<book>The Lost World</book>`);
    test:assertValueEqual(y1_val1 === xml `<book>The Lost World</book>`, false);

    xml:ProcessingInstruction|error y1_val2 = y1.cloneWithType();
    test:assertValueEqual(y1_val2 is error, true);
    if (y1_val2 is error) {
        test:assertValueEqual("{ballerina/lang.value}ConversionError", y1_val2.message());
        test:assertValueEqual("'string' value cannot be converted to 'lang.xml:ProcessingInstruction'",
        <string>checkpanic y1_val2.detail()["message"]);
    }

    xml:Comment|error y1_val3 = y1.cloneWithType();
    test:assertValueEqual(y1_val3 is error, true);
    if (y1_val3 is error) {
        test:assertValueEqual("{ballerina/lang.value}ConversionError", y1_val3.message());
        test:assertValueEqual("'string' value cannot be converted to 'lang.xml:Comment'",
        <string>checkpanic y1_val3.detail()["message"]);
    }

    xml:Text|error y1_val4 = y1.cloneWithType();
    test:assertValueEqual(y1_val4 is error, true);
    if (y1_val4 is error) {
        test:assertValueEqual("{ballerina/lang.value}ConversionError", y1_val4.message());
        test:assertValueEqual("'string' value cannot be converted to 'lang.xml:Text'",
        <string>checkpanic y1_val4.detail()["message"]);
    }

    xml:Element|error y4_val1 = y4.cloneWithType();
    test:assertValueEqual(y4_val1 is error, true);
    if (y4_val1 is error) {
        test:assertValueEqual("{ballerina/lang.value}ConversionError", y4_val1.message());
        test:assertValueEqual("'string' value cannot be converted to 'lang.xml:Element'",
        <string>checkpanic y4_val1.detail()["message"]);
    }

    xml:ProcessingInstruction y2_val1 = checkpanic y2.cloneWithType();
    test:assertValueEqual(y2_val1, xml `<?target data?>`);
    test:assertValueEqual(y2_val1 === xml `<?target data?>`, false);
    xml:Comment y3_val1 = checkpanic y3.cloneWithType();
    test:assertValueEqual(y3_val1, xml `<!--I am a comment-->`);
    test:assertValueEqual(y3_val1 === xml `<!--I am a comment-->`, false);
    xml:Text y4_val2 = checkpanic y4.cloneWithType();
    test:assertValueEqual(y4_val2, xml `Hello, world!`);
    test:assertValueEqual(y4_val2 === xml `Hello, world!`, true);
    xml:Text y5_val1 = checkpanic y5.cloneWithType();
    test:assertValueEqual(y5_val1, xml ``);
    test:assertValueEqual(y5_val1 === xml ``, true);
    xml y5_val = checkpanic y5.cloneWithType();
    test:assertValueEqual(y5_val, xml ``);
    test:assertValueEqual(y5_val === xml ``, true);

    xml:ProcessingInstruction|error y5_val2 = y5.cloneWithType();
    test:assertValueEqual(y5_val2 is error, true);
    if (y5_val2 is error) {
        test:assertValueEqual("{ballerina/lang.value}ConversionError", y5_val2.message());
        test:assertValueEqual("'string' value cannot be converted to 'lang.xml:ProcessingInstruction'",
        <string>checkpanic y5_val2.detail()["message"]);
    }

    xml:Comment|error y5_val3 = y5.cloneWithType();
    test:assertValueEqual(y5_val3 is error, true);
    if (y5_val3 is error) {
        test:assertValueEqual("{ballerina/lang.value}ConversionError", y5_val3.message());
        test:assertValueEqual("'string' value cannot be converted to 'lang.xml:Comment'",
        <string>checkpanic y5_val3.detail()["message"]);
    }

    xml:Element|error y5_val4 = y5.cloneWithType();
    test:assertValueEqual(y5_val4 is error, true);
    if (y5_val4 is error) {
        test:assertValueEqual("{ballerina/lang.value}ConversionError", y5_val4.message());
        test:assertValueEqual("'string' value cannot be converted to 'lang.xml:Element'",
        <string>checkpanic y5_val4.detail()["message"]);
    }
}

type Assertion [string, string];

type BoundAssertion ["let", int];

type UnionTuple Assertion|BoundAssertion;

type Table1 table<map<int>>;

type Table2 table<map<string>>;

type UnionTable Table1|Table2;

type UnionTable2 UnionTable & readonly;

type NewEmployee record {
    readonly string name;
    int salary;
};

type OldEmployee record {|
    readonly string name;
    int salary;
|};

type NewestEmployeee table<NewEmployee> key(name);

function testCloneWithTypeToUnionOfTypeReference() {
    anydata[] arrValue = ["let", 3];

    BoundAssertion|error t1 = arrValue.cloneWithType();
    assertFalse(t1 is error);
    assertEquality(t1, <BoundAssertion>["let", 3]);

    Assertion|BoundAssertion|error t2 = arrValue.cloneWithType();
    assertFalse(t2 is error);
    assertTrue(t2 is BoundAssertion);
    assertEquality(t2, <BoundAssertion>["let", 3]);

    UnionTuple|error t3 = arrValue.cloneWithType();
    assertFalse(t3 is error);
    assertTrue(t3 is BoundAssertion);
    assertEquality(t3, <BoundAssertion>["let", 3]);

    table<map<anydata>> tab = table [{a: "a", b: "b"}];
    Table1|Table2|error t4 = tab.cloneWithType();
    assertFalse(t4 is error);
    assertTrue(t4 is Table2);
    assertEquality(t4, <Table2>table [{a: "a", b: "b"}]);

    UnionTable|error t5 = tab.cloneWithType();
    assertFalse(t5 is error);
    assertTrue(t5 is Table2);
    assertEquality(t5, <Table2>table [{a: "a", b: "b"}]);

    map<json>[] tab2 = [{a: "a", b: "b"}];
    UnionTable2|error t10 = tab2.cloneWithType();
    assertTrue(t10 is UnionTable2);
    assertEquality(t10, <UnionTable2>table [{a: "a", b: "b"}]);

    table<map<json>> tab3 = table [{a: "a", b: "b"}];
    UnionTable2|error t11 = tab3.cloneWithType();
    assertTrue(t11 is UnionTable2);
    assertEquality(t11, <UnionTable2>table [{a: "a", b: "b"}]);

    [record {|string name; int salary;|}, OldEmployee, NewEmployee, map<anydata>...] listResult = [
        {name: "John", salary: 100},
        {name: "Jane", salary: 200},
        {name: "Jean", salary: 300},
        {name: "Joe", salary: 400}
    ];

    table<OldEmployee> key(name) oldEmployees = checkpanic listResult.cloneWithType();
    assertEquality(oldEmployees, <table<OldEmployee> key(name)>table [
        {name: "John", salary: 100},
        {name: "Jane", salary: 200},
        {name: "Jean", salary: 300},
        {name: "Joe", salary: 400}
    ]);

    NewestEmployeee newestEmployees = checkpanic listResult.cloneWithType();
    assertEquality(newestEmployees, <NewestEmployeee>table [
        {name: "John", salary: 100},
        {name: "Jane", salary: 200},
        {name: "Jean", salary: 300},
        {name: "Joe", salary: 400}
    ]);
}

function testCloneWithTypeToTableNegative() {
    table<map<anydata>> tab = table [{a: true, b: 10, c: (), d: "10", e: false}];
    Table1|Table2|error t1 = tab.cloneWithType();
    assertTrue(t1 is error);
    string errMsgPrefix = "'table<map<anydata>>' value cannot be converted to '(Table1|Table2)': ";
    string errMsgSuffix = "\n\t\t{" +
    "\n\t\t  map field '[0].a' should be of type 'int', found 'true'" +
    "\n\t\t  map field '[0].c' should be of type 'int', found '()'" +
    "\n\t\t  map field '[0].d' should be of type 'int', found '\"10\"'" +
    "\n\t\t  map field '[0].e' should be of type 'int', found 'false'" +
    "\n\t\tor" +
    "\n\t\t  map field '[0].a' should be of type 'string', found 'true'" +
    "\n\t\t  map field '[0].b' should be of type 'string', found '10'" +
    "\n\t\t  map field '[0].c' should be of type 'string', found '()'" +
    "\n\t\t  map field '[0].e' should be of type 'string', found 'false'" +
    "\n\t\t}";
    string errMsg = errMsgPrefix + errMsgSuffix;
    if (t1 is error) {
        assertEquality("{ballerina/lang.value}ConversionError", t1.message());
        assertEquality(errMsg, <string>checkpanic t1.detail()["message"]);
    }

    UnionTable|error t2 = tab.cloneWithType();
    assertTrue(t2 is error);
    errMsg = "'table<map<anydata>>' value cannot be converted to 'UnionTable': " + errMsgSuffix;
    if (t2 is error) {
        assertEquality("{ballerina/lang.value}ConversionError", t2.message());
        assertEquality(errMsg, <string>checkpanic t2.detail()["message"]);
    }

    map<json>[] tab2 = [{a: true, b: 10, c: (), d: "10", e: false}];
    UnionTable2|error t3 = tab2.cloneWithType();
    if (t3 is error) {
        assertEquality("{ballerina/lang.value}ConversionError", t3.message());
        assertEquality("'map<json>[]' value cannot be converted to 'UnionTable2': " + errMsgSuffix,
        <string>checkpanic t3.detail()["message"]);
    }

    table<map<anydata>> tab3 = table [
        {a: true},
        {b: 10},
        {c: ()},
        {d: "10", e: false}
    ];
    Table1|Table2|error t4 = tab3.cloneWithType();
    assertTrue(t4 is error);
    errMsgSuffix = "\n\t\t{" +
    "\n\t\t  map field '[0].a' should be of type 'int', found 'true'" +
    "\n\t\t  map field '[2].c' should be of type 'int', found '()'" +
    "\n\t\t  map field '[3].d' should be of type 'int', found '\"10\"'" +
    "\n\t\t  map field '[3].e' should be of type 'int', found 'false'" +
    "\n\t\tor" +
    "\n\t\t  map field '[0].a' should be of type 'string', found 'true'" +
    "\n\t\t  map field '[1].b' should be of type 'string', found '10'" +
    "\n\t\t  map field '[2].c' should be of type 'string', found '()'" +
    "\n\t\t  map field '[3].e' should be of type 'string', found 'false'" +
    "\n\t\t}";
    errMsg = errMsgPrefix + errMsgSuffix;
    if (t4 is error) {
        assertEquality("{ballerina/lang.value}ConversionError", t4.message());
        assertEquality(errMsg, <string>checkpanic t4.detail()["message"]);
    }

    UnionTable|error t5 = tab3.cloneWithType();
    assertTrue(t5 is error);
    errMsg = "'table<map<anydata>>' value cannot be converted to 'UnionTable': " + errMsgSuffix;
    if (t5 is error) {
        assertEquality("{ballerina/lang.value}ConversionError", t5.message());
        assertEquality(errMsg, <string>checkpanic t5.detail()["message"]);
    }

    map<json>[] tab4 = [{a: true}, {b: 10, c: ()}, {d: "10", e: false}];
    UnionTable2|error t6 = tab4.cloneWithType();
    errMsgSuffix = "\n\t\t{" +
    "\n\t\t  map field '[0].a' should be of type 'int', found 'true'" +
    "\n\t\t  map field '[1].c' should be of type 'int', found '()'" +
    "\n\t\t  map field '[2].d' should be of type 'int', found '\"10\"'" +
    "\n\t\t  map field '[2].e' should be of type 'int', found 'false'" +
    "\n\t\tor" +
    "\n\t\t  map field '[0].a' should be of type 'string', found 'true'" +
    "\n\t\t  map field '[1].b' should be of type 'string', found '10'" +
    "\n\t\t  map field '[1].c' should be of type 'string', found '()'" +
    "\n\t\t  map field '[2].e' should be of type 'string', found 'false'" +
    "\n\t\t}";
    if (t6 is error) {
        assertEquality("{ballerina/lang.value}ConversionError", t6.message());
        assertEquality("'map<json>[]' value cannot be converted to 'UnionTable2': " + errMsgSuffix,
        <string>checkpanic t6.detail()["message"]);
    }

    [record {string name; int salary;}, OldEmployee, NewEmployee, map<anydata>...] listResult = [
        {name: "John", salary: 100, "age": 20},
        {name: "Jane", salary: 200},
        {name: "Jean", salary: 300, "qualified": true},
        {name: 11, salary: 300, "qualified": true},
        {salary: 300}
    ];

    table<OldEmployee> key(name)|error oldEmployees = listResult.cloneWithType();
    errMsg = "'[record {| string name; int salary; anydata...; |},OldEmployee,NewEmployee,map<anydata>...]' value " +
    "cannot be converted to 'table<OldEmployee> key(name)': " +
    "\n\t\tfield '[0].age' cannot be added to the closed record 'OldEmployee'" +
    "\n\t\tfield '[2].qualified' cannot be added to the closed record 'OldEmployee'" +
    "\n\t\tfield '[3].name' in record 'OldEmployee' should be of type 'string', found '11'" +
    "\n\t\tfield '[3].qualified' cannot be added to the closed record 'OldEmployee'" +
    "\n\t\tmissing required field '[4].name' of type 'string' in record 'OldEmployee'";
    if (oldEmployees is error) {
        assertEquality("{ballerina/lang.value}ConversionError", oldEmployees.message());
        assertEquality(errMsg, <string>checkpanic oldEmployees.detail()["message"]);
    }

    NewestEmployeee|error newestEmployees = listResult.cloneWithType();
    errMsg = "'[record {| string name; int salary; anydata...; |},OldEmployee,NewEmployee,map<anydata>...]' value " +
    "cannot be converted to 'NewestEmployeee': " +
    "\n\t\tfield '[3].name' in record 'NewEmployee' should be of type 'string', found '11'" +
    "\n\t\tmissing required field '[4].name' of type 'string' in record 'NewEmployee'";
    if (newestEmployees is error) {
        assertEquality("{ballerina/lang.value}ConversionError", newestEmployees.message());
        assertEquality(errMsg, <string>checkpanic newestEmployees.detail()["message"]);
    }
}

type A1 record {
    int a;
};

type AB A1|int;

type AC A1|string;

function testCloneWithTypeToRecordWithIntersectingUnionMembers() {
    // `AB & AC` is `A1`
    map<anydata> x = {a: "Ballerina", b: true};
    AB|AC|boolean|error z = x.cloneWithType();
    assertTrue(z is error);
    string errMsg = "'map<anydata>' value cannot be converted to '(AB|AC|boolean)': " +
                    "\n\t\t{" +
                    "\n\t\t  field 'a' in record 'A1' should be of type 'int', found '\"Ballerina\"'" +
                    "\n\t\t}";
    error errorZ = <error>z;
    assertEquality("{ballerina/lang.value}ConversionError", errorZ.message());
    assertEquality(errMsg, <string>checkpanic errorZ.detail()["message"]);
}

type GraphQLQuery record {|
    string operationName?;
    string query?;
    record {} variables?;
|};

type PositiveIntegersUpto5 1|2|3|4|5;

type GraphQLQueries0 PositiveIntegersUpto5[];

type GraphQLQueries1 GraphQLQuery[];

type GraphQLQueries1Further GraphQLQueries1;

type GraphQLQueries2 [int, GraphQLQuery...];

type GraphQLQueries3 map<GraphQLQuery>;

type GraphQLQueries4 record {|
    GraphQLQuery a;
|};

type GraphQLQueries5 table<GraphQLQuery>;

type GraphQLQueries6 GraphQLQueries5;

function testCloneWithTypeToJson() {
    GraphQLQueries0 payload0 = [1, 4, 2];
    GraphQLQueries1 payload1 = [{query: "abc"}, {query: "xyz"}];
    GraphQLQueries1Further payload1Further = [{query: "abc"}, {query: "xyz"}];
    GraphQLQueries2 payload2 = [1, {query: "abc"}, {query: "xyz"}];
    GraphQLQueries3 payload3 = {"a": {query: "abc"}, "b": {query: "xyz"}};
    GraphQLQueries4 payload4 = {a: {query: "abc"}};
    json stdJson0 = checkpanic payload0.cloneWithType();
    json & readonly stdJson00 = checkpanic payload0.cloneWithType();
    json stdJson1 = checkpanic payload1.cloneWithType();
    json stdJson1Further = checkpanic payload1Further.cloneWithType();
    json stdJson2 = checkpanic payload2.cloneWithType();
    json stdJson3 = checkpanic payload3.cloneWithType();
    json stdJson4 = checkpanic payload4.cloneWithType();

    assertEquality(stdJson0, <json>[1, 4, 2]);
    assertEquality(stdJson00, <json>[1, 4, 2]);
    assertEquality(stdJson1, <json>[{query: "abc"}, {query: "xyz"}]);
    assertEquality(stdJson1Further, <json>[{query: "abc"}, {query: "xyz"}]);
    assertEquality(stdJson2, <json>[1, {query: "abc"}, {query: "xyz"}]);
    assertEquality(stdJson3, <json>{"a": {query: "abc"}, "b": {query: "xyz"}});
    assertEquality(stdJson4, <json>{a: {query: "abc"}});

    GraphQLQueries1 payload5 = [{query: "abc"}, {query: "xyz", variables: {"nonJson": xml `<a>abc</a>`}}];
    GraphQLQueries1Further payload5Further = [{query: "abc"}, {query: "xyz", variables: {"nonJson": xml `<a>abc</a>`}}];
    GraphQLQueries2 payload6 = [1, {query: "abc"}, {query: "xyz", variables: {"nonJson": xml `<a>abc</a>`}}];
    GraphQLQueries3 payload7 = {"a": {query: "abc"}, "b": {query: "xyz", variables: {"nonJson": xml `<a>abc</a>`}}};
    GraphQLQueries4 payload8 = {a: {query: "abc", variables: {"nonJson": xml `<a>abc</a>`}}};
    table<GraphQLQuery> payload9 = table [
        {query: "abc"},
        {query: "xyz"}
    ];
    GraphQLQueries5 payload10 = table [
        {query: "abc"},
        {query: "xyz"}
    ];
    GraphQLQueries6 payload11 = table [
        {query: "abc"},
        {query: "xyz"}
    ];

    json|error stdJson5 = payload5.cloneWithType();
    assertEquality(stdJson5 is error, true);
    if (stdJson5 is error) {
        assertEquality("'GraphQLQueries1' value cannot be converted to 'json'",
        <string>checkpanic stdJson5.detail()["message"]);
    }
    json|error stdJson5Further = payload5Further.cloneWithType();
    assertEquality(stdJson5Further is error, true);
    if (stdJson5Further is error) {
        assertEquality("'GraphQLQueries1Further' value cannot be converted to 'json'",
        <string>checkpanic stdJson5Further.detail()["message"]);
    }
    json|error stdJson6 = payload6.cloneWithType();
    assertEquality(stdJson6 is error, true);
    if (stdJson6 is error) {
        assertEquality("'GraphQLQueries2' value cannot be converted to 'json'",
        <string>checkpanic stdJson6.detail()["message"]);
    }
    json|error stdJson7 = payload7.cloneWithType();
    assertEquality(stdJson7 is error, true);
    if (stdJson7 is error) {
        assertEquality("'GraphQLQueries3' value cannot be converted to 'json'",
        <string>checkpanic stdJson7.detail()["message"]);
    }
    json|error stdJson8 = payload8.cloneWithType();
    assertEquality(stdJson8 is error, true);
    if (stdJson8 is error) {
        assertEquality("'GraphQLQueries4' value cannot be converted to 'json'",
        <string>checkpanic stdJson8.detail()["message"]);
    }
    json|error stdJson9 = payload9.cloneWithType();
    assertEquality(stdJson9 is error, true);
    if (stdJson9 is error) {
        assertEquality("'table<GraphQLQuery>' value cannot be converted to 'json'",
        <string>checkpanic stdJson9.detail()["message"]);
    }
    json|error stdJson10 = payload10.cloneWithType();
    assertEquality(stdJson10 is error, true);
    if (stdJson10 is error) {
        assertEquality("'GraphQLQueries5' value cannot be converted to 'json'",
        <string>checkpanic stdJson10.detail()["message"]);
    }
    json|error stdJson11 = payload11.cloneWithType();
    assertEquality(stdJson11 is error, true);
    if (stdJson11 is error) {
        assertEquality("'GraphQLQueries6' value cannot be converted to 'json'",
        <string>checkpanic stdJson11.detail()["message"]);
    }
}

/////////////////////////// Tests for `toJson()` ///////////////////////////

type Student2 record {
    string name;
    int age;
};

function testToJsonWithRecord1() {
    Student2 s = {name: "Adam", age: 23};
    json|error j = s.toJson();
    assert(j is json, true);
}

function testToJsonWithRecord2() {
    Address1 addr1 = {country: "x", city: "y", "street": "z", "no": 3};
    json jsonaddr1 = addr1.toJson();
}

function testToJsonWithLiterals() {
    () aNil = ();
    string aString = "some string";
    int aNumber = 10;

    json|error aNilJson = aNil.toJson();
    assert(aNilJson is json, true);

    json|error aStringJson = aString.toJson();
    assert(aStringJson is json, true);

    json|error aNumberJson = aNumber.toJson();
    assert(aNumberJson is json, true);
}

function testToJsonWithArray() {
    string[] arrString = ["hello", "world"];
    json arrStringJson = arrString.toJson();
    assert(arrStringJson is json[], true);
    assert(<json[]>arrStringJson, <json[]>["hello", "world"]);
}

type Counter record {|
    int[] x = getX();
    int[] y = getY();
|};

isolated int counter = 0;

isolated function getX() returns int[] {
    lock {
        counter += 1;
    }
    return [1];
}

isolated function getY() returns int[] {
    lock {
        counter += 1;
    }
    return [2];
}

function testCloneWithTypeRecordDefaultValues() {
    map<anydata> m = {y: [3]};
    Counter n = checkpanic m.cloneWithType();
    lock {
        assertEquality(1, counter);
    }
    assertEquality((typeof n).toString(), "typedesc Counter");

    m = {y: <readonly>[3]};
    Counter & readonly n2 = checkpanic m.cloneWithType();
    lock {
        assertEquality(2, counter);
    }
    assertTrue(n2 is readonly);
    assertEquality((typeof n2).toString(), "typedesc {\"x\":[1],\"y\":[3]}");
}

type XmlType xml;

function testToJsonWithXML() {
    xml x1 = xml `<movie>
                    <title>Some</title>
                    <writer>Writer</writer>
                  </movie>`;
    json j = x1.toJson();
    xml x2 = checkpanic j.fromJsonWithType(XmlType);
    assert(<xml>x2, x1);

    x2 = checkpanic j.cloneWithType(XmlType);
    assert(<xml>x2, x1);

    map<anydata> m2 = {a: 1, b: x1};
    json j3 = m2.toJson();
}

type MapOfString map<string>;

function testToJsonWithMap() {
    map<string> m = {
        line1: "Line1",
        line2: "Line2"
    };
    json j = m.toJson();
    assert(j.toJsonString(), "{\"line1\":\"Line1\", \"line2\":\"Line2\"}");
    map<string> m2 = checkpanic j.fromJsonWithType(MapOfString);
    assert(<map<string>>m2, m);
}

function testToJsonWithMapInt() {
    map<int> m = {a: 1, b: 2};
    map<json> mj = <map<json>>m.toJson();
    mj["c"] = "non-int json";
}

function testToJsonWithStringArray() {
    string[] a = ["Hello", "World"];
    json j = a.toJson();
    assert(j.toJsonString(), "[\"Hello\", \"World\"]");
}

function testToJsonWithIntArray() {
    int[] a = [1, 2];
    json j = a.toJson();
    assert(j.toJsonString(), "[1, 2]");
}

type Boo record {|
    int id;
    string str;
|};

type RetEmployee record {
    readonly string id;
    string name;
    float salary;
};

type PersonTable table<RetEmployee> key(id);

function testToJsonWithTable() {
    table<Boo> tb = table [
        {id: 12, str: "abc"},
        {id: 34, str: "def"}
    ];
    json j = tb.toJson();
    assert(j.toJsonString(), "[{\"id\":12, \"str\":\"abc\"}, {\"id\":34, \"str\":\"def\"}]");

    PersonTable tbPerson = table [
        {id: "AA", name: "John", salary: 300.50},
        {id: "BB", name: "Bella", salary: 500.50},
        {id: "CC", name: "Peter", salary: 750.0}
    ];

    json sourceJson = [
        {id: "AA", name: "John", salary: 300.50},
        {id: "BB", name: "Bella", salary: 500.50},
        {id: "CC", name: "Peter", salary: 750.0}
    ];

    assertEquality(sourceJson, tbPerson.toJson());

    table<map<anydata>> tab1 = table [
        {id: 12, name: "abc"},
        {id: 34, name: "def"}
    ];
    json j1 = tab1.toJson();
    assert(j1.toJsonString(), "[{\"id\":12, \"name\":\"abc\"}, {\"id\":34, \"name\":\"def\"}]");

    table<map<string>> tab2 = table [
        {fname: "John", lname: "Wick"},
        {fname: "Robert", lname: "Downey"}
    ];
    json j2 = tab2.toJson();
    assert(j2.toJsonString(), "[{\"fname\":\"John\", \"lname\":\"Wick\"}, {\"fname\":\"Robert\", " +
    "\"lname\":\"Downey\"}]");

    table<map<anydata>> tab3 = table [
        {id: 12, name: "abc"},
        {id: 34, name: "def"}
    ];
    json j3 = tab3.toJson();
    assert(j3.toJsonString(), "[{\"id\":12, \"name\":\"abc\"}, {\"id\":34, \"name\":\"def\"}]");

    table<map<Boo>> tab4 = table [
        {info: {id: 12, str: "abc"}},
        {info: {id: 12, str: "abc"}}
    ];
    json j4 = tab4.toJson();
    assert(j4.toJsonString(), "[{\"info\":{\"id\":12, \"str\":\"abc\"}}, {\"info\":{\"id\":12, \"str\":\"abc\"}}]");
}

function testToStringOnCycles() {
    map<anydata> x = {"ee": 3};
    map<anydata> y = {"qq": 5};
    anydata[] arr = [2, 3, 5];
    x["1"] = y;
    y["1"] = x;
    y["2"] = arr;
    arr.push(x);
    assert(x.toString(), "{\"ee\":3,\"1\":{\"qq\":5,\"1\":...,\"2\":[2,3,5,...]}}");
}

function testToJsonWithCyclicParameter() {
    anydata[] x = [];
    x.push(x);
    json|error y = trap x.toJson();
    assert(y is error, true);
    error err = <error>y;
    assert(err.message(), "{ballerina/lang.value}CyclicValueReferenceError");
    assert(<string>checkpanic err.detail()["message"], "'anydata[]' value has cyclic reference");
}

function assert(anydata actual, anydata expected) {
    if (expected == actual) {
        return;
    }
    typedesc<anydata> expT = typeof expected;
    typedesc<anydata> actT = typeof actual;
    string reason = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
    panic error(reason);
}

type RecordWithSimpleTypeFields record {
    int a;
    byte b;
    float c;
    decimal d;
    string e;
    boolean f;
};

type RecordWithArrayValueFields record {|
    float[] a;
    [string, int] t;
|};

type RecordAnydata record {|
    int id;
    anydata a;
|};

type RecordWithRecordField record {|
    RecordAnydata r;
|};

type RecordWithMapField record {
    map<anydata> m;
};

type RecordWithXmlField record {|
    xml x;
    xml:Element e;
    xml:Comment c;
    xml:ProcessingInstruction p;
    xml:Text t;
|};

type RecordWithOptionalAndRestFields record {|
    int a;
    map<B> b;
    string c?;
    float...;
|};

type B [float, string, B...];

function testTableToJsonConversion() {
    table<RecordWithSimpleTypeFields> tb1 = table [
        {a: 5, b: 1, c: 2.5, d: 3.1, e: "abc", f: true},
        {a: 7, b: 2, c: 4.5, d: 5.2, e: "def", f: false, "g": 13.5}
    ];

    json j1 = tb1.toJson();
    assert(j1.toJsonString(), "[{\"a\":5, \"b\":1, \"c\":2.5, \"d\":3.1, \"e\":\"abc\", \"f\":true}, " +
                                "{\"a\":7, \"b\":2, \"c\":4.5, \"d\":5.2, \"e\":\"def\", \"f\":false, \"g\":13.5}]");

    table<RecordWithArrayValueFields> tb2 = table [
        {a: [1.2, 2.4], t: ["abc", 5]},
        {a: [4.0, 6.5], t: ["def", 8]}
    ];

    json j2 = tb2.toJson();
    assert(j2.toJsonString(), "[{\"a\":[1.2, 2.4], \"t\":[\"abc\", 5]}, {\"a\":[4.0, 6.5], \"t\":[\"def\", 8]}]");

    table<RecordWithRecordField> tb3 = table [
        {r: {id: 10001, a: xml `<book>The Lost World</book>`}},
        {r: {id: 10002, a: xml `<book>Shadows of the Empire</book>`}}
    ];

    json j3 = tb3.toJson();
    assert(j3.toJsonString(), "[{\"r\":{\"id\":10001, \"a\":\"<book>The Lost World</book>\"}}, " +
                                        "{\"r\":{\"id\":10002, \"a\":\"<book>Shadows of the Empire</book>\"}}]");

    table<RecordWithMapField> tb4 = table [
        {m: {a: 5, b: "abc"}},
        {m: {c: 12.5, d: "A"}}
    ];

    json j4 = tb4.toJson();
    assert(j4.toJsonString(), "[{\"m\":{\"a\":5, \"b\":\"abc\"}}, {\"m\":{\"c\":12.5, \"d\":\"A\"}}]");

    table<RecordWithXmlField> tb5 = table [
        {x: xml `<bar>Text</bar>`, e: xml `<foo/>`, c: xml `<!--Comment-->`, p: xml `<?PI?>`, t: xml `Text`}
    ];

    json j5 = tb5.toJson();
    assert(j5.toJsonString(), "[{\"x\":\"<bar>Text</bar>\", \"e\":\"<foo/>\", \"c\":\"<!--Comment-->\", " +
                                        "\"p\":\"<?PI ?>\", \"t\":\"Text\"}]");

    table<RecordWithOptionalAndRestFields> tb6 = table [
        {a: 1, b: {x: [12.4, "abc"], y: [23.8, "def", [36.9, "ghi"]]}, c: "xyz"},
        {a: 5, b: {x: [45.6, "asd"]}, "d": 10, "e": 12.5}
    ];

    json j6 = tb6.toJson();
    assert(j6.toJsonString(), "[{\"a\":1, \"b\":{\"x\":[12.4, \"abc\"], \"y\":[23.8, \"def\", [36.9, \"ghi\"]]}, " +
                                "\"c\":\"xyz\"}, {\"a\":5, \"b\":{\"x\":[45.6, \"asd\"]}, \"d\":10.0, \"e\":12.5}]");
}

///////////////////////// Tests for `ensureType()` ///////////////////////////

json p = {
    name: "Chiran",
    age: 24,
    email: "chirans",
    height: 178.5,
    weight: 72.5,
    property: (),
    address: [
        125.0 / 3.0,
        "xyz street",
        {province: "southern", Country: "Sri Lanka"},
        81000
    ],
    married: false,
    bloodType: {
        bloodGroup: "O",
        RHD: "+"
    }
};

function testEnsureTypeWithInt() returns int|error {
    int age = check p.age;
    return age;
}

function testEnsureTypeWithInt2() returns int|error {
    int height = check p.height;
    return height;
}

function testEnsureTypeWithInt3() returns int|error {
    int married = check p.married;
    return married;
}

function testEnsureTypeWithDecimal() returns decimal|error {
    decimal height = check p.height;
    return height;
}

function testEnsureTypeWithDecimal2() returns decimal|error {
    decimal age = check p.age;
    return age;
}

function testEnsureTypeWithNil() returns ()|error {
    () property = check p.property;
    return property;
}

function testEnsureTypeWithString() returns string|error {
    string name = check p.name;
    return name;
}

function testEnsureTypeWithFloat() returns float|error {
    float weight = check p.weight;
    return weight;
}

function testEnsureTypeWithUnion1() returns float|int|error {
    float|int weight = check p.weight;
    return weight;
}

function testEnsureTypeWithUnion2() returns float|string|error {
    float|string name = check p.name;
    return name;
}

function testEnsureTypeWithJson1() returns json|error {
    json age = check p.age;
    return age;
}

function testEnsureTypeWithJson2() returns json|error {
    json height = check p.height;
    return height;
}

function testEnsureTypeWithJson3() returns json|error {
    json bloodType = check p.bloodType;
    return bloodType;
}

function testEnsureTypeWithJson4() returns json|error {
    json address = check p.address;
    return address;
}

function testEnsureTypeWithJson5() returns json|error {
    json weight = check p.weight;
    return weight;
}

function testEnsureTypeWithJson6() returns json|error {
    json isMarried = check p.married;
    return isMarried;
}

function testEnsureTypeWithCast1() returns boolean|error {
    boolean isMarried = <boolean>check p.married;
    return isMarried;
}

function testEnsureTypeWithCast2() returns json[]|error {
    json[] address = <json[]>check p.address;
    return address;
}

function testEnsureTypeWithCast3() returns map<json>|error {
    map<json> bloodType = <map<json>>check p.bloodType;
    return bloodType;
}

function testEnsureTypeFunction() returns int:Unsigned32|error {
    int number = 10;
    int:Unsigned32|error number1 = number.ensureType(int:Unsigned32);
    return number1;
}

type StrArray string[];

function testEnsureTypeFunction1() returns string|error {
    string|int a = "chirans";
    string|error str = a.ensureType(string);
    return str;
}

function testEnsureTypeFunction2() returns string[]|error {
    string[]|int a = ["Chiran", "Sachintha"];
    string[]|error strArray = a.ensureType(StrArray);
    return strArray;
}

type T string[]|int|string;

function testEnsureTypeFunction3() returns int|error {
    T a = "chiran";
    int|error val = a.ensureType(int);
    return val;
}

function testEnsureType() {
    decimal h = 178.5;
    decimal d = 24.0;
    float h1 = 178.5;
    decimal w = 72.5;
    json name = "Chiran";
    json w1 = 72.5;
    float|int w2 = 72.5;
    float|string name2 = "Chiran";
    assert(<int>(checkpanic testEnsureTypeWithInt()), 24);
    assert(<int>(checkpanic testEnsureTypeWithInt2()), 178);
    assert(testEnsureTypeWithInt3() is error, true);
    assert(<decimal>(checkpanic testEnsureTypeWithDecimal()), h);
    assert(<decimal>(checkpanic testEnsureTypeWithDecimal2()), d);
    assert(<()>(checkpanic testEnsureTypeWithNil()), ());
    assert(<string>(checkpanic testEnsureTypeWithString()), "Chiran");
    assert(<float>(checkpanic testEnsureTypeWithFloat()), w1);
    assert(<float|int>(checkpanic testEnsureTypeWithUnion1()), w2);
    assert(<float|string>(checkpanic testEnsureTypeWithUnion2()), name2);
    assert(<json>(checkpanic testEnsureTypeWithJson1()), 24);
    assert(<json>(checkpanic testEnsureTypeWithJson2()), h1);
    assert(<json>(checkpanic testEnsureTypeWithJson3()), {bloodGroup: "O", RHD: "+"});
    assert(<json>(checkpanic testEnsureTypeWithJson4()), [
        125.0 / 3.0,
        "xyz street",
        {province: "southern", Country: "Sri Lanka"},
        81000
    ]);
    assert(<json>(checkpanic testEnsureTypeWithJson5()), 72.5);
    assert(<json>(checkpanic testEnsureTypeWithJson6()), false);
    assert(<boolean>(checkpanic testEnsureTypeWithCast1()), false);
    assert(<json[]>(checkpanic testEnsureTypeWithCast2()), [
        125.0 / 3.0,
        "xyz street",
        {province: "southern", Country: "Sri Lanka"},
        81000
    ]);
    assert(<map<json>>(checkpanic testEnsureTypeWithJson3()), {bloodGroup: "O", RHD: "+"});
    assert(testEnsureTypeFunction() is int:Unsigned32, true);
    assert(testEnsureTypeFunction1() is string, true);
    assert(testEnsureTypeFunction2() is string[], true);
    assert(testEnsureTypeFunction3() is error, true);
}

function testRequiredTypeWithInvalidCast1() returns error? {
    int age = check p.name;
}

function testRequiredTypeWithInvalidCast2() returns error? {
    decimal name = check p.name;
}

function testRequiredTypeWithInvalidCast3() returns error? {
    float price = check p.name;
}

function testRequiredTypeWithInvalidCast4() returns error? {
    float[] quality = <float[]>check p.name;
}

function testRequiredTypeWithInvalidCast5() returns error? {
    int property = check p.property;
}

function testRequiredTypeWithInvalidCast6() returns error? {
    int property = check p.children;
}

function testEnsureTypeNegative() {
    error? err1 = testRequiredTypeWithInvalidCast1();
    error? err2 = testRequiredTypeWithInvalidCast2();
    error? err3 = testRequiredTypeWithInvalidCast3();
    error? err4 = trap testRequiredTypeWithInvalidCast4();
    error? err5 = testRequiredTypeWithInvalidCast5();
    error? err6 = testRequiredTypeWithInvalidCast6();

    error e1 = <error>err1;
    error e2 = <error>err2;
    error e3 = <error>err3;
    error e4 = <error>err4;
    error e5 = <error>err5;
    error e6 = <error>err6;

    assertEquality("error(\"{ballerina}TypeCastError\",message=\"incompatible types: 'string' cannot be cast to 'int'\")", e1.toString());
    assertEquality("error(\"{ballerina}TypeCastError\",message=\"incompatible types: 'string' cannot be cast to 'decimal'\")", e2.toString());
    assertEquality("error(\"{ballerina}TypeCastError\",message=\"incompatible types: 'string' cannot be cast to 'float'\")", e3.toString());
    assertEquality("error(\"{ballerina}TypeCastError\",message=\"incompatible types: 'string' cannot be cast to 'float[]'\")", e4.toString());
    assertEquality("error(\"{ballerina}TypeCastError\",message=\"incompatible types: '()' cannot be cast to 'int'\")", e5.toString());
    assertEquality("error(\"{ballerina/lang.map}KeyNotFound\",message=\"key 'children' not found in JSON mapping\")", e6.toString());
}

function testEnsureTypeJsonToNestedRecordsWithErrors() {

    json clonedJsonVal = jsonVal.cloneReadOnly();
    Factory|error val = trap clonedJsonVal.ensureType(Factory);

    error err = <error>val;
    string errorMsgPrefix = "incompatible types: 'map<(json & readonly)> & readonly' cannot be cast to 'Factory': ";
    string errorMsg = errorMsgPrefix + errorMsgContent;
    assert(<string>checkpanic err.detail()["message"], errorMsg);
    assert(err.message(), "{ballerina}TypeCastError");
}

function testEnsureTypeWithInferredArgument() {
    int|error age = p.age.ensureType();
    assertEquality(24, age);

    any a = <string[]>["hello", "world"];
    string[] strArray = checkpanic a.ensureType();
    assertEquality(a, strArray);
    string[]|error strArray2 = value:ensureType(a);
    assertEquality(a, strArray2);

    int[]|error intArr = a.ensureType();
    assertTrue(intArr is error);
    if (intArr is error) {
        assertEquality("{ballerina}TypeCastError", intArr.message());
        assertEquality("incompatible types: 'string[]' cannot be cast to 'int[]'",
        <string>checkpanic intArr.detail()["message"]);
    }
}

function testEnsureTypeFloatToIntNegative() {
    float a = 0.0 / 0;
    int|error nan = a.ensureType(int);
    assert(nan is error, true);
    error err = <error>nan;
    var message = err.detail()["message"];
    string messageString = message is error ? message.toString() : message.toString();
    assert(err.message(), "{ballerina}NumberConversionError");
    assert(messageString, "'float' value 'NaN' cannot be converted to 'int'");
}

public type Maps record {|int i; int...;|}|record {|int i?;|};

public type Value record {|
    Maps value;
|};

public function testConvertJsonToAmbiguousType() {
    json j = {"value": <map<int>>{i: 1}};
    Value|error res = j.cloneWithType(Value);

    if res is error {
        assertEquality("'map<json>' value cannot be converted to 'Value'", res.detail()["message"]);
        return;
    }

    panic error("Invalid respone.", message = "Expected error");
}

function testAssigningCloneableToAnyOrError() {
    value:Cloneable x = "25";
    any|error y = x;
    if (y is any) {
        assertEquality("25", y);
        return;
    }

    panic error("Invalid value.", message = "Expected 25");
}

function testXMLWithAngleBrackets() {
    xml xy = xml `x&amp;y`;
    xml:Element e = xml `<p/>`;
    e.setChildren(xy);
    xml exy = xy + e + xy;

    string expected = "x&amp;y<p>x&amp;y</p>x&amp;y";
    if (exy.toString() == expected) {
        return;
    }
    panic error("AssertionError : expected: " + expected + " found: " + exy.toString());
}

public type KeyVals record {|
    anydata...;
|};

function foo(*KeyVals kvPairs) returns string {
    return kvPairs.toString();
}

public function testDestructuredNamedArgs() returns any {
    string actual = foo(message = "This is a sample message", a = "foo", b = "bar", c = 100);
    assertEquality("{\"message\":\"This is a sample message\",\"a\":\"foo\",\"b\":\"bar\",\"c\":100}", actual);
}

type Ints 12|21;

type Strings "a"|"bc";

type True true;

type Boolean false|true;

type Combo 1|2f|"abc"|false|true;

function testToStringOnSubTypes() {
    int a = 12;
    byte b = 12;
    int:Signed8 c = 12;

    string s1 = a.toString();
    string s2 = b.toString();
    string s3 = c.toString();

    assertEquality("12", s1);
    assertEquality(s1, s2);
    assertEquality(s1, s3);

    string:Char e = "x";
    assertEquality("x", e.toString());
}

function testToStringOnFiniteTypes() {
    Ints d = 21;
    assertEquality("21", d.toString());

    Strings f = "bc";
    assertEquality("bc", f.toString());

    Ints|Strings g = 21;
    assertEquality("21", g.toString());

    True h = true;
    assertEquality("true", h.toString());

    Boolean i = false;
    assertEquality("false", i.toString());

    Combo j = 2.0;
    assertEquality("2.0", j.toString());
}

public client class Caller {
    public isolated function getAttribute(string key) returns value:Cloneable? {
        return "dummyVal";
    }
}

function getUsername(Caller ep, string key) returns string|error {
    return <string>check ep.getAttribute(key);
}

function testUsingCloneableReturnType() {
    Caller caller = new ();
    assertEquality("dummyVal", caller.getAttribute("dummy"));
}

function testDecimalZeroToString() {
    decimal d1 = 0.0d;
    decimal d2 = 0d;

    assertTrue(d1.toString() == d2.toString());
    assertTrue(checkpanic decimal:fromString(d1.toString()) == checkpanic decimal:fromString(d2.toString()));
    assertTrue(checkpanic decimal:fromString(d1.toString()) === checkpanic decimal:fromString(d2.toString()));

    d1 = 0.00000000d;
    d2 = 0.0d;

    assertTrue(d1.toString() == d2.toString());
    assertTrue(checkpanic decimal:fromString(d1.toString()) == checkpanic decimal:fromString(d2.toString()));
    assertTrue(checkpanic decimal:fromString(d1.toString()) === checkpanic decimal:fromString(d2.toString()));

    d1 = 0.0000d;
    d2 = 0.0000d;

    assertEquality(d1.toString(), d2.toString());
    assertTrue(checkpanic decimal:fromString(d1.toString()) == checkpanic decimal:fromString(d2.toString()));
    assertTrue(checkpanic decimal:fromString(d1.toString()) === checkpanic decimal:fromString(d2.toString()));
}

function testDecimalNonZeroToString() {
    decimal d1 = 1.0d;
    decimal d2 = 1.00d;

    assertFalse(d1.toString() == d2.toString());
    assertTrue(checkpanic decimal:fromString(d1.toString()) == checkpanic decimal:fromString(d2.toString()));
    assertFalse(checkpanic decimal:fromString(d1.toString()) === checkpanic decimal:fromString(d2.toString()));

    d1 = 10.0d;
    d2 = 10.0000d;

    assertFalse(d1.toString() == d2.toString());
    assertTrue(checkpanic decimal:fromString(d1.toString()) == checkpanic decimal:fromString(d2.toString()));
    assertFalse(checkpanic decimal:fromString(d1.toString()) === checkpanic decimal:fromString(d2.toString()));

    d1 = 0.1d;
    d2 = 0.10d;

    assertFalse(d1.toString() == d2.toString());
    assertTrue(checkpanic decimal:fromString(d1.toString()) == checkpanic decimal:fromString(d2.toString()));
    assertFalse(checkpanic decimal:fromString(d1.toString()) === checkpanic decimal:fromString(d2.toString()));

    d1 = 0.110d;
    d2 = 0.11000000d;

    assertFalse(d1.toString() == d2.toString());
    assertTrue(checkpanic decimal:fromString(d1.toString()) == checkpanic decimal:fromString(d2.toString()));
    assertFalse(checkpanic decimal:fromString(d1.toString()) === checkpanic decimal:fromString(d2.toString()));
}

function testCount() {
    assert(0, value:count());
    assert(2, value:count(2, -1));
    assert(1, value:count([1, 2, 3]));
    assert(1, value:count([]));
    assert(3, value:count(...[1, 2, 3]));
    assert(4, value:count(error("Message1"), error("Message1"), error("Message2"), error("Message2")));
    assert(6, value:count(error("Message1"), "str", true, 3.4, {"m": 2}, error("Message2")));
}

function testFirst() {
    assertEquality(2, value:first(2));
    assertEquality(2, value:first([2])[0]);
    assertEquality(21, value:first(...[21, 31]));
    assertEquality(1, value:first(1, 2, 4, 5));
    var f = value:first(error("Message"), 2.0, true, {"m": 2});
    assertEquality(true, f is error);
    assertEquality("Message", (<error>f).message());
    assertEquality(1, value:first(1, [2, 4, 5]));
    assertEquality(1, value:first(1, ...[2, 4, 5]));
    var b = value:first([false, true], ...[2, 4, 5]);
    assertFalse(b[0]);
    assertTrue(b[1]);
}

function testLast() {
    assertEquality(2, value:last(2));
    assertEquality(5, value:last(1, 2, 4, 5));
    assertTrue([2, 3] == value:last([2, 3]));
    assertEquality(3, value:last(...[21, 3]));
    int[] ar = [21, 3];
    assertEquality(3, value:last(4, ...ar));
    var x1 = value:last(error("Message1"), error("Message2"));
    assertEquality(true, x1 is error);
    assertEquality("Message2", (<error>x1).message());
    ar = [];
    assertEquality(4, value:last(4, ...ar));
    int[] ar1 = [1, 2, 3];
    assertTrue([] == value:last(ar1, ar));
}

public type Copybook record {
    DFH\-COMMAREA DFH\-COMMAREA?;
};

public type DFH\-COMMAREA record {
    record {
        string MI\-HDR\-VERSION?;
        string MI\-HDR\-MSGID?;
        string MI\-HDR\-LOGGINGID?;
        record {
            string MI\-HDR\-REPLYQMGR?;
        }[2] MI\-HDR\-REPLYSTACK?;
    } BROKER\-MESSAGE\-AREA?;
};

function testCloneWithTypeToRecordWithSpecialChars() {
    string s = string `{
    "DFH-COMMAREA": {
        "BROKER-MESSAGE-AREA": {
            "MI-HDR-VERSION": "2",
            "MI-HDR-MSGID":"3238763233323598798798712321187612",
            "MI-HDR-LOGGINGID": "Z5118761-Z"
        }
    }
    }`;
    json rec = checkpanic value:fromJsonString(s);
    map<json> mapJson = checkpanic rec.ensureType();
    Copybook|error dfh\-commarea = mapJson.cloneWithType();
    assertTrue(dfh\-commarea is Copybook);
    Copybook cb =  checkpanic dfh\-commarea.ensureType();
    assertEquality(cb.DFH\-COMMAREA?.BROKER\-MESSAGE\-AREA.toString(), string `{"MI-HDR-VERSION":"2","MI-HDR-MSGID":"3238763233323598798798712321187612","MI-HDR-LOGGINGID":"Z5118761-Z"}`);
}

type AssertionError distinct error;

const ASSERTION_ERROR_REASON = "AssertionError";

function assertFalse(any|error actual) {
    assertEquality(false, actual);
}

function assertTrue(any|error actual) {
    assertEquality(true, actual);
}

isolated function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error AssertionError(ASSERTION_ERROR_REASON,
            message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}

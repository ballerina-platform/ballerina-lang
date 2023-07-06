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

import ballerina/lang.value;

const int aInt = 4;
const str = "Hello";
const nameWithoutType = "Ballerina";
const string nameWithType = "Ballerina";
const int CAI = 10 + 5;
const float CAF = 10.0 + 5.0;
const decimal CAD = 11.5 + 4;
const string CAS = "hello" + "world";
const map<string> aStringMap = { name : "anObject", value : "10", sub : "Science"};
final int status = 1;

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
    map<string> aStringMap = { name : "anObject", value : "10", sub : "Science"};
    map<()|int|float|boolean|string| map<json>> anotherMap = { name : "anObject", value : "10", sub : "Science",
        intVal: 2324, boolVal: true, floatVal: 45.4, nestedMap: {xx: "XXStr", n: 343, nilVal: ()}};
    json anObject = { name : "anObject", value : 10, sub : { subName : "subObject", subValue : 10 }};
    map<()|int|float|boolean|string| map<json>>[] aArr =
        [{ name : "anObject", value : "10", sub : "Science", intVal: 2324, boolVal: true, floatVal: 45.4, nestedMap:
        {xx: "XXStr", n: 343, nilVal: ()}}, { name : "anObject", value : "10", sub : "Science"}];
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
    assert(result["aString"], "aString");
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
    //string aFloatNumber = "10.5";
    string anArray = "[\"hello\", \"world\"]";
    string anObject = "{\"name\":\"anObject\", \"value\":10, \"sub\":{\"subName\":\"subObject\", \"subValue\":10}}";
    string anInvalid = "{\"name\":\"anObject\",";
    map<json|error> result = {};

    result["aNil"] = aNil.fromJsonString();
    result["aNull"] = aNull.fromJsonString();
    result["aString"] = aString.fromJsonString();
    result["aNumber"] = aNumber.fromJsonString();
    //result["aFloatNumber"] = aFloatNumber.fromJsonString();
    result["anArray"] = anArray.fromJsonString();
    result["anObject"] = anObject.fromJsonString();
    result["anInvalid"] = anInvalid.fromJsonString();
    return result;
}

function testToStringMethod() returns [string, string, string, string] {
    int a = 4;
    anydata b = a;
    any c = b;
    var d = c.toString();
    return [a.toString(), b.toString(), c.toString(), d];
}

const MERGE_JSON_ERROR_REASON = "{ballerina/lang.value}MergeJsonError";
const MESSAGE = "message";

function testNilAndNonNilJsonMerge() returns boolean {
    json j1 = ();
    json j2 = { one: "hello", two: 2 };
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
        <string> checkpanic mj.detail()[MESSAGE] == "Cannot merge JSON values of types 'float' and 'json[]'";
}

function testMappingJsonAndNonMappingJsonMerge1() returns boolean {
    json j1 = { one: "hello", two: "world", three: 1 };
    json j2 = "string value";

    json|error mj = j1.mergeJson(j2);
    return mj is error && mj.message() == MERGE_JSON_ERROR_REASON &&
        <string> checkpanic mj.detail()[MESSAGE] == "Cannot merge JSON values of types 'map<json>' and 'string'";
}

function testMappingJsonAndNonMappingJsonMerge2() returns boolean {
    json j1 = { one: "hello", two: "world", three: 1 };
    json[] j2 = [1, 2];

    json|error mj = j1.mergeJson(j2);
    return mj is error && mj.message() == MERGE_JSON_ERROR_REASON &&
        <string> checkpanic mj.detail()[MESSAGE] == "Cannot merge JSON values of types 'map<json>' and 'json[]'";
}

function testMappingJsonNoIntersectionMergeSuccess() returns boolean {
    json j1 = { one: "hello", two: "world", three: 1 };
    map<json> j2 = { x: 12.0, y: "test value" };

    json|error mje = j1.mergeJson(j2);

    if (!(mje is map<json>) || mje.length() != 5) {
        return false;
    }

    json mj = <json> (checkpanic mje);
    return mj.one === "hello" && mj.two === "world" && mj.three === 1 && mj.x === 12.0 && mj.y === "test value";
}

function testMappingJsonWithIntersectionMergeFailure1() returns boolean {
    json j1 = { one: "hello", two: "world", three: 1 };
    json j2 = { two: 1, y: "test value" };

    json j1Clone = j1.clone();
    json j2Clone = j2.clone();

    json|error mj = j1.mergeJson(j2);

    if (!(mj is error) || mj.message() != MERGE_JSON_ERROR_REASON ||
            <string> checkpanic mj.detail()[MESSAGE] != "JSON Merge failed for key 'two'") {
        return false;
    }

    error err = <error> mj;
    error cause = <error> err.detail()["cause"];
    return cause.message() == MERGE_JSON_ERROR_REASON &&
            <string> checkpanic cause.detail()[MESSAGE] == "Cannot merge JSON values of types 'string' and 'int'" &&
            j1 == j1Clone && j2 == j2Clone;
}

function testMappingJsonWithIntersectionMergeFailure2() returns boolean {
    json j1 = { one: { a: "one", b: 2 }, three: 1 };
    json j2 = { two: "test value", one: true };

    json j1Clone = j1.clone();
    json j2Clone = j2.clone();

    json|error mj = j1.mergeJson(j2);

    if (!(mj is error) || mj.message() != MERGE_JSON_ERROR_REASON ||
            <string> checkpanic mj.detail()[MESSAGE] != "JSON Merge failed for key 'one'") {
        return false;
    }

    error err = <error> mj;
    error cause = <error> err.detail()["cause"];
    return cause.message() == MERGE_JSON_ERROR_REASON && <string> checkpanic
            cause.detail()[MESSAGE] == "Cannot merge JSON values of types 'map<json>' and 'boolean'" &&
            j1 == j1Clone && j2 == j2Clone;
}

function testMappingJsonWithIntersectionMergeSuccess() returns boolean {
    json j1 = { one: "hello", two: (), three: 1, four: { a: (), b: "test", c: "value" } };
    json j2 = { four: { a: "strings", b: () }, one: (), two: "world", five: 5  };

    json j2Clone = j2.clone();
    json|error mj = j1.mergeJson(j2);

    if (mj is error) {
        return false;
    } else {
        map<json> expMap = { a: "strings", b: "test", c: "value" };
        map<json> mj4 = <map<json>> (checkpanic mj.four);
        return mj === j1 && mj.one === "hello" && mj.two === "world" && mj.three === 1 &&
            mj4 == expMap && mj.five === 5 && j2 == j2Clone;
    }
}

function testMergeJsonSuccessForValuesWithNonIntersectingCyclicRefererences() returns boolean {
    map<json> j1 = { x: { a: 1 } };
    j1["z"] = j1;
    map<json> j2 = { x: { b: 2 } };
    j2["p"] = j2;
    var result = j1.mergeJson(j2);
    return result === j1 && (checkpanic j1.x) == <json> { a: 1, b: 2 } && j1.z === j1 && j2.p === j2;
}

function testMergeJsonFailureForValuesWithIntersectingCyclicRefererences() returns boolean {
    map<json> j1 = { x: { a: 1 } };
    j1["z"] = j1;
    map<json> j2 = { x: { b: 2 } };
    j2["z"] = j2;

    var result = j1.mergeJson(j2);
    if (result is json || <string> checkpanic result.detail()["message"] != "JSON Merge failed for key 'z'") {
        return false;
    } else {
        error? cause = <error?>result.detail()["cause"];
        if (cause is () || <string> checkpanic cause.detail()["message"] !=
            "Cannot merge JSON values withcyclic references") {
            return false;
        }
    }
    return (checkpanic j1.x) == <json> { a: 1 } && (checkpanic j2.x) == <json> { b: 2 }
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
    Address addr = {country : "Sri Lanka", city: "Colombo", street: "Palm Grove"};
    Person p = {name : "Gima", address: addr, age: 12};
    var s = {...p};
    boolean varBool = true;
    decimal varDecimal = 345.2425341;
    map<any|error> varMap = {};
    json varJson = {a: "STRING", b: 12, c: 12.4, d: true, e: {x:"x", y: ()}};
    any[] varArr = ["str", 23, 23.4, true];
    FirstError varErr = error FirstError(REASON_1, message = "Test passing error union to a function");
    Student varObj = new("Alaa", "MMV");
    Teacher varObj2 = new("Rola", "MMV");
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

    return [varInt.toString(), varFloat.toString(), varStr.toString(), varNil.toString(), varBool.toString(),
            varDecimal.toString(), varJson.toString(), varXml.toString(), varArr.toString(), varErr.toString(),
            value:toString(varObj), varObj2.toString(), varObjArr.toString(), p.toString(), varMap.toString()];
}

function testToStringMethodForTable() {
    table<Employee> employeeTable = table key(id) [
            { id: 1, age: 30,  salary: 300.5, name: "Mary", married: true },
            { id: 2, age: 20,  salary: 300.5, name: "John", married: true }
        ];

    assertEquality("id=1 age=30 salary=300.5 name=Mary married=true\nid=2 age=20 salary=300.5 name=John married=true", employeeTable.toString());
}

public function xmlSequenceFragmentToString() returns string {
   xml x = xml `<abc><def>DEF</def><ghi>1</ghi></abc>`;

   return (x/*).toString();
}

type AssertionError distinct error;

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
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

type Person2 record {
    string name;
    int age;
};

function testCloneWithTypeJsonRec1() {
    Person2  p = {name: "N", age: 3};
    json|error ss = p.cloneWithType(json);
    assert(ss is json, true);

    json j = <json> checkpanic ss;
    assert(j.toJsonString(), "{\"name\":\"N\", \"age\":3}");
}

function testCloneWithTypeJsonRec2() {
   json pj = { name : "tom", age: 2};
   Person2|error pe = pj.cloneWithType(Person2);
   assert(pe is Person2, true);

   Person2 p = <Person2> checkpanic pe;
   assert(p.name, "tom");
   assert(p.age, 2);

   Person2 s = { name : "bob", age: 4};
   json|error ss = s.cloneWithType(json);
   assert(ss is json, true);

   json j = <json> checkpanic ss;
   assert(j.toJsonString(), "{\"name\":\"bob\", \"age\":4}");
}

type BRec record {
    int i;
};

type CRec record {
    int i?;
};

public function testCloneWithTypeOptionalFieldToMandotoryField() {
    CRec c = {  };
    var b = c.cloneWithType(BRec);
    assert(b is error, true);

    error bbe = <error> b;
    assert(bbe.message(), "{ballerina/lang.typedesc}ConversionError");
    assert(<string> checkpanic bbe.detail()["message"], "'CRec' value cannot be converted to 'BRec'");
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
    Foo f = { s: "test string" };
    Bar|Baz|error bb = f.cloneWithType(BarOrBaz);
    assert(bb is error, true);

    error bbe = <error> bb;
    assert(bbe.message(), "{ballerina/lang.typedesc}ConversionError");
    assert(<string> checkpanic bbe.detail()["message"],
        "'Foo' value cannot be converted to 'Bar|Baz': ambiguous target type");
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

    error c1e = <error> c1;
    assert(<string> checkpanic c1e.detail()["message"], "cannot convert '()' to type 'string|int'");
}

function testCloneWithTypeNumeric1() {
    int a = 1234;
    float|error b = a.cloneWithType(float);
    assert(b is float, true);
    assert(<float> checkpanic b, 1234.0);
}

function testCloneWithTypeNumeric2() {
    anydata a = 1234.6;
    int|error b = a.cloneWithType(int);
    assert(b is int, true);
    assert(<int> checkpanic b, 1235);
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
    X x = { a: 21, b: "Alice", c : 1000.5 };
    Y|error ye = x.cloneWithType(Y);
    assert(ye is Y, true);

    Y y = <Y> checkpanic ye;
    assert(y.a, 21.0);
    assert(y.b, "Alice");
    assert(y["c"], <decimal> 1000.5);
}

function testCloneWithTypeNumeric4() {
    json j = { a: 21.3, b: "Alice", c : 1000 };
    X|error xe = j.cloneWithType(X);
    assert(xe is X, true);

    X x = <X> checkpanic xe;
    assert(x.a, 21);
    assert(x.b, "Alice");
    assert(x.c, 1000.0);
}

type FloatArray float[];
type FloatOrBooleanArray (float|boolean)[];
function testCloneWithTypeNumeric5() {
    int[] i = [1, 2];
    float[]|error j = i.cloneWithType(FloatArray);
    (float|boolean)[]|error j2 = i.cloneWithType(FloatOrBooleanArray);
    assert(j is float[], true);

    float[] jf = <float[]> checkpanic j;
    assert(jf.length(), i.length());
    assert(jf[0], 1.0);
    assert(jf[1], 2.0);
    assert(jf, <(float|boolean)[]> checkpanic j2);
}

type IntMap map<int>;
type IntOrStringMap map<string|int>;
function testCloneWithTypeNumeric6() {
    map<float> m = { a: 1.2, b: 2.7 };
    map<int>|error m2 = m.cloneWithType(IntMap);
    map<string|int>|error m3 = m.cloneWithType(IntOrStringMap);
    assert(m2 is map<int>, true);

    map<int> m2m = <map<int>> checkpanic m2;
    assert(m2m.length(), m.length());
    assert(m2m["a"], 1);
    assert(m2m["b"], 3);
    assert(m2m, <map<string|int>> checkpanic m3);
}

type DecimalArray decimal[];
function testCloneWithTypeNumeric7() {
    int[] a1 = [1, 2, 3];
    decimal[]|error a2 = a1.cloneWithType(DecimalArray);
    assert(a2 is decimal[], true);

    decimal[] a2d = <decimal[]> checkpanic a2;
    assert(a2d.length(), a1.length());
    assert(a2d[0], <decimal> 1);
    assert(a2d[1], <decimal> 2);
    assert(a2d[2], <decimal> 3);
}

type StringArray string[];
function testCloneWithTypeStringArray() {
   string anArray = "[\"hello\", \"world\"]";
   json j = <json> checkpanic anArray.fromJsonString();
    string[]|error cloned = j.cloneWithType(StringArray);
    assert(cloned is string[], true);
    string[]  clonedArr= <string[]> checkpanic cloned;
    assert(clonedArr[0], "hello");
    assert(clonedArr[1], "world");
}

type Student2 record {
    string name;
    int age;
};

function testFromJsonWIthTypeNegative() {
    string s = "foobar";
    int|error zz = s.fromJsonWithType(int);
    assert(zz is error, true);
}

function testFromJsonWithTypeRecord1() {
    string str = "{\"name\":\"Name\", \"age\":35}";
    json j = <json> checkpanic str.fromJsonString();
    Student2|error p = j.fromJsonWithType(Student2);

    assert(p is Student2, true);
    assert(p is error ? p.toString() : p.toString(), "name=Name age=35");
}

type Student3 record {
    string name;
    int age?;
};

type Foo2 record {|
    int x2 = 1;
    int y3 = 2;
|};

type Foo3 record {
    string x3;
    int y3;
};

type Foo4 record {
    string x3;
    int y3 = 1;
};

type Foo5 record {
    string x3;
    int y3?;
};

type Foo6 record {
    string x3;
};

function testFromJsonWithTypeRecord2() {
    string str = "{\"name\":\"Name\", \"age\":35}";
    json j = <json> checkpanic str.fromJsonString();
    Student3|error p = j.fromJsonWithType(Student3);

    assert(p is Student3, true);
    assert(p is error ? p.toString() : p.toString(), "name=Name age=35");
}

function testFromJsonWithTypeRecord3() {
    json j = {x3: "Chat"};

    Foo2|error f2 = j.fromJsonWithType(Foo2);
    assert(f2 is error, true);

    Foo3|error f3 = j.fromJsonWithType(Foo3);
    assert(f2 is error, true);

    Foo4|error f4 = j.fromJsonWithType(Foo4);
    assert(f4 is Foo4, true);

    Foo5|error f5 = j.fromJsonWithType(Foo5);
    assert(f5 is Foo5, true);
}

type Student2Or3 Student2|Student3;

function testFromJsonWithTypeAmbiguousTargetType() {
    string str = "{\"name\":\"Name\", \"age\":35}";
    json j = <json> checkpanic str.fromJsonString();
    Student3|error p = j.fromJsonWithType(Student2Or3);
    assert(p is error, true);
}

type XmlType xml;

function testFromJsonWithTypeXML() {
    string s1 = "<test>name</test>";
    xml|error x1 = s1.fromJsonWithType(XmlType);
    assert(x1 is xml, true);
    xml x11 = <xml> checkpanic x1;
    json|error j = x11.toJson();
    assert(<json> checkpanic j, s1);
}

type Student4 record {
    int id;
    xml x;
};

function testFromJsonWithTypeRecordWithXMLField() {
    Student4 student = {id: 1, x: xml `<book>DJ</book>`};
    json j = <json> student.toJson();
    Student4|error ss = j.fromJsonWithType(Student4);
    assert(ss is Student4, true);
}

type MapOfAnyData map<anydata>;

function testFromJsonWithTypeMap() {
    json movie = {
        title: "Some",
        year: 2010
    };
    map<anydata>|error movieMap = movie.fromJsonWithType(MapOfAnyData);
    map<anydata> movieMap2 = <map<anydata>> checkpanic movieMap;
    assert(movieMap2["title"], "Some");
    assert(movieMap2["year"], 2010);
}

function testFromJsonWithTypeStringArray() {
    json j = ["Hello", "World"];
    string[]|error a = j.fromJsonWithType(StringArray);
    string[] a2 = <string[]> checkpanic a;
    assert(a2.length(), 2);
    assert(a2[0], "Hello");
}

function testFromJsonWithTypeArrayNegative() {
    json j = [1, 2];
    string[]|error s = j.fromJsonWithType(StringArray);
    assert(s is error, true);
}

type IntArray int[];

function testFromJsonWithTypeIntArray() {
    json j = [1, 2];
    int[]|error arr = j.fromJsonWithType(IntArray);
    int[] intArr = <int[]> checkpanic arr;
    assert(intArr[0], 1);
    assert(intArr[1], 2);
}

type TableFoo2 table<Foo2>;
type TableFoo3 table<Foo3>;
type TableFoo4 table<Foo4>;
type TableFoo5 table<Foo5>;
type TableFoo6 table<Foo6>;

function testFromJsonWithTypeTable() {
    json jj = [
        {x3: "abc"},
        {x3: "abc"}
    ];

    table<Foo2>|error tabJ2 = jj.fromJsonWithType(TableFoo2);
    assert(tabJ2 is error, true);

    table<Foo3>|error tabJ3 = jj.fromJsonWithType(TableFoo3);
    assert(tabJ3 is error, true);

    table<Foo4>|error tabJ4 = jj.fromJsonWithType(TableFoo4);
    assert(tabJ4 is table<Foo4>, true);

    table<Foo5>|error tabJ5 = jj.fromJsonWithType(TableFoo5);
    assert(tabJ5 is table<Foo5>, true);

    table<Foo6>|error tabJ6 = jj.fromJsonWithType(TableFoo6);
    assert(tabJ6 is table<Foo6>, true);

}

function testFromJsonStringWithTypeJson() {
    string aNil = "()";
    string aNull = "null";
    string aString = "\"aString\"";
    string aNumber = "10";
    string anArray = "[\"hello\", \"world\"]";
    string anObject = "{\"name\":\"anObject\", \"value\":10, \"sub\":{\"subName\":\"subObject\", \"subValue\":10}}";
    string anInvalid = "{\"name\":\"anObject\",";
    map<json|error> result = {};

    result["aNil"] = aNil.fromJsonStringWithType(json);
    result["aNull"] = aNull.fromJsonStringWithType(json);
    result["aString"] = aString.fromJsonStringWithType(json);
    result["aNumber"] = aNumber.fromJsonStringWithType(json);
    result["anArray"] = anArray.fromJsonStringWithType(json);
    result["anObject"] = anObject.fromJsonStringWithType(json);
    result["anInvalid"] = anInvalid.fromJsonStringWithType(json);

    assert(result["aNil"] is error, true);
    assert(result["aNull"] is (), true);

    json aStringJson = <json> checkpanic result["aString"];
    assert(aStringJson.toJsonString(), "aString");

    json anArrayJson = <json> checkpanic result["anArray"];
    assert(anArrayJson.toJsonString(), "[\"hello\", \"world\"]");

    json anObjectJson = <json> checkpanic result["anObject"];
    assert(anObjectJson.toJsonString(), "{\"name\":\"anObject\", \"value\":10, \"sub\":{\"subName\":\"subObject\", \"subValue\":10}}");

    assert(result["anInvalid"] is error, true);
}

function testFromJsonStringWithTypeRecord() {
    string str = "{\"name\":\"Name\", \"age\":35}";
    Student3|error studentOrError = str.fromJsonStringWithType(Student3);

    assert(studentOrError is Student3, true);
    Student3 student = <Student3> checkpanic studentOrError;
    assert(student.name, "Name");
}

function testFromJsonStringWithAmbiguousType() {
    string str = "{\"name\":\"Name\", \"age\":35}";
    Student3|error p = str.fromJsonStringWithType(Student2Or3);
    assert(p is error, true);
}

function testFromJsonStringWithTypeMap() {
    string s = "{\"title\":\"Some\", \"year\":2010}";
    map<anydata>|error movieMap = s.fromJsonStringWithType(MapOfAnyData);
    map<anydata> movieMap2 = <map<anydata>> checkpanic movieMap;
    assert(movieMap2["title"], "Some");
    assert(movieMap2["year"], 2010);
}

function testFromJsonStringWithTypeStringArray() {
    string s = "[\"Hello\", \"World\"]";
    string[]|error a = s.fromJsonStringWithType(StringArray);
    string[] a2 = <string[]> checkpanic a;
    assert(a2.length(), 2);
    assert(a2[0], "Hello");
}

function testFromJsonStringWithTypeArrayNegative() {
    string s = "[1, 2]";
    string[]|error a = s.fromJsonStringWithType(StringArray);
    assert(a is error, true);
}

function testFromJsonStringWithTypeIntArray() {
    string s = "[1, 2]";
    int[]|error arr = s.fromJsonStringWithType(IntArray);
    int[] intArr = <int[]> checkpanic arr;
    assert(intArr[0], 1);
    assert(intArr[1], 2);
}

/////////////////////////// Tests for `toJson()` ///////////////////////////

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
    json|error arrStringJson = arrString.toJson();
    assert(arrStringJson is json[], true);
    assert(<json[]> checkpanic arrStringJson, <json[]> ["hello", "world"]);
}

function testToJsonWithXML() {
    xml x1 = xml `<movie>
                    <title>Some</title>
                    <writer>Writer</writer>
                  </movie>`;
    json j = x1.toJson();
    xml|error x2 = j.fromJsonWithType(XmlType);
    assert(<xml> checkpanic x2, x1);

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
    assert(j.toJsonString(),"{\"line1\":\"Line1\", \"line2\":\"Line2\"}");
    map<string>|error m2 = j.fromJsonWithType(MapOfString);
    assert(<map<string>> checkpanic m2, m);
}

function testToJsonWithMapInt() {
    map<int> m = {a: 1, b: 2};
    map<json> mj = <map<json>> m.toJson();
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

function testToJsonWithTable() {
    table<Boo> tb = table [
            {id: 12, str: "abc"},
            {id: 34, str: "def"}
    ];
    json j = tb.toJson();
    assert(j.toJsonString(), "[{\"id\":12, \"str\":\"abc\"}, {\"id\":34, \"str\":\"def\"}]");
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

public function functionWithImmutableValues() {
    map<string|int> m1 = {stringVal: "str", intVal: 1};

    map<string|int> m2 = m1.cloneReadOnly();

    map<string|int> m3 = m2.cloneReadOnly();

    map<string|int> m5 = {valueType: "map", constraint: "string"};

    var frozenVal = m5.cloneReadOnly();

    if (frozenVal is map<string>) {
        // do nothing
    }
}

public function functionWithRangeExpressions() {
    foreach int i in 25 ... 28 {
        // do nothing
    }

    foreach int i in 25 ..< 28 {
        // do nothing
    }
    var iterableObj = 25 ..< 28;

    var iterator = iterableObj.iterator();

    while (true) {
        record {|int value;|}? r = iterator.next();
        if (r is record {|int value;|}) {
            // do nothing
        } else {
            break;
        }
    }
}

public function functionWithStringTemplate() {
    string name = "Ballerina";
    string template = string `Hello ${name}!!!`;
}

public function functionWithLetExpressions() {
    int a = let int b = 1 in b * 2;
    string greeting = let string hello = "Hello ",
                          string ballerina = "Ballerina!"
                      in hello + ballerina;

    int three = let int one = getInt(), int two = one + one in one + two;

    int length = let var num = 10, var txt = "four" in num + txt.length();

    [int, int] v1 = [10, 20];
    int tupleBindingResult = let [int, int] [d1, d2] = v1,
                                 int d3 = d1 + d2
                             in  d3 * 2;

    int age = let Person3 {
                    name: firstName,
                    age: personAge,
                    ...otherDetails
              } = getPerson()
              in personAge;
    var fatal = let var error(reason, ...params) = getSampleError()
                    in params["fatal"];
}

public function getInt() returns int => 1;

type Person3 record {
    string name;
    int age;
    string country;
};

function getPerson() returns Person3 => {
    name: "John",
    age: 31,
    country: "USA",
    "occupation": "Lawyer"
};

function getSampleError() returns error {
    return error("SampleError", message = "Detail message", fatal = true);
}

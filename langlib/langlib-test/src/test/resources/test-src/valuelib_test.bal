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

function testToStringMethod() returns [string, string, string, string] {
    int a = 4;
    anydata b = a;
    any c = b;
    var d = c.toString();
    return [a.toString(), b.toString(), c.toString(), d];
}

/////////////////////////// Tests for `mergeJson()` ///////////////////////////
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
        mj.detail()[MESSAGE].toString() == "Cannot merge JSON values of types 'float' and 'json[]'";
}

function testMappingJsonAndNonMappingJsonMerge1() returns boolean {
    json j1 = { one: "hello", two: "world", three: 1 };
    json j2 = "string value";

    json|error mj = j1.mergeJson(j2);
    return mj is error && mj.message() == MERGE_JSON_ERROR_REASON &&
        mj.detail()[MESSAGE].toString() == "Cannot merge JSON values of types 'map<json>' and 'string'";
}

function testMappingJsonAndNonMappingJsonMerge2() returns boolean {
    json j1 = { one: "hello", two: "world", three: 1 };
    json[] j2 = [1, 2];

    json|error mj = j1.mergeJson(j2);
    return mj is error && mj.message() == MERGE_JSON_ERROR_REASON &&
        mj.detail()[MESSAGE].toString() == "Cannot merge JSON values of types 'map<json>' and 'json[]'";
}

function testMappingJsonNoIntersectionMergeSuccess() returns boolean {
    json j1 = { one: "hello", two: "world", three: 1 };
    map<json> j2 = { x: 12.0, y: "test value" };

    json|error mje = j1.mergeJson(j2);

    if (!(mje is map<json>) || mje.length() != 5) {
        return false;
    }

    json mj = <json> mje;
    return mj.one == "hello" && mj.two == "world" && mj.three == 1 && mj.x == 12.0 && mj.y == "test value";
}

function testMappingJsonWithIntersectionMergeFailure1() returns boolean {
    json j1 = { one: "hello", two: "world", three: 1 };
    json j2 = { two: 1, y: "test value" };

    json j1Clone = j1.clone();
    json j2Clone = j2.clone();

    json|error mj = j1.mergeJson(j2);

    if (!(mj is error) || mj.message() != MERGE_JSON_ERROR_REASON ||
            mj.detail()[MESSAGE].toString() != "JSON Merge failed for key 'two'") {
        return false;
    }

    error err = <error> mj;
    error cause = <error> err.detail()["cause"];
    return cause.message() == MERGE_JSON_ERROR_REASON &&
            cause.detail()[MESSAGE].toString() == "Cannot merge JSON values of types 'string' and 'int'" &&
            j1 == j1Clone && j2 == j2Clone;
}

function testMappingJsonWithIntersectionMergeFailure2() returns boolean {
    json j1 = { one: { a: "one", b: 2 }, three: 1 };
    json j2 = { two: "test value", one: true };

    json j1Clone = j1.clone();
    json j2Clone = j2.clone();

    json|error mj = j1.mergeJson(j2);

    if (!(mj is error) || mj.message() != MERGE_JSON_ERROR_REASON ||
            mj.detail()[MESSAGE].toString() != "JSON Merge failed for key 'one'") {
        return false;
    }

    error err = <error> mj;
    error cause = <error> err.detail()["cause"];
    return cause.message() == MERGE_JSON_ERROR_REASON &&
            cause.detail()[MESSAGE].toString() == "Cannot merge JSON values of types 'map<json>' and 'boolean'" &&
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
        map<json> mj4 = <map<json>> mj.four;
        return mj === j1 && mj.one == "hello" && mj.two == "world" && mj.three == 1 &&
            mj4 == expMap && mj.five == 5 && j2 == j2Clone;
    }
}

function testMergeJsonSuccessForValuesWithNonIntersectingCyclicRefererences() returns boolean {
    map<json> j1 = { x: { a: 1 } };
    j1["z"] = j1;
    map<json> j2 = { x: { b: 2 } };
    j2["p"] = j2;
    var result = j1.mergeJson(j2);
    return j1.x == <json> { a: 1, b: 2 } && j1.z === j1 && j2.p === j2;
}

function testMergeJsonFailureForValuesWithIntersectingCyclicRefererences() returns boolean {
    map<json> j1 = { x: { a: 1 } };
    j1["z"] = j1;
    map<json> j2 = { x: { b: 2 } };
    j2["z"] = j2;

    var result = j1.mergeJson(j2);
    if (result is json || result.detail()["message"].toString() != "JSON Merge failed for key 'z'") {
        return false;
    } else {
        error? cause = <error?>result.detail()["cause"];
        if (cause is () || cause.detail()["message"].toString() != "Cannot merge JSON values with cyclic references") {
            return false;
        }
    }
    return j1.x == <json> { a: 1 } && j2.x == <json> { b: 2 } && j1.z == j1 && j2.z == j2;
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
    boolean varBool = true;
    decimal varDecimal = 345.2425341;
    map<any|error> varMap = {};
    json varJson = {a: "STRING", b: 12, c: 12.4, d: true, e: {x:"x", y: ()}};
    any[] varArr = ["str", 23, 23.4, true];
    FirstError varErr = FirstError(REASON_1, message = "Test passing error union to a function");
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
            varObj.toString(), varObj2.toString(), varObjArr.toString(), p.toString(), varMap.toString()];
}

function testToStringMethodForTable() {
    table<Employee> employeeTable = table key(id) [
            { id: 1, age: 30,  salary: 300.5, name: "Mary", married: true },
            { id: 2, age: 20,  salary: 300.5, name: "John", married: true }
        ];

    assertEquality("[{\"id\":1,\"age\":30,\"salary\":300.5,\"name\":\"Mary\",\"married\":true},"
    + "{\"id\":2,\"age\":20,\"salary\":300.5,\"name\":\"John\",\"married\":true}]", employeeTable.toString());
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

    panic AssertionError(ASSERTION_ERROR_REASON,
            message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}

type Person2 record {
    string name;
    int age;
};

function testCloneWithTypeJsonRec1() {
    Person2  p = {name: "N", age: 3};
    json|error ss = p.cloneWithType(json);
    assert(ss is json, true);

    json j = <json> ss;
    assert(j.toJsonString(), "{\"name\":\"N\", \"age\":3}");
}

function testCloneWithTypeJsonRec2() {
   json pj = { name : "tom", age: 2};
   Person2|error pe = pj.cloneWithType(Person2);
   assert(pe is Person2, true);

   Person2 p = <Person2> pe;
   assert(p.name, "tom");
   assert(p.age, 2);

   Person2 s = { name : "bob", age: 4};
   json|error ss = s.cloneWithType(json);
   assert(ss is json, true);

   json j = <json> ss;
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
    assert(bbe.detail()["message"].toString(), "'CRec' value cannot be converted to 'BRec'");
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
    assert(bbe.detail()["message"].toString(), "'Foo' value cannot be converted to 'Bar|Baz': ambiguous target type");
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
    assert(c1e.detail()["message"].toString(), "cannot convert '()' to type 'string|int'");
}

function testCloneWithTypeNumeric1() {
    int a = 1234;
    float|error b = a.cloneWithType(float);
    assert(b is float, true);
    assert(<float> b, 1234.0);
}

function testCloneWithTypeNumeric2() {
    anydata a = 1234.6;
    int|error b = a.cloneWithType(int);
    assert(b is int, true);
    assert(<int> b, 1235);
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

    Y y = <Y> ye;
    assert(y.a, 21.0);
    assert(y.b, "Alice");
    assert(y["c"], <decimal> 1000.5);
}

function testCloneWithTypeNumeric4() {
    json j = { a: 21.3, b: "Alice", c : 1000 };
    X|error xe = j.cloneWithType(X);
    assert(xe is X, true);

    X x = <X> xe;
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

    float[] jf = <float[]> j;
    assert(jf.length(), i.length());
    assert(jf[0], 1.0);
    assert(jf[1], 2.0);
    assert(jf, <(float|boolean)[]> j2);
}

type IntMap map<int>;
type IntOrStringMap map<string|int>;
function testCloneWithTypeNumeric6() {
    map<float> m = { a: 1.2, b: 2.7 };
    map<int>|error m2 = m.cloneWithType(IntMap);
    map<string|int>|error m3 = m.cloneWithType(IntOrStringMap);
    assert(m2 is map<int>, true);

    map<int> m2m = <map<int>> m2;
    assert(m2m.length(), m.length());
    assert(m2m["a"], 1);
    assert(m2m["b"], 3);
    assert(m2m, <map<string|int>> m3);
}

type DecimalArray decimal[];
function testCloneWithTypeNumeric7() {
    int[] a1 = [1, 2, 3];
    decimal[]|error a2 = a1.cloneWithType(DecimalArray);
    assert(a2 is decimal[], true);

    decimal[] a2d = <decimal[]> a2;
    assert(a2d.length(), a1.length());
    assert(a2d[0], <decimal> 1);
    assert(a2d[1], <decimal> 2);
    assert(a2d[2], <decimal> 3);
}

type StringArray string[];
function testCloneWithTypeStringArray() {
   string anArray = "[\"hello\", \"world\"]";
   json j = <json> anArray.fromJsonString();
    string[]|error cloned = j.cloneWithType(StringArray);
    assert(cloned is string[], true);
    string[]  clonedArr= <string[]> cloned;
    assert(clonedArr[0], "hello");
    assert(clonedArr[1], "world");
}

/////////////////////////// Tests for `fromJsonWithType()` ///////////////////////////
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
    string str = "{\"name\":\"Name\",\"age\":35}";
    json j = <json> str.fromJsonString();
    Student2|error p = j.fromJsonWithType(Student2);

    assert(p is Student2, true);
    assert(p.toString(), "{\"name\":\"Name\",\"age\":35}");
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
    string str = "{\"name\":\"Name\",\"age\":35}";
    json j = <json> str.fromJsonString();
    Student3|error p = j.fromJsonWithType(Student3);

    assert(p is Student3, true);
    assert(p.toString(), "{\"name\":\"Name\",\"age\":35}");
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
    string str = "{\"name\":\"Name\",\"age\":35}";
    json j = <json> str.fromJsonString();
    Student3|error p = j.fromJsonWithType(Student2Or3);
    assert(p is error, true);
}

function testFromJsonWithTypeXML() {
    string s1 = "<test>name</test>";
    xml|error x1 = s1.fromJsonWithType(xml);
    assert(x1 is xml, true);
    xml x11 = <xml> x1;
    json|error j = x11.toJson();
    assert(<json> j, s1);
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
    map<anydata> movieMap2 = <map<anydata>> movieMap;
    assert(movieMap2["title"], "Some");
    assert(movieMap2["year"], 2010);
}

function testFromJsonWithTypeStringArray() {
    json j = ["Hello", "World"];
    string[]|error a = j.fromJsonWithType(StringArray);
    string[] a2 = <string[]> a;
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
    int[] intArr = <int[]> arr;
    assert(intArr[0], 1);
    assert(intArr[1], 2);
}

type TableString table<string>;

type TableFoo2 table<Foo2>;
type TableFoo3 table<Foo3>;
type TableFoo4 table<Foo4>;
type TableFoo5 table<Foo5>;
type TableFoo6 table<Foo6>;

function testFromJsonWithTypeTable() {

    json j = [
        "cake",
        "buscuit"
    ];

    table<string>|error tabString = j.fromJsonWithType(TableString);
    assert(tabString is error, true);

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

/////////////////////////// Tests for `fromJsonStringWithType()` ///////////////////////////

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

    json aStringJson = <json> result["aString"];
    assert(aStringJson.toJsonString(), "aString");

    json anArrayJson = <json> result["anArray"];
    assert(anArrayJson.toJsonString(), "[\"hello\", \"world\"]");

    json anObjectJson = <json> result["anObject"];
    assert(anObjectJson.toJsonString(), "{\"name\":\"anObject\", \"value\":10, \"sub\":{\"subName\":\"subObject\", \"subValue\":10}}");

    assert(result["anInvalid"] is error, true);
}

function testFromJsonStringWithTypeRecord() {
    string str = "{\"name\":\"Name\",\"age\":35}";
    Student3|error studentOrError = str.fromJsonStringWithType(Student3);

    assert(studentOrError is Student3, true);
    Student3 student = <Student3> studentOrError;
    assert(student.name, "Name");
}

function testFromJsonStringWithAmbiguousType() {
    string str = "{\"name\":\"Name\",\"age\":35}";
    Student3|error p = str.fromJsonStringWithType(Student2Or3);
    assert(p is error, true);
}

function testFromJsonStringWithTypeMap() {
    string s = "{\"title\":\"Some\",\"year\":2010}";
    map<anydata>|error movieMap = s.fromJsonStringWithType(MapOfAnyData);
    map<anydata> movieMap2 = <map<anydata>> movieMap;
    assert(movieMap2["title"], "Some");
    assert(movieMap2["year"], 2010);
}

function testFromJsonStringWithTypeStringArray() {
    string s = "[\"Hello\",\"World\"]";
    string[]|error a = s.fromJsonStringWithType(StringArray);
    string[] a2 = <string[]> a;
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
    int[] intArr = <int[]> arr;
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
    assert(<json[]> arrStringJson, <json[]> ["hello", "world"]);
}

function testToJsonWithXML() {
    xml x1 = xml `<movie>
                    <title>Some</title>
                    <writer>Writer</writer>
                  </movie>`;
    json j = x1.toJson();
    xml|error x2 = j.fromJsonWithType(xml);
    assert(<xml> x2, x1);

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
    assert(<map<string>> m2, m);
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

function testToStringOnCycles() {
     map<anydata> x = {"ee" : 3};
     map<anydata> y = {"qq" : 5};
     anydata[] arr = [2 , 3, 5];
     x["1"] = y;
     y["1"] = x;
     y["2"] = arr;
     arr.push(x);
     assert(x.toString(), "{\"ee\":3,\"1\":{\"qq\":5,\"1\":...,\"2\":[2,3,5,...]}}");
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

///////////////////////// Tests for `ensureType()` ///////////////////////////

json  p = {
    name: "Chiran",
    age: 24,
    email: "chirans",
    height: 178.5,
    weight: 72.5,
    property: (), 
    address: [
        125.0/3,
        "xyz street",
        {province: "southern", Country: "Sri Lanka"},
        81000
    ],
    married: false,
    bloodType: {
        group: "O",
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
    boolean isMarried = <boolean> check p.married;
    return isMarried;
}

function testEnsureTypeWithCast2() returns json[]|error {
    json[] address = <json[]> check p.address;
    return address;
}

function testEnsureTypeWithCast3() returns map<json>|error {
    map<json> bloodType = <map<json>> check p.bloodType;
    return bloodType;
}

function testEnsureType() {
    decimal h = 178.5;
    float h1 = 178.5;
    decimal w = 72.5;
    json name = "Chiran";
    json w1 = 72.5;
    float|int w2 = 72.5;
    float|string name2 = "Chiran";
    assert(<int>testEnsureTypeWithInt(), 24);
    assert(<int>testEnsureTypeWithInt2(), 178);
    assert(<int>testEnsureTypeWithInt3(), 0);
    assert(<decimal>testEnsureTypeWithDecimal(), h);
    assert(<decimal>testEnsureTypeWithDecimal2(), 24);
    assert(<()>testEnsureTypeWithNil(), ());
    assert( <string>testEnsureTypeWithString(), "Chiran");
    assert(<float>testEnsureTypeWithFloat(), w1);
    assert(<float|int>testEnsureTypeWithUnion1(), w2);
    assert(<float|string>testEnsureTypeWithUnion2(), name2);
    assert(<json>testEnsureTypeWithJson1(), 24);
    assert(<json>testEnsureTypeWithJson2(),h1);
    assert(<json>testEnsureTypeWithJson3(), {group: "O", RHD: "+"});
    assert(<json>testEnsureTypeWithJson4(), [125.0/3, "xyz street",
    {province: "southern", Country: "Sri Lanka"}, 81000]);
    assert(<json>testEnsureTypeWithJson5(), 72.5);
    assert(<json>testEnsureTypeWithJson6(), false);
    assert(<boolean>testEnsureTypeWithCast1(), false);
    assert(<json[]>testEnsureTypeWithCast2(), [125.0/3, "xyz street",
    {province: "southern", Country: "Sri Lanka"}, 81000]);
    assert(<map<json>>testEnsureTypeWithJson3(), {group: "O", RHD: "+"});
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
    float[] quality = <float[]> check p.name;
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

    error e1 = <error> err1;
    error e2 = <error> err2;
    error e3 = <error> err3;
    error e4 = <error> err4;
    error e5 = <error> err5;
    error e6 = <error> err6;

    assertEquality("error(\"{ballerina}TypeCastError\",message=\"incompatible types: 'string' cannot be cast to 'int'\")", e1.toString());
    assertEquality("error(\"{ballerina}TypeCastError\",message=\"incompatible types: 'string' cannot be cast to 'decimal'\")", e2.toString());
    assertEquality("error(\"{ballerina}TypeCastError\",message=\"incompatible types: 'string' cannot be cast to 'float'\")", e3.toString());
    assertEquality("error(\"{ballerina}TypeCastError\",message=\"incompatible types: 'string' cannot be cast to 'float[]'\")", e4.toString());
    assertEquality("error(\"{ballerina}TypeCastError\",message=\"incompatible types: '()' cannot be cast to 'int'\")", e5.toString());
    assertEquality("error(\"{ballerina/lang.map}KeyNotFound\",message=\"Key 'children' not found in JSON mapping\")", e6.toString());
}

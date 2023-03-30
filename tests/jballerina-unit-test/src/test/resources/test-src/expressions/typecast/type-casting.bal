import ballerina/lang.'float as floats;
import ballerina/lang.'int as ints;
import ballerina/test;

function floattoint(float value) returns (int) {
    int result;
    //float to int should be a conversion
    result = <int>value;
    return result;
}

function floattointWithError(float value) returns int|error {
    int | error result = <int>value;
    return result;
}

function inttofloat(int value) returns (float) {
    float result;
    //int to float should be a conversion
    result = <float> value;
    return result;
}

function stringtoint(string value) returns int|error {
    int result;
    //string to int should be a unsafe conversion
    result = check ints:fromString(value);
    return result;
}

function testDecimalToIntCasting() {
    decimal d = -9223372036854775807d;
    int res = <int> d;
    assertEquality(-9223372036854775807, res);

    d = 9223372036854775807d;
    res = <int> d;
    assertEquality(9223372036854775807, res);

    d = 9223372036854775806.5d;
    res = <int> d;
    assertEquality(9223372036854775806, res);

    d = -9223372036854775805.5d;
    res = <int> d;
    assertEquality(-9223372036854775806, res);

    d = -9223372036854775808.9d;
    int|error result = trap <int> d;
    assertEquality(true, result is error);
    error err = <error> result;
    assertEquality("{ballerina}NumberConversionError", err.message());
    assertEquality("'decimal' value '-9223372036854775808.9' cannot be converted to 'int'",
                    checkpanic <string|error> err.detail()["message"]);

    d = 9223372036854775807.5d;
    result = trap <int> d;
    assertEquality(true, result is error);
    err = <error> result;
    assertEquality("{ballerina}NumberConversionError", err.message());
    assertEquality("'decimal' value '9223372036854775807.5' cannot be converted to 'int'",
                    checkpanic <string|error> err.detail()["message"]);
}

function testDecimalToFloatCasting() {
    decimal d = 0.0000000000000009e+308;
    float f = <float> d;
    assertEquality(9E+292, f);

    d = -0.0000000000000009e+308;
    f = <float> d;
    assertEquality(-9E+292, f);

    d = -0.0000000000000005e-308;
    f = <float> d;
    assertEquality(-5E-324, f);

    d = 9.999999999999999999999999999999999E6001d;
    f = <float> d;
    assertEquality(float:Infinity, f);

    d = -9.999999999999999999999999999999999E6001d;
    f = <float> d;
    assertEquality(-float:Infinity, f);
}

function testFloatToDecimalCasting() {
    float f = 0.0000000000000009e+308;
    decimal d = <decimal> f;
    assertEquality(9E+292d, d);

    f = -1.999999999e-200;
    d = <decimal> f;
    assertEquality(-1.999999999E-200d, d);
}

function testIntSubtypeArrayCasting() {

    byte[] byteArray = [1, 128, 255];
    int:Signed8[] signed8Array = [-128, 0, 127];
    int:Signed16[] signed16Array = [-32768, 0, 32767];
    int:Signed32[] signed32Array = [-20000, 0, 50000];
    int:Unsigned8[] unsigned8Array = [1, 128, 255];
    int:Unsigned16[] unsigned16Array = [5, 5050, 65535];
    int:Unsigned32[] unsigned32Array = [0, 65536, 65555536];

    any anyByteArray = byteArray;
    any anySigned8Array = signed8Array;
    any anySigned16Array = signed16Array;
    any anySigned32Array = signed32Array;
    any anyUnsigned8Array = unsigned8Array;
    any anyUnsigned16Array = unsigned16Array;
    any anyUnsigned32Array = unsigned32Array;

    // byte
    test:assertEquals(<int[]> anyByteArray, [1, 128, 255]);
    test:assertEquals(<int:Signed16[]> anyByteArray, [1, 128, 255]);
    test:assertEquals(<int:Signed32[]> anyByteArray, [1, 128, 255]);
    test:assertEquals(<int:Unsigned16[]> anyByteArray, [1, 128, 255]);
    test:assertEquals(<int:Unsigned32[]> anyByteArray, [1, 128, 255]);

    // Unsigned8
    test:assertEquals(<int[]> anyUnsigned8Array, [1, 128, 255]);
    test:assertEquals(<int:Signed16[]> anyUnsigned8Array, [1, 128, 255]);
    test:assertEquals(<int:Signed32[]> anyUnsigned8Array, [1, 128, 255]);
    test:assertEquals(<int:Unsigned16[]> anyUnsigned8Array, [1, 128, 255]);
    test:assertEquals(<int:Unsigned32[]> anyUnsigned8Array, [1, 128, 255]);

    // Unsigned16
    test:assertEquals(<int[]> anyUnsigned16Array, [5, 5050, 65535]);
    test:assertEquals(<int:Signed32[]> anyUnsigned16Array, [5, 5050, 65535]);
    test:assertEquals(<int:Unsigned32[]> anyUnsigned16Array, [5, 5050, 65535]);

    // Unsigned32
    test:assertEquals(<int[]> anyUnsigned32Array, [0, 65536, 65555536]);

    // Signed8
    test:assertEquals(<int[]> anySigned8Array, [-128, 0, 127]);
    test:assertEquals(<int:Signed16[]> anySigned8Array, [-128, 0, 127]);
    test:assertEquals(<int:Signed32[]> anySigned8Array, [-128, 0, 127]);

    // Signed16
    test:assertEquals(<int[]> anySigned16Array, [-32768, 0, 32767]);
    test:assertEquals(<int:Signed32[]> anySigned16Array, [-32768, 0, 32767]);

    // Signed32
    test:assertEquals(<int[]> anySigned32Array, [-20000, 0, 50000]);
}

function testIntSubtypeArrayCastingWithErrors() {
    int:Signed32[] signed32Array = [-2147483648, 0, 2147483647];
    int:Unsigned32[] unsigned32Array = [0, 65536, 4294967295];
    int:Signed16[] signed16Array = [1, 2, 3];
    int:Unsigned16[] unsigned16Array = [1, 2, 3];
    int:Signed8[] signed8Array = [1, 2, 3];
    int:Unsigned8[] unsigned8Array = [1, 2, 3];

    any anySigned32Array = signed32Array;
    any anyUnsigned32Array = unsigned32Array;
    any anySigned16Array = signed16Array;
    any anyUnsigned16Array = unsigned16Array;
    any anySigned8Array = signed8Array;
    any anyUnsigned8Array = unsigned8Array;

    // cast to Signed32[]
    int:Signed32[]|error signed32ArrayOrError = trap <int:Signed32[]> anyUnsigned32Array;
    error err = <error> signed32ArrayOrError;
    assertEquality(err.detail()["message"], "incompatible types: 'lang.int:Unsigned32[]' cannot be cast to 'lang.int:Signed32[]'");

    // cast to UnSigned32[]
    int:Unsigned32[]|error unsigned32ArrayOrError = trap <int:Unsigned32[]> anySigned32Array;
    err = <error> unsigned32ArrayOrError;
    assertEquality(err.detail()["message"], "incompatible types: 'lang.int:Signed32[]' cannot be cast to 'lang.int:Unsigned32[]'");

    unsigned32ArrayOrError = trap <int:Unsigned32[]> anySigned16Array;
    err = <error> unsigned32ArrayOrError;
    assertEquality(err.detail()["message"], "incompatible types: 'lang.int:Signed16[]' cannot be cast to 'lang.int:Unsigned32[]'");

    unsigned32ArrayOrError = trap <int:Unsigned32[]> anySigned8Array;
    err = <error> unsigned32ArrayOrError;
    assertEquality(err.detail()["message"], "incompatible types: 'lang.int:Signed8[]' cannot be cast to 'lang.int:Unsigned32[]'");

    // cast to Unsigned16[]
    int:Unsigned16[]|error unsigned16ArrayOrError = trap <int:Unsigned16[]> anyUnsigned32Array;
    err = <error> unsigned16ArrayOrError;
    assertEquality(err.detail()["message"], "incompatible types: 'lang.int:Unsigned32[]' cannot be cast to 'lang.int:Unsigned16[]'");

    unsigned16ArrayOrError = trap <int:Unsigned16[]> anySigned32Array;
    err = <error> unsigned16ArrayOrError;
    assertEquality(err.detail()["message"], "incompatible types: 'lang.int:Signed32[]' cannot be cast to 'lang.int:Unsigned16[]'");

    unsigned16ArrayOrError = trap <int:Unsigned16[]> anySigned16Array;
    err = <error> unsigned16ArrayOrError;
    assertEquality(err.detail()["message"], "incompatible types: 'lang.int:Signed16[]' cannot be cast to 'lang.int:Unsigned16[]'");

    unsigned16ArrayOrError = trap <int:Unsigned16[]> anySigned8Array;
    err = <error> unsigned16ArrayOrError;
    assertEquality(err.detail()["message"], "incompatible types: 'lang.int:Signed8[]' cannot be cast to 'lang.int:Unsigned16[]'");

    // cast to Signed16[]
    int:Signed16[]|error signed16ArrayOrError = trap <int:Signed16[]> anyUnsigned32Array;
    err = <error> signed16ArrayOrError;
    assertEquality(err.detail()["message"], "incompatible types: 'lang.int:Unsigned32[]' cannot be cast to 'lang.int:Signed16[]'");

    signed16ArrayOrError = trap <int:Signed16[]> anySigned32Array;
    err = <error> signed16ArrayOrError;
    assertEquality(err.detail()["message"], "incompatible types: 'lang.int:Signed32[]' cannot be cast to 'lang.int:Signed16[]'");

    signed16ArrayOrError = trap <int:Signed16[]> anyUnsigned16Array;
    err = <error> signed16ArrayOrError;
    assertEquality(err.detail()["message"], "incompatible types: 'lang.int:Unsigned16[]' cannot be cast to 'lang.int:Signed16[]'");

    // cast to Signed8[]
    int:Signed8[]|error signed8ArrayOrError = trap <int:Signed8[]> anyUnsigned32Array;
    err = <error> signed8ArrayOrError;
    assertEquality(err.detail()["message"], "incompatible types: 'lang.int:Unsigned32[]' cannot be cast to 'lang.int:Signed8[]'");

    signed8ArrayOrError = trap <int:Signed8[]> anyUnsigned16Array;
    err = <error> signed8ArrayOrError;
    assertEquality(err.detail()["message"], "incompatible types: 'lang.int:Unsigned16[]' cannot be cast to 'lang.int:Signed8[]'");

    signed8ArrayOrError = trap <int:Signed8[]> anyUnsigned8Array;
    err = <error> signed8ArrayOrError;
    assertEquality(err.detail()["message"], "incompatible types: 'lang.int:Unsigned8[]' cannot be cast to 'lang.int:Signed8[]'");

    signed8ArrayOrError = trap <int:Signed8[]> anySigned32Array;
    err = <error> signed8ArrayOrError;
    assertEquality(err.detail()["message"], "incompatible types: 'lang.int:Signed32[]' cannot be cast to 'lang.int:Signed8[]'");

    signed8ArrayOrError = trap <int:Signed8[]> anySigned16Array;
    err = <error> signed8ArrayOrError;
    assertEquality(err.detail()["message"], "incompatible types: 'lang.int:Signed16[]' cannot be cast to 'lang.int:Signed8[]'");

    // cast to Unsigned8[]
    int:Unsigned8[]|error unsigned8ArrayOrError = trap <int:Unsigned8[]> anyUnsigned32Array;
    err = <error> unsigned8ArrayOrError;
    assertEquality(err.detail()["message"], "incompatible types: 'lang.int:Unsigned32[]' cannot be cast to 'lang.int:Unsigned8[]'");

    unsigned8ArrayOrError = trap <int:Unsigned8[]> anySigned32Array;
    err = <error> unsigned8ArrayOrError;
    assertEquality(err.detail()["message"], "incompatible types: 'lang.int:Signed32[]' cannot be cast to 'lang.int:Unsigned8[]'");

    unsigned8ArrayOrError = trap <int:Unsigned8[]> anyUnsigned16Array;
    err = <error> unsigned8ArrayOrError;
    assertEquality(err.detail()["message"], "incompatible types: 'lang.int:Unsigned16[]' cannot be cast to 'lang.int:Unsigned8[]'");

    unsigned8ArrayOrError = trap <int:Unsigned8[]> anySigned16Array;
    err = <error> unsigned8ArrayOrError;
    assertEquality(err.detail()["message"], "incompatible types: 'lang.int:Signed16[]' cannot be cast to 'lang.int:Unsigned8[]'");

    unsigned8ArrayOrError = trap <int:Unsigned8[]> anySigned8Array;
    err = <error> unsigned8ArrayOrError;
    assertEquality(err.detail()["message"], "incompatible types: 'lang.int:Signed8[]' cannot be cast to 'lang.int:Unsigned8[]'");

}

function testCastingToIntSubtypesInUnion() {
    int UnsignedMin = 0;
    int Unsigned8Max = int:UNSIGNED8_MAX_VALUE;
    int Unsigned16Max = int:UNSIGNED16_MAX_VALUE;
    int Unsigned32Max = int:UNSIGNED32_MAX_VALUE;
    int Signed8Max = int:SIGNED8_MAX_VALUE;
    int Signed8Min = int:SIGNED8_MIN_VALUE;
    int Signed16Max = int:SIGNED16_MAX_VALUE;
    int Signed16Min = int:SIGNED16_MIN_VALUE;
    int Signed32Max = int:SIGNED32_MAX_VALUE;
    int Signed32Min = int:SIGNED32_MIN_VALUE;

    test:assertEquals(<int:Unsigned8|boolean> UnsignedMin, 0);
    test:assertEquals(<int:Unsigned8|boolean|()> Unsigned8Max, int:UNSIGNED8_MAX_VALUE);
    test:assertEquals(<int:Unsigned16|string> Unsigned16Max, int:UNSIGNED16_MAX_VALUE);
    test:assertEquals(<int:Unsigned32|byte> Unsigned32Max, int:UNSIGNED32_MAX_VALUE);
    test:assertEquals(<int:Signed8|boolean> Signed8Max, int:SIGNED8_MAX_VALUE);
    test:assertEquals(<int:Signed8|()|byte> Signed8Min, int:SIGNED8_MIN_VALUE);
    test:assertEquals(<int:Signed16|boolean|string|byte> Signed16Max, int:SIGNED16_MAX_VALUE);
    test:assertEquals(<int:Signed16|string|boolean> Signed16Min, int:SIGNED16_MIN_VALUE);
    test:assertEquals(<int:Signed32|byte> Signed32Max, int:SIGNED32_MAX_VALUE);
    test:assertEquals(<int:Signed32|()|string> Signed32Min, int:SIGNED32_MIN_VALUE);
}

function testCastingToIntSubtypesInUnionNegative() {
    int notUnsigned = -1;
    int notUnsigned8 = int:UNSIGNED8_MAX_VALUE + 1;
    int notUnsigned16 = int:UNSIGNED16_MAX_VALUE + 1;
    int notUnsigned32 = int:UNSIGNED32_MAX_VALUE + 1;
    int notSigned8Upper = int:SIGNED8_MAX_VALUE + 1;
    int notSigned8Lower = int:SIGNED8_MIN_VALUE - 1;
    int notSigned16Upper = int:SIGNED16_MAX_VALUE + 1;
    int notSigned16Lower = int:SIGNED16_MIN_VALUE - 1;
    int notSigned32Upper = int:SIGNED32_MAX_VALUE + 1;
    int notSigned32Lower = int:SIGNED32_MIN_VALUE - 1;

    (int:Unsigned8|boolean)|error val1 = trap <int:Unsigned8|boolean> notUnsigned;
    error err = <error> val1;
    assertEquality(err.detail()["message"], "incompatible types: 'int' cannot be cast to '(lang.int:Unsigned8|boolean)'");
    assertEquality(err.message(),"{ballerina}TypeCastError");

    (int:Unsigned8|boolean)|error val2 = trap <int:Unsigned8|boolean> notUnsigned8;
    err = <error> val2;
    assertEquality(err.detail()["message"], "incompatible types: 'int' cannot be cast to '(lang.int:Unsigned8|boolean)'");
    assertEquality(err.message(),"{ballerina}TypeCastError");

    (int:Unsigned16|boolean)|error val3 = trap <int:Unsigned16|boolean> notUnsigned;
    err = <error> val3;
    assertEquality(err.detail()["message"], "incompatible types: 'int' cannot be cast to '(lang.int:Unsigned16|boolean)'");
    assertEquality(err.message(),"{ballerina}TypeCastError");

    (int:Unsigned16|boolean)|error val4 = trap <int:Unsigned16|boolean> notUnsigned16;
    err = <error> val4;
    assertEquality(err.detail()["message"], "incompatible types: 'int' cannot be cast to '(lang.int:Unsigned16|boolean)'");
    assertEquality(err.message(),"{ballerina}TypeCastError");

    (int:Unsigned32|boolean)|error val5 = trap <int:Unsigned32|boolean> notUnsigned;
    err = <error> val5;
    assertEquality(err.detail()["message"], "incompatible types: 'int' cannot be cast to '(lang.int:Unsigned32|boolean)'");
    assertEquality(err.message(),"{ballerina}TypeCastError");

    (int:Unsigned32|boolean)|error val6 = trap <int:Unsigned32|boolean> notUnsigned32;
    err = <error> val6;
    assertEquality(err.detail()["message"], "incompatible types: 'int' cannot be cast to '(lang.int:Unsigned32|boolean)'");
    assertEquality(err.message(),"{ballerina}TypeCastError");

    (int:Signed8|boolean)|error val7 = trap <int:Signed8|boolean> notSigned8Upper;
    err = <error> val7;
    assertEquality(err.detail()["message"], "incompatible types: 'int' cannot be cast to '(lang.int:Signed8|boolean)'");
    assertEquality(err.message(),"{ballerina}TypeCastError");

    (int:Signed8|boolean)|error val8 = trap <int:Signed8|boolean> notSigned8Lower;
    err = <error> val8;
    assertEquality(err.detail()["message"], "incompatible types: 'int' cannot be cast to '(lang.int:Signed8|boolean)'");
    assertEquality(err.message(),"{ballerina}TypeCastError");

    (int:Signed16|boolean)|error val9 = trap <int:Signed16|boolean> notSigned16Upper;
    err = <error> val9;
    assertEquality(err.detail()["message"], "incompatible types: 'int' cannot be cast to '(lang.int:Signed16|boolean)'");
    assertEquality(err.message(),"{ballerina}TypeCastError");

    (int:Signed16|boolean)|error val10 = trap <int:Signed16|boolean> notSigned16Lower;
    err = <error> val10;
    assertEquality(err.detail()["message"], "incompatible types: 'int' cannot be cast to '(lang.int:Signed16|boolean)'");
    assertEquality(err.message(),"{ballerina}TypeCastError");

    (int:Signed32|boolean)|error val11 = trap <int:Signed32|boolean> notSigned32Upper;
    err = <error> val11;
    assertEquality(err.detail()["message"], "incompatible types: 'int' cannot be cast to '(lang.int:Signed32|boolean)'");
    assertEquality(err.message(),"{ballerina}TypeCastError");

    (int:Signed32|boolean)|error val12 = trap <int:Signed32|boolean> notSigned32Lower;
    err = <error> val12;
    assertEquality(err.detail()["message"], "incompatible types: 'int' cannot be cast to '(lang.int:Signed32|boolean)'");
    assertEquality(err.message(),"{ballerina}TypeCastError");
}

function testCharArrayToStringArray() {
    string:Char[] arr = ["A", "a"];
    any a =  arr;
    test:assertTrue(a is string[]);
    string[] res = <string[]> a;
    test:assertEquals(res, ["A", "a"]);
}

function testMapOfCharToMapOfString() {
    map<string:Char> m = {};
    any a = m;
    test:assertTrue(a is map<string>);
    map<string> res = <map<string>> a;
    test:assertEquals(res, {});
}

type IntOneOrTwo 1|2;

function testFiniteTypeArrayToIntArray() {
    IntOneOrTwo[] intOneOrTwoArray = [1, 2];
    any anyIntOneOrTwoArray = intOneOrTwoArray;

    test:assertEquals(<int:Signed32[]> anyIntOneOrTwoArray, [1, 2]);
    test:assertEquals(<int:Signed16[]> anyIntOneOrTwoArray, [1, 2]);
    test:assertEquals(<int:Signed8[]> anyIntOneOrTwoArray, [1, 2]);
    test:assertEquals(<int:Unsigned32[]> anyIntOneOrTwoArray, [1, 2]);
    test:assertEquals(<int:Unsigned16[]> anyIntOneOrTwoArray, [1, 2]);
    test:assertEquals(<int:Unsigned8[]> anyIntOneOrTwoArray, [1, 2]);
}

type AOrBOrC "A"|"B"|"C";

function testFiniteTypeToStringArray() {
    AOrBOrC[] array = ["A", "C"];
    any anyArray = array;

    test:assertEquals(<string[]> anyArray, ["A", "C"]);
    test:assertEquals(<string:Char[]> anyArray, ["A", "C"]);
}

function testJsonIntToString() returns string|error {
    json j = 5;
    int value;
    value = check trap <int>j;
    return value.toString();
}

function stringtofloat(string value) returns float|error {
    float result;
    //string to float should be a conversion
    result = check floats:fromString(value);
    return result;
}

function inttostring(int value) returns (string) {
    string result;
    //int to string should be a conversion
    result = value.toString();
    return result;
}

function floattostring(float value) returns (string) {
    string result;
    //float to string should be a conversion
    result = value.toString();
    return result;
}

function booleantostring(boolean value) returns (string) {
    string result;
    //boolean to string should be a conversion
    result = value.toString();
    return result;
}

function booleanappendtostring(boolean value) returns (string) {
    string result;
    result = value.toString() + "-append-" + value.toString();
    return result;
}

function anyjsontostring() returns (string) {
    json j = {"a":"b"};
    // TODO: calling toJsonString on any is not allowed
    //any value = j;
    string result;
    result = j.toJsonString();
    return result;
}

function testJsonToStringCast() returns string|error {
    json j = "hello";
    string value;
    value = check trap <string> j;
    return value;
}

function testJSONObjectToStringCast() returns string|error {
    json j = {"foo":"bar"};
    var value = j.toJsonString();
    //TODO : Handle error

    return value;
}

function testJsonToInt() returns int|error {
    json j = 5;
    int value;
    value = check trap <int>j;
    return value;
}

function testJsonToFloat() returns float|error {
    json j = 7.65;
    float value;
    value = check trap <float>j;
    return value;
}

function testJsonToBoolean() returns boolean|error {
    json j = true;
    boolean value;
    value = check trap <boolean>j;
    return value;
}

function testStringToJson(string s) returns (json) {
    return s;
}

type Person record {
    string name;
    int age;
    map<anydata> address = {};
    int[] marks = [];
    Person | () parent = ();
    json info = {};
    anydata a = 0;
    float score = 0.0;
    boolean alive = true;
};

type Student record {
    string name;
    int age;
    map<any> address = {};
    int[] marks = [];
};

function testStructToStruct() returns (Student) {
    Person p = { name:"Supun",
                   age:25,
                   parent:{name:"Parent", age:50},
                   address:{"city":"Kandy", "country":"SriLanka"},
                   info:{status:"single"},
                   marks:[24, 81]
               };
    var p2 = p;
    return p2;
}

function testNullStructToStruct() returns Student {
    Person? p = ();
    return <Student> p;
}

function testStructAsAnyToStruct() returns Person|error {
    Person p1 = { name:"Supun",
                    age:25,
                    parent:{name:"Parent", age:50},
                    address:{"city":"Kandy", "country":"SriLanka"},
                    info:{status:"single"},
                    marks:[24, 81]
                };
    any a = p1;
    var p2 = check trap <Person> a;
    return p2;
}

function testAnyToStruct() returns Person {
    json address = {"city":"Kandy", "country":"SriLanka"};
    map<any> parent = {name:"Parent", age:50};
    map<any> info = {status:"single"};
    int[] marks = [24, 81];
    map<any> a = { name:"Supun",
                age:25,
                parent:parent,
                address:address,
                info:info,
                marks:marks
            };
    any b = a;
    var p2 = <Person> b;
    return p2;
}

function testAnyNullToStruct() returns Person {
    any a = ();
    var p = <Person> a;
    return p;
}

function testRecordToAny() returns (any) {
    Person p = { name:"Supun",
                   age:25,
                   parent:{name:"Parent", age:50},
                   address:{"city":"Kandy", "country":"SriLanka"},
                   info:{status:"single"},
                   marks:[24, 81]
               };
    return p;
}

function testMapToAny() returns (any) {
    map<any> m = {name:"supun"};
    return m;
}

function testIncompatibleJsonToInt() returns int|error {
    json j = "hello";
    int value;
    value = check ints:fromString(j.toJsonString());
    return value;
}

function testIntInJsonToFloat() returns float|error {
    json j = 7;
    float value;
    value = check floats:fromString(j.toJsonString());
    return value;
}

function testIncompatibleJsonToFloat() returns float|error {
    json j = "hello";
    float value;
    value = check floats:fromString(j.toJsonString());
    return value;
}

function testBooleanInJsonToInt() {
    json j = true;
    var result = trap <int> j;
    test:assertTrue(result is error);
    if (result is error) {
        test:assertEquals("{ballerina}TypeCastError", result.message());
        test:assertEquals("incompatible types: 'boolean' cannot be cast to 'int'",
        <string> checkpanic result.detail()["message"]);
    }
}

type Address record {
    string city;
    string country = "";
};

function testNullJsonToString() returns string {
    json j = {};
    string value;
    value = <string>j;
    return value;
}

function testNullJsonToInt() returns int {
    json j = {};
    int value;
    value = <int>j;
    return value;
}

function testNullJsonToFloat() returns float {
    json j = {};
    float value;
    value = <float>j;
    return value;
}

function testNullJsonToBoolean() returns boolean {
    json j = {};
    boolean value;
    value = <boolean> j;
    return value;
}

function testAnyIntToJson() returns json|error {
    any a = 8;
    json value;
    value = <json> a;
    return value;
}

function testAnyStringToJson() returns json {
    any a = "Supun";
    json value;
    value = <json> a;
    return value;
}

function testAnyBooleanToJson() returns json {
    any a = true;
    json value;
    value = <json> a;
    return value;
}

function testAnyFloatToJson() returns json {
    any a = 8.73;
    json value;
    value = <json> a;
    return value;
}

function testAnyMapToJson() returns json {
    map<any> m = {name:"supun"};
    any a = m;
    json value;
    value = <json> a;
    return value;
}

function testAnyStructToJson() returns json {
    Address adrs = {city:"CA"};
    any a = adrs;
    json value;
    value = <json> a;
    return value;
}

type JsonTypedesc typedesc<json>;

function testAnyNullToJson() returns json|error {
    anydata a = ();
    json value;
    value = check a.cloneWithType(JsonTypedesc);
    return value;
}

function testAnyJsonToJson() returns json|error {
    json j = {home:"SriLanka"};
    any a = j;
    json value;
    value = check trap <json> a;
    return value;
}

function testAnyNullToMap() returns map<any> {
    any a = ();
    map<any> value;
    value = <map<any>> a;
    return value;
}

function testAnyNullToXml() returns xml {
    any a = ();
    xml value;
    value = <xml> a;
    return value;
}

type A record {
    string x;
    int y;
};

type B record {
    string x;
};

function testCompatibleStructForceCasting() returns A|error {
    A a = {x: "x-valueof-a", y:4};
    B b = {x: "x-valueof-b"};

    b = a;
    A c = check trap <A> b;

    //TODO Handle error

    a.x = "updated-x-valueof-a";
    return c;
}

type ATypedesc typedesc<A>;

function testInCompatibleStructForceCasting() returns A|error {
    B b = {x: "x-valueof-b"};
    A a = check b.cloneWithType(ATypedesc);

    //TODO Handle error

    return a;
}

function testAnyToIntWithoutErrors() returns int|error {
    any a = 6;
    int s;
    s = check trap <int> a;
    //TODO Handle error

    return s;
}

function testAnyToFloatWithoutErrors() returns float|error {
    any a = 6.99;
    float s;
    s = check trap <float> a;
    // TODO Handle error

    return s;
}

function testAnyToBooleanWithoutErrors() returns boolean|error {
    any a = true;
    boolean s;
    s = check trap <boolean> a;
    //TODO Handle error

    return s;
}

function testAnyToBooleanWithErrors() returns boolean|error {
    map<any> m = { one: "one" };
    any a = m;
    boolean b;
    b = check trap <boolean> a;
    // TODO Handle error

    return b;
}

function testAnyNullToBooleanWithErrors() returns boolean|error {
    any a = ();
    boolean b;
    b = check trap <boolean> a;
    //TODO Handle error

    return b;
}

function testAnyToIntWithErrors() returns int|error {
    any a = "foo";
    int b;
    b = check trap <int> a;
    //TODO Handle error

    return b;
}

function testAnyNullToIntWithErrors() returns int|error {
    any a = ();
    int b;
    b = check trap <int> a;
    //TODO Handle error

    return b;
}

function testAnyToFloatWithErrors() returns float|error {
    any a = "foo";
    float b;
    b = check trap <float> a;
    //TODO Handle error

    return b;
}

function testAnyNullToFloatWithErrors() returns float|error {
    any a = ();
    float b;
    b = check trap <float> a;
    //TODO Handle error

    return b;
}

function testAnyToMapWithErrors() returns (map<any> | error) {
    any a = "foo";
    map<any> b;
    b = check trap <map<any>> a;
    //TODO Handle error

    return b;
}

function testAnyNullToString() returns string {
    any a = ();
    string s;
    s = a.toString();
    return s;
}

function testAnyBooleanToIntWithErrors() {
    any a = true;
    var result =  trap <int> a;
    assertEquality(result is error , true);
    error e = <error>result;
    assertEquality(e.detail()["message"], "incompatible types: 'boolean' cannot be cast to 'int'");
}

function testAnyBooleanToFloatWithErrors() {
    any a = true;
    var result = trap <float> a;
    assertEquality(result is error , true);
    error e = <error>result;
    assertEquality(e.detail()["message"], "incompatible types: 'boolean' cannot be cast to 'float'");
}

function testAnyBooleanToDecimalWithErrors() {
    any a = true;
    var result =  trap <decimal> a;
    assertEquality(result is error , true);
    error e = <error>result;
    assertEquality(e.detail()["message"], "incompatible types: 'boolean' cannot be cast to 'decimal'");
}

function testAnyBooleanToStringWithErrors() {
    any a = true;
    var result =  trap <string> a;
    assertEquality(result is error , true);
    error e = <error>result;
    assertEquality(e.detail()["message"], "incompatible types: 'boolean' cannot be cast to 'string'");
}

function testAnyBooleanToByteWithErrors() {
    any a = true;
    var result =  trap <byte> a;
    assertEquality(result is error , true);
    error e = <error>result;
    assertEquality(e.detail()["message"], "incompatible types: 'boolean' cannot be cast to 'byte'");
}

function testAnyBooleanToUnionWithErrors() {
    any a = true;
    var result =  trap <int|string> a;
    assertEquality(result is error , true);
    error e = <error>result;
    assertEquality(e.detail()["message"], "incompatible types: 'boolean' cannot be cast to '(int|string)'");
}

function testSameTypeCast() returns int {
    int a = 10;

    int b = <int> a;
    return b;
}

function testJSONValueCasting() returns [string|error, int|error, float|error, boolean|error] {

    // json to string
    json j1 = "hello";
    var s = <string> j1;

    // json to int
    json j2 = 4;
    var i = <int> j2;

    // json to float
    json j3 = 4.2;
    var f = <float> j3;

    // json to boolean
    json j4 = true;
    var b = <boolean> j4;

    return [s, i, f, b];
}

function testAnyToTable() {
    table<Employee> tb = table [
                    {id:1, name:"Jane"},
                    {id:2, name:"Anne"}
        ];

    any anyValue = tb;
    var casted = <table<Employee>> anyValue;
    table<Employee>  castedValue = casted;
    assertEquality("[{\"id\":1,\"name\":\"Jane\"},{\"id\":2,\"name\":\"Anne\"}]", castedValue.toString());
}

type Employee record {
    int id;
    string name;
};


function testAnonRecordInCast() returns record {| string name; |} {
    return <record {| string name; |}>{ name: "Pubudu" };
}

///////////////// Test casting of immutable array type //////////////////////

function testCastOfReadonlyIntArrayToByteArray() {
    readonly & int[] a = [1, 255];
    any b = a;

    assertEquality(true, b is (float|byte)[]);
    assertEquality(false, b is (boolean|float)[]);

    byte[] c = <byte[]> b;

    assertEquality(true, c === b);
    assertEquality("[1,255]", c.toString());
    assertEquality(1, c[0]);
    assertEquality(255, c[1]);
}

function testCastOfReadonlyIntArrayToByteArrayNegative() {
    readonly & int[] e = [1, 100, 1000];
    any f = e;
    byte[]|error g = trap <byte[]> f;
    assertEquality(true, g is error);
    error err = <error> g;
    assertEquality("{ballerina}TypeCastError", err.message());
    assertEquality("incompatible types: 'int[] & readonly' cannot be cast to 'byte[]'", <string> checkpanic err.detail()["message"]);
}

function testCastOfReadonlyAnyToByteArray() {
    readonly & any x = [1, 2, 3];
    any xx = x;
    byte[] y = <byte[]> xx;
    assertEquality(true, xx === y);
    assertEquality(1, y[0]);
}

function testCastOfReadonlyArrayToUnion() {
    readonly & int[] a = [1, 255];
    any b = a;
    (float|byte)[] i = <(float|byte)[]> b;
    assertEquality(true, i === b);
    assertEquality("[1,255]", i.toString());

    readonly & float[] d = [1, 2.5, 27.5f];
    any e = d;
    (int|float|byte)[] f = <(int|float|byte)[]> e;
    assertEquality("[1.0,2.5,27.5]", f.toString());
}

function testCastOfReadonlyUnionArrayToByteArray() {
    readonly & (int|byte)[] a = [1,2,3];
    any b = a;
    byte[] c = <byte[]> b;
    assertEquality("[1,2,3]", c.toString());

    readonly & (any|byte)[] d = [1,2,3];
    any e = d;
    byte[] f = <byte[]> e;
    assertEquality("[1,2,3]", f.toString());
}

type Foo record {|
    string s;
    int[] arr;
|};

type Bar record {|
    string s;
    byte[] arr;
|};

function testCastOfReadonlyRecord() {
    (Foo & readonly) f = {s: "a", arr: [1,2,3]};
    any a = f;
    Bar b = <Bar> a;
    assertEquality(true, b === a);
    assertEquality("{\"s\":\"a\",\"arr\":[1,2,3]}", b.toString());
}

function testCastOfReadonlyRecordNegative() {
    (Foo & readonly) f = {s: "a", arr: [1,2,300]};
    any a = f;
    Bar|error b = trap <Bar> a;
    assertEquality(true, b is error);
    error err = <error> b;
    string errMsg = "incompatible types: '(Foo & readonly)' cannot be cast to 'Bar': " +
    "\n\t\tfield 'arr' in record 'Bar' should be of type 'byte[]', found '[1,2,300]'";
    assertEquality("{ballerina}TypeCastError", err.message());
    assertEquality(errMsg, <string> checkpanic err.detail()["message"]);
}

const FOO = "foo";

function testCastOfReadonlyStringArrayToStringConstantArray() {

    readonly & string[] d = [FOO, FOO];
    any e =  d;
    FOO[] f = <FOO[]> e;

    assertEquality(true, f === e);
    assertEquality("[\"foo\",\"foo\"]", f.toString());
}

function testCastOfTwoDimensionalIntArrayToByteArray() {
    (readonly & int[][]) a = [[1,2,3], [4,5,6]];
    any b = a;
    byte[][] c = <byte[][]> b;

    assertEquality(true, c === b);
    assertEquality("[[1,2,3],[4,5,6]]", c.toString());
}

function testCastJsonToMapOfAnydata() {
    json j1 = {a1: (), a2: 1, a3: 1.0f, a4: 1.2d, a5: "a5", a6: true};
    map<anydata> m1 = <map<anydata>> j1;
    assertEquality("{\"a1\":null,\"a2\":1,\"a3\":1.0,\"a4\":1.2,\"a5\":\"a5\",\"a6\":true}", m1.toString());

    json j2 = {a1: null, a2: 1, a3: 1.0, a4: 1d, a5: "a5", a6: false, a7: {a1: j1}};
    map<anydata> m2 = <map<anydata>> j2;
    assertEquality("{\"a1\":null,\"a2\":1,\"a3\":1.0,\"a4\":1,\"a5\":\"a5\",\"a6\":false,\"a7\":{\"a1\":{\"a1\":null,\"a2\":1,\"a3\":1.0,\"a4\":1.2,\"a5\":\"a5\",\"a6\":true}}}", m2.toString());
}

function testCastMapOfJsonToMapOfAnydata() {
    map<json> j1 = {a1: (), a2: 1, a3: 1.0f, a4: 1.2d, a5: "a5", a6: true};
    map<anydata> m1 = <map<anydata>> j1;
    assertEquality("{\"a1\":null,\"a2\":1,\"a3\":1.0,\"a4\":1.2,\"a5\":\"a5\",\"a6\":true}", m1.toString());
    anydata any1 = <map<anydata>> j1;
    assertEquality("{\"a1\":null,\"a2\":1,\"a3\":1.0,\"a4\":1.2,\"a5\":\"a5\",\"a6\":true}", any1.toString());

    map<json> j2 = {a1: null, a2: 1, a3: 1.0, a4: 1d, a5: "a5", a6: false, a7: {a1: j1}};
    map<anydata> m2 = <map<anydata>> j2;
    assertEquality("{\"a1\":null,\"a2\":1,\"a3\":1.0,\"a4\":1,\"a5\":\"a5\",\"a6\":false,\"a7\":{\"a1\":{\"a1\":null,\"a2\":1,\"a3\":1.0,\"a4\":1.2,\"a5\":\"a5\",\"a6\":true}}}", m2.toString());
    anydata any2 = <map<anydata>> j2;
    assertEquality("{\"a1\":null,\"a2\":1,\"a3\":1.0,\"a4\":1,\"a5\":\"a5\",\"a6\":false,\"a7\":{\"a1\":{\"a1\":null,\"a2\":1,\"a3\":1.0,\"a4\":1.2,\"a5\":\"a5\",\"a6\":true}}}", any2.toString());

    map<json>|json j3 = {a1: null, a2: 1, a3: 1.0, a4: 1d, a5: "a5", a6: false, a7: {a1: j1}};
    map<anydata> m3 = <map<anydata>> j3;
    assertEquality("{\"a1\":null,\"a2\":1,\"a3\":1.0,\"a4\":1,\"a5\":\"a5\",\"a6\":false,\"a7\":{\"a1\":{\"a1\":null,\"a2\":1,\"a3\":1.0,\"a4\":1.2,\"a5\":\"a5\",\"a6\":true}}}", m2.toString());
    anydata any3 = <map<anydata>> j3;
    assertEquality("{\"a1\":null,\"a2\":1,\"a3\":1.0,\"a4\":1,\"a5\":\"a5\",\"a6\":false,\"a7\":{\"a1\":{\"a1\":null,\"a2\":1,\"a3\":1.0,\"a4\":1.2,\"a5\":\"a5\",\"a6\":true}}}", any2.toString());

}

function testInvalidTypeCastMapOfJsonToMapOfBasicType() {
    map<json> j1 = {a1: (), a2: 1, a3: 1.0f, a4: 1.2d, a5: "a5", a6: true};
    map<int>|error m1 = trap (<map<int>> j1);
    error expectedMsgM1 = error("{ballerina}TypeCastError\",message=\"incompatible types: 'map<json>' cannot be cast to 'map<int>'");
    assertEquality(expectedMsgM1.toString(), m1 is error ? m1.toString() : m1.toString());

    map<string>|error m2 = trap (<map<string>> j1);
    error expectedMsgM2 = error("{ballerina}TypeCastError\",message=\"incompatible types: 'map<json>' cannot be cast to 'map<string>'");
    assertEquality(expectedMsgM2.toString(), m2 is error ? m2.toString() : m2.toString());

    json j2 = {a1: (), a2: 1, a3: 1.0f, a4: 1.2d, a5: "a5", a6: true};
    map<int>|error m3 = trap (<map<int>> j2);
    error expectedMsgM3 = error("{ballerina}TypeCastError\",message=\"incompatible types: 'map<json>' cannot be cast to 'map<int>'");
    assertEquality(expectedMsgM3.toString(), m3 is error ? m3.toString() : m3.toString());
}

function testInvalidTypeCastJsonToMapOfAnydata() {
    json j1 = [{a1: (), a2: 1, a3: 1.0f, a4: 1.2d, a5: "a5", a6: true}];
    map<anydata>|error m1 = trap (<map<anydata>> j1);
    error expectedMsgM1 = error("{ballerina}TypeCastError\",message=\"incompatible types: 'json[]' cannot be cast to 'map<anydata>'");
    assertEquality(expectedMsgM1.toString(), m1 is error ? m1.toString() : m1.toString());

    json j2 = "json";
    map<anydata>|error m3 = trap (<map<anydata>> j2);
    error expectedMsgM3 = error("{ballerina}TypeCastError\",message=\"incompatible types: 'string' cannot be cast to 'map<anydata>'");
    assertEquality(expectedMsgM3.toString(), m3 is error ? m3.toString() : m3.toString());
}

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
    panic error(ASSERTION_ERROR_REASON,
                        message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}

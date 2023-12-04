// Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

import ballerina/jballerina.java;
//import ballerina/test;

type intArray int[];
type intArray2d int[][];
type booleanArray boolean[];
type type1 [int, boolean, decimal, string];
type type2 map<int|string>;
type type3 map<boolean>;
type type4 map<map<int>>;
type type5 map<int>[];
type mapIntArray map<int[]>;
type jsonType json;
type int2Array int[2];
type char2Array string:Char[2];
type char2DFixedArray string:Char[3][4];
type int2DFixedArray int[2][1];
type intTuple [[int], [int]];
type intTupleRest [[int], [int]...];
type intStringTuple [[int], [string]];
type intStringTupleRest [[int], [string]...];
type NilType ();
type BooleanType boolean;
type intType int;
type floatType float;
type decimalType decimal;
type stringType string;
type charType string:Char;
type ByteType byte;
type intUnsigned8 int:Unsigned8;
type intSigned8 int:Signed8;
type intUnsigned16 int:Unsigned16;
type intSigned16 int:Signed16;
type intUnsigned32 int:Unsigned32;
type intSigned32 int:Signed32;
type strinttuple [int, int];
type stringArr string[];
type tuple1 [[int, string], [boolean, float]];
type tuple2 [[float, string], [boolean, decimal]...];
type stringArrayType string[];

type Rec1 record {|
    string name;
    int age;
    boolean isMarried = true;
    float...;
|};

type Rec2 record {|
    Rec1 student;
    string address;
    int count;
    float weight = 18.3;
    boolean...;
|};

type Rec3 record {|
    Rec1 student;
|};

type Rec4 record {|
    int age;
    float gain = 18.3;
    boolean...;
|};

type Rec5 record {|
    float age;
    int gain = 18;
|};

type Union1 map<int>|map<float>|int|string|float|Rec4|Rec5|[float, int]|int[]|float[]|[int, float];
type Union1Sub1 Rec4|map<int>|map<float>|int|string|float|Rec5|[float, int]|int[]|float[]|[int, float];
type RecUnion1 record {|
    Union1 val;
|};
type MapUnion1 map<Union1>;
type RecMapUnion1 RecUnion1|MapUnion1;
type TupleUnion1 [Union1];
type ArrayUnion1 Union1[];

type intArrayReadonly int[] & readonly;
type intArray2dReadonly int[][] & readonly;
type booleanArrayReadonly boolean[] & readonly;
type type1Readonly [int, boolean, decimal, string] & readonly;
type type2Readonly map<int|string> & readonly;
type type3Readonly map<boolean> & readonly;
type type4Readonly map<map<int>> & readonly;
type type5Readonly map<int>[] & readonly;
type mapIntArrayReadonly map<int[]> & readonly;
type jsonTypeReadonly json & readonly;
type int2ArrayReadonly int[2] & readonly;
type char2ArrayReadonly string:Char[2] & readonly;
type char2DFixedArrayReadonly string:Char[3][4] & readonly;
type int2DFixedArrayReadonly int[2][1] & readonly;
type intTupleReadonly [[int], [int]] & readonly;
type intTupleRestReadonly [[int], [int]...] & readonly;
type intStringTupleReadonly [[int], [string]] & readonly;
type intStringTupleRestReadonly [[int], [string]...] & readonly;
type NilTypeReadonly () & readonly;
type BooleanTypeReadonly boolean & readonly;
type intTypeReadonly int & readonly;
type floatTypeReadonly float & readonly;
type decimalTypeReadonly decimal & readonly;
type stringTypeReadonly string & readonly;
type charTypeReadonly string:Char & readonly;
type ByteTypeReadonly byte & readonly;
type intUnsigned8Readonly int:Unsigned8 & readonly;
type intSigned8Readonly int:Signed8 & readonly;
type intUnsigned16Readonly int:Unsigned16 & readonly;
type intSigned16Readonly int:Signed16 & readonly;
type intUnsigned32Readonly int:Unsigned32 & readonly;
type intSigned32Readonly int:Signed32 & readonly;
type strinttupleReadonly [int, int] & readonly;
type stringArrReadonly string[] & readonly;
type tuple1Readonly [[int, string], [boolean, float]] & readonly;
type tuple2Readonly [[float, string], [boolean, decimal]...] & readonly;
type stringArrayTypeReadonly string[] & readonly;

type Rec1ReadOnly Rec1 & readonly;
type Rec2ReadOnly Rec2 & readonly;
type Rec3ReadOnly Rec3 & readonly;

public function main() {
    testParsingCharacterStreamToTypes();
//    testStreamsAndTypesBal();
}

type Union1Decimal Union1|decimal;

function testParsingCharacterStreamToTypes() {

    [string, typedesc<anydata>][] testPositiveCases = [

    ];

    [string, typedesc<anydata>][] testNegativeCases = [
    ];

    [string, typedesc<anydata>][] notWorkingPosCases = [
    ];

    [string, typedesc<anydata>][] positiveCases = [
    [string `[1, 2, 3]`, intArray],
    ["[12, true, 123.4, \"hello\"]", type1],
    ["[12, 13]", intArray],
    ["[[12], [13]]", intArray2d],
    ["{\"id\": false, \"age\": true}", type3],
    ["{\"key1\": {\"id\": 12, \"age\": 24}, \"key2\": {\"id\": 12, \"age\": 24}}", type4],
    ["[{\"id\": 12, \"age\": 24}, {\"id\": 12, \"age\": 24}]", type5],
    ["{\"key1\": [12, 13], \"key2\": [132, 133]}", mapIntArray],
    ["[12]", int2Array],
    ["[[1],[2]]", int2DFixedArray],
    ["[[1],[2]]", intTuple],
    ["[[1],[2],[3]]", intTupleRest],
    ["[[1]]", intTupleRest],
    ["[[1],[\"2\"],[\"3\"]]", intStringTupleRest],
    ["[[1]]", intStringTupleRest],
    ["[[1],[2]]", intTuple],
    ["[[1],[2]]", int2DFixedArray],
    ["true", BooleanType],
    ["false", BooleanType],
    ["12", intType],
    ["12.3", floatType],
    ["12.3", decimalType],
    ["\"hello\"", stringType],
    ["\"h\"", charType],
    ["12", ByteType],
    ["13", intUnsigned8],
    ["14", intSigned8],
    ["15", intUnsigned16],
    ["16", intSigned16],
    ["17", intUnsigned32],
    ["18", intSigned32],
    ["null", NilType],
    [string `{"name": "John", "age": 30, "height": 1.8}`, Rec1],
    [string `{"student": {"name": "John", "age": 30, "height": 1.8}}`, Rec3],
    [string `{"isSingle": true, "address": "this is address", "count": 14, "student": {"name": "John", "age": 30, "height": 1.8}}`, Rec2],

    [string `[1, 2, 3]`, intArrayReadonly],
    ["[12, true, 123.4, \"hello\"]", type1Readonly],
    ["[12, 13]", intArrayReadonly],
    ["[[12], [13]]", intArray2dReadonly],
    ["{\"id\": false, \"age\": true}", type3Readonly],
    ["{\"key1\": {\"id\": 12, \"age\": 24}, \"key2\": {\"id\": 12, \"age\": 24}}", type4Readonly],
    ["[{\"id\": 12, \"age\": 24}, {\"id\": 12, \"age\": 24}]", type5Readonly],
    ["{\"key1\": [12, 13], \"key2\": [132, 133]}", mapIntArrayReadonly],
    ["[12]", int2ArrayReadonly],
    ["[[1],[2]]", int2DFixedArrayReadonly],
    ["[[1],[2]]", intTupleReadonly],
    ["[[1],[2],[3]]", intTupleRestReadonly],
    ["[[1]]", intTupleRestReadonly],
    ["[[1],[\"2\"],[\"3\"]]", intStringTupleRestReadonly],
    ["[[1]]", intStringTupleRestReadonly],
    ["[[1],[2]]", intTupleReadonly],
    ["[[1],[2]]", int2DFixedArrayReadonly],
    ["true", BooleanTypeReadonly],
    ["false", BooleanTypeReadonly],
    ["12", intTypeReadonly],
    ["12.3", floatTypeReadonly],
    ["12.3", decimalTypeReadonly],
    ["\"hello\"", stringTypeReadonly],
    ["\"h\"", charTypeReadonly],
    ["12", ByteTypeReadonly],
    ["13", intUnsigned8Readonly],
    ["14", intSigned8Readonly],
    ["15", intUnsigned16Readonly],
    ["16", intSigned16Readonly],
    ["17", intUnsigned32Readonly],
    ["18", intSigned32Readonly],
    ["null", NilTypeReadonly],

    [string `{"name": "John", "age": 30, "height": 1.8}`, Rec1ReadOnly],
    [string `{"student": {"name": "John", "age": 30, "height": 1.8}}`, Rec3ReadOnly],
    [string `{"isSingle": true, "address": "this is address", "count": 14, "student": {"name": "John", "age": 30, "height": 1.8}}`, Rec2ReadOnly],
    [string `{"gain": 122, "age": 130}`, Union1],
    [string `{"gain": 122, "age": 130}`, Union1Sub1],

    [string `{"gain": 122, "age": 130}`, Union1],
    [string `[12, 13.4]`, Union1],
    [string `{"val" : {"gain": 122, "age": 130}}`, RecUnion1],
    [string `[{"gain": 122, "age": 130}]`, TupleUnion1],
    [string `[{"gain": 122, "age": 130}]`, ArrayUnion1],
    [string `{"val" : [12, 13.4]}`, RecUnion1],
    [string `[[12, 13.4]]`, TupleUnion1],
    [string `[[12, 13.4]]`, ArrayUnion1],
    [string `"apple"`, Union1],
    [string `13.5`, Union1],
    [string `{"val" : "apple"}`, RecUnion1],
    [string `[13.6]`, TupleUnion1],
    [string `[13.6]`, ArrayUnion1],
    [string `{"val" : "apple"}`, RecUnion1],
    [string `[13.7]`, TupleUnion1],
    [string `[13.7]`, ArrayUnion1],
    ["[12, true, 123.4, \"hello\"]", jsonType],
    ["{\"id\": 12, \"name\": \"John\"}", type2],
    ["{\"id\": 12, \"name\": \"John\"}", jsonType],

    [string `{"val" : {"gain": 122, "age": 130}}`, RecMapUnion1],
    [string `{"val" : {"gain": 122, "age": 130}}`, MapUnion1],
    [string `{"val" : {"gain": 122, "age": 130}}`, RecUnion1],
    [string `12.4`, Union1],
    [string `12.4`, Union1],
    [string `12.4`, Union1Decimal],
    [string `"hey"`, Union1],
    [string `{"gain": 122, "age": 130}`, Union1] // add cases with strings also
    ];

    [string, typedesc<anydata>][] negativeCases = [
    [string `[1, 2, 3]`, booleanArray],
    ["[\"1\"]", type1],
    ["[\"1\"]", char2Array],
    ["[2]", type1],
    ["[[\"a\"],[\"b\"]]", char2DFixedArray],
    ["[[\"a\",\"a\",\"a\",\"a\"],[\"b\",\"b\",\"b\",\"b\"]]", char2DFixedArray],
    ["[[1],[2],[3]]", char2DFixedArray],
    ["[[1],[2],[3]]", int2DFixedArray],
    ["[[1],[true]]", int2DFixedArray],
    ["[1,2]", int2DFixedArray],
    ["[[1],[2]]", intArray],
    ["[[1],[2],[3]]", intTuple],
    ["()", NilType], // not supported, is this the correct behavior
    ["true", intSigned16],
    ["false", NilType],
    ["12", BooleanType],
    ["12.3", BooleanType],
    ["\"hello\"", BooleanType],
    ["\"h\"", intSigned32],

    [string `[1, 2, 3]`, booleanArrayReadonly],
    ["[\"1\"]", type1Readonly],
    ["[\"1\"]", char2ArrayReadonly],
    ["[2]", type1Readonly],
    ["[[\"a\"],[\"b\"]]", char2DFixedArrayReadonly],
    ["[[\"a\",\"a\",\"a\",\"a\"],[\"b\",\"b\",\"b\",\"b\"]]", char2DFixedArrayReadonly],
    ["[[1],[2],[3]]", char2DFixedArrayReadonly],
    ["[[1],[2],[3]]", int2DFixedArrayReadonly],
    ["[[1],[true]]", int2DFixedArrayReadonly],
    ["[1,2]", int2DFixedArrayReadonly],
    ["[[1],[2]]", intArrayReadonly],
    ["[[1],[2],[3]]", intTupleReadonly],
    ["()", NilTypeReadonly], // not supported, is this the correct behavior
    ["true", intSigned16Readonly],
    ["false", NilTypeReadonly],
    ["12", BooleanTypeReadonly],
    ["12.3", BooleanTypeReadonly],
    ["\"hello\"", BooleanTypeReadonly],
    ["\"h\"", intSigned32Readonly],

    ["[\"123\"]", char2Array]
    ];

    foreach [string, typedesc<anydata>] [givenStr, targetType] in testPositiveCases {
        anydata|error result = trap convertStringToType(givenStr, targetType);
            if (result is error) {
                println("testPositiveCases WRONG RESULT:: error: " + givenStr + "  ***  " + targetType.toString());
            }
    }

    foreach [string, typedesc<anydata>] [givenStr, targetType] in testNegativeCases {
        anydata|error result = trap convertStringToType(givenStr, targetType);
//        test:assertEquals(result is error, true);
        if (!(result is error)) {
            println("testNegativeCases WRONG RESULT:: not an error: " + givenStr + "  ***  " + targetType.toString());
        }
    }
//
//    foreach [string, typedesc<anydata>] [givenStr, targetType] in positiveCases {
//        anydata|error result = trap convertStringToType(givenStr, targetType);
//        if (result is error) {
//            panic error(givenStr + targetType.toString());
//        }
//    }

    foreach [string, typedesc<anydata>] [givenStr, targetType] in notWorkingPosCases {
            anydata|error result = trap convertStringToType(givenStr, targetType);
            if (result is error) {
                println("positiveCases WRONG RESULT:: error: " + givenStr + "  ***  " + targetType.toString());
            }
        }

    foreach [string, typedesc<anydata>] [givenStr, targetType] in positiveCases {
        anydata|error result = trap convertStringToType(givenStr, targetType);
        if (result is error) {
            println("positiveCases WRONG RESULT:: error: " + givenStr + "  ***  " + targetType.toString());
        }
    }

    foreach [string, typedesc<anydata>] [givenStr, targetType] in negativeCases {
        anydata|error result = trap convertStringToType(givenStr, targetType);
//        test:assertEquals(result is error, true);
        if (!(result is error)) {
            println("negativeCases WRONG RESULT:: not an error: " + givenStr + "  ***  " + targetType.toString());
        }
    }
}

public isolated function convertStringToType(string str, typedesc<anydata> t = <>) returns t  = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Stream"
} external;

function testStreamsAndTypesBal() {
//    streamWithType([intArray]);
//    streamWithType([intArray, strinttuple, typeof [1,2]]);
//    streamWithType([tuple1, tuple2]);
//    streamWithType([stringType]);
    streamWithType([stringType,stringArrayType]);
}

public isolated function streamWithType(typedesc<anydata>[] typedescArr) = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Stream"
} external;

function println(string value) {
handle strValue = java:fromString(value);
handle stdout1 = stdout();
printlnInternal(stdout1, strValue);
}
public function stdout() returns handle = @java:FieldGet {
name: "out",
'class: "java/lang/System"
} external;

public function printlnInternal(handle receiver, handle strValue) = @java:Method {
name: "println",
'class: "java/io/PrintStream",
paramTypes: ["java.lang.String"]
} external;

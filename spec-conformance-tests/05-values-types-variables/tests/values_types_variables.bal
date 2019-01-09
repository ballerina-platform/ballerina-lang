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

const int I = 1;
const string S = "test string const";
const float F = 1.0;

// Values can be stored in variables or as members of structures.

// A simple value is stored directly in the variable or structure.
@test:Config {}
function testSimpleValuesStoredInStructures() {
    int i = I;
    int[] s1 = [12, 13, i, 14];
    i = I + 10;
    test:assertEquals(s1[2], I, msg = "expected array member to not have changed");

    string s = S;
    (int, string) s2 = (12, s);
    s = "test string 2";
    test:assertEquals(s2[1], S, msg = "expected tuple member to not have changed");

    float f = F;
    map<float> s3 = { one: f, two: 2.00 };
    f = 3.0;
    test:assertEquals(s3.one, F, msg = "expected map member to not have changed");
}

// However, for other types of value, what is stored in the variable or member is a 
// reference to the value; the value itself has its own separate storage.
@test:Config {}
function testNonSimpleValuesStoredInStructures() {
    int[] s1 = [12, 13, 14, 15];
    int[][] s2 = [s1, [1, 2, 3]];
    s1[0] = I;
    test:assertEquals(s2[0][0], I, msg = "expected array member to have been updated");

    FooRecord f1 = { fooFieldOne: "test string 1" };
    (int, FooRecord) s3 = (1, f1);
    f1.fooFieldOne = S;
    FooRecord f2 = s3[1];
    test:assertEquals(f2.fooFieldOne, S, msg = "expected tuple member to have been updated");

    FooObject f3 = new("test string 3");
    FooObject f4 = new("test string 4");
    map<FooObject> s4 = { one: f3, two: f4 };
    f3.fooFieldOne = S;
    test:assertEquals(s4.one.fooFieldOne, S, msg = "expected map member to not have been updated");
}

// References make it possible for distinct members of a structure to refer to values that are
// identical, in the sense that they are stored in the same location.
@test:Config {}
function testDistinctStructureMembersReferringToSameValue() {
    int[4] s1 = [12, 13, 14, 15];
    int[4][4] s2 = [s1, [1, 2, 3, 4], s1, [21, 22, 23, 24]];
    test:assertTrue(s2[0] === s2[2], msg = "expected values to be at the same location");

    FooRecord f1 = { fooFieldOne: "test string 1" };
    (int, FooRecord, FooRecord) s3 = (1, f1, f1);
    test:assertTrue(s3[1] === s3[2], msg = "expected values to be at the same location");

    FooObject f2 = new("test string 2");
    FooObject f3 = new("test string 3");
    map<FooObject> s4 = { one: f2, two: f3, three: f2 };
    test:assertTrue(s4.one === s4.three, msg = "expected values to be at the same location");
}

// For an immutable value, looking like a type and belonging to a type are the same thing.
@test:Config {}
function testLooksLikeAndBelongsToOfImmutableValues() {
    map<string|int> mutableMap = { fieldOne: "valueOne", fieldTwo: "valueTwo" };
    var immutableMap = mutableMap.freeze();

    if !(immutableMap is map<string>) {
        test:assertFail(msg = "expected immutable value to belong to type map<string>");
    }

    var result = trap map<string>.stamp(immutableMap);
    if result is error {
        test:assertFail(msg = "expected immutable value to look like map<string>");
    }
}

type One 1;

// Values can look like and belong to arbitrarily many types, even though they look like
// or belong to exactly one basic type
@test:Config {}
function testValueBelongingToMultipleTypes() {
    int varWithValueOne = 1;
    int|string varWithValueTwo = 1;
    One varWithValueThree = 1;
    testIfValueIsOne(varWithValueOne);
    testIfValueIsOne(varWithValueTwo);
    testIfValueIsOne(varWithValueThree);
}

function testIfValueIsOne(any value) {
    match value {
        1 => test:assertTrue(true);
        _ => test:assertFail(msg = "expected value to be 1");
    }
}

// Most basic types of structured values (along with one basic type of simple value) are
// iterable, meaning that a value of the type can be accessed as a sequence of simpler values.
@test:Config {}
function testIterableTypes() {
    int[] iterableArray = [1, 2, 3];
    int count = 0;
    foreach int value in iterableArray {
        count += value;
    }
    test:assertEquals(count, 6, msg = "expected int array to iterate over its members");

    map<string> iterableMap = { fieldOne: "valueOne", fieldTwo: "valueTwo", fieldThree: "valueThree" };
    string result = "";
    foreach (string, string) (key, value) in iterableMap {
        result += value;
    }
    test:assertEquals(result, "valueOnevalueTwovalueThree", msg = "expected map to iterate over its members");

    BazRecord iterableRecord = { bazFieldOne: 2.2, bazFieldTwo: true, bazFieldThree: "valueThree" };
    result = "";
    foreach (string, any) (key, value) in iterableRecord {
        result += <string>value;
    }
    test:assertEquals(result, "2.2truevalueThree", msg = "expected record type to iterate over its fields");

    BarRecord barRecord1 = { barFieldOne: 1 };
    BarRecord barRecord2 = { barFieldOne: 2 };
    BarRecord barRecord3 = { barFieldOne: 3 };
    table<BarRecord> iterableTable = table{};
    _ = iterableTable.add(barRecord1);
    _ = iterableTable.add(barRecord2);
    _ = iterableTable.add(barRecord3);

    count = 0;
    foreach BarRecord barRecord in iterableTable {
        count += barRecord.barFieldOne;
    }
    test:assertEquals(count, 6, msg = "expected table type to iterate over its entries");

    xml bookstore = xml `<bookstore>
                            <book category="cooking">
                                <title lang="en">Title1</title>
                                <author>Giada De Laurentiis</author>
                            </book>
                            <book category="children">
                                <title lang="en">Title2</title>
                                <author>J. K. Rowling</author>
                            </book>
                            <book category="web" cover="paperback">
                                <title lang="en">Title3</title>
                                <author>Erik T. Ray</author>
                            </book>
                        </bookstore>`;
    result = "";
    foreach var x in bookstore["book"] {
        if x is xml {
            result += x["title"].getTextValue();
        }
    }
    test:assertEquals(result, "Title1Title2Title3", msg = "expected xml to iterate over its elements");
}

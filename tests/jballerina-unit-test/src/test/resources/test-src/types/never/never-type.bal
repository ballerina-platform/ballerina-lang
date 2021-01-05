// Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
import ballerina/lang.'xml;

const ASSERTION_ERROR_REASON = "AssertionError";

//------------ Testing a function with 'never' return type ---------

function functionWithNeverReturnType() returns never {
    string a = "hello";
    if (a == "a") {
        a = "b";
    } else {
        a = "c";
    }
}

function testTypeOfNeverReturnTypedFunction() {
    any|error expectedFunctionType = typedesc<function () returns (never)>;

    typedesc <any|error> actualFunctionType = typeof functionWithNeverReturnType;
    
    if (actualFunctionType is typedesc<function () returns (never)>) {
        return;
    }
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expectedFunctionType.toString() + "', found '" + actualFunctionType.toString () + "'");
}

function testNeverReturnTypedFunctionCall() {
    functionWithNeverReturnType();
}

//------------ Testing record type with 'never' field ---------

type InclusiveRecord record {
    int j;
    never p?;
}; 

type ExclusiveRecord record {|
    int j;
    never p?;
|}; 

function testInclusiveRecord() {
    InclusiveRecord inclusiveRecord = {j:0, "q":1};
}

function testExclusiveRecord() {
    ExclusiveRecord exclusiveRecord = {j:0};
}


//------------- Testing XML<never> -----------------

function testXMLWithNeverType() {
    xml<never> x = <xml<never>> 'xml:concat();  //generates an empty XML sequence and assign it to XML<never>
    xml<never> a = xml ``;
    string testString1 = a;
    xml<never> b = <xml<never>> 'xml:createText("");
    xml c = xml ``;
    'xml:Text d = xml ``;
    string testString2 = d;
    xml<'xml:Text> e = a;
    string testString3 = e;
    xml f = a;
    xml<xml<never>> g = xml ``;
    string testString4 = g;
    xml<xml<'xml:Text>> h = xml ``;
    string testString5 = h;
    string empty = "";
    'xml:Text j = xml `${empty}`;
    xml k = xml `${empty}`;
    xml<never>|'xml:Text l = xml ``;
    string testString6 = l;
    xml<never> & readonly m =  xml ``;
    string|'xml:Text n = a;
    int|string t = a;

    string|'xml:Text p = d;
    string q = p;

    string|'xml:Text r = a;
    string s = r;
}

//---------------Test 'never' types with 'union-type' descriptors ------------
function testNeverWithUnionType1() {
    string|never j;
}

function testNeverWithUnionType2() {
    float|(int|never) j;
}

function testNeverWithUnionType3() {
    string|never j = "sample";
    string h = j;
}

// -------------Test 'never' with table key constraints --------------
type Person record {
  readonly string name;
  int age;
};

type PersonalTable table<Person> key<never>;

function testNeverWithKeyLessTable() {
    PersonalTable personalTable = table [
        { name: "DD", age: 33},
        { name: "XX", age: 34}
    ];
}

type SomePersonalTable table<Person> key<never|string>;

function testNeverInUnionTypedKeyConstraints() {
    SomePersonalTable somePersonalTable = table key(name) [
        { name: "MM", age: 33},
        { name: "PP", age: 34}
    ];
}

// --------------Test 'never' with 'future' type ----------------------

function testNeverAsFutureTypeParam() {
    future<never> someFuture;
}


// --------------Test 'never' with 'map' type ----------------------

function testNeverAsMappingTypeParam() {
    map<never> mp;
}

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

type Age record {
    int age;
    string format;
};

type ClosedAge record {
    int age;
    string format;
    !...
};

type Person record {
    string name;
    boolean married;
    Age age;
    (string, int) extra;
    !...
};

function testUndefinedSymbol() {

    Age a = {age:12, format: "Y", three: "three"};
    {name: fName, married, age: { age: theAge, format, !...}, ...theMap} = {name: "Peter", married: true, age: a, extra: ("extra", 12)};
}

function testClosedRecordVarRef() {
    string fName;
    boolean married;
    int theAge;
    string format;
    string extraLetter;
    int extraInt;
    map theMap;

    Age age1 = {age:12, format: "Y", three: "three"};
    ClosedAge age2 = {age:12, format: "Y"};
    {name: fName, married, age: { age: theAge, format, !...}, ...theMap} = {name: "Peter", married: true, age: age1, extra: ("extra", 12)}; // age1 is not a closed record
    {name: fName, married, age: { age: theAge, format, !...}, ...theMap} = {name: "Peter", married: true, age: age2, extra: ("extra", 12)}; // valid
    {name: fName, married, age: { age: theAge, format, !...}, ...theMap} = {name: "Peter", married: true, age: {age:12, format: "Y", three: "three"}, extra: ("extra", 12)}; // age literal invalid
    {name: fName, married, age: { age: theAge, format, !...}, ...theMap} = {name: "Peter", married: true, age: {age:12, format: "Y"}, extra: ("extra", 12)}; // valid
    {name: fName, married, age: { age: theAge, format, !...}, !...} = {name: "Peter", married: true, age: {age:12, format: "Y"}, extra: ("extra", 12)}; // literal invalid
    {name: fName, married, age: { age: theAge, format, !...}} = {name: "Peter", married: true, age: {age:12, format: "Y"}, extra: ("extra", 12)}; // valid
    {name: fName, married, age: { age: theAge, format}} = {name: "Peter", married: true, age: {age:12, format: "Y", three: "three"}, extra: ("extra", 12)}; // valid
}

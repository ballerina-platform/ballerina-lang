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

public function testDiagnostics() {
    map<string> words = { a: "ant", b: "bear"};
    var s = words.'map(s => s.toLowerAscii()).indexOf("bear");
}

json j = {
    name: "apple", 
    color: "red", 
    price: [
        1.22,
        4, 
        3.5
    ], 
    quality: true
};

function testEnsureTypeWithArray() returns error? {
    float[] age = check j.price;
}

function testEnsureTypeWithUnion1() returns error? {
    int|string|float[] name = check j.name;
}

function testToStringNegative() {
    json j = {
        name: "Name",
        address: {
            country: "Country",
            city: "City"
        }
    };

    var x1 = j.age.toString();
    var x2 = j.address.town.toString();
    var x3 = foo().toString();

    var x4 = j.age.toBalString();
    var x5 = j.address.town.toBalString();
    var x6 = foo().toBalString();
}

function testUndefinedLanLibFunction() {
    int value = 5;
    xml s = value.toXML();
}

function foo() returns int|error {
    return error("Error");
}

public function joinStrings(){
    string result = strings:join("string", "string...");
}

type Ints 12|"foo";

function testLangLibCallOnFiniteType() {
    Ints x = 12;
    string s = x.toHexString();
}

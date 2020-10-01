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

function testDecimalToString() returns int {
    decimal res3 = 8;
    string s = "test 🏹" + res3.toString();
    return s.length();
}

function testFunctionPointerToString() returns int {
    return foo(hello);
}

public function hello() returns int {
    return "say 🏖️".length();
}

function foo(function () returns int bar) returns int {
    string s = "what 💼 ";
    return s.length() + bar.toString().length();
}

function testMapToString() returns int {
    map<string> addrMap = { line1: "No. 20", line2: "Palm Grove",
            city: "Colombo 03", country: "Sri Lanka" };
    return addrMap.toString().length();
}

function testMapToStringWithSymbol() returns int {
    map<string> addrMap = { line1: "No. 20", line2: "Palm Grove 🎬",
            city: "Colombo 03", "country🔲": "Sri Lanka" };
    return addrMap.toString().length();
}

function testTupleToString() returns int {
    [int, string, string] a = [10, "John🥄", "Silva"];
    return a.toString().length();
}

function testArrayToString() returns int {
   string[] arr = ["10", "John🚰", "Silva"];
    return arr.toString().length();
}

function testJsonToString() returns int {
    json j9 = {"smile👍": "smile"};
    return j9.toString().length();
}

class Person {
    public string name = "Riyafa";
}

function testObjectToString() returns int {
    Person p3 = new Person();
    return p3.toString().length();
}

function testArrayValueToString() returns int {
    string[][] arr2 = [["h🤷llo", "h🤷llo", "h🤷llo"], ["h🤷llo", "h🤷llo", "h🤷llo"]];
    return arr2[0].toString().length() + arr2[0][1].toString().length();
}

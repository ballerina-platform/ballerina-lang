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

import ballerinax/java;

public function testVarArgs() returns int {
    var answer = "Good work 👍";
    var javaList = asList("1", "2", answer);
    return javaList.toString().length();
}

public function asList(string... values) returns handle = @java:Method {
    name:"asList",
    class: "java.util.Arrays"
} external;

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

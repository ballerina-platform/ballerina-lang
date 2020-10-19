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

import ballerina/test;

# Execute tests to verify the diff error messages during assert equality checks

@test:Config {}
function testAssertStringValues() {
    error? err = trap test:assertEquals("hello dilhasha","hello dilhashaaa");
    error result = <error>err;
    test:assertTrue(result.message().toString().endsWith("--- expected\n+++ actual\n@@ -1,1 +1,1 @@\n\n-hello dilhashaaa\n+hello dilhasha\n\n\n"));
}

@test:Config {}
function testAssertLongStringValues() {
    string value1 = "Ballerina is an open source programming language and platform for cloud-era application " + "programmers.\nSequence diagrams have been everyone’s favorite tool to describe how distributed & conccurrent " + "programs work.";
    string value2 = "Ballerina is an open source programming language and platform for cloud-era application " + "programmersss.\nSequence diagrams have been everyone’s favorite tool to describe how distributed & concurrent " + "programs work.";
    error? err = trap test:assertEquals(value1, value2);
    error result = <error>err;
    test:assertTrue(result.message().toString().endsWith("--- expected\n+++ actual\n@@ -1,4 +1,4 @@\n\n Ballerina is an open source programming language and platform for cloud-era appl\n-ication programmersss.\n+ication programmers.\n Sequence diagrams have been everyone’s favorite tool to " + "describe how distributed\n- & concurrent programs work.\n+ & conccurrent programs work.\n\n\n"));
}

@test:Config {}
function testAssertIntValues() {
    error? err = trap test:assertEquals(124, 123);
    error result = <error>err;
    test:assertEquals(result.message().toString(), "Assertion Failed!\nexpected: '123'\nactual\t: '124'");
}

@test:Config {}
function testAssertDecimalValues() {
    decimal d = 27.5;
    decimal f = 27.6;
    error? err = trap test:assertEquals(d, f);
    error result = <error>err;
    test:assertEquals(result.message().toString(), "Assertion Failed!\nexpected: '27.6'\nactual\t: '27.5'");
}

@test:Config {}
function testAssertJsonValues() {
    json bioData = {name:"John Doe", age:25, address:{city:"Colombo", country:"Sri Lanka"}};
    json bioData2 = {name:"John Doe New", age:25, address:{city:"Colombo", country:"Sri Lanka"}};
    error? err = trap test:assertEquals(bioData, bioData2);
    error result = <error>err;
    test:assertTrue(result.message().toString().endsWith("--- expected\n+++ actual\n@@ -1,8 +1,8 @@\n\n {\n-name: <string> John Doe New,\n+name: <string> John Doe,\n age: <int> 25,\n address: <map> {\n city: <string> Colombo,\n country: <string> Sri Lanka\n }\n }\n\n\n"));
}

@test:Config {}
function testAssertJsonInJson() {
     json j1 = {name: "Anne", age: "21", marks: {maths: 100, physics: 90, status: {pass:true}}};
     json j2 = {name: "Anne", age: 21, marks: {maths: 100, physics: 90, status: {pass:false}}};
     error? err = trap test:assertEquals(j1, j2);
     error result = <error>err;
     test:assertTrue(result.message().toString().endsWith("--- expected\n+++ actual\n@@ -1,11 +1,11 @@\n\n {\n name: <string> Anne,\n-age: <int> 21,\n+age: <string> 21,\n marks: <map> {\n maths: <int> 100,\n physics: <int> 90,\n status: <map> {\n-pass: <boolean> false\n+pass: <boolean> true\n }\n }\n }\n\n\n"));
}

function testAssertLongJsonValues() {
    json bioData = {name:"John Doe Old", age:25, designation: "SSE", address:{city:"Colombo", country:"Sri Lankaa"}};
    json bioData2 = {name:"John Doe New", age:25, designation: "SSE", address:{city:"Colombo", country:"Sri Lanka"}};
    error? err = trap test:assertEquals(bioData, bioData2);
    error result = <error>err;
    test:assertTrue(result.message().toString().endsWith("--- expected\n+++ actual\n@@ -1,9 +1,9 @@\n\n {\n-name: <string> John Doe New,\n+name: <string> John Doe Old,\n age: <int> 25,\n designation: <string> SSE,\n address: <map> {\n city: <string> Colombo,\n-country: <string> Sri Lanka\n+country: <string> Sri Lankaa\n }\n }\n\n\n"));
}

@test:Config {}
function testAssertTuples() {
    [int, string] a = [10, "John"];
    [int, string] b = [12, "John"];
    error? err = trap test:assertEquals(a, b);
    error result = <error>err;
    test:assertEquals(result.message().toString(), "Assertion Failed!\nexpected: '12 John'\nactual\t: '10 John'");
}

@test:Config {}
function testAssertObjects() {
    Person person = new();
    Person person2 = new();
    person.name = "dilhasha";
    error? err = trap test:assertExactEquals(person, person2);
    error result = <error>err;
    test:assertEquals(result.message().toString(), "Assertion Failed!\nexpected: 'object assertions-error-messages:Person'\nactual\t: 'object assertions-error-messages:Person'");
}

@test:Config {}
function testAssertJsonArray() {
        json j1 = [1, false, null, "foo", {first: "John", last: "Pala"}];
        json j2 = [2, false, null, "foo", {first: "John", last: "Pala"}];
        error? err = trap test:assertEquals(j1, j2);
        error result = <error>err;
        test:assertEquals(result.message().toString(), "Assertion Failed!\nexpected: '[2,false,null,\"foo\",{\"first\":\"John\",\"last\":\"Pala\"}]'\nactual\t: '[1,false,null,\"foo\",{\"first\":\"John\",\"last\":\"Pala\"}]'");
}

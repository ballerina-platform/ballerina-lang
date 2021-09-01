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

class Person {
    public string name = "";
    public int age = 0;
    public Person? parent = ();
    private string email = "default@abc.com";
    string address = "No 20, Palm grove";
}

type Member record {
    string fname;
    string lname;
    int age;
};

type Movie record {
    string title;
    string year;
    string released;
    Member writer;
};

@test:Config {}
function testAssertStringValues() {
    error? err = trap test:assertEquals("hello userr","hello user");
    error result = <error>err;
    test:assertTrue(result.message().toString().endsWith("--- actual\n+++ expected \n \n @@ -1,1 +1,1 @@ \n \n -hello userr\n+hello user\n"));
}

@test:Config {}
function testAssertLongStringValues() {
    string value1 = "Ballerina is an open source programming language and platform for cloud-era application " +
    "programmers.\nSequence diagrams have been everyone’s favorite tool to describe how distributed & conccurrent programs work.";
    string value2 = "Ballerina is an open source programming language and platform for cloud-era application " +
    "programmersss.\nSequence diagrams have been everyone’s favorite tool to describe how distributed & concurrent programs work.";
    error? err = trap test:assertEquals(value1, value2);
    error result = <error>err;
    test:assertTrue(result.message().toString().endsWith("--- actual\n+++ " +
    "expected \n \n @@ -1,4 +1,4 @@ \n \n  Ballerina is an open source " +
    "programming language and platform for cloud-era appl\n-ication programmers." +
    "\n+ication programmersss.\n Sequence diagrams have been everyone’s " +
    "favorite tool to describe how distributed\n- & conccurrent programs work.\n" +
    "+ & concurrent programs work.\n"));
}

@test:Config {}
function testAssertMultipleLinesString() {
    error? err = trap test:assertEquals("hello user\nWelcome to Ballerina","hello userr\nWelcome to Ballerina");
    error result = <error>err;
    test:assertTrue(result.message().toString().endsWith("--- actual\n+++ expected \n \n @@ -1,2 +1,2 @@ \n \n " +
    "-hello user\n+hello userr\n Welcome to Ballerina\n"));
}

@test:Config {}
function testAssertIntValues() {
    error? err = trap test:assertEquals(124, 123);
    error result = <error>err;
    test:assertEquals(result.message().toString(), "Assertion Failed!\n \nexpected: '123'\nactual\t: '124'");
}

@test:Config {}
function testAssertDecimalValues() {
    decimal d = 27.5;
    decimal f = 27.6;
    error? err = trap test:assertEquals(d, f);
    error result = <error>err;
    test:assertEquals(result.message().toString(), "Assertion Failed!\n \nexpected: '27.6'\nactual\t: '27.5'");
}

@test:Config {}
function testAssertJsonValues() {
    json bioData = {name:"John Doe", age:25, address:{city:"Colombo", country:"Sri Lanka"}};
    json bioData2 = {name:"John Doe New", age:25, address:{city:"Colombo", country:"Sri Lanka"}};
    error? err = trap test:assertEquals(bioData, bioData2);
    error result = <error>err;
    test:assertEquals(result.message().toString(), "Assertion Failed!\n " +
    "\nexpected: '{\"name\":\"John Doe New\",\"age\":25,\"address\":" +
    "{\"city\":\"Colombo\"," + "\"country\":\"Sri Lanka...'\nactual\t: '" +
    "{\"name\":\"John Doe\",\"age\":25,\"address\":{\"city\":\"Colombo\"," +
    "\"country\":\"Sri Lanka\"}}'\n \nDiff\t:\n\nkey: name\n \nexpected " +
    "value\t: John Doe New\nactual value\t: John Doe");
}

@test:Config {}
function testAssertJsonInJson() {
    json j1 = {name: "Anne", age: "21", marks: {maths: 100, physics: 90, status: {pass:true}}};
    json j2 = {name: "Anne", age: 21, marks: {maths: 100, physics: 90, status: {pass:false}}};
    error? err = trap test:assertEquals(j1, j2);
    error result = <error>err;
    test:assertEquals(result.message().toString(), "Assertion Failed!\n " +
    "\nexpected: '{\"name\":\"Anne\",\"age\":21,\"marks\":{\"maths\":100," +
    "\"physics\":90,\"status\":{\"pass\":false...'\nactual\t: '" +
    "{\"name\":\"Anne\",\"age\":\"21\",\"marks\":{\"maths\":100,\"physics\":90," +
    "\"status\":{\"pass\":tru...'\n \nDiff\t:\n\nkey: age\n \nexpected " +
    "value\t: <int> 21\nactual value\t: <string> 21\n\nkey: marks.status." +
    "pass\n \nexpected value\t: false\nactual value\t: true");
}

@test:Config {}
function testAssertLongJsonValues() {
    json bioData = {name:"John Doe Old", age:25, designation: "SSE", address:{city:"Colombo", country:"Sri Lankaa"}};
    json bioData2 = {name:"John Doe New", age:25, designation: "SSE", address:{city:"Colombo", country:"Sri Lanka"}};
    error? err = trap test:assertEquals(bioData, bioData2);
    error result = <error>err;
    test:assertEquals(result.message().toString(), "Assertion Failed!\n \nexpected: '{\"name\":\"John Doe New\"," +
    "\"age\":25,\"designation\":\"SSE\",\"address\":{\"city\":\"Colombo\",...'\nactual\t: '{\"name\":" +
    "\"John Doe Old\",\"age\":25,\"designation\":\"SSE\",\"address\":{\"city\":\"Colombo\",...'\n \n" +
    "Diff\t:\n\nkey: name\n \nexpected value\t: John Doe New\nactual value\t: John Doe Old\n\n" +
    "key: address.country\n \nexpected value\t: Sri Lanka\nactual value\t: Sri Lankaa");
}

@test:Config {}
function testAssertJsonWithKeyDiff() {
    json j1 = {name: "Anne", age: "21", marks: {maths: 100, physics: 90, status: {pass:true}}};
    json j2 = {name2: "Anne", age: 21, marks: {maths: 100, physics: 90, status: {pass2:false}}};
    error? err = trap test:assertEquals(j1, j2);
    error result = <error>err;
    test:assertEquals(result.message().toString(), "Assertion Failed!\n " +
    "\nexpected: '{\"name2\":\"Anne\",\"age\":21,\"marks\":{\"maths\":100," +
    "\"physics\":90,\"status\":{\"pass2\":fal...'\nactual\t: '" +
    "{\"name\":\"Anne\",\"age\":\"21\",\"marks\":{\"maths\":100,\"physics\":90," +
    "\"status\":{\"pass\":tru...'\n \nDiff\t:\n\nexpected keys\t: name2, " +
    "marks.status.pass2\nactual keys\t: name, marks.status.pass\n\n" +
    "key: age\n \nexpected value\t: <int> 21\nactual value\t: <string> 21\n");
}

@test:Config {}
function testAssertJsonWithCount() {
    json j1 = {name: "Anne", age: "21",
    marks: {maths: 99, physics: 80, chemistry: 70, english: 95, status: {pass:true}}};
    json j2 = {name2: "Anne", age: 21,
    marks: {maths: 10, physics: 40, chemistry: 50, english: 55, status: {pass:false}}};
    error? err = trap test:assertEquals(j1, j2);
    error result = <error>err;
    test:assertEquals(result.message().toString(), "Assertion Failed!\n \nexpected: '{\"name2\":\"Anne\",\"age\":21," +
    "\"marks\":{\"maths\":10,\"physics\":40,\"chemistry\":50,\"englis...'\nactual\t: '{\"name\":\"Anne\",\"age\":" +
    "\"21\",\"marks\":{\"maths\":99,\"physics\":80,\"chemistry\":70,\"engli...'\n \nDiff\t:\n\nexpected keys\t: " +
    "name2\nactual keys\t: name\n\nkey: age\n \nexpected value\t: <int> 21\nactual value\t: <string> 21\n\n" +
    "key: marks.maths\n \nexpected value\t: 10\nactual value\t: 99\n\nkey: marks.physics\n \nexpected value\t: 40\n" +
    "actual value\t: 80\n\nkey: marks.chemistry\n \nexpected value\t: 50\nactual value\t: 70\n\nkey: " +
    "marks.english\n \nexpected value\t: 55\nactual value\t: 95\n\nkey: marks.status.pass\n \nexpected value\t: false\n" +
    "actual value\t: true\n \nTotal value mismatches: 6\n");
}

@test:Config {}
function testAssertTuples() {
    [int, string] a = [10, "John"];
    [int, string] b = [12, "John"];
    error? err = trap test:assertEquals(a, b);
    error result = <error>err;
    test:assertEquals(result.message().toString(), "Assertion Failed!\n \nexpected: '[12,\"John\"]'\nactual\t: " +
    "'[10,\"John\"]'");
}

@test:Config {}
function testAssertObjects() {
    Person person = new();
    Person person2 = new();
    person.name = "dilhasha";
    error? err = trap test:assertExactEquals(person, person2);
    error result = <error>err;
    test:assertEquals(result.message().toString(), "Assertion Failed!\n \n" +
    "expected: 'object assertions:Person'\nactual\t: 'object assertions:Person'");
}

@test:Config {}
function testAssertJsonArray() {
        json j1 = [1, false, null, "foo", {first: "John", last: "Pala"}];
        json j2 = [2, false, null, "foo", {first: "John", last: "Pala"}];
        error? err = trap test:assertEquals(j1, j2);
        error result = <error>err;
        test:assertEquals(result.message().toString(), "Assertion Failed!\n \nexpected: '[2,false,null,\"foo\"," +
        "{\"first\":\"John\",\"last\":\"Pala\"}]'\nactual\t: '[1,false,null,\"foo\"," +
        "{\"first\":\"John\",\"last\":\"Pala\"}]'");
}

@test:Config {}
function testAssertRecords() {
    Movie theRevenant = {
        title: "The Revenant",
        year: "2015",
        released: "08 Jan 2016",
        writer: {
            fname: "Michael",
            lname: "Punke",
            age: 30
        }
    };
    Movie theRevenantNew = {
        title: "The Revenant",
        year: "2020",
        released: "08 Jan 2020",
        writer: {
            fname: "Michael",
            lname: "Punke",
            age: 35
        }
    };
    error? err = trap test:assertEquals(theRevenant, theRevenantNew);
    error result = <error>err;
    test:assertEquals(result.message().toString(), "Assertion Failed!\n \n" +
    "expected: '{\"title\":\"The Revenant\",\"year\":\"2020\",\"released\":\"08 Jan 2020\",\"writer\":{\"fname\"...'" +
    "\nactual\t: '{\"title\":\"The Revenant\",\"year\":\"2015\",\"released\":\"08 Jan 2016\",\"writer\":{\"fname\"" +
    "...'\n \nDiff\t:\n\nkey: year\n \nexpected value\t: 2020\nactual value\t: 2015\n\nkey: released\n \n" +
    "expected value\t: 08 Jan 2020\nactual value\t: 08 Jan 2016\n\nkey: writer.age\n \nexpected value\t: 35\n" +
    "actual value\t: 30");
}

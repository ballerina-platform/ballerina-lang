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

# Execute tests to verify the type mismatch error messages during assert equality checks

class Person {
    public string name = "";
    public int age = 0;
    public Person? parent = ();
    private string email = "default@abc.com";
    string address = "No 20, Palm grove";
}

class Employee {
    public string name = "";
    public int age = 0;
    public Person? parent = ();
    private string email = "default@abc.com";
    string address = "No 20, Palm grove";
}

type CustomerTable table<map<anydata>>;

@test:Config {}
function testAssertStringAndInt() {
    error? err = trap test:assertEquals(1, "1");
    error result = <error>err;
    test:assertEquals(result.message().toString(), "Assertion Failed!\n \nexpected: <string> '1'\nactual\t: <int> '1'");
}

@test:Config {}
function testAssertDecimalAndFloat() {
    decimal d = 27.5;
    float f = 27.5;
    error? err = trap test:assertEquals(d, f);
    error result = <error>err;
    test:assertEquals(result.message().toString(), "Assertion Failed!\n \nexpected: <float> '27.5'\nactual\t: <decimal> '27.5'");
}

@test:Config {}
function testAssertJsonAndString() {
    json bioData = {name:"John Doe", age:25, address:{city:"Colombo", country:"Sri Lanka"}};
    string bioDataString = "{name:\"John Doe\", age:25, address:{city:\"Colombo\", country:\"Sri Lanka\"}}";
    error? err = trap test:assertEquals(bioData, bioDataString);
    error result = <error>err;
    test:assertEquals(result.message().toString(), 
    "Assertion Failed!\n \nexpected: <string> '{name:\"John Doe\", age:25, address:{city:\"Colombo\", country:\"Sri " + "Lanka\"}}'\nactual\t: <map> '{\"name\":\"John Doe\",\"age\":25,\"address\":{\"city\":\"Colombo\"," + "\"country\":\"Sri Lanka\"}}'");
}

@test:Config {}
function testAssertXmlAndString() {
    anydata xmlValue = xml `<book>The Lost World</book>` + xml `Hello, world!` + xml `<!--I am a comment-->` + xml `<?target data?>`;
    string xmlString = "<book>The Lost World</book>Hello, world!<!--I am a comment--><?target data?>";
    error? err = trap test:assertEquals(xmlString, xmlValue);
    error result = <error>err;
    test:assertEquals(result.message().toString(), 
    "Assertion Failed!\n \nexpected: <xml> '<book>The Lost World</book>Hello, world!<!--I am a comment--><?target data?" + ">'\nactual\t: <string> '<book>The Lost World</book>Hello, world!<!--I am a comment--><?target data?>'");
}

@test:Config {}
function testAssertDifferentTuples() {
    [int, string] a = [10, "John"];
    [string, string] b = ["10", "John"];
    error? err = trap test:assertEquals(a, b);
    error result = <error>err;
    test:assertEquals(result.message().toString(), 
    "Assertion Failed!\n \nexpected: <[string,string]> '[\"10\",\"John\"]'\nactual\t: <[int,string]> '[10,\"John\"]'");
}

@test:Config {}
function testAssertTableAndString() {
    CustomerTable customerTab = table [
      {id: 1, name: "John", salary: 300.50},
      {id: 2, name: "Bella", salary: 500.50}
    ];
    string customerTabString = "table [{id: 1, name: \"John\", salary: 300.50},{id: 2, name: \"Bella\", salary: 500.50}]";
    error? err = trap test:assertEquals(customerTab, customerTabString);
    error result = <error>err;
    test:assertEquals(result.message().toString(), "Assertion Failed!\n \n" +
    "expected: <string> 'table [{id: 1, name: \"John\", salary: 300.50}," +
    "{id: 2, name: \"Bella\", salary: 500.\n50}]'" +
    "\nactual\t: <CustomerTable> '[{\"id\":1,\"name\":\"John\",\"salary\":300.5},{\"id\":2,\"name\":\"Bella\"," +
    "\"salary\":500.5}]'");
}

@test:Config {}
function testAssertDifferentObjects() {
    Person person = new();
    Employee employee = new();
    error? err = trap test:assertExactEquals(person, employee);
    error result = <error>err;
    test:assertEquals(result.message().toString(), "Assertion Failed!\n \nexpected: <Employee> 'object assertions:Employee'\nactual\t: <Person> 'object assertions:Person'");
}

@test:Config {}
function testAssertAnnonymousRecords() {
    record {|
        string city;
        string country;
    |} address = {city: "London", country: "UK"};

    record {|
        string newCity;
        string newCountry;
    |} address2 = {newCity: "London", newCountry: "UK"};

    error? err = trap test:assertEquals(address, address2);
    error result = <error>err;
    test:assertEquals(result.message().toString(), "Assertion Failed!\n \nexpected: <record {| string newCity; string newCountry; |}> '{\"newCity\":" +
    "\"London\",\"newCountry\":\"UK\"}'\nactual\t: <record {| string city; string country; |}> '{\"city\":\"London\",\"country\":\"UK\"}'");
}

@test:Config {}
function testAssertLongValues() {
    string value1 = "Ballerina is an open source programming language and platform for cloud-era application programmers.";
    json value2 = {description: "Ballerina is an open source programming language and platform for cloud-era application programmers."};

    error? err = trap test:assertEquals(value1, value2);
    error result = <error>err;
    test:assertEquals(result.message().toString(), "Assertion Failed!\n \nexpected: <map> '{\"description\":\"Ballerina" +
    " is an open source programming language and platform fo\n" +
    "r cloud-era application programmers.\"}'" +
    "\nactual\t: <string> 'Ballerina is an open source" +
    " programming language and platform for cloud-era appl\n" +
    "ication programmers.'");
}

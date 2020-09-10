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

type Person object {
    public string name = "";
    public int age = 0;
    public Person? parent = ();
    private string email = "default@abc.com";
    string address = "No 20, Palm grove";
};

type Employee object {
    public string name = "";
    public int age = 0;
    public Person? parent = ();
    private string email = "default@abc.com";
    string address = "No 20, Palm grove";
};

type CustomerTable table<map<any>>;

@test:Config {}
function testAssertStringAndInt() {
    error? err = trap test:assertEquals(1, "1");
    error result = <error>err;
    test:assertEquals(result.message().toString(), "Assertion Failed!: expected '<string> 1' but found '<int> 1'");
}

@test:Config {}
function testAssertDecimalAndFloat() {
    decimal d = 27.5;
    float f = 27.5;
    error? err = trap test:assertEquals(d, f);
    error result = <error>err;
    test:assertEquals(result.message().toString(), "Assertion Failed!: expected '<float> 27.5' but found '<decimal> 27.5'");
}

@test:Config {}
function testAssertJsonAndString() {
    json bioData = {name:"John Doe", age:25, address:{city:"Colombo", country:"Sri Lanka"}};
    string bioDataString = "{name:\"John Doe\", age:25, address:{city:\"Colombo\", country:\"Sri Lanka\"}}";
    error? err = trap test:assertEquals(bioData, bioDataString);
    error result = <error>err;
    test:assertEquals(result.message().toString(), "Assertion Failed!: expected '<string> {name:\"John Doe\", age:25, address:{city:\"Colombo\", country:\"Sri Lanka\"}}' but found '<map> name=John Doe age=25 address=city=Colombo country=Sri Lanka'");
}

@test:Config {}
function testAssertXmlAndString() {
    anydata xmlValue = xml `<book>The Lost World</book>` + xml `Hello, world!` + xml `<!--I am a comment-->` + xml `<?target data?>`;
    string xmlString = "<book>The Lost World</book>Hello, world!<!--I am a comment--><?target data?>";
    error? err = trap test:assertEquals(xmlString, xmlValue);
    error result = <error>err;
    test:assertEquals(result.message().toString(), "Assertion Failed!: expected '<xml> <book>The Lost World</book>Hello, world!<!--I am a comment--><?target data?>' but found '<string> <book>The Lost World</book>Hello, world!<!--I am a comment--><?target data?>'");
}

@test:Config {}
function testAssertDifferentTuples() {
    [int, string] a = [10, "John"];
    [string, string] b = ["10", "John"];
    error? err = trap test:assertEquals(a, b);
    error result = <error>err;
    test:assertEquals(result.message().toString(), "Assertion Failed!: expected '<[string,string]> 10 John' but found '<[int,string]> 10 John'");
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
    test:assertEquals(result.message().toString(), "Assertion Failed!: expected '<string> table [{id: 1, name: \"John\", salary: 300.50},{id: 2, name: \"Bella\", salary: 500.50}]' but found '<table> id=1 name=John salary=300.5\nid=2 name=Bella salary=500.5'");
}

@test:Config {}
function testAssertDifferentObjects() {
    Person person = new();
    Employee employee = new();
    error? err = trap test:assertExactEquals(person, employee);
    error result = <error>err;
    test:assertEquals(result.message().toString(), "Assertion Failed!: expected '<Employee> object assertions-error-messages:Employee' but found '<Person> object assertions-error-messages:Person'");
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
    test:assertEquals(result.message().toString(), "Assertion Failed!: expected '<$anonType$1> newCity=London newCountry=UK' but found '<$anonType$0> city=London country=UK'");
}

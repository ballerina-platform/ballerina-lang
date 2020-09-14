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

type CustomerTable table<map<any>>;

@test:Config {}
function testAssertStringAndInt() {
    error? err = trap test:assertEquals(1, "1");
    error result = <error>err;
    test:assertEquals(result.message().toString(), "Assertion Failed!\nexpected: <string> '1'\nactual\t: <int> '1'");
}

@test:Config {}
function testAssertDecimalAndFloat() {
    decimal d = 27.5;
    float f = 27.5;
    error? err = trap test:assertEquals(d, f);
    error result = <error>err;
    test:assertEquals(result.message().toString(), "Assertion Failed!\nexpected: <float> '27.5'\nactual\t: <decimal> '27.5'");
}

@test:Config {}
function testAssertJsonAndString() {
    json bioData = {name:"John Doe", age:25, address:{city:"Colombo", country:"Sri Lanka"}};
    string bioDataString = "{name:\"John Doe\", age:25, address:{city:\"Colombo\", country:\"Sri Lanka\"}}";
    error? err = trap test:assertEquals(bioData, bioDataString);
    error result = <error>err;
    test:assertEquals(result.message().toString(), "Assertion Failed!\nexpected: <string> '{name:\"John Doe\", age:25, address:{city:\"Colombo\", country:\"Sri Lanka\"}}'\nactual\t: <map> 'name=John Doe age=25 address=city=Colombo country=Sri Lanka'");
}

@test:Config {}
function testAssertXmlAndString() {
    anydata xmlValue = xml `<book>The Lost World</book>` + xml `Hello, world!` + xml `<!--I am a comment-->` + xml `<?target data?>`;
    string xmlString = "<book>The Lost World</book>Hello, world!<!--I am a comment--><?target data?>";
    error? err = trap test:assertEquals(xmlString, xmlValue);
    error result = <error>err;
    test:assertEquals(result.message().toString(), "Assertion Failed!\nexpected: <xml> '<book>The Lost World</book>Hello, world!<!--I am a comment--><?target data?>'\nactual\t: <string> '<book>The Lost World</book>Hello, world!<!--I am a comment--><?target data?>'");
}

@test:Config {}
function testAssertDifferentTuples() {
    [int, string] a = [10, "John"];
    [string, string] b = ["10", "John"];
    error? err = trap test:assertEquals(a, b);
    error result = <error>err;
    test:assertEquals(result.message().toString(), "Assertion Failed!\nexpected: <[string,string]> '10 John'\nactual\t: <[int,string]> '10 John'");
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
    test:assertEquals(result.message().toString(), "Assertion Failed!\nexpected: <string> 'table [{id: 1, name: \"John\", salary: 300.50},{id: 2, name: \"Bella\", salary: 500.50}]'\nactual\t: <table> 'id=1 name=John salary=300.5\nid=2 name=Bella salary=500.5'");
}

@test:Config {}
function testAssertDifferentObjects() {
    Person person = new();
    Employee employee = new();
    error? err = trap test:assertExactEquals(person, employee);
    error result = <error>err;
    test:assertEquals(result.message().toString(), "Assertion Failed!\nexpected: <Employee> 'object assertions-error-messages:Employee'\nactual\t: <Person> 'object assertions-error-messages:Person'");
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
    test:assertEquals(result.message().toString(), "Assertion Failed!\nexpected: <$anonType$1> 'newCity=London newCountry=UK'\nactual\t: <$anonType$0> 'city=London country=UK'");
}

@test:Config {}
function testAssertLongValues() {

    string value1 = "Ballerina is an open source programming language and platform for cloud-era application programmers to easily write software that just works. For decades, programming languages have treated networks simply as I/O sources. Ballerina introduces fundamental, new abstractions of client objects, services, resource functions, and listeners to bring networking into the language so that programmers can directly address the Fallacies of Distributed Computing as part of their application logic. This facilitates resilient, secure, performant network applications to be within every programmer’s reach. In a microservice architecture, smaller services are built, deployed and scaled individually. These disaggregated services communicate with each other over the network forcing developers to deal with the Fallacies of Distributed Computing as a part of their application logic. For decades, programming languages have treated networks simply as I/O sources. Ballerina treats the network differently by making networking concepts like client objects, services, resource functions, and listeners a part of the syntax. So you can use the language-provided constructs to write network programs that just work. Ballerina introduces service typing where services, which work in conjunction with a listener object, can have one or more resource methods in which the application logic is implemented. The listener object provides an interface between the network and the service. It receives network messages from a remote process according to the defined protocol and translates it into calls on the resource methods of the service that has been attached to the listener object. Sequence diagrams have been everyone’s favorite tool to describe how distributed & concurrent programs work. Yet, no current programming language lets you write your logic as a sequence diagram. In Ballerina, every program is a sequence diagram that illustrates distributed and concurrent interactions automatically. Static typing is the network application programmer’s development headache and dynamic typing is the reliability engineer’s nightmare.";

    json value2 = {description: "Ballerina is an open source programming language and platform for cloud-era application programmers to easily write software that just works. For decades, programming languages have treated networks simply as I/O sources. Ballerina introduces fundamental, new abstractions of client objects, services, resource functions, and listeners to bring networking into the language so that programmers can directly address the Fallacies of Distributed Computing as part of their application logic. This facilitates resilient, secure, performant network applications to be within every programmer’s reach. In a microservice architecture, smaller services are built, deployed and scaled individually. These disaggregated services communicate with each other over the network forcing developers to deal with the Fallacies of Distributed Computing as a part of their application logic. For decades, programming languages have treated networks simply as I/O sources. Ballerina treats the network differently by making networking concepts like client objects, services, resource functions, and listeners a part of the syntax. So you can use the language-provided constructs to write network programs that just work. Ballerina introduces service typing where services, which work in conjunction with a listener object, can have one or more resource methods in which the application logic is implemented. The listener object provides an interface between the network and the service. It receives network messages from a remote process according to the defined protocol and translates it into calls on the resource methods of the service that has been attached to the listener object. Sequence diagrams have been everyone’s favorite tool to describe how distributed & concurrent programs work. Yet, no current programming language lets you write your logic as a sequence diagram. In Ballerina, every program is a sequence diagram that illustrates distributed and concurrent interactions automatically. Static typing is the network application programmer’s development headache and dynamic typing is the reliability engineer’s nightmare. "};
    error? err = trap test:assertEquals(value1, value2);
    error result = <error>err;
    test:assertEquals(result.message().toString(), "Assertion Failed!\nexpected: <map> 'description=Ballerina is an open source programming language and platform for cloud-era application programmers to easily write software that just works. For decades, programming languages have treated networks simply as I/O sources. Ballerina introduces fundamental, new abstractions of client objects, services, resource functions, and listeners to bring networking into the language so that programmers can directly address the Fallacies of Distributed Computing as part of their application logic. This facilitates resilient, secure, performant network applications to be within every programmer’s reach. In a microservice architecture, smaller services are built, deployed and scaled individually. These disaggregated services communicate with each other over the network forcing developers to deal with the Fallacies of Distributed Computing as a part of their application logic. For decades, programming languages have treated networks simply as I/O sources. Ballerina treats the network differently by making networking concepts like client objects, services, resource functions, and listeners a part of the syntax. So you can use the language-provided constructs to write network programs that just work. Ballerina introduces service typing where services, which work in conjunction with a listener object, can have one or more resource methods in which the application logic is implemented. The listener object provides an interface between the network and the service. It receives network messages from a remote process according to the defined protocol and translates it into calls on the resource methods of the service that has been attached to the listener object. Sequence diagrams have been everyone’s favorite tool to describe how distributed & concurrent programs work. Yet, no current programming language lets you write your logic as a sequence diagram. In Ballerina, every program is a sequence diagram that illustrates distributed and concurrent interactions automatically. Static typing is the network application programmer'\nactual\t: <string> 'Ballerina is an open source programming language and platform for cloud-era application programmers to easily write software that just works. For decades, programming languages have treated networks simply as I/O sources. Ballerina introduces fundamental, new abstractions of client objects, services, resource functions, and listeners to bring networking into the language so that programmers can directly address the Fallacies of Distributed Computing as part of their application logic. This facilitates resilient, secure, performant network applications to be within every programmer’s reach. In a microservice architecture, smaller services are built, deployed and scaled individually. These disaggregated services communicate with each other over the network forcing developers to deal with the Fallacies of Distributed Computing as a part of their application logic. For decades, programming languages have treated networks simply as I/O sources. Ballerina treats the network differently by making networking concepts like client objects, services, resource functions, and listeners a part of the syntax. So you can use the language-provided constructs to write network programs that just work. Ballerina introduces service typing where services, which work in conjunction with a listener object, can have one or more resource methods in which the application logic is implemented. The listener object provides an interface between the network and the service. It receives network messages from a remote process according to the defined protocol and translates it into calls on the resource methods of the service that has been attached to the listener object. Sequence diagrams have been everyone’s favorite tool to describe how distributed & concurrent programs work. Yet, no current programming language lets you write your logic as a sequence diagram. In Ballerina, every program is a sequence diagram that illustrates distributed and concurrent interactions automatically. Static typing is the network application programmer’s developme'");
}

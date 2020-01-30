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

type Person record {
    string name;
    int age;
};

type Employee record {
    string name;
    string company;
};

function getPersonList() returns Person[] {
    Person[] personList = [];
    Person gima = {name: "Gima", age: 100};
    Person mohan = {name: "Mohan", age: 200};
    Person grainier = {name: "Grainier", age: 150};
    Person chiran = {name: "Chiran", age: 75};
    Person sinthuja = {name: "Sinthuja", age: 150};
    personList.push(gima);
    personList.push(mohan);
    personList.push(grainier);
    personList.push(chiran);
    personList.push(sinthuja);
    return personList;
}

function testFilterFunc() returns boolean {
    boolean testPassed = true;
    Person[] personList = getPersonList();

    stream<Person> personStream =  personList.toStream();
    stream<Person> filteredPersonStream = personStream.filter(function (Person person) returns boolean {
        return person.age > 100 && person.name != "James";
    });

    var filteredPerson = filteredPersonStream.next();
    testPassed = testPassed && filteredPerson?.value == personList[1];

    filteredPerson = filteredPersonStream.next();
    testPassed = testPassed && filteredPerson?.value == personList[2];

    filteredPerson = filteredPersonStream.next();
    testPassed = testPassed && filteredPerson?.value == personList[4];

    filteredPerson = filteredPersonStream.next();
    testPassed = testPassed && filteredPerson == ();

    return testPassed;
}

function testMapFunc() returns boolean {
    boolean testPassed = true;
    Person[] personList = getPersonList();

    stream<Person> personStream =  personList.toStream();
    stream<Employee> employeeStream = personStream.'map(function (Person person) returns Employee {
            Employee e = {
                name: person.name,
                company: "WSO2"
            };
            return e;
    });

    var employee = employeeStream.next();
    testPassed = testPassed && employee?.value?.name == "Gima" && employee?.value?.company == "WSO2" ;

    employee = employeeStream.next();
    testPassed = testPassed && employee?.value?.name == "Mohan" && employee?.value?.company == "WSO2" ;

    employee = employeeStream.next();
    testPassed = testPassed && employee?.value?.name == "Grainier" && employee?.value?.company == "WSO2" ;

    employee = employeeStream.next();
    testPassed = testPassed && employee?.value?.name == "Chiran" && employee?.value?.company == "WSO2" ;

    employee = employeeStream.next();
    testPassed = testPassed && employee?.value?.name == "Sinthuja" && employee?.value?.company == "WSO2" ;

    employee = employeeStream.next();
    testPassed = testPassed && employee == ();

    return testPassed;
}

function testFilterAndMapFunc() returns boolean {
    boolean testPassed = true;
    Person[] personList = getPersonList();

    stream<Person> personStream =  personList.toStream();
    stream<Employee> employeeStream = personStream
        .filter(function (Person person) returns boolean {
                return person.age > 100 && person.name != "James";
            }
        )
        .'map(function (Person person) returns Employee {
                Employee e = {
                    name: person.name,
                    company: "WSO2"
                };
                return e;
            }
        );

    var employee = employeeStream.next();
    testPassed = testPassed && employee?.value?.name == "Mohan" && employee?.value?.company == "WSO2" ;

    employee = employeeStream.next();
    testPassed = testPassed && employee?.value?.name == "Grainier" && employee?.value?.company == "WSO2" ;

    employee = employeeStream.next();
    testPassed = testPassed && employee?.value?.name == "Sinthuja" && employee?.value?.company == "WSO2" ;

    employee = employeeStream.next();
    testPassed = testPassed && employee == ();

    return testPassed;
}

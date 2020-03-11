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

type PersonValue record {|
    Person value;
|};

type EmployeeValue record {|
    Employee value;
|};

function getPersonValue((record {| Person value; |}|error?)|(record {| Person value; |}?) returnedVal) returns PersonValue? {
    var result = returnedVal;
    if (result is PersonValue) {
        return result;
    } else {
        return ();
    }
}

function getEmployeeValue((record {| Employee value; |}|error?)|(record {| Employee value; |}?) returnedVal) returns EmployeeValue? {
    var result = returnedVal;
    if (result is EmployeeValue) {
        return result;
    } else {
        return ();
    }
}

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

    stream<Person> personStream = personList.toStream();
    stream<Person> filteredPersonStream = personStream.filter(function (Person person) returns boolean {
        return person.age > 100 && person.name != "James";
    });

    record {| Person value; |}? filteredPerson = getPersonValue(filteredPersonStream.next());
    testPassed = testPassed && filteredPerson?.value == personList[1];

    filteredPerson = getPersonValue(filteredPersonStream.next());
    testPassed = testPassed && filteredPerson?.value == personList[2];

    filteredPerson = getPersonValue(filteredPersonStream.next());
    testPassed = testPassed && filteredPerson?.value == personList[4];

    filteredPerson = getPersonValue(filteredPersonStream.next());
    testPassed = testPassed && filteredPerson == ();

    return testPassed;
}

function testMapFunc() returns boolean {
    boolean testPassed = true;
    Person[] personList = getPersonList();

    stream<Person> personStream = personList.toStream();
    stream<Employee> employeeStream = personStream.'map(function (Person person) returns Employee {
        Employee e = {
            name: person.name,
            company: "WSO2"
        };
        return e;
    });

    record {| Employee value; |}? employee = getEmployeeValue(employeeStream.next());
    testPassed = testPassed && employee?.value?.name == "Gima" && employee?.value?.company == "WSO2";

    employee = getEmployeeValue(employeeStream.next());
    testPassed = testPassed && employee?.value?.name == "Mohan" && employee?.value?.company == "WSO2";

    employee = getEmployeeValue(employeeStream.next());
    testPassed = testPassed && employee?.value?.name == "Grainier" && employee?.value?.company == "WSO2";

    employee = getEmployeeValue(employeeStream.next());
    testPassed = testPassed && employee?.value?.name == "Chiran" && employee?.value?.company == "WSO2";

    employee = getEmployeeValue(employeeStream.next());
    testPassed = testPassed && employee?.value?.name == "Sinthuja" && employee?.value?.company == "WSO2";

    employee = getEmployeeValue(employeeStream.next());
    testPassed = testPassed && employee == ();

    return testPassed;
}

function testFilterAndMapFunc() returns boolean {
    boolean testPassed = true;
    Person[] personList = getPersonList();

    stream<Person> personStream = personList.toStream();
    stream<Employee> employeeStream = personStream
    . filter(function (Person person) returns boolean {
        return person.age > 100 && person.name != "James";
    }
    )
    . 'map(function (Person person) returns Employee {
        Employee e = {
            name: person.name,
            company: "WSO2"
        };
        return e;
    }
    );

    record {| Employee value; |}? employee = getEmployeeValue(employeeStream.next());
    testPassed = testPassed && employee?.value?.name == "Mohan" && employee?.value?.company == "WSO2";

    employee = getEmployeeValue(employeeStream.next());
    testPassed = testPassed && employee?.value?.name == "Grainier" && employee?.value?.company == "WSO2";

    employee = getEmployeeValue(employeeStream.next());
    testPassed = testPassed && employee?.value?.name == "Sinthuja" && employee?.value?.company == "WSO2";

    employee = getEmployeeValue(employeeStream.next());
    testPassed = testPassed && employee == ();

    return testPassed;
}

function testReduce() returns float {
    Person[] personList = getPersonList();
    stream<Person> personStream = personList.toStream();
    float|error? avg = personStream.reduce(function (float accum, Person person) returns float {
        return accum + <float>person.age / personList.length();
    }, 0.0);
    return <float>avg;
}

function testForEach() returns float {
    Person[] personList = getPersonList();
    float avg = 0.0;
    stream<Person> personStream = personList.toStream();
    error? err = personStream.forEach(function (Person person) {
        avg += <float>person.age / personList.length();
    });
    return avg;
}

function testIterator() returns boolean {
    boolean testPassed = true;
    Person[] personList = getPersonList();

    stream<Person> personStream = personList.toStream();
    var iterator = personStream.iterator();

    record {|Person value;|}? filteredPerson = getPersonValue(iterator.next());
    testPassed = testPassed && filteredPerson?.value == personList[0];

    filteredPerson = getPersonValue(iterator.next());
    testPassed = testPassed && filteredPerson?.value == personList[1];

    filteredPerson = getPersonValue(iterator.next());
    testPassed = testPassed && filteredPerson?.value == personList[2];

    filteredPerson = getPersonValue(iterator.next());
    testPassed = testPassed && filteredPerson?.value == personList[3];

    filteredPerson = getPersonValue(iterator.next());
    testPassed = testPassed && filteredPerson?.value == personList[4];

    filteredPerson = getPersonValue(iterator.next());
    testPassed = testPassed && filteredPerson == ();

    return testPassed;
}

function testMapFuncWithRecordType() returns boolean {
     boolean testPassed = true;
     Person[] personList = getPersonList();

    stream<Person> personStream =  personList.toStream();

    stream<Employee> mappedEmpStream = personStream.'map(function (Person person) returns Employee {
        Employee e = {
          name: person.name,
          company: "WSO2"
        };
        return e;

        });

    record {| Employee value; |}? nextValue = getEmployeeValue(mappedEmpStream.next());
    Employee employee = <Employee>nextValue?.value;

    testPassed = testPassed && employee.name == "Gima" && employee.company == "WSO2";
    return  testPassed;
}


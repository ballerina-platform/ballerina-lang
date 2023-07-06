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
  readonly string name;
  int age;
};

type Employee record {|
  readonly string name;
  string department;
|};

type EmployeeValue record {|
   Employee value;
|};

type PersonalTable table<Person> key(name);

type EmployeeTable table<Employee> key(name);

type EmployeeKeylessTable table<Employee>;

PersonalTable tab = table key(name)[
  { name: "Chiran", age: 33 },
  { name: "Mohan", age: 37 },
  { name: "Gima", age: 38 },
  { name: "Granier", age: 34 }
];

function getPersonList() returns Person[] {
    Person[] personList = [];
    Person personA = { name: "Chiran", age: 33 };
    Person personB = { name: "Mohan", age: 37 };
    Person personC = { name: "Gima", age: 38 };
    Person personD = { name: "Granier", age: 34 };

    personList.push(personA);
    personList.push(personB);
    personList.push(personC);
    personList.push(personD);

    return personList;
}

function getEmployee(record {| Employee value; |}? returnedVal) returns Employee? {
    if (returnedVal is EmployeeValue) {
        return returnedVal.value;
    } else {
        return ();
    }
}

function testFilterNegative() returns boolean {
    EmployeeTable  filteredTable = tab.filter(function (Employee employee) returns boolean {
                                                 return employee.name == "Chiran";
                                             });
    return filteredTable.length() == 1;
}

function testIteratorNegative() returns boolean {
    boolean testPassed = true;
    Person[] personList = getPersonList();
    object { public isolated function next() returns record {| Employee value; |}?;} itr = tab.iterator();
    Employee? emp = getEmployee(itr.next());
    testPassed = testPassed && emp?.name == personList[0].name;
    return testPassed;
}

function testNextKeyNegative() returns int {
    return tab.nextKey();
}

type PersonalKeyLessTable table<Person>;

function getKeysFromKeyLessTbl() returns boolean {
    PersonalKeyLessTable keyless = table [
      { name: "Chiran", age: 33 },
      { name: "Mohan", age: 37 },
      { name: "Gima", age: 38 },
      { name: "Granier", age: 34 }
    ];
    return keyless.keys().length() == 0;
}

function getValueFromTable() {
    table<Person> tab = table [
      { name: "Chiran", age: 33 },
      { name: "Mohan", age: 37 },
      { name: "Gima", age: 38 },
      { name: "Granier", age: 34 }
    ];

    Person person = tab.get("Chiran");
    int value = person["age"];
}

function removeWithInvalidKey() returns boolean {
    table<Person> tab = table [
      { name: "Chiran", age: 33 },
      { name: "Mohan", age: 37 },
      { name: "Gima", age: 38 },
      { name: "Granier", age: 34 }
    ];

    Person removedPerson = tab.remove("Chiran");
    return removedPerson.name == "Chiran";
}

function removeIfHasKey() returns boolean {
    table<Person> tbl = table [{ name: "Chiran", age: 33 },
    { name: "Mohan", age: 37 },
    { name: "Gima", age: 38 },
    { name: "Granier", age: 34 }];
    Person? removedPerson1 = tbl.removeIfHasKey("AAA");
    Person? removedPerson2 = tbl.removeIfHasKey("Chiran");
    return removedPerson1 == () && removedPerson2?.name == "Chiran";
}

function testPutWithIncompatibleTypes()  {
    EmployeeTable employeeTbl = table key(name) [
       { name: "Lisa", department: "HR" },
       { name: "Jonas", department: "Marketing" }
    ];
    var person = { name: "Chiran", age: 33 };
    employeeTbl.put(person);
}

function testAddWithIncompatibleTypes()  {
    EmployeeTable employeeTbl = table key(name) [
       { name: "Lisa", department: "HR" },
       { name: "Jonas", department: "Marketing" }
    ];
    var person = { name: "Chiran", age: 33 };
    employeeTbl.add(person);
}

function testPutIncompatibleTypesWithKeyLessTbl()  {
    EmployeeKeylessTable employeeTbl = table [
       { name: "Lisa", department: "HR" },
       { name: "Jonas", department: "Marketing" }
    ];
    var person = { name: "Chiran", age: 33 };
    employeeTbl.put(person);
}

function testAddIncompatibleTypesWithKeyLessTbl()  {
    EmployeeKeylessTable employeeTbl = table [
       { name: "Lisa", department: "HR" },
       { name: "Jonas", department: "Marketing" }
    ];
    var person = { name: "Chiran", age: 33 };
    employeeTbl.add(person);
}

function testTableMemberAccess()  {
    PersonalTable tab = table [
      { name: "Chiran", age: 33 },
      { name: "Mohan", age: 37 },
      { name: "Granier", age: 34 }
    ];

    Person person = { name: "Gima", age: 38 };
    tab["Gima"] = person;
}

function testLangLibInvocationOnSemanticallyIncorrectTable() {
    table<int> key(age) tab = table [1];
    _ = tab.remove(2);
}

type MapType map<any|error>;
type KeyType anydata;

function foo(table <MapType> key<KeyType>? t) {

}

function (Person) returns string arrowExpr = (per) => "";

function testMap() {
    any empTab = tab.'map(function (Person person) returns string {
          return "";
    });

    _ = tab.'map(arrowExpr);
    _ = tab.'map((per) => "");
}

type PersonEmptyKeyedTbl table<Person> key();

function testGettingKeysFromEmptyKeyedKeyLessTbl() {
    table<Person> key() tbl1 = table [
            {name: "John", age: 23},
            {name: "Anne", age: 20}
        ];
    _ = tbl1.keys();

    PersonEmptyKeyedTbl tbl2 = table [
            {name: "John", age: 23},
            {name: "Anne", age: 20}
        ];
    _ = tbl2.keys();
}

type EmployeeEmptyKeyedTbl table<Employee> key();

function testPutIncompatibleTypesWithEmptyKeyedKeyLessTbl() {
    EmployeeEmptyKeyedTbl employeeTbl = table [
            {name: "Lisa", department: "HR"},
            {name: "Jonas", department: "Marketing"}
        ];
    var person = {name: "Robert", age: 33};
    employeeTbl.put(person);

    table<Employee> key() employeeTbl2 = table [
            {name: "Lisa", department: "HR"},
            {name: "Jonas", department: "Marketing"}
        ];
    employeeTbl2.put(person);
}

function testAddIncompatibleTypesWithEmptyKeyedKeyLessTbl() {
    EmployeeEmptyKeyedTbl employeeTbl = table [
            {name: "Lisa", department: "HR"},
            {name: "Jonas", department: "Marketing"}
        ];
    var person = {name: "Robert", age: 33};
    employeeTbl.add(person);

    table<Employee> key() employeeTbl2 = table [
            {name: "Lisa", department: "HR"},
            {name: "Jonas", department: "Marketing"}
        ];
    employeeTbl2.add(person);
}

function testGettingMemberFromEmptyKeyedKeyLessTbl() {
    table<Person> key() tbl1 = table [
            {name: "John", age: 23},
            {name: "Anne", age: 20}
        ];
    _ = tbl1.get("John");

    PersonEmptyKeyedTbl tbl2 = table [
            {name: "John", age: 23},
            {name: "Anne", age: 20}
        ];
    _ = tbl2.get("John");
}

function testRemovingMemberFromEmptyKeyedKeyLessTbl() {
    EmployeeEmptyKeyedTbl tbl1 = table [
            {name: "Lisa", department: "HR"},
            {name: "Jonas", department: "Marketing"}
        ];
    _ = tbl1.remove("Lisa");

    table<Employee> key() tbl2 = table [
            {name: "Lisa", department: "HR"},
            {name: "Jonas", department: "Marketing"}
        ];
    _ = tbl2.remove("Lisa");
}

function testRemovingMemberFromEmptyKeyedKeyLessTbl2() {
    EmployeeEmptyKeyedTbl tbl1 = table [
            {name: "Lisa", department: "HR"},
            {name: "Jonas", department: "Marketing"}
        ];
    _ = tbl1.removeIfHasKey("Lisa");

    table<Employee> key() tbl2 = table [
            {name: "Lisa", department: "HR"},
            {name: "Jonas", department: "Marketing"}
        ];
    _ = tbl2.removeIfHasKey("Lisa");
}

function testRemovingIfKeyIsAvaibleFromEmptyKeyedKeyLessTbl() {
    table<Person> key() tbl1 = table [
            {name: "John", age: 23},
            {name: "Anne", age: 20}
        ];
    _ = tbl1.hasKey("John");

    PersonEmptyKeyedTbl tbl2 = table [
            {name: "John", age: 23},
            {name: "Anne", age: 20}
        ];
    _ = tbl2.hasKey("Anne");
}

function testNextKeyFromEmptyKeyedKeyLessTbl() {
    table<Person> key() tbl1 = table [
            {name: "John", age: 23},
            {name: "Anne", age: 20}
        ];
    _ = tbl1.nextKey();

    PersonEmptyKeyedTbl tbl2 = table [
            {name: "John", age: 23},
            {name: "Anne", age: 20}
        ];
    _ = tbl2.nextKey();
}

function testFilterFromEmptyKeyedKeyLessTbl() {
    PersonEmptyKeyedTbl personTable = table [
            {name: "Harry", age: 14},
            {name: "Hermione", age: 28}
        ];
    _ = personTable.filter(function(Person person) returns boolean {
        return person.age < 15;
    });

    table<Person> key() personTable2 = table [
            {name: "Harry", age: 14},
            {name: "Hermione", age: 28}
        ];
    _ = personTable2.filter(function(Person person) returns boolean {
        return person.age < 15;
    });
}

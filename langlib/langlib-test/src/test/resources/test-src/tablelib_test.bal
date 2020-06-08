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

type PersonValue record {|
  Person value;
|};

type Customer record {
  readonly int id;
  string firstName;
  string lastName;
};

type Employee record {|
  readonly string name;
  string department;
|};

type PersonalTable table<Person> key(name);

type EmployeeTable table<Employee> key(name);

type CustomerTable table<Customer> key(id);

type CustomerKeyLessTable table<Customer>;

PersonalTable tab = table key(name)[
  { name: "Chiran", age: 33 },
  { name: "Mohan", age: 37 },
  { name: "Gima", age: 38 },
  { name: "Granier", age: 34 }
];

function getPerson(record {| Person value; |}? returnedVal) returns Person? {
    if (returnedVal is PersonValue) {
       return returnedVal.value;
    } else {
       return ();
    }
}

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

function testTableLength() returns int {
    return tab.length();
}

function testIterator() returns boolean {
    boolean testPassed = true;
    Person[] personList = getPersonList();
    abstract object { public function next() returns record {| Person value; |}?;} itr = tab.iterator();

    Person? person = getPerson(itr.next());
    testPassed = testPassed && person == personList[0];
    person = getPerson(itr.next());
    testPassed = testPassed && person == personList[1];
    person = getPerson(itr.next());
    testPassed = testPassed && person == personList[2];
    person = getPerson(itr.next());
    testPassed = testPassed && person == personList[3];
    person = getPerson(itr.next());
    testPassed = testPassed && person == ();
    return testPassed;
}

function testAddNewRecordAfterIteratorCreation() {
    table<Person> key(name) tab = table [
      { name: "Chiran", age: 33 },
      { name: "Mohan", age: 37 },
      { name: "Gima", age: 38 },
      { name: "Granier", age: 34 }
    ];

    var itr = tab.iterator();
    Person p = { name: "Lasini", age: 25 };
    tab.put(p);
    var value = itr.next();
}

function testRemoveAlreadyReturnedRecordFromIterator() returns boolean {
    table<Person> key(name) tab = table [
      { name: "Chiran", age: 33 },
      { name: "Mohan", age: 37 },
      { name: "Gima", age: 38 },
      { name: "Granier", age: 34 }
    ];

    var itr = tab.iterator();
    var value = itr.next();
    value = itr.next();
    _ = tab.remove("Chiran");
    value = itr.next();

    return value?.value?.name == "Gima";
}

function removeIfHasKeyReturnedRecordFromIterator() returns boolean {
    table<Person> key(name) tab = table [
      { name: "Chiran", age: 33 },
      { name: "Mohan", age: 37 },
      { name: "Gima", age: 38 },
      { name: "Granier", age: 34 }
    ];

    var itr = tab.iterator();
    var value = itr.next();
    value = itr.next();
    _ = tab.removeIfHasKey("Chiran");
    value = itr.next();

    return value?.value?.name == "Gima";
}

function testChangeValueForAGivenKeyWhileIterating() returns boolean {
    table<Person> key(name) tab = table [
      { name: "Chiran", age: 33 },
      { name: "Mohan", age: 37 },
      { name: "Gima", age: 38 },
      { name: "Granier", age: 34 }
    ];

    var itr = tab.iterator();
    var value = itr.next();
    value = itr.next();
    Person p = { name: "Gima", age: 50 };
    tab["Gima"] = p;
    value = itr.next();

    return value?.value?.age == 50;
}

function getValueFromKey() returns boolean {
    boolean testPassed = true;
    Person[] personList = getPersonList();
    testPassed = testPassed && tab.get("Chiran") == personList[0];
    testPassed = testPassed && tab.get("Mohan") == personList[1];
    testPassed = testPassed && tab.get("Gima") == personList[2];
    testPassed = testPassed && tab.get("Granier") == personList[3];

    return testPassed;
}

function getWithInvalidKey() returns boolean {
     Person person = tab.get("AAA");
     return person.name == "AAA";
}

function testMap() returns boolean {
    boolean testPassed = true;
    Person[] personList = getPersonList();

    table<Employee> empTab = tab.'map(function (Person person) returns Employee {
          return {name: person.name, department : "HR"};
    });

    var personTblKeys = tab.keys();
    var castedEmpTab = <table<Employee> key(name)> empTab;
    var empTblKeys = castedEmpTab.keys();
    testPassed = testPassed && personTblKeys.length() == empTblKeys.length();
    //check keys of both tables are equal. Cannot check it until KeyType[] is returned

    int index = 0;
    castedEmpTab.forEach(function (Employee emp) {
    testPassed = testPassed && emp.name == personList[index].name;
    testPassed = testPassed && emp.department == "HR";
    index+=1;
    });

    return testPassed;
}

function testForeach() returns string {
    string filtered = "";

    tab.forEach(function (Person person) {
       if(person.age < 35) {
          filtered += person.name;
          filtered += " ";
       }
    });
    return filtered;
}

function testFilter() returns boolean {
    table<Person> key<string>  filteredTable = tab.filter(function (Person person) returns boolean {
                                                  return person.age < 35;
                                              });
    return filteredTable.length() == 2;
}

function testReduce() returns float {
    float avg = tab.reduce(function (float accum, Person val) returns float {
                               return accum + <float>val.age / tab.length();
                           }, 0.0);
    return avg;
}

function removeWithKey() returns boolean {
    PersonalTable tbl = table key(name) [{ name: "Chiran", age: 33 },
    { name: "Mohan", age: 37 },
    { name: "Gima", age: 38 },
    { name: "Granier", age: 34 }];

    Person[] personList = getPersonList();
    Person removedPerson = tbl.remove("Gima");

    return removedPerson == personList[2];
}

function removeWithInvalidKey() returns boolean {
    Person removedPerson = tab.remove("AAA");
    return removedPerson.name == "AAA";
}

function removeIfHasKey() returns boolean {
    PersonalTable tbl = table key(name) [{ name: "Chiran", age: 33 },
    { name: "Mohan", age: 37 },
    { name: "Gima", age: 38 },
    { name: "Granier", age: 34 }];
    Person? removedPerson1 = tbl.removeIfHasKey("AAA");
    Person? removedPerson2 = tbl.removeIfHasKey("Chiran");
    return removedPerson1 == () && removedPerson2?.name == "Chiran";
}

function testHasKey() returns boolean {
    return tab.hasKey("Mohan");
}

function testGetKeyList() returns any[] {
    return tab.keys();
}

function removeAllFromTable() returns boolean {
    PersonalTable tbl = table key(name) [{ name: "Chiran", age: 33 },
    { name: "Mohan", age: 37 },
    { name: "Gima", age: 38 },
    { name: "Granier", age: 34 }];

    tbl.removeAll();
    return tbl.length() == 0;
}

function tableToArray() returns boolean {
    boolean testPassed = true;
    Person[] personList = getPersonList();
    Person[] tableToList = tab.toArray();
    testPassed = testPassed && personList[0] == tableToList[0];
    testPassed = testPassed && personList[1] == tableToList[1];
    testPassed = testPassed && personList[2] == tableToList[2];
    testPassed = testPassed && personList[3] == tableToList[3];
    return testPassed;
}

function testNextKey() returns int {
    CustomerTable custTbl = table key(id) [
      { id: 1, firstName: "Sanjiva", lastName: "Weerawarana" },
      { id: 2, firstName: "James", lastName: "Clark" },
      { id: 100, firstName: "Chiran", lastName: "Fernando" },
      { id: 5, firstName: "Gimantha", lastName: "Bandara" }
    ];
    return custTbl.nextKey();
}

function testAddData() returns boolean {
    boolean testPassed = true;
    CustomerTable custTbl = table key(id) [
      { id: 1, firstName: "Sanjiva", lastName: "Weerawarana" },
      { id: 2, firstName: "James", lastName: "Clark" },
      { id: 5, firstName: "Gimantha", lastName: "Bandara" }
    ];
    Customer data = { id: 100, firstName: "Chiran", lastName: "Fernando" };
    custTbl.add(data);
    Customer[] tableToList = custTbl.toArray();
    testPassed = testPassed && tableToList.length() == 4;
    testPassed = testPassed && tableToList[3] == data;
    return testPassed;
}

function testAddExistingMember() returns any[]|error {
    CustomerTable custTbl = table key(id) [
      { id: 1, firstName: "Sanjiva", lastName: "Weerawarana" },
      { id: 5, firstName: "Gimantha", lastName: "Bandara" }
    ];
    Customer data = { id: 5, firstName: "Gimantha", lastName: "Bandara" };
    custTbl.add(data);
    return custTbl.toArray();
}

function testPutData() returns boolean {
    boolean testPassed = true;
    CustomerTable custTbl = table key(id) [
      { id: 1, firstName: "Sanjiva", lastName: "Weerawarana" },
      { id: 2, firstName: "James", lastName: "Clark" },
      { id: 5, firstName: "Gimantha", lastName: "Bandara" }
    ];
    Customer customer1 = { id: 100, firstName: "Chiran", lastName: "Fernando" };
    Customer customer2 = { id: 5, firstName: "Grainier", lastName: "Perera" };
    custTbl.put(customer1);
    custTbl.put(customer2);
    Customer[] tableToList = custTbl.toArray();
    testPassed = testPassed && tableToList.length() == 4;
    testPassed = testPassed && tableToList[2] == customer2;
    testPassed = testPassed && tableToList[3] == customer1;
    return testPassed;
}

//function testPutInconsistentData() returns any[]|error {
//    CustomerTable custTbl = table key(id) [
//      { id: 1, firstName: "Sanjiva", lastName: "Weerawarana" },
//      { id: 5, firstName: "Gimantha", lastName: "Bandara" }
//    ];
//    var person = { name: "Chiran", age: 33 };
//    custTbl.put(person);
//    return custTbl.toArray();
//}

function testPutWithKeyLessTbl() returns boolean {
    boolean testPassed = true;
    CustomerKeyLessTable custTbl = table [
      { id: 1, firstName: "Sanjiva", lastName: "Weerawarana" },
      { id: 5, firstName: "Gimantha", lastName: "Bandara" }
    ];
    Customer customer = { id: 100, firstName: "Chiran", lastName: "Fernando" };
    custTbl.put(customer);
    Customer[] tableToList = custTbl.toArray();
    testPassed = testPassed && tableToList.length() == 3;
    testPassed = testPassed && tableToList[2] == customer;
    return testPassed;
}

function testAddWithKeyLessTbl() returns boolean {
    boolean testPassed = true;
    CustomerKeyLessTable custTbl = table [
      { id: 1, firstName: "Sanjiva", lastName: "Weerawarana" },
      { id: 5, firstName: "Gimantha", lastName: "Bandara" }
    ];
    Customer customer = { id: 100, firstName: "Chiran", lastName: "Fernando" };
    custTbl.add(customer);
    Customer[] tableToList = custTbl.toArray();
    testPassed = testPassed && tableToList.length() == 3;
    testPassed = testPassed && tableToList[2] == customer;
    return testPassed;
}

function testPutWithKeylessTableAfterIteratorCreation() {
    CustomerKeyLessTable custTbl = table [
      { id: 1, firstName: "Sanjiva", lastName: "Weerawarana" },
      { id: 2, firstName: "James", lastName: "Clark" }
    ];

    var itr = custTbl.iterator();
    Customer customer = { id: 2, firstName: "Jane", lastName: "Eyre"};
    custTbl.put(customer);
    var value = itr.next();
}

function testAddWithKeylessTableAfterIteratorCreation() {
    CustomerKeyLessTable custTbl = table [
      { id: 1, firstName: "Sanjiva", lastName: "Weerawarana" },
      { id: 2, firstName: "James", lastName: "Clark" }
    ];

    var itr = custTbl.iterator();
    Customer customer = { id: 3, firstName: "Jane", lastName: "Eyre"};
    custTbl.add(customer);
    var value = itr.next();
}

function testRemoveAllReturnedRecordsFromIteratorKeylessTbl() {
    CustomerKeyLessTable custTbl = table [
      { id: 1, firstName: "Sanjiva", lastName: "Weerawarana" },
      { id: 2, firstName: "James", lastName: "Clark" }
    ];

    var itr = custTbl.iterator();
    var value = itr.next();
    value = itr.next();
    custTbl.removeAll();
    value = itr.next();
}

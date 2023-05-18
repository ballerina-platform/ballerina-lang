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

type Engineer record {
   readonly string name;
   int age;
   boolean intern;
};

type Intern record {|
    readonly string name;
    int age;
    boolean intern;
    int salary;
|};

type Student record {|
     readonly string name;
     int age;
     int studentId;
|};

type Teacher record {|
   string fname;
   string lname;
|};

type PersonalTable table<Person> key(name);

type EmployeeTable table<Employee> key(name);

type CustomerTable table<Customer> key(id);

type CustomerKeyLessTable table<Customer>;

type PersonTable table<Person>;

type EngineerTable table<Engineer>;

type InternTable table<Intern>;

type PersonAnyTable table<map<any>>;

type TeacherTable table<Teacher>;

type ExampleTable table<map<string>>;

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
    object { public isolated function next() returns record {| Person value; |}?;} itr = tab.iterator();

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
    tab.put(p);
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
    string expected = "[{\"name\":\"Chiran\",\"department\":\"HR\"}," +
                      "{\"name\":\"Mohan\",\"department\":\"HR\"},{\"name\":\"Gima\",\"department\":\"HR\"}," +
                      "{\"name\":\"Granier\",\"department\":\"HR\"}]";
    boolean testPassed = true;
    Person[] personList = getPersonList();

    table<Employee> empTab = tab.'map(function (Person person) returns Employee {
          return {name: person.name, department : "HR"};
    });

    Employee[] tableToList = empTab.toArray();
    testPassed = testPassed && tableToList.length() == 4;
    testPassed = testPassed && empTab.toString() == expected;

    function (Person) returns Employee arrowExpr = (person) => {name: person.name, department : "HR"};

    empTab = tab.'map(arrowExpr);
    tableToList = empTab.toArray();
    testPassed = testPassed && tableToList.length() == 4;
    testPassed = testPassed && empTab.toString() == expected;

    empTab = tab.'map((person) => <Employee>{name: person.name, department : "HR"});
    tableToList = empTab.toArray();
    testPassed = testPassed && tableToList.length() == 4;
    testPassed = testPassed && empTab.toString() == expected;

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
                               return accum + <float>val.age / <float>tab.length();
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

type age record {
    int|string? age;
};

type NewPerson record {
  string name;
  readonly age age;
};

type NewPersonalTable table<NewPerson> key(age);

function testHasKey() {
    table<record { readonly int|string|float? k; }> key(k) tbl1 = table[];
    tbl1.add({k: 0});
    tbl1.add({k: 5});
    tbl1.add({k: -31});
    tbl1.add({k: "10"});
    tbl1.add({k: 100.05});
    assertFalse(tbl1.hasKey(()));
    assertFalse(tbl1.hasKey(30));
    assertTrue(tbl1.hasKey(0));
    assertTrue(tbl1.hasKey(-31));
    assertTrue(tbl1.hasKey(5));
    assertFalse(tbl1.hasKey(10));
    assertFalse(tbl1.hasKey("100.05"));

    NewPersonalTable tbl2 = table key(age) [{ name: "Chiran", age: {age: ()}},
        { name: "Mohan", age: {age: 54} },
        { name: "Gima", age: {age: "34"} },
        { name: "Granier", age: {age: "65"} }];
    assertFalse(tbl2.hasKey({age: 0}));
    assertFalse(tbl2.hasKey({age: 34}));
    assertTrue(tbl2.hasKey({age: 54}));
    assertTrue(tbl2.hasKey({age: "65"}));
}

function testHashCollisionHandlingScenarios() {
    table<record { readonly int|string|float? k; }> key(k) tbl1 = table[];
    tbl1.add({k: 0});
    tbl1.add({k: 5});
    tbl1.add({k: -31});
    tbl1.add({k: "10"});
    tbl1.add({k: 100.05});
    tbl1.add({k: ()});
    tbl1.add({k: 30});

    record { readonly int|string|float? k; } a = tbl1.get(0);
    record { readonly int|string|float? k; } b = tbl1.get(30);

    assertEquals(a.k, 0);
    assertEquals(b.k, 30);

    record { readonly int|string|float? k; } c = tbl1.remove(0);
    record { readonly int|string|float? k; } d = tbl1.remove(30);

    assertEquals(c.k, 0);
    assertEquals(d.k, 30);

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

function testAddInconsistentData() {
    EngineerTable engineerTbl = table key(name) [
      { name: "Lisa", age: 22, intern: true },
      { name: "Jonas", age: 21, intern: false }
    ];
    PersonTable personTbl = engineerTbl;
    Person person1 = { name: "John", age: 23 };
    personTbl.add(person1);
}

function testAddInconsistentData2() {
    EngineerTable engineerTbl = table key(name) [
      { name: "Lisa", age: 22, intern: true },
      { name: "Jonas", age: 21, intern: false }
    ];
    PersonTable personTbl = engineerTbl;
    Student student1 = { name: "John", age: 20, studentId: 1 };
    personTbl.add(student1);
}

function testAddInconsistentDataWithMapConstrTbl() {
    EngineerTable engineerTbl = table [
      { name: "Lisa", age: 22, intern: true },
      { name: "Jonas", age: 21, intern: false }
    ];
    PersonAnyTable personTbl = engineerTbl;
    Student student1 = { name: "John", age: 20, studentId: 1 };
    personTbl.put(student1);
}

function testAddInconsistentDataWithMapConstrTbl2() {
    TeacherTable teacherTbl = table [
      {fname: "Alex", lname: "Maddox"},
      {fname: "Nina", lname: "Smith"}
    ];
    ExampleTable exampleTbl = teacherTbl;
    map<string> s1= { name: "John" };
    exampleTbl.add(s1);
}

function testAddValidData() returns boolean {
    boolean testPassed = true;
    EngineerTable engineerTbl = table key(name) [
      { name: "Lisa", age: 22, intern: true },
      { name: "Jonas", age: 21, intern: false }
    ];
    PersonTable personTbl = engineerTbl;
    Engineer engineer1 = { name: "John", age: 23, intern: true };
    personTbl.add(engineer1);
    Person[] tableToList = personTbl.toArray();
    testPassed = testPassed && tableToList.length() == 3;
    testPassed = testPassed && tableToList[2] == engineer1;
    return testPassed;
}

function testAddValidData2() returns boolean {
    boolean testPassed = true;
    EngineerTable engineerTbl = table key(name) [
      { name: "Lisa", age: 22, intern: true },
      { name: "Jonas", age: 21, intern: false }
    ];
    PersonTable personTbl = engineerTbl;
    Intern intern1 = { name: "John", age: 23, intern: true, salary: 100 };
    personTbl.add(intern1);
    Person[] tableToList = personTbl.toArray();
    testPassed = testPassed && tableToList.length() == 3;
    testPassed = testPassed && tableToList[2] == intern1;
    return testPassed;
}

function testAddValidDataWithMapConstrTbl() returns boolean {
    boolean testPassed = true;
    EngineerTable engineerTbl = table key(name) [
      { name: "Lisa", age: 22, intern: true },
      { name: "Jonas", age: 21, intern: false }
    ];
    PersonAnyTable personTbl = engineerTbl;
    Intern intern1 = { name: "John", age: 23, intern: true, salary: 100 };
    personTbl.add(intern1);
    testPassed = testPassed && personTbl.toString() == "[{\"name\":\"Lisa\",\"age\":22,\"intern\":true}," +
    "{\"name\":\"Jonas\",\"age\":21,\"intern\":false},{\"name\":\"John\",\"age\":23,\"intern\":true," +
    "\"salary\":100}]";
    return testPassed;
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

function testPutInconsistentData() {
    EngineerTable engineerTbl = table key(name) [
      { name: "Lisa", age: 22, intern: true },
      { name: "Jonas", age: 21, intern: false }
    ];
    PersonTable personTbl = engineerTbl;
    Person person1 = { name: "John", age: 23 };
    personTbl.put(person1);
}

function testPutInconsistentData2() {
    EngineerTable engineerTbl = table key(name) [
      { name: "Lisa", age: 22, intern: true },
      { name: "Jonas", age: 21, intern: false }
    ];
    PersonTable personTbl = engineerTbl;
    Student student1 = { name: "John", age: 20, studentId: 1 };
    personTbl.put(student1);
}

function testPutInconsistentDataWithMapConstrTbl() {
    EngineerTable engineerTbl = table [
      { name: "Lisa", age: 22, intern: true },
      { name: "Jonas", age: 21, intern: false }
    ];
    PersonAnyTable personTbl = engineerTbl;
    Student student1 = { name: "John", age: 20, studentId: 1 };
    personTbl.put(student1);
}

function testPutInconsistentDataWithMapConstrTbl2() {
    TeacherTable teacherTbl = table [
      {fname: "Alex", lname: "Maddox"},
      {fname: "Nina", lname: "Smith"}
    ];
    ExampleTable exampleTbl = teacherTbl;
    map<string> s1= { name: "John" };
    exampleTbl.put(s1);
}

function testPutValidData() returns boolean {
    boolean testPassed = true;
    EngineerTable engineerTbl = table key(name) [
      { name: "Lisa", age: 22, intern: true },
      { name: "Jonas", age: 21, intern: false }
    ];
    PersonTable personTbl = engineerTbl;
    Engineer engineer1 = { name: "John", age: 23, intern: true };
    personTbl.put(engineer1);
    Person[] tableToList = personTbl.toArray();
    testPassed = testPassed && tableToList.length() == 3;
    testPassed = testPassed && tableToList[2] == engineer1;
    return testPassed;
}

function testPutValidData2() returns boolean {
    boolean testPassed = true;
    EngineerTable engineerTbl = table key(name) [
      { name: "Lisa", age: 22, intern: true },
      { name: "Jonas", age: 21, intern: false }
    ];
    PersonTable personTbl = engineerTbl;
    Intern intern1 = { name: "John", age: 23, intern: true, salary: 100 };
    personTbl.put(intern1);
    Person[] tableToList = personTbl.toArray();
    testPassed = testPassed && tableToList.length() == 3;
    testPassed = testPassed && tableToList[2] == intern1;
    return testPassed;
}

function testPutValidDataWithMapConstrTbl() returns boolean {
    boolean testPassed = true;
    EngineerTable engineerTbl = table key(name) [
      { name: "Lisa", age: 22, intern: true },
      { name: "Jonas", age: 21, intern: false }
    ];
    PersonAnyTable personTbl = engineerTbl;
    Intern intern1 = { name: "John", age: 23, intern: true, salary: 100 };
    personTbl.put(intern1);
    testPassed = testPassed && personTbl.toString() == "[{\"name\":\"Lisa\",\"age\":22,\"intern\":true}," +
    "{\"name\":\"Jonas\",\"age\":21,\"intern\":false},{\"name\":\"John\",\"age\":23,\"intern\":true," +
    "\"salary\":100}]";
    return testPassed;
}

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

function testRemoveThenIterate() returns boolean {
    table<Employee> key(name) data = table [
        { name: "Mary", department: "IT"},
        { name: "John", department: "HR" },
        { name: "Jim", department: "Admin" }
    ];

    Employee[] ar = [];
    var rm = data.remove("Mary");
    foreach var v in data {
        ar.push(v);
    }
    return ar.length() == 2 && ar[0].name == "John" && ar[1].name == "Jim";
}

function testRemoveEmptyThenIterate() returns boolean {
    table<Employee> key(name) data = table [
        { name: "Mary", department: "IT"},
        { name: "John", department: "HR" },
        { name: "Jim", department: "Admin" }
    ];

    Employee[] ar = [];
    var rm1 = data.remove("Mary");
    var rm2 = data.remove("John");
    var rm3 = data.remove("Jim");

    foreach var v in data {
        ar.push(v);
    }
    return ar.length() == 0;
}

function testRemoveEmptyAddThenIterate() returns boolean {
    table<Employee> key(name) data = table [
        { name: "Mary", department: "IT"},
        { name: "John", department: "HR" },
        { name: "Jim", department: "Admin" }
    ];

    Employee[] ar = [];
    var rm1 = data.remove("Mary");
    var rm2 = data.remove("John");
    var rm3 = data.remove("Jim");

    Employee newEmp = { name: "JesB", department: "Security" };
    data.add(newEmp);
    foreach var v in data {
        ar.push(v);
    }
    return ar.length() == 1 && ar[0].name == "JesB";
}

function testRemoveEmptyIterateThenAdd() returns boolean {
    table<Employee> key(name) data = table [
        { name: "Mary", department: "IT"},
        { name: "John", department: "HR" },
        { name: "Jim", department: "Admin" }
    ];

    Employee[] ar = [];
    var rm1 = data.remove("Mary");
    var rm2 = data.remove("John");
    var rm3 = data.remove("Jim");

    foreach var v in data {
        ar.push(v);
    }
    data.add({name: "JesB", department: "Security"});
    return data.length() == 1 && data["JesB"]?.name == "JesB" && ar.length() == 0;
}

function testRemoveEmptyIterateThenAddQueryExpr() returns boolean {
    table<Employee> key(name) data = table [
            {name: "Mary", department: "IT"},
            {name: "John", department: "HR"},
            {name: "Jim", department: "Admin"}
        ];

    var _ = data.remove("Mary");
    var _ = data.remove("John");
    var _ = data.remove("Jim");

    Employee[] ar = from var v in data
        select v;
    data.add({name: "JesB", department: "Security"});
    return data.length() == 1 && data["JesB"]?.name == "JesB" && ar.length() == 0;
}

function testRemoveEmptyIterateThenAddQueryAction() returns boolean|error {
    table<Employee> key(name) data = table [
            {name: "Mary", department: "IT"},
            {name: "John", department: "HR"},
            {name: "Jim", department: "Admin"}
        ];

    Employee[] ar = [];
    var _ = data.remove("Mary");
    var _ = data.remove("John");
    var _ = data.remove("Jim");

    check from var v in data
        do {
            ar.push(v);
        };
    data.add({name: "JesB", department: "Security"});
    return data.length() == 1 && data["JesB"]?.name == "JesB" && ar.length() == 0;
}

function testAddInconsistentDataToKeylessTbl() {
    EngineerTable engineerTbl = table [
      { name: "Lisa", age: 22, intern: true },
      { name: "Jonas", age: 21, intern: false }
    ];
    PersonTable personTbl = engineerTbl;
    Person person1 = { name: "John", age: 23 };
    personTbl.add(person1);
}

function testAddInconsistentDataToKeylessTbl2() {
    EngineerTable engineerTbl = table [
      { name: "Lisa", age: 22, intern: true },
      { name: "Jonas", age: 21, intern: false }
    ];
    PersonTable personTbl = engineerTbl;
    Student student1 = { name: "John", age: 20, studentId: 1 };
    personTbl.add(student1);
}

function testPutInconsistentDataToKeylessTbl() {
    EngineerTable engineerTbl = table [
      { name: "Lisa", age: 22, intern: true },
      { name: "Jonas", age: 21, intern: false }
    ];
    PersonTable personTbl = engineerTbl;
    Person person1 = { name: "John", age: 23 };
    personTbl.put(person1);
}

function testPutInconsistentDataToKeylessTbl2() {
    EngineerTable engineerTbl = table [
      { name: "Lisa", age: 22, intern: true },
      { name: "Jonas", age: 21, intern: false }
    ];
    PersonTable personTbl = engineerTbl;
    Student student1 = { name: "John", age: 20, studentId: 1 };
    personTbl.put(student1);
}

function testAddValidDataToKeylessTbl() returns boolean {
    boolean testPassed = true;
    EngineerTable engineerTbl = table [
      { name: "Lisa", age: 22, intern: true },
      { name: "Jonas", age: 21, intern: false }
    ];
    PersonTable personTbl = engineerTbl;
    Engineer engineer1 = { name: "John", age: 23, intern: true };
    personTbl.add(engineer1);
    Person[] tableToList = personTbl.toArray();
    testPassed = testPassed && tableToList.length() == 3;
    testPassed = testPassed && tableToList[2] == engineer1;
    return testPassed;
}

function testPutValidDataToKeylessTbl() returns boolean {
    boolean testPassed = true;
    EngineerTable engineerTbl = table [
      { name: "Lisa", age: 22, intern: true },
      { name: "Jonas", age: 21, intern: false }
    ];
    PersonTable personTbl = engineerTbl;
    Intern intern1 = { name: "John", age: 23, intern: true, salary: 100 };
    personTbl.add(intern1);
    Person[] tableToList = personTbl.toArray();
    testPassed = testPassed && tableToList.length() == 3;
    testPassed = testPassed && tableToList[2] == intern1;
    return testPassed;
}

function testReadOnlyTableFilter() {
    PersonalTable & readonly personTable = table [
      { name: "Harry", age: 14 },
      { name: "Hermione", age: 28 },
      { name: "Ron", age: 11 },
      { name: "Draco", age: 23 }
    ];
    table<Person> children = personTable.filter(function (Person person) returns boolean {
                                                      return person.age < 18;
                                                  });
    assertEquals(children.length(), 2);
    children.forEach(function(Person person) {
        assertTrue(person.age < 18);
        assertTrue(person.isReadOnly());
    });
    assertFalse(children.isReadOnly());
}

type UnionConstraint Person|Employee;

type UnionConstrinedTbl table<UnionConstraint> key(name);

function testGetKeysFromUnionConstrained() returns any[] {
    UnionConstrinedTbl tab = table key(name)[
      { name: "Adam", age: 33 },
      { name: "Mark", department: "HR" }
    ];
    return tab.keys();
}

type KeylessPersonTable table<Person>;

function testKeylessTableForeach() {
    KeylessPersonTable personTable = table [
          { name: "Harry", age: 14 },
          { name: "Hermione", age: 28 },
          { name: "Ron", age: 11 },
          { name: "Draco", age: 23 }
    ];

    int ageSum = 0;
    personTable.forEach(function(Person person) {
        ageSum += person.age;
    });
    assertTrue(ageSum == 76);
}

function testKeylessReadOnlyTableForeach() {
    KeylessPersonTable & readonly personTable = table [
      { name: "Harry", age: 14 },
      { name: "Hermione", age: 28 },
      { name: "Ron", age: 11 },
      { name: "Draco", age: 23 }
    ];

    int ageSum = 0;
    personTable.forEach(function(Person person) {
        ageSum += person.age;
    });
    assertTrue(ageSum == 76);
}

type R record {|
    readonly int|float|decimal|boolean v;
    int code;
|};

type Tab table<R> key(v);

function testGetValue() {
    Tab t = table [
            {v: 0, code: 0},
            {v: 1d, code: 1},
            {v: false, code: 3},
            {v: 2.0, code: 4}
        ];

    R r1 = {"v":1d,"code":1};
    R r2 = {v: false, code: 3};

    assertEquals(t[0f], ());
    assertEquals(t[1d], r1);
    assertEquals(t[false], r2);
    assertEquals(t[2], ());
}

function testReduceForKeylessTables() {
    KeylessPersonTable personTable = table [
          { name: "Harry", age: 14 },
          { name: "Hermione", age: 28 },
          { name: "Ron", age: 11 },
          { name: "Draco", age: 23 }
    ];
    float avg = personTable.reduce(function (float accum, Person val) returns float {
                               return accum + <float>val.age / <float>tab.length();
                           }, 0.0);
    assertEquals(19.0, avg);
}

function testReduceForKeylessReadOnlyTables() {
    KeylessPersonTable & readonly personTable = table [
          { name: "Harry", age: 14 },
          { name: "Hermione", age: 28 },
          { name: "Ron", age: 11 },
          { name: "Draco", age: 23 }
    ];
    float avg = personTable.reduce(function (float accum, Person val) returns float {
                               return accum + <float>val.age / <float>tab.length();
                           }, 0.0);
    assertEquals(19.0, avg);
}

type CustomerEmptyKeyedTbl table<Customer> key();

function testPutWithEmptyKeyedKeyLessTbl() {
    CustomerEmptyKeyedTbl custTbl = table [
            {id: 1, firstName: "Robert", lastName: "Downey"},
            {id: 5, firstName: "Ryan", lastName: "Reynolds"}
        ];
    Customer customer = {id: 100, firstName: "Mark", lastName: "Ruffalo"};
    custTbl.put(customer);

    Customer[] tableToList = custTbl.toArray();
    assertEquals(3, tableToList.length());
    assertEquals(customer, tableToList[2]);
}

function testPutWithEmptyKeyedKeyLessTblAfterIteratorCreation() {
    table<Customer> key() custTbl = table [
            {id: 1, firstName: "Robert", lastName: "Downey"},
            {id: 2, firstName: "Ryan", lastName: "Reynolds"}
        ];

    var itr = custTbl.iterator();
    Customer customer = {id: 2, firstName: "Mark", lastName: "Ruffalo"};
    custTbl.put(customer);

    var value = trap itr.next();
    assertTrue(value is error);
    if value is error {
        assertEquals("{ballerina}IteratorMutabilityError", value.message());
        assertEquals("Table was mutated after the iterator was created", <string> checkpanic value.detail()["message"]);
    }
}

function testAddWithEmptyKeyedKeyLessTbl() {
    table<Customer> key() custTbl = table [
            {id: 1, firstName: "Robert", lastName: "Downey"},
            {id: 2, firstName: "Ryan", lastName: "Reynolds"}
        ];
    Customer customer = {id: 100, firstName: "Mark", lastName: "Ruffalo"};
    custTbl.add(customer);

    Customer[] tableToList = custTbl.toArray();
    assertEquals(3, tableToList.length());
    assertEquals(customer, tableToList[2]);
}

function testAddWithEmptyKeyedKeyLessTblAfterIteratorCreation() {
    CustomerEmptyKeyedTbl custTbl = table [
            {id: 1, firstName: "Robert", lastName: "Downey"},
            {id: 2, firstName: "Ryan", lastName: "Reynolds"}
        ];

    var itr = custTbl.iterator();
    Customer customer = {id: 2, firstName: "Mark", lastName: "Ruffalo"};
    custTbl.add(customer);

    var value = trap itr.next();
    assertTrue(value is error);
    if value is error {
        assertEquals("{ballerina}IteratorMutabilityError", value.message());
        assertEquals("Table was mutated after the iterator was created", <string> checkpanic value.detail()["message"]);
    }
}

function testRemoveAllReturnedRecordsFromIteratorEmptyKeyedKeyLessTbl() {
    CustomerEmptyKeyedTbl custTbl = table [
            {id: 1, firstName: "Robert", lastName: "Downey"},
            {id: 2, firstName: "Ryan", lastName: "Reynolds"}
        ];

    var itr = custTbl.iterator();
    var value = itr.next();
    assertEquals({id: 1, firstName: "Robert", lastName: "Downey"}, value["value"]);
    value = itr.next();
    assertEquals({id: 2, firstName: "Ryan", lastName: "Reynolds"}, value["value"]);

    custTbl.removeAll();

    var value2 = trap itr.next();
    assertTrue(value2 is error);
    if value2 is error {
        assertEquals("{ballerina}IteratorMutabilityError", value2.message());
        assertEquals("Table was mutated after the iterator was created", <string> checkpanic value2.detail()["message"]);
    }
}

type EngineerEmptyKeyedTbl table<Engineer> key();

type PersonEmptyKeyedTbl table<Person> key();

function testAddInconsistentDataToEmptyKeyedKeyLessTbl() {
    EngineerEmptyKeyedTbl engineerTbl = table [
            {name: "Lisa", age: 22, intern: true},
            {name: "Jonas", age: 21, intern: false}
        ];

    table<Person> key() personTbl = engineerTbl;

    Person person = {name: "John", age: 23};
    var value = trap personTbl.add(person);
    assertTrue(value is error);
    if value is error {
        assertEquals("{ballerina/lang.table}InherentTypeViolation", value.message());
        assertEquals("value type 'Person' inconsistent with the inherent table type 'table<Engineer>'", <string> checkpanic value.detail()["message"]);
    }
}

function testAddInconsistentDataToEmptyKeyedKeyLessTbl2() {
    table<Engineer> key() engineerTbl = table [
            {name: "Lisa", age: 22, intern: true},
            {name: "Jonas", age: 21, intern: false}
        ];

    PersonEmptyKeyedTbl personTbl = engineerTbl;

    Student student = {name: "John", age: 20, studentId: 1};
    var value = trap personTbl.add(student);
    assertTrue(value is error);
    if value is error {
        assertEquals("{ballerina/lang.table}InherentTypeViolation", value.message());
        assertEquals("value type 'Student' inconsistent with the inherent table type 'table<Engineer>'", <string>checkpanic value.detail()["message"]);
    }
}

function testPutInconsistentDataToEmptyKeyedKeyLessTbl() {
    EngineerEmptyKeyedTbl engineerTbl = table [
            {name: "Lisa", age: 22, intern: true},
            {name: "Jonas", age: 21, intern: false}
        ];

    table<Person> key() personTbl = engineerTbl;

    Person person = {name: "John", age: 23};
    var value = trap personTbl.put(person);
    assertTrue(value is error);
    if value is error {
        assertEquals("{ballerina/lang.table}InherentTypeViolation", value.message());
        assertEquals("value type 'Person' inconsistent with the inherent table type 'table<Engineer>'", <string> checkpanic value.detail()["message"]);
    }
}

function testPutInconsistentDataToEmptyKeyedKeyLessTbl2() {
    table<Engineer> key() engineerTbl = table [
            {name: "Lisa", age: 22, intern: true},
            {name: "Jonas", age: 21, intern: false}
        ];

    PersonEmptyKeyedTbl personTbl = engineerTbl;

    Student student = {name: "John", age: 20, studentId: 1};
    var value = trap personTbl.put(student);
    assertTrue(value is error);
    if value is error {
        assertEquals("{ballerina/lang.table}InherentTypeViolation", value.message());
        assertEquals("value type 'Student' inconsistent with the inherent table type 'table<Engineer>'", <string>checkpanic value.detail()["message"]);
    }
}

function testAddValidDataToEmptyKeyedKeyLessTbl() {
    EngineerEmptyKeyedTbl engineerTbl = table [
            {name: "Lisa", age: 22, intern: true},
            {name: "Jonas", age: 21, intern: false}
        ];

    PersonEmptyKeyedTbl personTbl = engineerTbl;

    Engineer engineer = {name: "John", age: 23, intern: true};
    personTbl.add(engineer);

    Person[] tableToList = personTbl.toArray();

    assertEquals(3, tableToList.length());
    assertEquals(engineer, tableToList[2]);
}

function testPutValidDataToEmptyKeyedKeyLessTbl() {
    EngineerEmptyKeyedTbl engineerTbl = table [
            {name: "Lisa", age: 22, intern: true},
            {name: "Jonas", age: 21, intern: false}
        ];

    PersonEmptyKeyedTbl personTbl = engineerTbl;

    Engineer engineer = {name: "John", age: 23, intern: true};
    personTbl.put(engineer);

    Person[] tableToList = personTbl.toArray();

    assertEquals(3, tableToList.length());
    assertEquals(engineer, tableToList[2]);
}

function testEmptyKeyedKeyLessTblForeach() {
    PersonEmptyKeyedTbl personTable = table [
            {name: "Harry", age: 14},
            {name: "Hermione", age: 28},
            {name: "Ron", age: 11},
            {name: "Draco", age: 23}
        ];

    int ageSum = 0;
    personTable.forEach(function(Person person) {
        ageSum += person.age;
    });
    assertEquals(76, ageSum);
}

function testEmptyKeyedKeyLessTblReadOnlyTableForeach() {
    PersonEmptyKeyedTbl & readonly personTable = table [
            {name: "Harry", age: 14},
            {name: "Hermione", age: 28},
            {name: "Ron", age: 11},
            {name: "Draco", age: 23}
        ];

    int ageSum = 0;
    personTable.forEach(function(Person person) {
        ageSum += person.age;
    });
    assertEquals(76, ageSum);
}

function testReduceForEmptyKeyedKeyLessTbl() {
    PersonEmptyKeyedTbl personTable = table [
            {name: "Harry", age: 14},
            {name: "Hermione", age: 28},
            {name: "Ron", age: 11},
            {name: "Draco", age: 23}
        ];

    float avg = personTable.reduce(function(float accum, Person val) returns float {
        return accum + <float>val.age / <float>tab.length();
    }, 0.0);
    assertEquals(19.0, avg);
}

function testReduceForEmptyKeyedKeyLessReadOnlyTbl() {
    PersonEmptyKeyedTbl & readonly personTable = table [
            {name: "Harry", age: 14},
            {name: "Hermione", age: 28},
            {name: "Ron", age: 11},
            {name: "Draco", age: 23}
        ];

    float avg = personTable.reduce(function(float accum, Person val) returns float {
        return accum + <float>val.age / <float>tab.length();
    }, 0.0);
    assertEquals(19.0, avg);
}

type EmployeeEmptyKeyedTbl table<Employee> key();

function testMapWithEmptyKeyedKeyLessTbl() {
    PersonEmptyKeyedTbl personTable = table [
            {name: "Harry", age: 14},
            {name: "Hermione", age: 28},
            {name: "Ron", age: 11},
            {name: "Draco", age: 23}
        ];

    table<Employee> key() empTab = personTable.map(function (Person person) returns Employee {
          return {name: person.name, department : "HR"};
    });

    EmployeeEmptyKeyedTbl employeeTbl = table [
            {"name": "Harry", "department": "HR"},
            {"name": "Hermione", "department": "HR"},
            {"name": "Ron", "department": "HR"},
            {"name": "Draco", "department": "HR"}
        ];

    assertEquals(employeeTbl, empTab);
}

function testLengthWithEmptyKeyedKeyLessTbl() {
    PersonEmptyKeyedTbl personTable = table [
            {name: "Harry", age: 14},
            {name: "Hermione", age: 28},
            {name: "Ron", age: 11},
            {name: "Draco", age: 23}
        ];
    assertEquals(4, personTable.length());
}

type Coordinate1 record {|
    readonly int x;
    int y?;
|};

function testTableIterationAfterPut1() {
    table<Coordinate1> key(x) positions = table [];
    positions.put({x: 0});
    positions.put({x: 1});
    positions.put({x: -1});
    assertEquals(positions.toString(), "[{\"x\":0},{\"x\":1},{\"x\":-1}]");
    int sum = -2;
    foreach var position in positions {
        sum = sum + position.x;
    }
    assertEquals(sum, -2);
}

type Coordinate2 record {|
    readonly float x;
    int y?;
|};

function testTableIterationAfterPut2() {
    table<Coordinate2> key(x) positions = table [];
    positions.put({x: 0});
    positions.put({x: 1});
    positions.put({x: -1});
    assertEquals(positions.toString(), "[{\"x\":0.0},{\"x\":1.0},{\"x\":-1.0}]");
    float sum = -2;
    foreach var position in positions {
        sum = sum + position.x;
    }
    assertEquals(sum, -2.0);
}

type Coordinate3 record {|
    readonly decimal x;
    int y?;
|};

function testTableIterationAfterPut3() {
    table<Coordinate3> key(x) positions = table [];
    positions.put({x: 0});
    positions.put({x: 1});
    positions.put({x: -1});
    assertEquals(positions.toString(), "[{\"x\":0},{\"x\":1},{\"x\":-1}]");
    decimal sum = -2;
    foreach var position in positions {
        sum = sum + position.x;
    }
    assertEquals(sum, -2d);
}

type Position record {|
    readonly int x;
    readonly int y;
|};

Position 'start = {x: 0, y: 0};
Position end = {x: 5, y: 5};

function testTableIterationAfterPut4() {
    int iterations = 0;
    int length = 0;
    table<Position> key(x,y) possible = table [];
    possible.add('start);
    while !possible.hasKey([end.x, end.y]) {
        iterations = iterations + 1;
        table<Position> key(x,y) next = table [];
        foreach var p in possible {
            next.put({x: p.x, y: p.y});
            next.put({x: p.x + 1, y: p.y});
            next.put({x: p.x - 1, y: p.y});
            next.put({x: p.x, y: p.y + 1});
            next.put({x: p.x, y: p.y - 1});
        }
        var _ = next.removeIfHasKey(['start.x, 'start.y - 1]);
        possible = next;
        length = possible.length();
    }
    assertEquals(iterations, 10);
    assertEquals(length, 218);
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(boolean actual) {
    assertEquals(true, actual);
}

function assertFalse(boolean actual) {
    assertEquals(false, actual);
}

function assertEquals(anydata expected, anydata actual) {
    if (expected == actual) {
        return;
    }
    typedesc<anydata> expT = typeof expected;
    typedesc<anydata> actT = typeof actual;
    string msg = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
    panic error(ASSERTION_ERROR_REASON, message = msg);
}

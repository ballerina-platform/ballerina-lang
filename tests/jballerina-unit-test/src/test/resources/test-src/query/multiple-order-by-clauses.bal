// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type Student record {|
   readonly int id;
   string? fname;
   float fee;
   decimal impact;
   boolean isUndergrad;
|};

type Person record {|
    string firstName;
    string lastName;
    int age;
    string address;
|};

type Customer record {|
    readonly int id;
    readonly string name;
    int noOfItems;
|};

type CustomerProfile record {|
    string name;
    int age;
    int noOfItems;
    string address;
|};

type StudentTable table<Student> key(id);

function testQueryExprWithMultipleOrderByClauses() returns boolean {
    boolean testPassed = true;

    Student s1 = {id: 1, fname: "John", fee: 2000.56, impact: 0.4, isUndergrad: true};
    Student s2 = {id: 2, fname: "John", fee: 2000.56, impact: 0.45, isUndergrad: true};
    Student s3 = {id: 2, fname: (), fee: 4000.56, impact: 0.4, isUndergrad: true};
    Student s4 = {id: 2, fname: "Kate", fee: 2000.56, impact: 0.4, isUndergrad: true};
    Student s5 = {id: 3, fname: "John", fee: 2000.56, impact: 0.45, isUndergrad: true};

    Student[] studentList = [s1, s2, s3, s4, s5];

    Student[] opStudentList =
        from var student in studentList
        order by student.fname descending, student.impact
        order by student.id descending
        select student;

    testPassed = testPassed && opStudentList.length() == 5;
    testPassed = testPassed &&  opStudentList[0] ==  studentList[4];
    testPassed = testPassed &&  opStudentList[1] ==  studentList[3];
    testPassed = testPassed &&  opStudentList[2] ==  studentList[1];
    testPassed = testPassed &&  opStudentList[3] ==  studentList[2];
    testPassed = testPassed &&  opStudentList[4] ==  studentList[0];

    return testPassed;
}

function testQueryExprWithMultipleFromAndMultipleOrderByClauses() returns boolean {
    boolean testPassed = true;

    Customer c1 = {id: 1, name: "Melina", noOfItems: 12};
    Customer c2 = {id: 5, name: "James", noOfItems: 5};
    Customer c3 = {id: 7, name: "James", noOfItems: 25};
    Customer c4 = {id: 0, name: "James", noOfItems: 25};

    Person p1 = {firstName: "Amy", lastName: "Melina", age: 23, address: "New York"};
    Person p2 = {firstName: "Frank", lastName: "James", age: 30, address: "California"};

    Customer[] customerList = [c1, c2, c3, c4];
    Person[] personList = [p1, p2];

    Customer[] opCustomerList =
        from var customer in customerList
        from var person in personList
        let string customerName = "Johns"
        where person.lastName == customer.name
        order by customer.id descending
        order by person.address
        select {
            id: customer.id,
            name: customerName,
            noOfItems: customer.noOfItems
        };

    testPassed = testPassed && opCustomerList.length() == 4;
    Customer cus;
    cus = opCustomerList[0];
    testPassed = testPassed && cus.id == 7 && cus.noOfItems == 25;
    cus = opCustomerList[1];
    testPassed = testPassed && cus.id == 5 && cus.noOfItems == 5;
    cus = opCustomerList[2];
    testPassed = testPassed && cus.id == 0 && cus.noOfItems == 25;
    cus = opCustomerList[3];
    testPassed = testPassed && cus.id == 1 && cus.noOfItems == 12;
    return testPassed;
}

function testQueryExprWithJoinAndMultipleOrderByClauses() returns boolean {
    boolean testPassed = true;

    Customer c1 = {id: 1, name: "Melina", noOfItems: 12};
    Customer c2 = {id: 2, name: "James", noOfItems: 5};
    Customer c3 = {id: 3, name: "James", noOfItems: 25};
    Customer c4 = {id: 4, name: "James", noOfItems: 25};

    Person p1 = {firstName: "Amy", lastName: "Melina", age: 23, address: "New York"};
    Person p2 = {firstName: "Frank", lastName: "James", age: 30, address: "California"};

    Customer[] customerList = [c1, c2, c3, c4];
    Person[] personList = [p1, p2];

    CustomerProfile[] customerProfileList =
        from var customer in customerList
        join var person in personList
        on customer.name equals person.lastName
        order by customer.noOfItems descending
        order by person.address
        select {
            name: person.firstName,
            age : person.age,
            noOfItems: customer.noOfItems,
            address: person.address
        };

    testPassed = testPassed && customerProfileList.length() == 4;
    CustomerProfile cp;
    cp = customerProfileList[0];
    testPassed = testPassed && cp.name == "Frank" && cp.age == 30 && cp.noOfItems == 25 && cp.address == "California";
    cp = customerProfileList[1];
    testPassed = testPassed && cp.name == "Frank" && cp.age == 30 && cp.noOfItems == 25 && cp.address == "California";
    cp = customerProfileList[2];
    testPassed = testPassed && cp.name == "Frank" && cp.age == 30 && cp.noOfItems == 5 && cp.address == "California";
    cp = customerProfileList[3];
    testPassed = testPassed && cp.name == "Amy" && cp.age == 23 && cp.noOfItems == 12 && cp.address == "New York";
    return testPassed;
}

function testQueryExprWithInnerQueriesAndMultipleOrderByClauses() returns boolean {
    boolean testPassed = true;

    Customer c1 = {id: 1, name: "Melina", noOfItems: 62};
    Customer c2 = {id: 5, name: "James", noOfItems: 5};
    Customer c3 = {id: 9, name: "James", noOfItems: 25};
    Customer c4 = {id: 0, name: "James", noOfItems: 25};
    Customer c5 = {id: 2, name: "James", noOfItems: 30};

    Person p1 = {firstName: "Jennifer", lastName: "Melina", age: 23, address: "California"};
    Person p2 = {firstName: "Frank", lastName: "James", age: 30, address: "New York"};
    Person p3 = {firstName: "Zeth", lastName: "James", age: 50, address: "Texas"};

    Customer[] customerList = [c1, c2, c3, c4, c5];
    Person[] personList = [p1, p2, p3];

    CustomerProfile[] customerProfileList =
        from var customer in (stream from var c in customerList
                              order by c.id descending limit 4 select c)
        join var person in (from var p in personList order by p.firstName descending select p)
        on customer.name equals person.lastName
        order by customer.noOfItems descending
        order by person.address
        limit 4
        select {
            name: person.firstName,
            age : person.age,
            noOfItems: customer.noOfItems,
            address: person.address
        };

    testPassed = testPassed && customerProfileList.length() == 4;
    CustomerProfile cp;
    cp = customerProfileList[0];
    testPassed = testPassed && cp.name == "Jennifer" && cp.age == 23 && cp.noOfItems == 62
                    && cp.address == "California";
    cp = customerProfileList[1];
    testPassed = testPassed && cp.name == "Frank" && cp.age == 30 && cp.noOfItems == 30 && cp.address == "New York";
    cp = customerProfileList[2];
    testPassed = testPassed && cp.name == "Frank" && cp.age == 30 && cp.noOfItems == 25 && cp.address == "New York";
    cp = customerProfileList[3];
    testPassed = testPassed && cp.name == "Frank" && cp.age == 30 && cp.noOfItems == 5 && cp.address == "New York";
    return testPassed;
}

function testQueryExprWithMultipleOrderByAndMultipleLimitClauses() returns boolean {
    boolean testPassed = true;

    Customer c1 = {id: 1, name: "Melina", noOfItems: 62};
    Customer c2 = {id: 5, name: "James", noOfItems: 5};
    Customer c3 = {id: 9, name: "James", noOfItems: 25};
    Customer c4 = {id: 0, name: "James", noOfItems: 25};
    Customer c5 = {id: 2, name: "James", noOfItems: 30};

    Person p1 = {firstName: "Jennifer", lastName: "Melina", age: 23, address: "California"};
    Person p2 = {firstName: "Frank", lastName: "James", age: 30, address: "New York"};
    Person p3 = {firstName: "Zeth", lastName: "James", age: 50, address: "Texas"};

    Customer[] customerList = [c1, c2, c3, c4, c5];
    Person[] personList = [p1, p2, p3];

    CustomerProfile[] customerProfileList =
        from var customer in (stream from var c in customerList
                              order by c.id descending select c)
        join var person in (from var p in personList order by p.firstName descending select p)
        on customer.name equals person.lastName
        order by customer.noOfItems descending
        limit 5
        order by person.address
        limit 2
        select {
            name: person.firstName,
            age : person.age,
            noOfItems: customer.noOfItems,
            address: person.address
        };

    testPassed = testPassed && customerProfileList.length() == 2;
    CustomerProfile cp;
    cp = customerProfileList[0];
    testPassed = testPassed && cp.name == "Jennifer" && cp.age == 23 && cp.noOfItems == 62
                    && cp.address == "California";
    cp = customerProfileList[1];
    testPassed = testPassed && cp.name == "Frank" && cp.age == 30 && cp.noOfItems == 30 && cp.address == "New York";
    return testPassed;
}

function testQueryActionWithMultipleOrderByClauses() returns boolean {
    boolean testPassed = true;
    CustomerProfile[] customerProfileList = [];

    Customer c1 = {id: 1, name: "Melina", noOfItems: 62};
    Customer c2 = {id: 5, name: "James", noOfItems: 5};
    Customer c3 = {id: 9, name: "James", noOfItems: 25};
    Customer c4 = {id: 0, name: "James", noOfItems: 25};
    Customer c5 = {id: 2, name: "James", noOfItems: 30};

    Person p1 = {firstName: "Jennifer", lastName: "Melina", age: 23, address: "California"};
    Person p2 = {firstName: "Frank", lastName: "James", age: 30, address: "New York"};
    Person p3 = {firstName: "Zeth", lastName: "James", age: 50, address: "Texas"};

    Customer[] customerList = [c1, c2, c3, c4, c5];
    Person[] personList = [p1, p2, p3];

    error? op =
        from var customer in customerList
        join var person in personList
        on customer.name equals person.lastName
        order by customer.noOfItems descending
        limit 5
        order by person.address
        limit 2
        do {
           CustomerProfile cp = {name: person.firstName, age : person.age, noOfItems: customer.noOfItems,
                                          address: person.address};
           customerProfileList[customerProfileList.length()] = cp;
        };

    testPassed = testPassed && customerProfileList.length() == 2;
    CustomerProfile cp;
    cp = customerProfileList[0];
    testPassed = testPassed && cp.name == "Jennifer" && cp.age == 23 && cp.noOfItems == 62
                    && cp.address == "California";
    cp = customerProfileList[1];
    testPassed = testPassed && cp.name == "Frank" && cp.age == 30 && cp.noOfItems == 30 && cp.address == "New York";
    return testPassed;
}

function testQueryExprWithMultipleOrderByClausesReturnTable() returns boolean {
    boolean testPassed = true;

    Student s1 = {id: 1, fname: "John", fee: 2000.56, impact: 0.4, isUndergrad: true};
    Student s2 = {id: 2, fname: "John", fee: 2000.56, impact: 0.45, isUndergrad: true};
    Student s3 = {id: 9, fname: (), fee: 4000.56, impact: 0.4, isUndergrad: true};
    Student s4 = {id: 4, fname: "Kate", fee: 2000.56, impact: 0.4, isUndergrad: true};
    Student s5 = {id: 10, fname: "John", fee: 2000.56, impact: 0.45, isUndergrad: true};

    Student[] studentList = [s1, s2, s3, s4, s5];

    StudentTable|error opStudentTable =
        table key(id) from var student in studentList
        order by student.fname descending, student.impact
        order by student.id descending
        select student;

    if (opStudentTable is StudentTable) {
        Student[] opStudentList = opStudentTable.toArray();
        testPassed = testPassed &&  opStudentList[0] == studentList[4];
        testPassed = testPassed &&  opStudentList[1] == studentList[2];
        testPassed = testPassed &&  opStudentList[2] == studentList[3];
        testPassed = testPassed &&  opStudentList[3] == studentList[1];
        testPassed = testPassed &&  opStudentList[4] == studentList[0];
    }
    return testPassed;
}

function testQueryExprWithMultipleOrderByClausesReturnStream() returns boolean {
    boolean testPassed = true;

    Student s1 = {id: 1, fname: "John", fee: 2000.56, impact: 0.4, isUndergrad: true};
    Student s2 = {id: 2, fname: "John", fee: 2000.56, impact: 0.45, isUndergrad: true};
    Student s3 = {id: 9, fname: (), fee: 4000.56, impact: 0.4, isUndergrad: true};
    Student s4 = {id: 4, fname: "Kate", fee: 2000.56, impact: 0.4, isUndergrad: true};
    Student s5 = {id: 10, fname: "John", fee: 2000.56, impact: 0.45, isUndergrad: true};

    Student[] studentList = [s1, s2, s3, s4, s5];

    stream<Student> opStudentStream =
        stream from var student in studentList
        order by student.fname descending, student.impact
        order by student.id descending
        select student;

    Student[] opStudentList = [];
    record {| Student value; |}|error? v = opStudentStream.next();
    while (v is record {| Student value; |}) {
        opStudentList.push(v.value);
        v = opStudentStream.next();
    }
    testPassed = testPassed &&  opStudentList[0] == studentList[4];
    testPassed = testPassed &&  opStudentList[1] == studentList[2];
    testPassed = testPassed &&  opStudentList[2] == studentList[3];
    testPassed = testPassed &&  opStudentList[3] == studentList[1];
    testPassed = testPassed &&  opStudentList[4] == studentList[0];
    return testPassed;
}

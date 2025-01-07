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

type Student record {|
   int id;
   string? fname;
   float fee;
   decimal impact;
   boolean isUndergrad;
|};

type PersonPos record {|
    string firstName;
    string lastName;
    int age;
|};

type CustomerPos record {|
    readonly int id;
    readonly string name;
    int noOfItems;
|};

type CustomerProfile record {|
    string name;
    int age;
    int noOfItems;
|};

type Employee record {|
    string name;
    Address address;
    map<float?> tokens;
    int[] noOfShifts;
|};

type Address record {|
    int unitNo;
    string street;
|};

type PaymentInfo record {|
    int custId;
    string modeOfPayment;
|};

type CustomerTable table<CustomerPos> key(id, name);

type CustomerValue record {|
    CustomerPos value;
|};

type PersonValue record {|
    PersonPos value;
|};

function getCustomer(record {|CustomerPos value;|}? returnedVal) returns CustomerPos? {
    if (returnedVal is CustomerValue) {
        return returnedVal.value;
    } else {
        return ();
    }
}

function getPersonValue((record {|PersonPos value;|}|error?)|(record {|PersonPos value;|}?) returnedVal)
returns PersonValue? {
    var result = returnedVal;
    if (result is PersonValue) {
        return result;
    } else {
        return ();
    }
}

function testQueryExprWithOrderByClause() returns boolean {
    boolean testPassed = true;

    Student s1 = {id: 1, fname: "John", fee: 2000.56, impact: 0.4, isUndergrad: true};
    Student s2 = {id: 2, fname: "John", fee: 2000.56, impact: 0.45, isUndergrad: true};
    Student s3 = {id: 2, fname: (), fee: 4000.56, impact: 0.4, isUndergrad: true};
    Student s4 = {id: 2, fname: "Kate", fee: 2000.56, impact: 0.4, isUndergrad: true};

    Student[] studentList = [s1, s2, s3, s4];

    Student[] opStudentList = from var student in studentList
        order by student.fname descending, student.impact
        select student;

    testPassed = testPassed && opStudentList[0] == studentList[3];
    testPassed = testPassed && opStudentList[1] == studentList[0];
    testPassed = testPassed && opStudentList[2] == studentList[1];
    testPassed = testPassed && opStudentList[3] == studentList[2];

    return testPassed;
}

function testQueryExprWithOrderByClause2() returns boolean {
    boolean testPassed = true;

    Student s1 = {id: 1, fname: "John", fee: 2000.56, impact: 0.4, isUndergrad: true};
    Student s2 = {id: 2, fname: "John", fee: 2000.56, impact: 0.45, isUndergrad: false};
    Student s3 = {id: 2, fname: (), fee: 4000.56, impact: 0.4, isUndergrad: false};
    Student s4 = {id: 2, fname: "Kate", fee: 2000.56, impact: 0.4, isUndergrad: true};

    Student[] studentList = [s1, s2, s3, s4];

    Student[] opStudentList = from var student in studentList
        order by student.isUndergrad ascending, student.fee
        select student;

    testPassed = testPassed && opStudentList[0] == studentList[1];
    testPassed = testPassed && opStudentList[1] == studentList[2];
    testPassed = testPassed && opStudentList[2] == studentList[0];
    testPassed = testPassed && opStudentList[3] == studentList[3];

    return testPassed;
}

function testQueryExprWithOrderByClause3() returns CustomerPos[] {
    CustomerPos c1 = {id: 1, name: "Melina", noOfItems: 12};
    CustomerPos c2 = {id: 5, name: "James", noOfItems: 5};
    CustomerPos c3 = {id: 7, name: "James", noOfItems: 25};
    CustomerPos c4 = {id: 0, name: "James", noOfItems: 25};

    PersonPos p1 = {firstName: "Amy", lastName: "Melina", age: 23};
    PersonPos p2 = {firstName: "Frank", lastName: "James", age: 30};

    CustomerPos[] customerList = [c1, c2, c3, c4];
    PersonPos[] personList = [p1, p2];

    CustomerPos[] opCustomerList = from var customer in customerList
        from var person in personList
        let string customerName = "Johns"
        where person.lastName == "James"
        order by customer.id descending
        select {
            id: customer.id,
            name: customerName,
            noOfItems: customer.noOfItems
        };

    return opCustomerList;
}

function testQueryExprWithOrderByClauseReturnTable() returns boolean {
    boolean testPassed = true;

    CustomerPos c1 = {id: 1, name: "Melina", noOfItems: 12};
    CustomerPos c2 = {id: 2, name: "James", noOfItems: 5};
    CustomerPos c3 = {id: 3, name: "James", noOfItems: 25};
    CustomerPos c4 = {id: 4, name: "James", noOfItems: 25};

    PersonPos p1 = {firstName: "Amy", lastName: "Melina", age: 23};
    PersonPos p2 = {firstName: "Frank", lastName: "James", age: 30};

    CustomerPos[] customerList = [c1, c2, c3, c4];
    PersonPos[] personList = [p1, p2];

    CustomerTable|error customerTable = table key(id, name) from var customer in customerList
        from var person in personList
        where person.firstName == "Frank"
        order by customer.noOfItems descending, customer.id
        limit 3
        select {
            id: customer.id,
            name: customer.name,
            noOfItems: customer.noOfItems
        };

    if (customerTable is CustomerTable) {
        var itr = customerTable.iterator();
        CustomerPos? customer = getCustomer(itr.next());
        testPassed = testPassed && customer == customerList[2];
        customer = getCustomer(itr.next());
        testPassed = testPassed && customer == customerList[3];
        customer = getCustomer(itr.next());
        testPassed = testPassed && customer == customerList[0];
        customer = getCustomer(itr.next());
        testPassed = testPassed && customer == ();
    }

    return testPassed;
}

function testQueryExprWithOrderByClauseReturnStream() returns boolean {
    boolean testPassed = true;

    PersonPos p1 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    PersonPos p2 = {firstName: "John", lastName: "David", age: 33};
    PersonPos p3 = {firstName: "John", lastName: "Fonseka", age: 28};
    PersonPos p4 = {firstName: "John", lastName: "Fonseka", age: 30};
    PersonPos p5 = {firstName: "John", lastName: "Fonseka", age: 20};

    CustomerPos c1 = {id: 1, name: "John", noOfItems: 25};
    CustomerPos c2 = {id: 2, name: "Frank", noOfItems: 20};

    PersonPos[] personList = [p1, p2, p3, p4, p5];
    CustomerPos[] customerList = [c1, c2];

    stream<PersonPos> outputPersonStream = stream from var person in personList.toStream()
        from var customer in customerList
        let string newLastName = "Turin"
        let string newFirstName = "Johnas"
        where customer.name == "John"
        where person.lastName == "Fonseka"
        order by person.age descending
        select {
            firstName: newFirstName,
            lastName: newLastName,
            age: person.age
        };

    record {|PersonPos value;|}? person = getPersonValue(outputPersonStream.next());
    testPassed = testPassed && person?.value?.firstName == "Johnas" && person?.value?.lastName == "Turin" &&
    person?.value?.age == 30;

    person = getPersonValue(outputPersonStream.next());
    testPassed = testPassed && person?.value?.firstName == "Johnas" && person?.value?.lastName == "Turin" &&
    person?.value?.age == 30;

    person = getPersonValue(outputPersonStream.next());
    testPassed = testPassed && person?.value?.firstName == "Johnas" && person?.value?.lastName == "Turin" &&
    person?.value?.age == 28;

    person = getPersonValue(outputPersonStream.next());
    testPassed = testPassed && person?.value?.firstName == "Johnas" && person?.value?.lastName == "Turin" &&
    person?.value?.age == 20;

    person = getPersonValue(outputPersonStream.next());
    testPassed = testPassed && person == ();

    return testPassed;
}

function testQueryExprWithOrderByClauseAndJoin() returns CustomerProfile[] {
    CustomerPos c1 = {id: 1, name: "Melina", noOfItems: 12};
    CustomerPos c2 = {id: 2, name: "James", noOfItems: 5};
    CustomerPos c3 = {id: 3, name: "James", noOfItems: 25};
    CustomerPos c4 = {id: 4, name: "James", noOfItems: 25};

    PersonPos p1 = {firstName: "Amy", lastName: "Melina", age: 23};
    PersonPos p2 = {firstName: "Frank", lastName: "James", age: 30};

    CustomerPos[] customerList = [c1, c2, c3, c4];
    PersonPos[] personList = [p1, p2];

    CustomerProfile[] customerProfileList = from var customer in customerList
        join var person in personList
        on customer.name equals person.lastName
        order by customer.noOfItems
        select {
            name: person.firstName,
            age: person.age,
            noOfItems: customer.noOfItems
        };

    return customerProfileList;
}

function testQueryExprWithOrderByClauseHavingUserDefinedOrderKeyFunction() returns boolean {
    boolean testPassed = true;

    Employee e1 = {
        name: "Frank",
        address: {unitNo: 111, street: "Main Street"},
        tokens: {one: 1, two: 2, three: 3},
        noOfShifts: [1, 2, 3]
    };
    Employee e2 = {
        name: "James",
        address: {unitNo: 222, street: "Main Street"},
        tokens: {one: 1, two: 2, three: 3},
        noOfShifts: [1, 2, 3]
    };
    Employee e3 = {
        name: "James",
        address: {unitNo: 222, street: "Cross Street"},
        tokens: {one: 1, two: 2, three: 3},
        noOfShifts: [1, 2, 3]
    };
    Employee e4 = {
        name: "Frank",
        address: {unitNo: 111, street: "Cross Street"},
        tokens: {one: 1, two: 2, three: 3},
        noOfShifts: [1, 2, 3]
    };

    Employee[] empList = [e1, e2, e3, e4];

    Employee[] opEmpList = from var emp in empList
        order by emp.address.unitNo descending, emp.address.street.toLowerAscii()
        select emp;

    testPassed = testPassed && opEmpList[0] == empList[2];
    testPassed = testPassed && opEmpList[1] == empList[1];
    testPassed = testPassed && opEmpList[2] == empList[3];
    testPassed = testPassed && opEmpList[3] == empList[0];

    return testPassed;
}

function testQueryExprWithOrderByClauseHavingUserDefinedOrderKeyFunction2() returns boolean {
    boolean testPassed = true;

    Employee e1 = {
        name: "Frank",
        address: {unitNo: 111, street: "Main Street"},
        tokens: {one: 1, two: 2, three: 3},
        noOfShifts: [1, 2, 3]
    };
    Employee e2 = {
        name: "James",
        address: {unitNo: 222, street: "Main Street"},
        tokens: {one: 11, two: 2, three: 3},
        noOfShifts: [1, 2, 3]
    };
    Employee e3 = {
        name: "James",
        address: {unitNo: 222, street: "Cross Street"},
        tokens: {one: 111, two: 2, three: 3},
        noOfShifts: [1, 2, 3]
    };
    Employee e4 = {
        name: "Frank",
        address: {unitNo: 111, street: "Cross Street"},
        tokens: {one: 1111, two: 2, three: 3},
        noOfShifts: [1, 2, 3]
    };
    Employee e5 = {
        name: "Frank",
        address: {unitNo: 111, street: "Cross Street"},
        tokens: {one: 1111, two: 2, three: 3},
        noOfShifts: [3, 2, 3]
    };

    Employee[] empList = [e1, e2, e3, e4, e5];

    Employee[] opEmpList = from var emp in empList
        order by emp.name, emp.tokens["one"] descending, emp.noOfShifts[0] descending
        select emp;

    testPassed = testPassed && opEmpList[0] == empList[4];
    testPassed = testPassed && opEmpList[1] == empList[3];
    testPassed = testPassed && opEmpList[2] == empList[0];
    testPassed = testPassed && opEmpList[3] == empList[2];
    testPassed = testPassed && opEmpList[4] == empList[1];

    return testPassed;
}

function testQueryExprWithOrderByClauseHavingUserDefinedOrderKeyFunction3() returns CustomerProfile[] {
    CustomerPos c1 = {id: 1, name: "Melina", noOfItems: 12};
    CustomerPos c2 = {id: 2, name: "James", noOfItems: 5};
    CustomerPos c3 = {id: 3, name: "James", noOfItems: 25};
    CustomerPos c4 = {id: 4, name: "James", noOfItems: 25};

    PersonPos p1 = {firstName: "Amy", lastName: "Melina", age: 23};
    PersonPos p2 = {firstName: "Frank", lastName: "James", age: 30};

    CustomerPos[] customerList = [c1, c2, c3, c4];
    PersonPos[] personList = [p1, p2];

    CustomerProfile[] customerProfileList = from var customer in customerList
        join var person in personList
        on customer.name equals person.lastName
        order by incrementCount(0), customer.noOfItems descending
        select {
            name: person.firstName,
            age: person.age,
            noOfItems: customer.noOfItems
        };

    return customerProfileList;
}

function testQueryExprWithOrderByClauseHavingNaNNilValues() returns boolean {
    boolean testPassed = true;

    Employee e1 = {
        name: "Frank",
        address: {unitNo: 111, street: "Main Street"},
        tokens: {one: 1, two: 2, three: 3},
        noOfShifts: [1, 2, 3]
    };
    Employee e2 = {
        name: "James",
        address: {unitNo: 222, street: "Main Street"},
        tokens: {one: 11, two: (), three: 3},
        noOfShifts: [1, 2, 3]
    };
    Employee e3 = {
        name: "James",
        address: {unitNo: 222, street: "Cross Street"},
        tokens: {
            one: 11,
            two: (0.0 / 0.0),
            three: 3
        },
        noOfShifts: [1, 2, 3]
    };
    Employee e4 = {
        name: "Frank",
        address: {unitNo: 111, street: "Cross Street"},
        tokens: {one: 11, two: 4, three: 3},
        noOfShifts: [1, 2, 3]
    };
    Employee e5 = {
        name: "Frank",
        address: {unitNo: 111, street: "Cross Street"},
        tokens: {one: 11, two: 4, three: ()},
        noOfShifts: [1, 2, 3]
    };
    Employee e6 = {
        name: "Frank",
        address: {unitNo: 111, street: "Cross Street"},
        tokens: {
            one: 11,
            two: 4,
            three: (0.0 / 0.0)
        },
        noOfShifts: [1, 2, 3]
    };
    Employee e7 = {
        name: "Frank",
        address: {unitNo: 111, street: "Cross Street"},
        tokens: {one: 11, two: 4, three: 55},
        noOfShifts: [1, 2, 3]
    };

    Employee[] empList = [e1, e2, e3, e4, e5, e6, e7];

    Employee[] opEmpList = from var emp in empList
        order by emp.tokens["two"] descending, emp.tokens["three"] ascending
        select emp;

    testPassed = testPassed && opEmpList[0] == empList[3];
    testPassed = testPassed && opEmpList[1] == empList[6];
    testPassed = testPassed && opEmpList[2] == empList[5];
    testPassed = testPassed && opEmpList[3] == empList[4];
    testPassed = testPassed && opEmpList[4] == empList[0];
    testPassed = testPassed && opEmpList[5] == empList[2];
    testPassed = testPassed && opEmpList[6] == empList[1];

    return testPassed;
}

function testQueryExprWithOrderByClauseReturnString() returns string {
    PersonPos p1 = {firstName: "Amy", lastName: "Melina", age: 34};
    PersonPos p2 = {firstName: "Frank", lastName: "James", age: 30};
    PersonPos p3 = {firstName: "Melina", lastName: "Kodel", age: 72};
    PersonPos p4 = {firstName: "Terrence", lastName: "Lewis", age: 19};
    PersonPos p5 = {firstName: "Meghan", lastName: "Markle", age: 55};

    PersonPos[] personList = [p1, p2, p3, p4, p5];

    string outputNameString = from var person in personList
        order by person.age descending
        limit 3
        select person.firstName + " " + person.lastName + ",";

    return outputNameString;
}

function testQueryExprWithOrderByClauseReturnXML() returns xml {
    xml bookStore = xml `<bookStore>
                     <book>
                           <name>Sherlock Holmes</name>
                           <author>Sir Arthur Conan Doyle</author>
                     </book>
                     <book>
                           <name>The Da Vinci Code</name>
                           <author>Dan Brown</author>
                     </book>
                     <book>
                           <name>The Enchanted Wood</name>
                           <author>Enid Blyton</author>
                     </book>
                  </bookStore>`;

    xml authors = from var book in bookStore/<book>/<author>
        order by book.toString()
        limit 2
        select book;

    return authors;
}

function testQueryExprWithOrderByClauseAndInnerQueries() returns CustomerProfile[] {
    CustomerPos c1 = {id: 1, name: "Melina", noOfItems: 62};
    CustomerPos c2 = {id: 5, name: "James", noOfItems: 5};
    CustomerPos c3 = {id: 9, name: "James", noOfItems: 25};
    CustomerPos c4 = {id: 0, name: "James", noOfItems: 25};
    CustomerPos c5 = {id: 2, name: "James", noOfItems: 30};

    PersonPos p1 = {firstName: "Jennifer", lastName: "Melina", age: 23};
    PersonPos p2 = {firstName: "Frank", lastName: "James", age: 30};
    PersonPos p3 = {firstName: "Zeth", lastName: "James", age: 50};

    CustomerPos[] customerList = [c1, c2, c3, c4, c5];
    PersonPos[] personList = [p1, p2, p3];

    CustomerProfile[] customerProfileList = from var customer in (stream from var c in customerList
            order by c.id descending
            limit 4
            select c)
        join var person in (from var p in personList
            order by p.firstName descending
            limit 2
            select p)
        on customer.name equals person.lastName
        order by incrementCount(0), customer.noOfItems descending
        limit 3
        select {
            name: person.firstName,
            age: person.age,
            noOfItems: customer.noOfItems
        };

    return customerProfileList;
}

function testQueryExprWithOrderByClauseAndInnerQueries2() returns CustomerProfile[] {
    CustomerPos c1 = {id: 1, name: "Melina", noOfItems: 62};
    CustomerPos c2 = {id: 5, name: "James", noOfItems: 5};
    CustomerPos c3 = {id: 9, name: "James", noOfItems: 25};
    CustomerPos c4 = {id: 0, name: "James", noOfItems: 25};
    CustomerPos c5 = {id: 2, name: "James", noOfItems: 30};
    CustomerPos c6 = {id: 3, name: "Melina", noOfItems: 20};

    PersonPos p1 = {firstName: "Jennifer", lastName: "Melina", age: 23};
    PersonPos p2 = {firstName: "Frank", lastName: "James", age: 30};
    PersonPos p3 = {firstName: "Zeth", lastName: "James", age: 50};

    PaymentInfo i1 = {custId: 1, modeOfPayment: "cash"};
    PaymentInfo i2 = {custId: 9, modeOfPayment: "debit card"};
    PaymentInfo i3 = {custId: 1, modeOfPayment: "creadit card"};
    PaymentInfo i4 = {custId: 5, modeOfPayment: "cash"};
    PaymentInfo i5 = {custId: 9, modeOfPayment: "cash"};
    PaymentInfo i6 = {custId: 0, modeOfPayment: "cash"};
    PaymentInfo i7 = {custId: 2, modeOfPayment: "cash"};
    PaymentInfo i8 = {custId: 3, modeOfPayment: "cash"};

    CustomerPos[] customerList = [c1, c2, c3, c4, c5, c6];
    PersonPos[] personList = [p1, p2, p3];
    PaymentInfo[] paymentList = [i1, i2, i3, i4, i5, i6, i7, i8];

    CustomerProfile[] customerProfileList = from var customer in (stream from var c in customerList
                                                                    order by c.id descending limit 4 select c)
         join var person in (from var p in personList order by p.firstName descending limit 2 select p)
         on customer.name equals person.lastName
         join var payment in (table key() from var paym in paymentList where paym.modeOfPayment == "cash"
                                   order by paym.custId ascending limit 5 select paym)
         on customer.id equals payment.custId
         order by customer.noOfItems descending
         limit 2
         select {
             name: person.firstName,
             age : person.age,
             noOfItems: customer.noOfItems
         };

    return customerProfileList;
}

function incrementCount(int i) returns int {
    int count = i + 2;
    return count;
}

enum Color {
    RED,
    GREEN,
    BLUE
}

function testQueryExprWithOrderByClauseWithArrayKey() {
    record {|int[] t; string s;|}[] data1 = [
        {s: "s5", t: [5, 2]},
        {s: "s1", t: [1, 1, 3, 4]},
        {s: "s0", t: [0]},
        {s: "s4", t: [4]}
    ];

    string[] q1 = from var rec in data1
        order by rec.t descending
        select rec.s;
    assertEquality(["s5", "s4", "s1", "s0"], q1);

    record {|int k; [decimal, int] arr;|}[] data2 = [
        {k: 1, arr: [3.2, 1]},
        {k: 2, arr: [212.21, 3]},
        {k: 3, arr: [0.2, 4]},
        {k: 4, arr: [-1.2, 10]}
    ];

    int[] q2 = from var rec in data2
        order by rec.arr descending
        select rec.k;
    assertEquality([2, 1, 3, 4], q2);

    record {|int k; [boolean, string, int] arr;|}[] data3 = [
        {k: 1, arr: [true, "abcd", 1]},
        {k: 2, arr: [false, "efgh", 9]},
        {k: 3, arr: [false, "abcd", 10]},
        {k: 4, arr: [true, "ijkl", -5]}
    ];

    int[] q3 = from var rec in data3
        order by rec.arr
        select rec.k;
    assertEquality([3, 2, 1, 4], q3);

    record {|int k; [float...] arr;|}[] data4 = [
        {k: 1, arr: [1.2, 3.1, 5.6, 9.2]},
        {k: 2, arr: []},
        {k: 3, arr: [-0.7, 0.8, 1.45]},
        {k: 4, arr: [1, 2]}
    ];

    int[] q4 = from var rec in data4
        order by rec.arr
        select rec.k;
    assertEquality([2, 3, 4, 1], q4);

    record {|int k; [float...] arr1; [string, string...] arr2;|}[] data5 = [
        {k: 1, arr1: [1.2, 3.1, 5.6, 9.2], arr2: ["s1", "s2"]},
        {k: 2, arr1: [], arr2: ["s10", "s9"]},
        {k: 3, arr1: [], arr2: ["s1", "s2"]},
        {k: 4, arr1: [], arr2: ["s1", "s2"]},
        {k: 5, arr1: [1, 2], arr2: ["s24", "s2"]}
    ];

    int[] q5 = from var rec in data5
        order by rec.arr1 descending, rec.arr2 ascending
        select rec.k;
    assertEquality([1, 5, 3, 4, 2], q5);

    record {|int k; float?[]? arr1;|}[] data6 = [
        {k: 1, arr1: [1.2, 3.1, 5.6, 9.2]},
        {k: 2, arr1: [1.2, 3.1, 5.6, ()]},
        {k: 3, arr1: [(), (), ()]},
        {k: 4, arr1: ()},
        {k: 5, arr1: [1, 2]}
    ];

    int[] q6 = from var rec in data6
        order by rec.arr1 descending
        select rec.k;
    assertEquality([1, 2, 5, 3, 4], q6);

    record {|int k; [int?, string?, decimal?...] arr1;|}[] data7 = [
        {k: 1, arr1: [1, "a", 9.2]},
        {k: 2, arr1: [1, (), ()]},
        {k: 3, arr1: [(), (), 4.2]},
        {k: 4, arr1: [2, "b"]},
        {k: 5, arr1: [2, (), 2.3]}
    ];

    int[] q7 = from var rec in data7
        order by rec.arr1 descending
        select rec.k;
    assertEquality([4, 5, 1, 2, 3], q7);

    record {|int k; Color[] arr1;|}[] data8 = [
        {k: 1, arr1: [RED]},
        {k: 2, arr1: [RED, GREEN]},
        {k: 3, arr1: [GREEN, BLUE, RED]},
        {k: 4, arr1: [BLUE, RED]},
        {k: 5, arr1: [BLUE, BLUE]}
    ];

    int[] q8 = from var rec in data8
        order by rec.arr1 descending
        select rec.k;
    assertEquality([2, 1, 3, 4, 5], q8);
}

function assertEquality(anydata expected, anydata actual) {
    if expected != actual {
        panic error(string `Expected ${expected.toBalString()}, found ${actual.toBalString()}`);
    }
}

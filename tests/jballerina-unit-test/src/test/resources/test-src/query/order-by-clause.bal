type Student record {|
   int id;
   string? fname;
   float fee;
   decimal impact;
   boolean isUndergrad;
|};

type Person record {|
    string firstName;
    string lastName;
    int age;
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
|};

type CustomerTable table<Customer> key(id, name);

type CustomerValue record {|
  Customer value;
|};

type PersonValue record {|
    Person value;
|};

function getCustomer(record {| Customer value; |}? returnedVal) returns Customer? {
    if (returnedVal is CustomerValue) {
       return returnedVal.value;
    } else {
       return ();
    }
}

function getPersonValue((record {| Person value; |}|error?)|(record {| Person value; |}?) returnedVal)
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
       order by fname descending, impact
       select student;

    testPassed = testPassed &&  opStudentList[0] ==  studentList[3];
    testPassed = testPassed &&  opStudentList[1] ==  studentList[0];
    testPassed = testPassed &&  opStudentList[2] ==  studentList[1];
    testPassed = testPassed &&  opStudentList[3] ==  studentList[2];

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
       order by isUndergrad, fee
       select student;

    testPassed = testPassed &&  opStudentList[0] ==  studentList[1];
    testPassed = testPassed &&  opStudentList[1] ==  studentList[2];
    testPassed = testPassed &&  opStudentList[2] ==  studentList[3];
    testPassed = testPassed &&  opStudentList[3] ==  studentList[0];

    return testPassed;
}

function testQueryExprWithOrderByClause3() returns Customer[] {
    Customer c1 = {id: 1, name: "Melina", noOfItems: 12};
    Customer c2 = {id: 5, name: "James", noOfItems: 5};
    Customer c3 = {id: 7, name: "James", noOfItems: 25};
    Customer c4 = {id: 0, name: "James", noOfItems: 25};

    Person p1 = {firstName: "Amy", lastName: "Melina", age: 23};
    Person p2 = {firstName: "Frank", lastName: "James", age: 30};

    Customer[] customerList = [c1, c2, c3, c4];
    Person[] personList = [p1, p2];

    Customer[] opCustomerList = from var customer in customerList
           from var person in personList
           let string customerName = "Johns"
           where person.lastName == "James"
           order by id descending
           select {
               id: customer.id,
               name: customerName,
               noOfItems: customer.noOfItems
           };

    return  opCustomerList;
}

function testQueryExprWithOrderByClauseReturnTable() returns boolean {
    boolean testPassed = true;

    Customer c1 = {id: 1, name: "Melina", noOfItems: 12};
    Customer c2 = {id: 2, name: "James", noOfItems: 5};
    Customer c3 = {id: 3, name: "James", noOfItems: 25};
    Customer c4 = {id: 4, name: "James", noOfItems: 25};

    Person p1 = {firstName: "Amy", lastName: "Melina", age: 23};
    Person p2 = {firstName: "Frank", lastName: "James", age: 30};

    Customer[] customerList = [c1, c2, c3, c4];
    Person[] personList = [p1, p2];

    CustomerTable|error customerTable = table key(id, name) from var customer in customerList
         from var person in personList
         where person.firstName == "Frank"
         order by noOfItems descending, id
         select {
             id: customer.id,
             name: customer.name,
             noOfItems: customer.noOfItems
         }
         limit 3;

    if (customerTable is CustomerTable) {
        var itr = customerTable.iterator();
        Customer? customer = getCustomer(itr.next());
        testPassed = testPassed && customer == customerList[2];
        customer = getCustomer(itr.next());
        testPassed = testPassed && customer == customerList[0];
        customer = getCustomer(itr.next());
        testPassed = testPassed && customer == customerList[1];
        customer = getCustomer(itr.next());
        testPassed = testPassed && customer == ();
    }

    return testPassed;
}

function testQueryExprWithOrderByClauseReturnStream() returns boolean {
    boolean testPassed = true;

    Person p1 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p2 = {firstName: "John", lastName: "David", age: 33};
    Person p3 = {firstName: "John", lastName: "Fonseka", age: 28};
    Person p4 = {firstName: "John", lastName: "Fonseka", age: 30};
    Person p5 = {firstName: "John", lastName: "Fonseka", age: 20};

    Customer c1 = {id: 1, name: "John", noOfItems: 25};
    Customer c2 = {id: 2, name: "Frank", noOfItems: 20};

    Person[] personList = [p1, p2, p3, p4, p5];
    Customer[] customerList = [c1, c2];

    stream<Person> outputPersonStream = stream from var person in personList.toStream()
        from var customer in customerList
        let string newLastName = "Turin"
        let string newFirstName = "Johnas"
        where customer.name == "John"
        where person.lastName == "Fonseka"
        order by age descending
        select {
            firstName: newFirstName,
            lastName: newLastName,
            age: person.age
        };

    record {| Person value; |}? person = getPersonValue(outputPersonStream.next());
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
    Customer c1 = {id: 1, name: "Melina", noOfItems: 12};
    Customer c2 = {id: 2, name: "James", noOfItems: 5};
    Customer c3 = {id: 3, name: "James", noOfItems: 25};
    Customer c4 = {id: 4, name: "James", noOfItems: 25};

    Person p1 = {firstName: "Amy", lastName: "Melina", age: 23};
    Person p2 = {firstName: "Frank", lastName: "James", age: 30};

    Customer[] customerList = [c1, c2, c3, c4];
    Person[] personList = [p1, p2];

    CustomerProfile[] customerProfileList = from var customer in customerList
         join var person in personList
         on customer.name equals person.lastName
         order by noOfItems
         select {
             name: person.firstName,
             age : person.age,
             noOfItems: customer.noOfItems
         };

    return customerProfileList;
}

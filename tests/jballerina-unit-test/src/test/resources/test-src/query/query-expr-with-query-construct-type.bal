type Person record {|
    string firstName;
    string lastName;
    int age;
|};

type Employee record {|
    string firstName;
    string lastName;
    string dept;
|};

type Department record {
    string dept;
};

type EmpProfile record {|
    string firstName;
    string lastName;
    int age;
    string dept;
    string status;
|};

type PersonValue record {|
    Person value;
|};

type EmployeeValue record {|
    Employee value;
|};

type EmpProfileValue record {|
    EmpProfile value;
|};

type Customer record {|
    readonly int id;
    readonly string name;
    int noOfItems;
|};

type CustomerTable table<Customer> key(id, name);

type CustomerKeyLessTable table<Customer>;

type CustomerValue record {|
  Customer value;
|};

function getPersonValue((record {| Person value; |}|error?)|(record {| Person value; |}?) returnedVal)
returns PersonValue? {
    var result = returnedVal;
    if (result is PersonValue) {
        return result;
    } else {
        return ();
    }
}

function getEmployeeValue((record {| Employee value; |}|error?)|(record {| Employee value; |}?) returnedVal)
returns EmployeeValue? {
    var result = returnedVal;
    if (result is EmployeeValue) {
        return result;
    } else {
        return ();
    }
}

function getEmpProfileValue((record {| EmpProfile value; |}|error?)|(record {| EmpProfile value; |}?) returnedVal)
returns EmpProfileValue? {
    var result = returnedVal;
    if (result is EmpProfileValue) {
        return result;
    } else {
        return ();
    }
}

function getCustomer(record {| Customer value; |}? returnedVal) returns Customer? {
    if (returnedVal is CustomerValue) {
       return returnedVal.value;
    } else {
       return ();
    }
}

# functions to test query-construct-type --> stream

function testSimpleQueryReturnStream() returns boolean {
    boolean testPassed = true;

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    stream<Person> outputPersonStream = stream from var person in personList
        where person.firstName == "John"
        let int newAge = 34
        select {
            firstName: person.firstName,
            lastName: person.lastName,
            age: newAge
        };

    record {| Person value; |}? person = getPersonValue(outputPersonStream.next());
    testPassed = testPassed && person?.value?.firstName == "John" && person?.value?.lastName == "David" &&
    person?.value?.age == 34;

    person = getPersonValue(outputPersonStream.next());
    testPassed = testPassed && person == ();

    return testPassed;
}

function testStreamInFromClauseWithReturnStream() returns boolean {
    boolean testPassed = true;

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    stream<Employee> outputEmployeeStream = stream from var {firstName, lastName, dept} in
                   <stream<Employee>>personList.toStream().filter(function (Person person) returns boolean {
                       return person.firstName == "John";
                       }).'map(function (Person person) returns Employee {
                           Employee employee = {
                               firstName: person.firstName,
                               lastName: person.lastName,
                               dept: "Engineering"
                           };
                           return employee;
                           })
                   select {
                       firstName: firstName,
                       lastName: lastName,
                       dept: dept
                   };

    record {| Employee value; |}? employee = getEmployeeValue(outputEmployeeStream.next());
    testPassed = testPassed && employee?.value?.firstName == "John" && employee?.value?.lastName == "David" &&
    employee?.value?.dept == "Engineering";

    employee = getEmployeeValue(outputEmployeeStream.next());
    testPassed = testPassed && employee == ();

    return testPassed;
}

function testMultipleFromWhereAndLetReturnStream() returns boolean {
    boolean testPassed = true;

    Employee e1 = {firstName: "John", lastName: "Fonseka", dept: "Engineering"};
    Employee e2 = {firstName: "John", lastName: "David", dept: "HR"};

    Department d1 = {dept: "Support"};
    Department d2 = {dept: "Dev"};

    Employee[] employeeList = [e1, e2];
    Department[] departmentList = [d1, d2];

    stream<Employee> outputEmployeeStream = stream from var emp in employeeList
         from var department in departmentList
         where emp.firstName == "John"
         where emp.dept == "Engineering"
         let string fname = "Johns"
         let string deptName = "Research"
         select {
             firstName: fname,
             lastName: emp.lastName,
             dept: deptName
         };

    record {| Employee value; |}? employee = getEmployeeValue(outputEmployeeStream.next());
    testPassed = testPassed && employee?.value?.firstName == "Johns" && employee?.value?.lastName == "Fonseka" &&
        employee?.value?.dept == "Research";

    employee = getEmployeeValue(outputEmployeeStream.next());
    testPassed = testPassed && employee?.value?.firstName == "Johns" && employee?.value?.lastName == "Fonseka" &&
        employee?.value?.dept == "Research";

    employee = getEmployeeValue(outputEmployeeStream.next());
    testPassed = testPassed && employee == ();

    return testPassed;
}

function testInnerJoinAndLimitReturnStream() returns boolean {
    boolean testPassed = true;

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};

    Employee e1 = {firstName: "Alex", lastName: "George", dept: "Engineering"};
    Employee e2 = {firstName: "John", lastName: "David", dept: "HR"};
    Employee e3 = {firstName: "Ranjan", lastName: "Fonseka", dept: "Operations"};

    Person[] personList = [p1, p2];
    Employee[] employeeList = [e1, e2, e3];

    stream<EmpProfile> outputEmpProfileStream = stream from var person in personList.toStream()
            join Employee employee in employeeList.toStream()
            on person.firstName equals employee.firstName
            limit 1
            select {
                firstName: employee.firstName,
                lastName: employee.lastName,
                age: person.age,
                dept: employee.dept,
                status: "Permanent"
            };

    record {| EmpProfile value; |}? empProfile = getEmpProfileValue(outputEmpProfileStream.next());
    testPassed = testPassed && empProfile?.value?.firstName == "Alex" && empProfile?.value?.lastName == "George" &&
        empProfile?.value?.age == 23 && empProfile?.value?.dept == "Engineering" &&
        empProfile?.value?.status == "Permanent";

    empProfile = getEmpProfileValue(outputEmpProfileStream.next());
    testPassed = testPassed && empProfile == ();

    return testPassed;
}

# functions to test query-construct-type --> table

function testSimpleQueryExprReturnTable() returns boolean {
    boolean testPassed = true;

    Customer c1 = {id: 1, name: "Melina", noOfItems: 12};
    Customer c2 = {id: 2, name: "James", noOfItems: 5};
    Customer c3 = {id: 3, name: "Anne", noOfItems: 20};

    Customer[] customerList = [c1, c2, c3];

    CustomerTable|error customerTable = table key(id, name) from var customer in customerList
        select {
            id: customer.id,
            name: customer.name,
            noOfItems: customer.noOfItems
        };

    if (customerTable is CustomerTable) {
        var itr = customerTable.iterator();
        Customer? customer = getCustomer(itr.next());
        testPassed = testPassed && customer == customerList[0];
        customer = getCustomer(itr.next());
        testPassed = testPassed && customer == customerList[1];
        customer = getCustomer(itr.next());
        testPassed = testPassed && customer == customerList[2];
        customer = getCustomer(itr.next());
        testPassed = testPassed && customer == ();
    }

    return testPassed;
}

function testTableWithDuplicateKeys() returns CustomerTable|error {
    boolean testPassed = false;

    Customer c1 = {id: 1, name: "Melina", noOfItems: 12};
    Customer c2 = {id: 2, name: "James", noOfItems: 5};

    Customer[] customerList = [c1, c2, c1];

    CustomerTable|error customerTable = table key(id, name) from var customer in customerList
        select {
            id: customer.id,
            name: customer.name,
            noOfItems: customer.noOfItems
        };

    return customerTable;
}

function testTableNoDuplicatesAndOnConflictReturnTable() returns boolean {
    boolean testPassed = true;
    error onConflictError = error("Key Conflict", message = "cannot insert.");

    Customer c1 = {id: 1, name: "Melina", noOfItems: 12};
    Customer c2 = {id: 2, name: "James", noOfItems: 5};
    Customer c3 = {id: 3, name: "Anne", noOfItems: 20};

    Customer[] customerList = [c1, c2, c3];

    CustomerTable|error customerTable = table key(id, name) from var customer in customerList
        select {
            id: customer.id,
            name: customer.name,
            noOfItems: customer.noOfItems
        }
        on conflict onConflictError;

    if (customerTable is CustomerTable) {
        var itr = customerTable.iterator();
        Customer? customer = getCustomer(itr.next());
        testPassed = testPassed && customer == customerList[0];
        customer = getCustomer(itr.next());
        testPassed = testPassed && customer == customerList[1];
        customer = getCustomer(itr.next());
        testPassed = testPassed && customer == customerList[2];
        customer = getCustomer(itr.next());
        testPassed = testPassed && customer == ();
    }

    return testPassed;
}

function testTableWithDuplicatesAndOnConflictReturnTable() returns CustomerTable|error {
     error onConflictError = error("Key Conflict", message = "cannot insert.");

     Customer c1 = {id: 1, name: "Melina", noOfItems: 12};
     Customer c2 = {id: 2, name: "James", noOfItems: 5};

     Customer[] customerList = [c1, c2, c1];

     CustomerTable|error customerTable = table key(id, name) from var customer in customerList
         select {
             id: customer.id,
             name: customer.name,
             noOfItems: customer.noOfItems
         }
         on conflict onConflictError;

     return customerTable;
}

function testQueryExprWithOtherClausesReturnTable() returns CustomerTable|error {
     error onConflictError = error("Key Conflict", message = "cannot insert.");

     Customer c1 = {id: 1, name: "Melina", noOfItems: 12};
     Customer c2 = {id: 2, name: "James", noOfItems: 5};

    Person p1 = {firstName: "Amy", lastName: "Melina", age: 23};
    Person p2 = {firstName: "Frank", lastName: "James", age: 30};

     Customer[] customerList = [c1, c2, c1];
     Person[] personList = [p1, p2];

     CustomerTable|error customerTable = table key(id, name) from var customer in customerList
         from var person in personList
         let int items = 25
         let string customerName = "Bini"
         where customer.id == 1
         where person.firstName == "Amy"
         select {
             id: customer.id,
             name: customerName,
             noOfItems: items
         }
         on conflict onConflictError;

     return customerTable;
}

function testQueryExprWithJoinClauseReturnTable() returns CustomerTable|error {
     error onConflictError = error("Key Conflict", message = "cannot insert.");

     Customer c1 = {id: 1, name: "Melina", noOfItems: 12};
     Customer c2 = {id: 2, name: "James", noOfItems: 5};

     Person p1 = {firstName: "Amy", lastName: "Melina", age: 23};
     Person p2 = {firstName: "Frank", lastName: "James", age: 30};

     Customer[] customerList = [c1, c2, c1];
     Person[] personList = [p1, p2];

     CustomerTable|error customerTable = table key(id, name) from var customer in customerList
         join var person in personList
         on customer.name equals person.lastName
         select {
             id: customer.id,
             name: person.firstName,
             noOfItems: customer.noOfItems
         }
         on conflict onConflictError;

     return customerTable;
}

function testQueryExprWithLimitClauseReturnTable() returns boolean {
     boolean testPassed = true;
     error onConflictError = error("Key Conflict", message = "cannot insert.");

     Customer c1 = {id: 1, name: "Melina", noOfItems: 12};
     Customer c2 = {id: 2, name: "James", noOfItems: 5};
     Customer c3 = {id: 3, name: "Melina", noOfItems: 25};

     Customer[] customerList = [c1, c2, c3];

     CustomerTable|error customerTable = table key(id, name) from var customer in customerList.toStream()
         where customer.name == "Melina"
         limit 1
         select {
             id: customer.id,
             name: customer.name,
             noOfItems: customer.noOfItems
         }
         on conflict onConflictError;

    if (customerTable is CustomerTable) {
        var itr = customerTable.iterator();
        Customer? customer = getCustomer(itr.next());
        testPassed = testPassed && customer == customerList[0];
        customer = getCustomer(itr.next());
        testPassed = testPassed && customer == ();
    }

    return testPassed;
}

function testKeyLessTableWithReturnTable() returns boolean {
    boolean testPassed = true;

    Customer c1 = {id: 1, name: "Melina", noOfItems: 12};
    Customer c2 = {id: 2, name: "James", noOfItems: 5};
    Customer c3 = {id: 3, name: "Anne", noOfItems: 20};

    Customer[] customerList = [c1, c2, c3];

    CustomerKeyLessTable|error customerTable = table key(id, name) from var customer in customerList
        select {
            id: customer.id,
            name: customer.name,
            noOfItems: customer.noOfItems
        };

    if (customerTable is CustomerKeyLessTable) {
        var itr = customerTable.iterator();
        Customer? customer = getCustomer(itr.next());
        testPassed = testPassed && customer == customerList[0];
        customer = getCustomer(itr.next());
        testPassed = testPassed && customer == customerList[1];
        customer = getCustomer(itr.next());
        testPassed = testPassed && customer == customerList[2];
        customer = getCustomer(itr.next());
        testPassed = testPassed && customer == ();
    }

    return testPassed;
}

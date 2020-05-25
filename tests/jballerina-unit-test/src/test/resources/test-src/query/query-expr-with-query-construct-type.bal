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

function getPersonValue((record {| Person value; |}|error?)|(record {| Person value; |}?) returnedVal) returns PersonValue? {
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

    stream<Employee> outputEmployeeStream =
         stream from var emp in employeeList
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

function testInnerJoinReturnStream() returns boolean {
    boolean testPassed = true;

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};

    Employee e1 = {firstName: "Alex", lastName: "George", dept: "Engineering"};
    Employee e2 = {firstName: "John", lastName: "David", dept: "HR"};
    Employee e3 = {firstName: "Ranjan", lastName: "Fonseka", dept: "Operations"};

    Person[] personList = [p1, p2];
    Employee[] employeeList = [e1, e2, e3];

    stream<EmpProfile> outputEmpProfileStream =
            stream from var person in personList.toStream()
            join Employee employee in employeeList.toStream()
            on person.firstName equals employee.firstName
            select {
                firstName: employee.firstName,
                lastName: employee.lastName,
                age: person.age,
                dept: employee.dept,
                status: "Permanent"
            }
            limit 1;

    record {| EmpProfile value; |}? empProfile = getEmpProfileValue(outputEmpProfileStream.next());
    testPassed = testPassed && empProfile?.value?.firstName == "Alex" && empProfile?.value?.lastName == "George" &&
        empProfile?.value?.age == 23 && empProfile?.value?.dept == "Engineering" &&
        empProfile?.value?.status == "Permanent";

    empProfile = getEmpProfileValue(outputEmpProfileStream.next());
    testPassed = testPassed && empProfile == ();

    return testPassed;
}

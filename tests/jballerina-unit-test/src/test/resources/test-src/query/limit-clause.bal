type Person record {|
    string firstName;
    string lastName;
    int age;
|};

type FullName record{|
	string firstName;
	string lastName;
|};

type Employee record {|
    string firstName;
    string lastName;
    string dept;
|};

type EmpProfile record {|
    string firstName;
    string lastName;
    int age;
    string dept;
    string status;
|};

type EmpProfileValue record {|
    EmpProfile value;
|};

function getEmpProfileValue((record {| EmpProfile value; |}|error?)|(record {| EmpProfile value; |}?) returnedVal)
returns EmpProfileValue? {
    var result = returnedVal;
    if (result is EmpProfileValue) {
        return result;
    } else {
        return ();
    }
}

function testLimitClauseWithQueryExpr() returns Person[] {

    Person p1 = {firstName: "Alex", lastName: "George", age: 33};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 33};
    Person p3 = {firstName: "John", lastName: "David", age: 35};
    Person p4 = {firstName: "Max", lastName: "Gomaz", age: 33};

    Person[] personList = [p1, p2, p3, p4];

    Person[] outputPersonList =
            from var person in personList
            let int newAge = 34
            where person.age == 33
            select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   age: newAge
            }
            limit 2;

    return outputPersonList;
}

function testLimitClauseWithQueryExpr2() returns Person[] {

    Person p1 = {firstName: "Alex", lastName: "George", age: 33};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 31};
    Person p3 = {firstName: "John", lastName: "David", age: 35};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from var person in personList
            select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   age: person.age
            }
            limit 4;

    return outputPersonList;
}

function testLimitClauseWithQueryExpr3() returns Person[] {

    Person p1 = {firstName: "Alex", lastName: "George", age: 33};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 31};
    Person p3 = {firstName: "John", lastName: "David", age: 35};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from var person in personList
            select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   age: person.age
            }
            limit 3;

    return outputPersonList;
}

function testLimitClauseReturnStream() returns boolean {
    boolean testPassed = true;

    Person p1 = {firstName: "Alex", lastName: "George", age: 33};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 31};

    Employee e1 = {firstName: "Alex", lastName: "George", dept: "Engineering"};
    Employee e2 = {firstName: "John", lastName: "David", dept: "HR"};
    Employee e3 = {firstName: "Alex", lastName: "Fonseka", dept: "Operations"};

    Person[] personList = [p1, p2];
    Employee[] employeeList = [e1, e2, e3];

    stream<EmpProfile> outputEmpProfileStream =
            stream from var person in personList.toStream()
            from var emp in employeeList
            let string empStatus = "Permanent"
            where emp.firstName == "Alex"
            select {
                firstName: emp.firstName,
                lastName: emp.lastName,
                age: person.age,
                dept: emp.dept,
                status: empStatus
            }
            limit 1;

    record {| EmpProfile value; |}? empProfile = getEmpProfileValue(outputEmpProfileStream.next());
    testPassed = testPassed && empProfile?.value?.firstName == "Alex" && empProfile?.value?.lastName == "George" &&
        empProfile?.value?.age == 33 && empProfile?.value?.dept == "Engineering" &&
        empProfile?.value?.status == "Permanent";

    empProfile = getEmpProfileValue(outputEmpProfileStream.next());
    testPassed = testPassed && empProfile == ();

    return testPassed;
}

function testLimitClauseWithQueryAction() returns int {

    int[] intList = [1, 2, 3, 4, 5];
    int count = 0;

    var x =  from var value in intList
            do {
                count += value;
            }
            limit 3;

    return count;
}

function testLimitClauseWithQueryAction2() returns FullName[] {

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 22};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];
    FullName[] nameList = [];

    var x =  from var person in personList
            let int twiceAge  = (person.age * 2)
            do {
                if(twiceAge < 50) {
                    FullName fullName = {firstName: person.firstName, lastName: person.lastName};
                    nameList[nameList.length()] = fullName;
                }
            }
            limit 1;

    return nameList;
}

function testLimitClauseWithQueryAction3() returns FullName[] {

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 22};

    Person[] personList = [p1, p2];
    FullName[] nameList = [];

    var x =  from var person in personList
            do {
                FullName fullName = {firstName: person.firstName, lastName: person.lastName};
                nameList[nameList.length()] = fullName;
            }
            limit 3;

    return nameList;
}

function testLimitClauseWithQueryAction4() returns FullName[] {

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 22};

    Person[] personList = [p1, p2];
    FullName[] nameList = [];

    var x =  from var person in personList
            do {
                FullName fullName = {firstName: person.firstName, lastName: person.lastName};
                nameList[nameList.length()] = fullName;
            }
            limit 2;

    return nameList;
}

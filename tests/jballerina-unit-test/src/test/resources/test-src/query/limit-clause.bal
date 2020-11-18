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
            limit 2
            select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   age: newAge
            };

    return outputPersonList;
}

function testLimitClauseWithQueryExpr2() returns Person[] {

    Person p1 = {firstName: "Alex", lastName: "George", age: 33};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 31};
    Person p3 = {firstName: "John", lastName: "David", age: 35};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from var person in personList
            limit 4
            select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   age: person.age
            };

    return outputPersonList;
}

function testLimitClauseWithQueryExpr3() returns Person[] {

    Person p1 = {firstName: "Alex", lastName: "George", age: 33};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 31};
    Person p3 = {firstName: "John", lastName: "David", age: 35};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from var person in personList
            limit 3
            select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   age: person.age
            };

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
            limit 1
            select {
                firstName: emp.firstName,
                lastName: emp.lastName,
                age: person.age,
                dept: emp.dept,
                status: empStatus
            };

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
            limit 3
            do {
                count += value;
            };

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
            limit 1
            do {
                if(twiceAge < 50) {
                    FullName fullName = {firstName: person.firstName, lastName: person.lastName};
                    nameList[nameList.length()] = fullName;
                }
            };

    return nameList;
}

function testLimitClauseWithQueryAction3() returns FullName[] {

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 22};

    Person[] personList = [p1, p2];
    FullName[] nameList = [];

    var x =  from var person in personList
            limit 3
            do {
                FullName fullName = {firstName: person.firstName, lastName: person.lastName};
                nameList[nameList.length()] = fullName;
            };

    return nameList;
}

function testLimitClauseWithQueryAction4() returns FullName[] {

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 22};

    Person[] personList = [p1, p2];
    FullName[] nameList = [];

    var x =  from var person in personList
            limit 2
            do {
                FullName fullName = {firstName: person.firstName, lastName: person.lastName};
                nameList[nameList.length()] = fullName;
            };

    return nameList;
}

function testLetExpressionWithLimitClause() returns boolean {

    Person p1 = {firstName: "Alex", lastName: "George", age: 33};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 35};
    Person p3 = {firstName: "John", lastName: "David", age: 33};
    Person p4 = {firstName: "Max", lastName: "Gomaz", age: 33};

    Person[] personList = [p1, p2, p3, p4];

    Person[] outputPersonList =
            from var person in personList
            let int newAge = 34
            let int limitValue = 2
            where person.age == 33
            limit limitValue
            select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   age: newAge
            };
            
    boolean testPassed = true;
    Person p;
    any res = outputPersonList;
    testPassed = testPassed && res is Person[];
    testPassed = testPassed && res is (any|error)[];
    testPassed = testPassed && outputPersonList.length() == 2;
    p = outputPersonList[0];
    testPassed = testPassed && p.firstName == "Alex" && p.lastName == "George" && p.age == 34;
    p = outputPersonList[1];
    testPassed = testPassed && p.firstName == "John" && p.lastName == "David" && p.age == 34;
    return testPassed;
}

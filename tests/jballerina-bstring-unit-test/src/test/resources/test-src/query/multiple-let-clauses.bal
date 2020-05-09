type Person record {|
   string firstName;
   string lastName;
   string deptAccess;
|};

type Company record {|
   string name;
|};

type Teacher record {|
   string firstName;
   string lastName;
   int age;
   string teacherId;
|};

function testMultipleLetClausesWithSimpleVariable1() returns Person[] {
    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ"};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ"};
    Person p3 = {firstName: "John", lastName: "David", deptAccess: "XYZ"};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from var person in personList
            let string depName = "WSO2", string replaceName = "Alexander"
            where person.deptAccess == "XYZ" && person.firstName == "Alex"
            select {
                   firstName: replaceName,
                   lastName: person.lastName,
                   deptAccess: depName
            };
    return  outputPersonList;
}

function testMultipleLetClausesWithSimpleVariable2() returns Person[] {
    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ"};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ"};
    Person p3 = {firstName: "John", lastName: "David", deptAccess: "XYZ"};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from var person in personList
            let string depName = "WSO2"
            let string replaceName = "Alexander"
            where person.deptAccess == "XYZ" && person.firstName == "Alex"
            select {
                   firstName: replaceName,
                   lastName: person.lastName,
                   deptAccess: depName
            };
    return  outputPersonList;
}

function testMultipleLetClausesWithRecordVariable() returns Person[] {
    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ"};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ"};
    Person p3 = {firstName: "John", lastName: "David", deptAccess: "XYZ"};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from var { firstName: nm1, lastName: nm2, deptAccess: d } in personList
            let Company companyRecord = { name: "WSO2" }
            select {
                   firstName: nm1,
                   lastName: nm2,
                   deptAccess: companyRecord.name
            };
    return  outputPersonList;
}

function testMultipleVarDeclReuseLetClause() returns Teacher[] {

    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ"};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ"};

    Person[] personList = [p1, p2];

    var outputPersonList =
            from var person in personList
            let int x = 20, int y = x + 10
            select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   age: y,
                   teacherId: "TER1200"
            };
    return  outputPersonList;
}

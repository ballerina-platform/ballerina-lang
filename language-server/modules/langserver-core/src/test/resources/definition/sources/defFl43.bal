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

function testMultipleLetClausesWithSimpleVariable2() returns Person[] {
    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ"};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ"};
    Person p3 = {firstName: "John", lastName: "David", deptAccess: "XYZ"};

    Person[] personList = [p1, p2, p3];
    int limitVal = 2;
    Person[] outputPersonList =
            from Person person in personList
            let string depName = "WSO2"
            let string replaceName = "Alexander"
            let Person testPerson = {firstName: "George", lastName: "Bravo", deptAccess: "XYZ"}
            where person.deptAccess == "XYZ" && person.firstName == "Alex"
            select {
                   firstName: replaceName,
                   lastName: person.lastName,
                   deptAccess: depName
            }
            limit limitVal;
    return  outputPersonList;
}

function testSimpleSelectQueryWithMultipleFromClauses() {
    Person p1 = {firstName:"Alex", lastName: "George", deptAccess: "XYZ"};
    Person p2 = {firstName:"Ranjan", lastName: "Fonseka", deptAccess: "XYZ"};
    Person p3 = {firstName:"John", lastName: "David", deptAccess: "XYZ"};

    Person[] personList = [p1, p2, p3];

    var x = from var person in personList
            where person.deptAccess.startsWith("XYZ")
            let string hrDepartment = "Human Resource"
            do {
                string personName = person.firstName;
                string hrDept = hrDepartment;
            };
}

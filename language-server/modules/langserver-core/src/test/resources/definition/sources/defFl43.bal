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
            };
    return  outputPersonList;
}

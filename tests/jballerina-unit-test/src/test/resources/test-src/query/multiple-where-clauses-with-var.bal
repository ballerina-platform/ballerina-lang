type Person record {|
   string firstName;
   string lastName;
   string deptAccess;
|};


type Department record {|
   string name;
|};

function testMultipleWhereClausesWithSimpleVariable() returns Person[]{

    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ"};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ"};
    Person p3 = {firstName: "John", lastName: "David", deptAccess: "XYZ"};

     Department d1 = {name:"HR"};
     Department d2 = {name:"Operations"};

    Person[] personList = [p1, p2, p3];
    Department[] deptList = [d1, d2];

    Person[] outputPersonList =
            from var person in personList
            from var dept in deptList
            where person.firstName == "Alex"
            where person.deptAccess == "XYZ"
            select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   deptAccess: dept.name
            };
    return  outputPersonList;
}


function testMultipleWhereClausesWithRecordVariable() returns Person[]{

    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ"};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ"};
    Person p3 = {firstName: "John", lastName: "David", deptAccess: "XYZ"};

    Department d1 = {name:"HR"};
    Department d2 = {name:"Operations"};

    Person[] personList = [p1, p2, p3];
    Department[] deptList = [d1, d2];

    Person[] outputPersonList =
            from var { firstName: nm1, lastName: nm2, deptAccess: d } in personList
            from var { name: deptName } in deptList
            where nm1 == "Alex"
            where deptName == "Operations"
            select {
                   firstName: nm1,
                   lastName: nm2,
                   deptAccess: deptName
            };
    return  outputPersonList;
}

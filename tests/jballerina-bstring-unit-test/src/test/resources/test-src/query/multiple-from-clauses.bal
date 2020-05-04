type Person record {|
   string firstName;
   string lastName;
   string deptAccess;
|};

type Department record {|
   string name;
|};

type Person1 record {|
   string firstName;
   string lastName;
   string deptAccess;
   Address address;
|};

type Address record{|
    string city;
    string country;
|};

type Section record {
   string name;
   Grades grades;
};

type Grades record{|
    int physics;
    int chemistry;

    int...;
|};

function testMultipleSelectClausesWithSimpleVariable() returns Person[]{

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
            select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   deptAccess: dept.name
            };

    return  outputPersonList;
}


function testMultipleSelectClausesWithRecordVariable() returns Person[]{

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
            select {
                   firstName: nm1,
                   lastName: nm2,
                   deptAccess: deptName
            };

    return  outputPersonList;
}

function testMultipleSelectClausesWithRecordVariableV2() returns Person[]{

    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ"};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ"};
    Person p3 = {firstName: "John", lastName: "David", deptAccess: "XYZ"};

    Department d1 = {name:"HR"};
    Department d2 = {name:"Operations"};

    Person[] personList = [p1, p2, p3];
    Department[] deptList = [d1, d2];

    Person[] outputPersonList =
            from var { firstName, lastName, deptAccess} in personList
            from var { name} in deptList
            select {
                   firstName: firstName,
                   lastName: lastName,
                   deptAccess: name
            };

    return  outputPersonList;
}

function testMultipleFromClausesWithStream() returns Person[]{
    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ"};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ"};
    Person p3 = {firstName: "John", lastName: "David", deptAccess: "XYZ"};

    Department d1 = {name:"HR"};
    Department d2 = {name:"Operations"};

    Person[] personList = [p1, p2, p3];
    Department[] deptList = [d1, d2];

    Person[] outputPersonList =
            from var { firstName, lastName, deptAccess} in personList.toStream()
            from var { name} in deptList
            select {
                   firstName: firstName,
                   lastName: lastName,
                   deptAccess: name
            };

    return  outputPersonList;
}

function testMultipleFromWithLetClauses() returns Person[]{
    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ"};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ"};
    Person p3 = {firstName: "John", lastName: "David", deptAccess: "XYZ"};

    Department d1 = {name:"HR"};
    Department d2 = {name:"Operations"};

    Person[] personList = [p1, p2, p3];
    Department[] deptList = [d1, d2];

    Person[] outputPersonList =
            from var { firstName, lastName, deptAccess} in personList.toStream()
            from var { name} in deptList
            let string companyName = "WSO2"
            select {
                   firstName: firstName,
                   lastName: lastName,
                   deptAccess: companyName
            };
    return  outputPersonList;
}

function testMultipleFromAndSelectClausesWithRecordVariable() returns Person1[]{

    Person1 p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ", address:{city:"NY", country:"America"}};
    Person1 p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ", address:{city:"NY", country:"America"}};

    Department d1 = {name:"HR"};
    Department d2 = {name:"Operations"};

    Address a1 = {city: "New York", country: "USA"};
    Address a2 = {city: "Springfield", country: "USA" };

    Person1[] personList = [p1, p2];
    Department[] deptList = [d1, d2];
    Address[] addressList = [a1, a2];

    Person1[] outputPersonList =
            from var { firstName: nm1, lastName: nm2, deptAccess: d, address: {city:c1, country:c2} } in personList
            from var { name } in deptList
            from var addr in addressList
            select {
                   firstName: nm1,
                   lastName: nm2,
                   deptAccess: name,
                   address: addr
            };

    return  outputPersonList;
}

function testQueryExprTupleTypedBinding1() returns string[]{

    [string,float][] arr1 = [["A",2.0],["B",3.0],["C",4.0]];
    [string|int|float,[string|float, int]][] arr2 = [["Ballerina",[3.4,234]],[34,["APIM",89]],[45.9,[78.2,90]]];

    string[] ouputStringList =
    	from [string,float] [a,b] in arr1
    	from [string|int|float,[string|float, int]] [g1,[g2,g3]] in arr2
    	where g1 == "Ballerina"
    	select a;

    return  ouputStringList;
}

function testQueryExprWithOpenRecords() returns Section[]{

    Section d1={name:"Maths",grades:{physics:80,chemistry:90}};
    Section d2={name:"Bio",grades:{physics:70,chemistry:60}};

    Grades g1={physics:30,chemistry:50,"maths":60};
    Grades g2={physics:50,chemistry:60,"bio":70};

    Section[] sectionList=[d1,d2];
    Grades[] gradesList=[g1,g2];

    Section[] outputSectionmentList=
        from var section in sectionList
        from var grades in gradesList
        let int noOfStudents = 100
        select{
            name: section.name,
            grades:grades,
            "noOfStudents":noOfStudents
        };

    return  outputSectionmentList;
}

function testQueryExprRecordTypedBinding() returns Person1[]{

    Person1 p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ", address:{city:"NY", country:"America"}};
    Person1 p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ", address:{city:"NY", country:"America"}};

    Department d1 = {name:"HR"};
    Department d2 = {name:"Operations"};

	Person1[] personList = [p1, p2];
    Department[] deptList = [d1, d2];

	Person1[] outputPersonList =
            from Person1 { firstName: nm1, lastName: nm2, deptAccess: d, address: a1 } in personList
            from var dept in deptList
            select {
                   firstName: nm1,
                   lastName: nm2,
                   deptAccess: dept.name,
                   address: a1

            };

    return  outputPersonList;
}

type Person record {|
   string firstName;
   string lastName;
   int age;
|};

type Teacher record {|
   string firstName;
|};

type Department record {|
   string name;
|};


function testFromClauseWithInvalidType() returns Person[]{

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from Teacher person in personList
            select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   age: person.age
            };

    return  outputPersonList;
}

function testFromClauseWithUnDefinedType() returns Person[]{

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from XYZ person in personList
            select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   age: person.age
            };

    return  outputPersonList;
}

function testSelectTypeMismatch() {
    Person[] personList = [
        {firstName: "Alex", lastName: "George", age: 23},
        {firstName: "Ranjan", lastName: "Fonseka", age: 30}
    ];

    Teacher[] outputPersonList =
            from Person person in personList
            select {
                   firstName: person.firstName,
                   lastName: person.lastName
            };
}

function testQueryWithInvalidExpressions() returns Person[]{

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from var person in 10
            where 20
            select 30;

    return  outputPersonList;
}

function testQueryActionWithMutableParams() returns Person[]{

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    from var person in personList
    do {
            person = {firstName: "XYZ", lastName: "George", age: 30};
    }

    return personList;
}

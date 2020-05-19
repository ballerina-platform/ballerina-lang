type Person record {|
    string firstName;
    string lastName;
    int age;
|};

type Department record {|
   string name;
|};

function testSimpleOnClause() returns Person[] {

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Department d1 = {name:"HR"};
    Department d2 = {name:"Operations"};

    Person[] personList = [p1, p2, p3];
    Department[] deptList = [d1, d2];

    Person[] outputPersonList =
            from var person in personList
            join var dept in deptList
            on condition(person.firstName)
            select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   age: person.age
            };

    return outputPersonList;
}

function condition(string name) returns boolean{
    return name == "Alex";
}

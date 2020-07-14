type Person record {|
    string firstName;
    string lastName;
    int age;
|};

type PersonTable table<Person> key(firstName);

function testOrderByClauseWithInvalidOrderField() {
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};
    Person p4 = {firstName: "John", lastName: "Fonseka", age: 28};

    Person[] personList = [p1, p2, p3, p4];

    Person[] outputPersonStream = from var person in personList
        order by lastname descending
        select {
            firstName: person.firstName,
            lastName: person.lastName,
            age: person.age
        };
}

function testOrderByClauseWithInvalidOrderField2() {
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};
    Person p4 = {firstName: "John", lastName: "Fonseka", age: 28};

    Person[] personList = [p1, p2, p3, p4];

    PersonTable|error personTable = table key(firstName) from var person in personList
        order by lastname descending
        select {
            firstName: person.firstName,
            lastName: person.lastName,
            age: person.age
        };
}

function testOrderByClauseWithInvalidOrderField3() {
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};
    Person p4 = {firstName: "John", lastName: "Fonseka", age: 28};

    Person[] personList = [p1, p2, p3, p4];

    stream<Person> personTable = stream from var person in personList
        order by lastname descending
        select {
            firstName: person.firstName,
            lastName: person.lastName,
            age: person.age
        };
}

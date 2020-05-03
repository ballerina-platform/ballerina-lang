import ballerina/io;

type Person record {|
    string firstName;
    string lastName;
    int age;
|};

function testSimpleOnClause() returns Person[] {

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from var person in personList
            on printHello()
            select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   age: person.age
            };

    return outputPersonList;
}

function printHello() {
    io:println("Hello");
}

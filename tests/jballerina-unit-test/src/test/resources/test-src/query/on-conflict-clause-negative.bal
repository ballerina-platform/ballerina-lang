import ballerina/io;

type Person record {|
    string firstName;
    string lastName;
    int age;
|};

function testOnConflictClauseWithFunction() returns Person[]|error {

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    Person[]|error outputPersonList =
            from var person in personList
            select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   age: person.age
            }
            on conflict printHello();

    return outputPersonList;
}

function printHello() {
    io:println("Hello");
}

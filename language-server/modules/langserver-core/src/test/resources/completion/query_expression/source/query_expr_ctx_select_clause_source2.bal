import ballerina/module1;

function testOnConflictClauseWithNonTableTypes() {
    error onConflictError = error("Key Conflict", message = "cannot insert.");
    Person p1 = {firstName: "Alex", lastName: "George", age: 33};
    Person p2 = {firstName: "John", lastName: "David", age: 35};
    Person p3 = {firstName: "Max", lastName: "Gomaz", age: 33};
    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from var person in personList
            let int newAge = 34
            where person.age == 33
            select m
}


type Customer record {|
    readonly int id;
    readonly string name;
    int noOfItems;
|};

type Person record {|
    string firstName;
    string lastName;
    int age;
|};

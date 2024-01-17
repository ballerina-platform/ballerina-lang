type Person record {|
    string firstName;
    string lastName;
    int age;
|};
function testFunction() {
    Person p1 = {firstName: "Alex", lastName: "George", age: 33};
    Person p2 = {firstName: "John", lastName: "David", age: 35};
    Person p3 = {firstName: "Max", lastName: "Gomez", age: 33};
    Person[] personList = [p1, p2, p3];

    int outputPersonList =
            from var person in personList
            let string newAge = 34
            where person.age == 33
            select person.firstName;
}

type Person record {|
    string name;
    int id;
|};

function foo(Person person) returns int {
    Person {id, name: nameVar} = person;
    return id;
}

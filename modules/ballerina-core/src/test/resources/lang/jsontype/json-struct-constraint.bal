struct Person {
    string name;
    string address;
}

function testJsonStructConstraint() (json){
    json<Person> j = {};
    j.name = "John Doe";
    j.address = "London";

    return j.name;
}
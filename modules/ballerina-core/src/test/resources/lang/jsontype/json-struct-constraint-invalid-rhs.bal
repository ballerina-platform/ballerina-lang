struct Person {
    string name;
    int age;
    string address;
}

function testJsonStructConstraintInvalid()(json){
    json<Person> j = {};
    j.name = "Ann";
    string name = j.firstName;
    return name;
}
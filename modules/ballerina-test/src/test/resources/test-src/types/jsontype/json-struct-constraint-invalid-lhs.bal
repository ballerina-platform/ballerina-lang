struct Person {
    string name;
    int age;
    string address;
}

function testJsonStructConstraintInvalid()(json){
    json<Person> j = {};
    j.firstName = "Ann";
    return j.firstName;
}
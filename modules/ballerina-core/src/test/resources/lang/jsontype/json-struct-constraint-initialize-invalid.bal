struct Person {
    string name;
    int age;
    string address;
}

function testJsonInitializationWithStructConstraintInvalid() (json, json, json){
    json<Person> j = {name:"John Doe", age:5, address:"London"};
    return j.name, j.age, j.address;
}
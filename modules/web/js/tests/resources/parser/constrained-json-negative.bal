struct Person {
    string name;
    int age;
    string address;
}

struct Student {
    string name;
    int age;
    string address;
    string class;
}

function testJsonInitializationWithStructConstraintInvalid() (json, json, json){
    json<Person> j = {firstName:"John Doe", age:5, address:"London"};
    return j.name, j.age, j.address;
}

function testInvalidStructFieldConstraintLhs()(json){
    json<Person> j = {};
    j.firstName = "Ann";
    return j;
}

function tesInvalidStructFieldConstraintRhs()(json){
    json<Person> j = {};
    j.name = "Ann";
    json name = j.firstName;
    return name;
}

function testConstraintJSONIndexing() (json){
    json<Student> j = {name:"John Doe", age:30, address:"Colombo", class:"5"};
    return j["bus"];
}

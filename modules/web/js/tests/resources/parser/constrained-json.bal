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

function testJsonStructConstraint() (json, json, json, string, int, string) {
    json<Person> j = {};
    j.name = "John Doe";
    j.age = 30;
    j.address = "London";
    var name, _ = (string) j.name;
    var age, _ = (int) j.age;
    var address, _ = (string) j.address;
    return j.name, j.age, j.address, name, age, address;
}

function testJsonInitializationWithStructConstraint() (json, json, json){
    json<Person> j = {name:"John Doe", age:30, address:"London"};
    return j.name, j.age, j.address;
}

function testGetPlainJson() (json) {
    json j = getPlainJson();
    return j;
}

function testGetConstraintJson() (json) {
    json<Person> j = getPerson();
    return j;
}

function getPersonJson() (json){
    json<Person> j = {name:"John Doe", age:30, address:"London"};
    return j;
}

function getPlainJson() (json){
    json j = {firstName:"John Doe", age:30, address:"London"};
    return j;
}

function getPersonEquivalentPlainJson() (json){
    json j = {name:"John Doe", age:30, address:"London"};
    return j;
}

function getPerson() (json<Person>){
    json<Person> j = {name:"John Doe", age:30, address:"London"};
    return j;
}

function getStudent() (json<Student>){
    json<Student> j = {name:"John Doe", age:30, address:"Colombo", class:"5"};
    return j;
}

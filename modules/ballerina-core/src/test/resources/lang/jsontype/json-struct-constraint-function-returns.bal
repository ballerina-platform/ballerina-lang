struct Person {
    string name;
    int age;
    string address;
}

struct Student {
    string name;
    int age;
    string class;
}

function testJsonStructConstraint() (json, json, json, json) {
    json j1 = getPlainJson(); // works
    json<Person> j2 = getPersonJson(); // works
    json<Person> j3 = getPlainJson(); //should work if fields match ==> runtime validation ==> unsafe casting
    json<Person> j4 = getStudent(); // should work if fields match ==> unsafe casting
    return j1, j2, j3, j4;
}

function getPersonJson() (json){
    json<Person> j = {name:"John Doe", age:30, address:"London"};
    return j;
}

function getPlainJson() (json){
    json j = {firstName:"John Doe", age:30, address:"London"};
    return j;
}

function getPerson() (json<Person>){
    json<Person> j = {name:"John Doe", age:30, address:"London"};
    return j;
}

function getStudent() (json<Student>){
    json<Student> j = {name:"John Doe", age:30, class:"5"};
    return j;
}


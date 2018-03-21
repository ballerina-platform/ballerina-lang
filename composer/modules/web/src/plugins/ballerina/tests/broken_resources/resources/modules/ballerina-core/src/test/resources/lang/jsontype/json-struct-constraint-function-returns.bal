import ballerina/lang.errors;

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

function testGetPlainJson() (json) {
    json j = getPlainJson();
    return j;
}

function testGetConstraintJson() (json) {
    json<Person> j = getPerson();
    return j;
}

function testJSONToConstraintJsonUnsafeCast() (json, errors:TypeCastError) {
    json<Person> j;
    errors:TypeCastError err;
    j,err = (json<Person>)getPlainJson();
    return j,err;
}

function testJSONToConstraintJsonUnsafeCastPositive() (json) {
    json<Person> j;
    j,_ = (json<Person>)getPersonEquivalentPlainJson();
    return j;
}

function testConstraintJSONToConstraintJsonAssignment() (json) {
    json<Person> j = (json<Person>)getStudent();
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

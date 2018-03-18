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

struct Employee {
    string first_name;
    string last_name;
    int age;
    Address address;
}

struct Address {
    string number;
    string street;
    string city;
    PhoneNumber phoneNumber;
}

struct PhoneNumber {
    string areaCode;
    string number;
}

function tesInvalidNestedStructFieldAccess() {
    json<Employee> e = {first_name: "John",last_name: "Doe",age: 30,address: {phoneNumber: {number:"456"}, street:"York St"}};
    json j = e.address.phoneNumber.foo;
}

function tesInvalidNestedStructFieldIndexAccess() {
    json<Employee> e = {first_name: "John",last_name: "Doe",age: 30,address: {phoneNumber: {number:"456"}, street:"York St"}};
    json j = e["address"]["phoneNumber"]["bar"];
}

function tesInitializationWithInvalidNestedStruct() {
    json<Employee> e = {first_name: "John",last_name: "Doe",age: 30,address: {phoneNumber: {number:"456", foo:5}, street:"York St"}};
}

function testConstrainedJSONArrayToConstraintJsonArrayCast() (json<Student>[], error) {
    json<Person>[] j1 = [{name:"John Doe", age:30, address:"London"}];
    var j2, e = (json<Student>[]) j1;
    return j2, e;
}

function testBooleanArrayToJsonAssignment() (json) {
    blob[] b = [];
    json j = b;
    return j;
}
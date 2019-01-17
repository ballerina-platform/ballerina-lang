type Person record {
    string name;
    int age;
    string address;
    string...;
};

type InvalidPerson record {
    string fname;
    string lname;
    int age;
};

type Student record {
    string name;
    int age;
    string address;
    string class;
    string...;
};

function testConstrainingUsingRecordWithIncompatibleRestField() {
    json<InvalidPerson> j = {fname:"John", lname:"Doe", age:20};
}

function testJsonInitializationWithIncompatibleRestField() returns (json, json, json, json){
    json<Person> j = {firstName:"John Doe", age:20, address:"London", height:5.5};
    return (j.name, j.age, j.address, j.height);
}

// TODO: Enable this test once type checking for constrained JSON is supported
//function testAssigningIncompatibleRestField() returns (json){
//    json<Person> j = {};
//    j.height = 5.5;
//    return j;
//}

type Employee record {
    string first_name;
    string last_name;
    int age;
    Address address = {};
    string...;
};

type Address record {
    string number = "";
    string street = "";
    string city = "";
    PhoneNumber phoneNumber = {};
    string...;
};

type PhoneNumber record {
    string areaCode = "";
    string number = "";
    string...;
};

function tesInvalidNestedStructFieldAccess() {
    json<Employee> e = {first_name: "John",last_name: "Doe",age: 30,address: {phoneNumber: {number:"456"}, street:"York St"}};
    json j = e.address.phoneNumber.foo;
}

function tesInvalidNestedStructFieldIndexAccess() {
    json<Employee> e = {first_name: "John",last_name: "Doe",age: 30,address: {phoneNumber: {number:"456"}, street:"York St"}};
    json j = e["address"]["phoneNumber"]["bar"];
}

function tesInitializationWithInvalidNestedRecord() {
    json<Employee> e = {first_name: "John",last_name: "Doe",age: 30,address: {phoneNumber: {number:"456", foo:5}, street:"York St"}};
}

function testConstrainedJSONArrayToConstraintJsonArrayStamp() returns json<Student>[] {
    json<Person>[] j1 = [{name:"John Doe", age:30, address:"London"}];
    var j2 = json<Student>[].stamp(j1);
    return j2;
}

function testByteArrayToJsonAssignment() returns (json) {
    byte[] b = [];
    json j = b;
    return j;
}

function testConstraintJSONToConstraintJsonUnsafeNegativeCast() returns (json | error) {
    json<Employee> je = {first_name:"John", last_name:"Doe", age:30, address:{phoneNumber:{number:"1234"}, 
                        street:"York St"}};
    var js = json<Student>.convert(je);
    return js;
}
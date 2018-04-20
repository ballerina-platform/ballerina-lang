
type Person  {
    string name;
    int age;
    string address;
};

type Student {
    string name;
    int age;
    string address;
    string class;
};

function benchmarkJsonStructConstraint() {
    json<Person> j = {};
    j.name = "John Doe";
    j.age = 30;
    j.address = "London";
    var name = <string>j.name;
}

function benchmarkJsonInitializationWithStructConstraint() {
    json<Person> j = {name:"John Doe", age:30, address:"London"};
}

function benchmarkGetPlainJson() {
    json j = getPlainJson();
}

function benchmarkGetConstraintJson() {
    json<Person> j = getPerson();
}

function benchmarkGetPersonJson() {
    json<Person> j = {name:"John Doe", age:30, address:"London"};
}

type Employee {
    string first_name;
    string last_name;
    int age;
    Address address;
};

type Address {
    string number;
    string street;
    string city;
    PhoneNumber phoneNumber;
};

type PhoneNumber {
    string areaCode;
    string number;
};

function benchmarkConstrainingWithNestedRecords() {
    json<Employee> e = {first_name:"John", last_name:"Doe", age:30, address:{phoneNumber:{number:"1234"}, street:"York St"}};
    json address = e.address;
    json phoneNumber = e.address.phoneNumber.number;
}

function benchmarkConstraintJSONToJSONCast() {
    json<Person> j1 = getPerson();
    json j2 = <json>j1;
}

function benchmarkJSONToConstraintJsonUnsafeCast() {
    var j = <json<Person>>getPlainJson();
}

function benchmarkConstraintJSONToConstraintJsonCast() {
    json<Person> j = <json<Person>>getStudent();
}

function benchmarkConstraintJSONToConstraintJsonUnsafePositiveCast() {
    json<Person> jp = <json<Person>>getStudent();
    var js = <json<Student>>jp;
}

function benchmarkConstraintJSONToConstraintJsonUnsafeNegativeCast() {
    json<Employee> je = {first_name:"John", last_name:"Doe", age:30, address:{phoneNumber:{number:"1234"}, street:"York St"}};
    var js = <json<Student>>je;
}

function benchmarkJSONArrayToConstraintJsonArrayCastPositive() {
    json j1 = [getStudent()];
    var j2 = < json<Student>[]>j1;
}

function benchmarkJSONArrayToConstraintJsonArrayCastNegative() {
    json j1 = [{"a":"b"}, {"c":"d"}];
    var j2 = < json<Student>[]>j1;
}

function benchmarkJSONArrayToCJsonArrayCast() {
    json[] j1 = [{"name":"John Doe", "age":30, "address":"London", "class":"B"}];
    json j2 = j1;
    var j3 = < json<Student>[]>j2;
}

function benchmarkJSONArrayToCJsonArrayCastNegative() {
    json[] j1 = [{name:"John Doe", age:30, address:"London"}]; // one field is missing
    json j2 = j1;
    var j3 = < json<Student>[]>j2;
}

function benchmarkCJSONArrayToJsonAssignment() {
    json<Person> tempJ = getPerson();
    tempJ.age = 40;
    json<Person>[] j1 = [getPerson(), tempJ];
    json j2 = j1;
}

function benchmarkMixedTypeJSONArrayToCJsonArrayCastNegative() {
    json[] j1 = [{name:"John Doe", age:30, address:"London", "class":"B"}, [4, 6]];
    json j2 = j1;
    var j3 = < json<Student>[]>j2;
}

function benchmarkConstrainedJsonWithFunctions() {
    json<Person> j = {name:"John Doe", age:30, address:"London"};
    string| () result = j.toString();
}

function benchmarkConstrainedJsonWithFunctionGetKeys() {
    json<Person> j = {name:"John Doe", age:30, address:"London"};
    string[] | () result = j.getKeys();
}

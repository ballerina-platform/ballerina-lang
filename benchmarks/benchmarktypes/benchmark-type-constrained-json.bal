type Person record {
    string name;
    int age;
    string address;
};

type Student record {
    string name;
    int age;
    string address;
    string class;
};

public function benchmarkJsonStructConstraint() {
    json<Person> j = {};
    j.name = "John Doe";
    j.age = 30;
    j.address = "London";
    var name = <string>j.name;
}

public function benchmarkJsonInitializationWithStructConstraint() {
    json<Person> j = { name: "John Doe", age: 30, address: "London" };
}

public function benchmarkGetPlainJson() {
    json j = getPlainJson();
}

public function benchmarkGetConstraintJson() {
    json<Person> j = getPerson();
}

public function benchmarkGetPersonJson() {
    json<Person> j = { name: "John Doe", age: 30, address: "London" };
}

type Employee record {
    string first_name;
    string last_name;
    int age;
    Address address;
};

type Address record {
    string number;
    string street;
    string city;
    PhoneNumber phoneNumber;
};

type PhoneNumber record {
    string areaCode;
    string number;
};

public function benchmarkConstrainingWithNestedRecords() {
    json<Employee> e = { first_name: "John", last_name: "Doe", age: 30, address: { phoneNumber: { number: "1234" },
        street: "York St" } };
    json address = e.address;
    json phoneNumber = e.address.phoneNumber.number;
}

public function benchmarkConstraintJSONToJSONCast() {
    json<Person> j1 = getPerson();
    json j2 = <json>j1;
}

public function benchmarkJSONToConstraintJsonUnsafeCast() {
    var j = <json<Person>>getPlainJson();
}

public function benchmarkConstraintJSONToConstraintJsonCast() {
    json<Person> j = <json<Person>>getStudent();
}

public function benchmarkConstraintJSONToConstraintJsonUnsafePositiveCast() {
    json<Person> jp = <json<Person>>getStudent();
    var js = <json<Student>>jp;
}

public function benchmarkConstraintJSONToConstraintJsonUnsafeNegativeCast() {
    json<Employee> je = { first_name: "John", last_name: "Doe", age: 30, address: { phoneNumber: { number: "1234" },
        street: "York St" } };
    var js = <json<Student>>je;
}

public function benchmarkJSONArrayToConstraintJsonArrayCastPositive() {
    json j1 = [getStudent()];
    var j2 = <json<Student>[]>j1;
}

public function benchmarkJSONArrayToConstraintJsonArrayCastNegative() {
    json j1 = [{ "a": "b" }, { "c": "d" }];
    var j2 = <json<Student>[]>j1;
}

public function benchmarkJSONArrayToCJsonArrayCast() {
    json[] j1 = [{ "name": "John Doe", "age": 30, "address": "London", "class": "B" }];
    json j2 = j1;
    var j3 = <json<Student>[]>j2;
}

public function benchmarkJSONArrayToCJsonArrayCastNegative() {
    json[] j1 = [{ name: "John Doe", age: 30, address: "London" }];
    // one field is missing
    json j2 = j1;
    var j3 = <json<Student>[]>j2;
}

public function benchmarkCJSONArrayToJsonAssignment() {
    json<Person> tempJ = getPerson();
    tempJ.age = 40;
    json<Person>[] j1 = [getPerson(), tempJ];
    json j2 = j1;
}

public function benchmarkMixedTypeJSONArrayToCJsonArrayCastNegative() {
    json[] j1 = [{ name: "John Doe", age: 30, address: "London", "class": "B" }, [4, 6]];
    json j2 = j1;
    var j3 = <json<Student>[]>j2;
}

public function benchmarkConstrainedJsonWithFunctions() {
    json<Person> j = { name: "John Doe", age: 30, address: "London" };
    string|() result = j.toString();
}

public function benchmarkConstrainedJsonWithFunctionGetKeys() {
    json<Person> j = { name: "John Doe", age: 30, address: "London" };
    string[]|() result = j.getKeys();
}

function getPlainJson() returns (json) {
    json j = { firstName: "John Doe", age: 30, address: "London" };
    return j;
}

function getPerson() returns (json<Person>) {
    json<Person> j = { name: "John Doe", age: 30, address: "London" };
    return j;
}

function getStudent() returns (json<Student>) {
    json<Student> j = { name: "John Doe", age: 30, address: "Colombo", 'class: "5" };
    return j;
}


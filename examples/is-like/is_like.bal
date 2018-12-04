import ballerina/io;

type Person record {
    string name = "";
    int age = 0;
};

type Employee record {
    string name = "";
    int age = 0;
    int empNo = 0;
};

// Concept is-like contains `convert()` and `stamp()` functions. is-like is about converting runtime value's inherent storage type.
// But to change inherent storage type, runtime value should be is-like the other type. E.g. Person.convert(emp); emp's
// inherent storage type Employee. Here Employee is like Person, thus `emp` runtime inherent storage type can be coverted
// to Person.
function explainIsLike(Employee emp) returns () {
    // `stamp()` changes inherent type of itself and does not convert new value.
    Person|error empPerson = Person.convert(emp);
    io:println("empPerson name: ", (empPerson is Person) ? empPerson["name"] : empPerson.reason());

    // `convert()` converts a new value and changes its type without editing provided value's inherent type.
    Person|error stampedEmpPerson = Person.stamp(emp);
    if (stampedEmpPerson is Person) {
        io:println("stampedEmpPerson name: ", <string>stampedEmpPerson["name"]);
        stampedEmpPerson["name"] = "John Doe";
    } else {
        io:println("stampedEmpPerson error: ", stampedEmpPerson.reason());
    }

    // `emp.name` was changed due to the behavior of `stamp()`
    io:println("emp name: ", emp.name);
}

public function main() {
    Employee emp = {name: "Jack Sparrow", age: 54, empNo: 100};
    explainIsLike(emp);
}

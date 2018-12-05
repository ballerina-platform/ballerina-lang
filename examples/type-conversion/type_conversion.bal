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

function convertType(Employee emp) returns () {
    // The `convert()` creates a new value and changes its type without editing provided value's inherent type.
    Person|error empPerson = Person.convert(emp);
    io:println("empPerson name: ", (empPerson is Person) ? empPerson["name"] : empPerson.reason());
}

public function main() {
    Employee emp = {name: "Jack Sparrow", age: 54, empNo: 100};
    convertType(emp);
}

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

function explainTypeAssertion(anydata emp) returns () {
    // There is no type casting happens here. Here, emp variable's type is asserted as `Employee` and assigns the same emp
    // value to employee variable. This is safe type assertion.
    Employee employee = <Employee>emp;
    io:println("Type asserted employee name: ", employee.name);

    // This will panic since emp is inherently an Employee. `trap` is used to handle the error.
    Person|error person = trap <Person>emp;
    io:println("Type asserted person name or error: ", (person is Person) ? person.name : person.reason());
}

public function main() {
    Employee emp2 = {name: "Speedy Gonzales", age: 4, empNo: 1};
    explainTypeAssertion(emp2);
}

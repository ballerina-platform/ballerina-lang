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

function assertTypes(anydata emp) {
    // The `emp` is asserted to be of type `Employee` and if successful the value is assigned to variable `employee`.
    Employee employee = <Employee>emp;
    io:println("Type asserted employee name: ", employee.name);

    // This will panic since emp is inherently an Employee. `trap` is used to handle the error.
    Person|error person = trap <Person>emp;
    io:println("Type asserted person name or error: ",
                    (person is Person) ? person.name : person.reason());
}

public function main() {
    Employee emp = { name: "Speedy Gonzales", age: 4, empNo: 1 };
    assertTypes(emp);
}

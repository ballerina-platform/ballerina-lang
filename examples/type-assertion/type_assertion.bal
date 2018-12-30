import ballerina/io;

type Person record {
    string name;
    int age;
};

type Employee record {
    string name;
    int age;
    int empNo;
};

public function main() {
    // Define an `Employee` and assign it to a `Person` typed variable.
    Employee employee = { name: "Speedy Gonzales", age: 4, empNo: 1 };
    Person person = employee;

    // The `person` variable is asserted to be of type `Employee`, and is assigned to another variable of
    // type `Employee`.
    Employee employeeTwo = <Employee>person;
    io:println("asserted employee's name: ", employeeTwo.name);

    // Asserting `person` to be of type `Person` will result in a panic, since `person` is inherently an `Employee` here.
    // `trap` is used to trap the panic and retrieve it as an `error`.
    Person|error result = trap <Person>person;
    if (result is Person) {
        io:println("asserted person's name: ", result.name);
    } else {
        // Print the detail message from the error.
        io:println(result.detail().message);
    }
}

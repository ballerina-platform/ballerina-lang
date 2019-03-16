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
}

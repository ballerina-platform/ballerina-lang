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

type Department record {
    string code;
};

public function main() {
    // Define an `Employee` value and cast it to a `Person`.
    Employee employee = {name: "Jane Doe", age: 25, empNo: 1};
    Person person = <Person>employee;
    io:println("Person Name: ", person.name);
    io:println("Person Age: ", person.age);

    // Cast the value held by the `person` variable to an `Employee`.
    // This should succeed since `person` holds an `Employee` value.
    Employee employeeTwo = <Employee>person;
    io:println("Employee Name: ", employeeTwo.name);
    io:println("Employee Age: ", employeeTwo.age);

    // Cast an `int` value held by an `anydata` typed variable as an
    // `int` value.
    anydata value = 100;
    int i = <int>value;
    io:println("Integer Value: ", i);

    // Use the type cast expression with `value`, which currently holds
    // an `int` value, to cast it to `float`.
    // A numeric conversion would happen from `int` to `float`.
    float f = <float>value;
    io:println("Converted Float Value: ", f);

    // Casting to a union type would also work similarly.
    // If `value` belongs to the union type, the resultant value would
    // be `value` itself. Else, if applicable, a numeric conversion will
    // be attempted.
    // A numeric conversion would happen from `int` to `float` here.
    float|boolean u = <float|boolean>value;
    io:println("Converted Float Value: ", u);

    // Assign `employee` to `value`.
    value = employee;
    // Casting a value to an incorrect type (the value does not belong
    // to the type and numeric conversion is not possible) would result
    // in an abrupt completion with a panic.
    Department department = <Department>value;
}

import ballerina/io;

// This function returns a value of the `any` type.
function getValue() returns (any) {
    string name = "cat";
    return name;
}

public function main() {
    // In this example, the variable named `a` of the `any` type holds an `int` value.
    any a = 5;
    io:println(a);

    // First, the variable of the `any` type needs to be cast to the required type (`int` in this example) as shown here.
    int|error intVal = trap <int>a;
    if (intVal is int) {
        io:println(intVal + 10);
    } else {
        // Runtime value is casted to correct type since Ballerina runtime can infer the correct type to error.
        io:println("Error occurred: " + intVal.reason());
    }

    // In Ballerina, a variable of the `any` type can hold values of any data type.
    int[] ia = [1, 3, 5, 6];
    any ar = ia;
    io:println(ar);

    io:println(getValue());
}

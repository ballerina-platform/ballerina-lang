import ballerina/io;

// This function returns a value of the `anydata` type.
function getValue() returns anydata {
    string name = "cat";
    return name;
}

public function main() {
    // In this example, the variable named `a` of the `anydata` type holds an `int` value.
    anydata a = 5;
    io:println(a);

    // First, the variable of type `anydata` needs to be asserted to be of the required type (`int` in this example)
    int|error intVal = trap <int>a;
    if (intVal is int) {
        io:println(intVal + 10);
    } else {
        // Runtime value is cast to correct type since Ballerina runtime can infer the correct type to error.
        io:println("Error occurred: " + intVal.reason());
    }

    // A variable of type `anydata` can hold any value of an `anydata` compatible type.
    int[] ia = [1, 3, 5, 6];
    anydata ar = ia;
    io:println(ar);

    io:println(getValue());
}

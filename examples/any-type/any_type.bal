import ballerina/io;

// This function returns a value of the `any` type.
function getValue() returns any {
    string name = "cat";
    return name;
}

public function main() {
    // In this example, the variable named `a` of the `any` type holds an `int` value.
    any a = 5;
    io:println(a);

    // First, the variable of type `any` needs to be asserted to be of the required type.
    int intVal = <int>a;
    io:println(intVal + 10);

    // In Ballerina, a variable of the `any` type can hold values of any type.
    int[] ia = [1, 3, 5, 6];
    any ar = ia;
    io:println(ar);

    io:println(getValue());
}

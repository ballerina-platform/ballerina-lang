import ballerina/io;

// This function returns a value of the `any` type.
function getValue() returns (any) {
    string name = "cat";
    return name;
}

function main(string... args) {
    // In this example, the variable named `a` of the `any` type holds an `int` value.
    any a = 5;
    io:println(a);

    // First, the variable of the `any` type needs to be casted to the required type (`int` in this example) as shown here. More information about type casting is available in the next section.
    int intVal = check <int>a;
    io:println(intVal + 10);

    // In Ballerina, a variable of the `any` type can hold values of any data type.
    int[] ia = [1, 3, 5, 6];
    any ar = ia;
    io:println(ar);

    io:println(getValue());
}

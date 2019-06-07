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

    // Before using the value of `a` in arithmetic operations, we need to
    // ascertain that it is indeed an `int`. To this end, a type cast or
    // a type guard can be used.
    int intVal = <int>a;
    io:println(intVal + 10);

    if (a is int) {
        io:println(a + 20);
    }

    // A variable of type `anydata` can hold any value of an `anydata` compatible type.
    int[] ia = [1, 3, 5, 6];
    anydata ar = ia;
    io:println(ar);

    io:println(getValue());
}

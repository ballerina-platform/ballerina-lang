import ballerina/io;

public function main() {
    // In this example, the variable named `data1` of the `anydata` type holds an `int` value.
    anydata data1 = 5;
    io:println(data1);

    // Before using the value of `data1` in arithmetic operations, it is required to ascertain 
    // that is actually an `int`. A type cast or a type guard can be used for this.
    int intVal = <int> data1;
    io:println(intVal + 10);

    if data1 is int {
        io:println(data1 + 20);
    }

    // A variable of type `anydata` can hold any value of an `anydata` compatible type.
    int[] intArray = [1, 3, 5, 6];
    anydata dataArray = intArray;
    io:println(dataArray);
}

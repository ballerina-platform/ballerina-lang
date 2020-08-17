import ballerina/io;
import ballerina/math;

// This function calls the `intAdd` function and returns the result.
public function addValues(int a, int b) returns int {
    return intAdd(a, b);
}

// This function adds two integers and returns the result.
public function intAdd(int a, int b) returns int {
    return (a + b);
}

// This function prints the value of PI using the `io:println` function.
public function printMathConsts() {
   io:println("Value of PI : ", math:PI);
}

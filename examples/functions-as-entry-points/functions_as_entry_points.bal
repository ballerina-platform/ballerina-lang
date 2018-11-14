import ballerina/io;

error unknownOpError = error("unknown operation");

// A function named `main` acts as a default entry point to a Ballerina program.
// This entry function can have zero or more parameters, and can either return
// an int or can have no return value.
public function main(int i, int j, string s) returns int {
    if (s == "add") {
        io:println("Operation: Addition");
        return add(i, j);
    } else if (s == "subtract") {
        io:println("Operation: Subtraction");
        return subtract(i, j);
    }
    throw unknownOpError;
}

// A public function to perform a math operation. The first parameter `op`,
// representing the operation, is a defaultable parameter with the default
// value `add`.
public function performMathOp(string op = "add", int... values) returns int {
    if (isKnownOp(op) && lengthof values == 0) {
        error invalidArgsError = error("insufficient arguments specified");
        throw invalidArgsError;
    }

    int value = values[0];
    if (op == "add") {
        foreach i in 1 ..< lengthof values {
            value = add(value, values[i]);
        }
        return value;
    } else if (op == "subtract") {
        foreach i in 1 ..< lengthof values {
            value = subtract(value, values[i]);
        }
        return value;
    }
    throw unknownOpError;
}

public function add(int i, int j) returns int {
    return i + j;
}

public function subtract(int i, int j) returns int {
    return i - j;
}

public function isKnownOp(string op) returns boolean {
    return op == "add" || op =="subtract";
}

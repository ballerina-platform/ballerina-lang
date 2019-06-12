import ballerina/io;

// The `main` function can perform a math operation. The first parameter `operation`
// represents the math operation while the second parameter `initialValue` is a default
// parameter with the default value `0` representing the base value upon which the operation
// should be performed. The other parameter `values` represent the additional arguments.
// The `main` function may return an `error` or `()`.
public function main(string operation, int initialValue = 0, int... values)
                    returns error? {
    int value = initialValue;
    if (operation == "add") {
        foreach int intValue in values {
            value += intValue;
        }
        io:println("Result: ", value);
    } else if (operation == "subtract") {
        foreach var intValue in values {
            value -= intValue;
        }
        io:println("Result: ", value);
    } else {
        io:println("Error: Unknown Operation");
        error unknownOpError = error("unknown operation");
        return unknownOpError;
    }
}

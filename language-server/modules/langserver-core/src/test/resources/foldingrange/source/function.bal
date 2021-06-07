import ballerina/io;
import ballerina/math;

public function main() {
    io:println("Hello, World!");
}

# Prints PI value
# Refers math library
public function printPI(int value) {
    // Refer symbols of another module.
    // `math:PI` is a qualified identifier. Note the usage of the module prefix.
    float piValue = math:PI;

    // Use the explicit prefix `console` to invoke a function defined in the `ballerina/io` module.
    io:println(piValue);
    if value == 0 {
        io:println("Value cannot be zero.");
    } else if value < 0 {
        io:println("Value cannot be negative.");
    } else {
        io:println("Value is acceptable.");
    }
}

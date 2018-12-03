import ballerina/io;

// Declare a module-level variable that is private to the module.
int total = 99;

// Declare a public module-level variable.
public int count = 0;

// Declare a constant. `const`'s value is frozen. Value is computed at compile time therefore immutable.
const string OK = "ok";

// Declare a public final variable that behaves as a constant for other modules.
// Value of final variable is forzen. All the parameters for a function call is also implicitly final.
// Value is computed at runtime therefore not immutable but cannot assign any other value since variable is final.
public final int status = 1;

public function main() {

    // Access a global variable.
    io:println(total);

    count += 1;
    io:println(count);

    // This is a local variable.
    boolean available = false;
    io:println(available);
}

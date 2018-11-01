import ballerina/io;

// Declare a module-level variable that is private to the module.
int total = 99;

// Declare a public module-level variable.
public int count;

// Declare a public constant.
@final
public string OK = "ok";

// Declare a public variable that behaves as a constant for other modules.
// The code in the current module can update the value.
@readonly
public int status = 1;

public function main() {

    // Access a global variable.
    io:println(total);

    count += 1;
    io:println(count);

    // This is a local variable.
    boolean available = false;
    io:println(available);
}

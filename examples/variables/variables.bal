import ballerina/io;

// Declare a module-level variable that is private to the module.
int total = 99;

// Declare a public module-level variable.
public int count = 0;

// Declare a constant.
const string OK = "ok";

// Declare a public final variable that behaves as a constant for other modules.
// Value of the final variable is frozen. Once a value is assigned to a final
// variable it becomes immutable. All parameters of a function call are
// implicitly final.
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

import ballerina/io;

// Declares a module-level variable.
int total = 99;

// Declares a constant.
const string OK = "ok";

// Declares a `public` constant.
public const int COUNT = 1;

// Declares a final variable.
// The value of the `final` variable is frozen. Once a value is assigned to a final
// variable, it becomes immutable. All parameters of a function call are
// implicitly final.
final int status = 1;

public function main() {

    // Accesses a global variable.
    io:println(total);

    // Accesses a public constant.
    io:println(COUNT);

    // This is a local variable.
    boolean available = false;
    io:println(available);
}

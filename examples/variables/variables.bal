import ballerina/io;

// Declare a module-level variable.
int total = 99;

// Declare a constant.
const string OK = "ok";

// Declare a `public` constant.
public const int COUNT = 1;

// Declare a final variable.
// The value of the `final` variable is readonly. Once a value is assigned to a final
// variable, it becomes immutable. All parameters of a function call are
// implicitly final.
final int status = 1;

public function main() {

    // Access a global variable.
    io:println(total);

    // Access a public constant.
    io:println(COUNT);

    // This is a local variable.
    boolean available = false;
    io:println(available);
}

// Here's how you can import a module. You can only refer to public symbols of an imported module.
// By default, the last element of the module name becomes an alias that is used to refer to symbols of the imported module.
// If the module name has dots, then the last word after the last dot becomes the alias.
import ballerina/math;

// Declare an explicit alias
import ballerina/io as console;

public function main() {

    // Refer to the symbols of another module.
    // Here `math:PI` is a qualified identifier. Note the usage of the module alias.
    float piValue = math:PI;

    // Use the explicit alias `console` to invoke a function defined in the `ballerina/io` module.
    console:println(piValue);
}

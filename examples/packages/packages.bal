// Here's how you can import a package. You can only refer to public symbols of an imported package.
// By default, the last element of the package name becomes an alias that is used to refer to symbols of the imported package.
// If the package name has dots, then the last word after the last dot becomes the alias.
import ballerina/math;

// Declare an explicit alias
import ballerina/io as console;

function main(string... args) {

    // Refer to the symbols of another package.
    // Here `math:PI` is a qualified identifier. Note the usage of the package alias.
    float piValue = math:PI;

    // Use the explicit alias `console` to invoke a function defined in the `ballerina/io` package.
    console:println(piValue);
}

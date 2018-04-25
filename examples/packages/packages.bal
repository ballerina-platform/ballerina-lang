// Here's how you can import a package. You can only refer to public symbols of the imported package.
// By default, the last element of the package name becomes alias which is used to refer to symbols of the imported package.
// If the package name has dots, then the last word after the last dot becomes the alias.
import ballerina/math;

// You can declare an explicit alias by using the following syntax.
import ballerina/io as console;

function main(string... args) {

    // Here's how you refer to the symbols of another package.
    // Here `math:PI` is a qualified identifier. Notice the usage the package alias.
    float piValue = math:PI;

    // Invokes a function defined in the package balleirna/io using the explicit alias `console`.
    console:println(math:PI);
}

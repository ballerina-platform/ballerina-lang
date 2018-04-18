import ballerina/lang.system;
import ballerina/doc;

@doc:Description {value:"This function takes a string argument."}
function print (string value) {
    system:println(value);
}

@doc:Description {value:"This function takes two int values and return their sum as an int."}
function add (int a, int b) (int) {
    return a + b;
}

function main (string... args) {
    // Call a function which prints the given value to the console.
    print("This is a sample text");

    int result = add(5, 6);
    system:print(result);
}


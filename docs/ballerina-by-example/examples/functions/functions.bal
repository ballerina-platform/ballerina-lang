import ballerina.io;

@Description {value:"This function takes a string argument."}
function printValue (string value) {
    io:println(value);
}

@Description {value:"This function takes two int values and return their sum as an int."}
function add (int a, int b) (int) {
    return a + b;
}

function main (string[] args) {
    // Call a function which prints the given value to the console.
    printValue("This is a sample text");

    // Directly print a value to the console.
    int result = add(5, 6);
    io:print(result);
}

import ballerina.lang.system;
import ballerina.doc;

@doc:Description {value:"The (int, int) in this function indicates that it returns 2 int values."}
function getInts () (int, int) {
    return 2, 3;
}

@doc:Description {value:"This function returns 3 values: 2 integers and a string."}
function newVals () (int, int, string) {
    return 5, 8, "Hello World!";
}

function main (string[] args) {
    int a;
    int b;
    string c;

    // The returned values can be assigned to individual variables.
    a, b = getInts();
    system:println(a + " " + b);

    // If a particular value returned is not needed, it can be ignored by using '_'.
    a, _, c = newVals();
    system:println(a + " " + b + " " + c);
}


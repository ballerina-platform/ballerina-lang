import ballerina/io;

@Description {value:"This function returns an 'any' typed value."}
function getValue () returns (any) {
    string name = "cat";
    return name;
}

function main (string[] args) {
    //The 'any' typed variable 'a' holds a value of type 'int'.
    any a = 5;
    io:println(a);

    //You need to cast an 'any' typed variable to the required type first. You can find more about type casting in the next section.
    int intVal = check <int> a;
    io:println(intVal + 10);

    //You can assign a variable of any data type in Ballerina to an 'any' typed variable.
    int[] ia = [1, 3, 5, 6];
    any ar = ia;
    io:println(ar);

    io:println(getValue());
}

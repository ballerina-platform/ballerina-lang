import ballerina/lang.system;
import ballerina/doc;

@doc:Description {value:"This function return an 'any' typed value."}
function getValue () (any) {
    string name = "cat";
    return name;
}

function main (string... args) {
    //'any' typed variable 'a' holds a value of type int.
    any a = 5;
    system:println(a);

    //You need to cast an 'any' typed variable to the required type first. You can find more about type casting in the next section.
    var intVal, _ = (int)a;
    system:println(intVal + 10);

    //You can assign a variable of any data type in Ballerina to an 'any' typed variable.
    int[] ia = [1, 3, 5, 6];
    any ar = ia;
    system:println(ar);

    system:println(getValue());
}
import ballerina/lang.system;

function main (string... args) {
    //Here we assign a 'string' typed value to a variable of type 'any'.
    any a = "Jungle cat";
    //Here is how you can cast an 'any' typed variable to the 'string' type.
    //This cast is an unsafe, because the value of the variable 'a' is unknown till runtime.
    //Therefore the compiler will enforce you to use multi-return type cast expression.
    var s, castErr = (string)a;
    if(castErr != null) {
        system:println("error: " + castErr.msg);
    }
    system:println(s);
}
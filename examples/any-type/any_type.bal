import ballerina/io;

@Description {value: "This function returns a value of type 'any'."}
function getValue() returns (any) {
    string name = "cat";
    return name;
}

function main(string... args) {
    //The variable named 'a' of type 'any' holds a value of type 'int' in this case.
    any a = 5;
    io:println(a);

    //First, you should cast the variable of type 'any' to the type you want (e.g., int). You can learn more about type casting in the next section.
    int intVal = check <int>a;
    io:println(intVal + 10);

    //In Ballerina, a variable of type 'any' can hold values of any data type.
    int[] ia = [1, 3, 5, 6];
    any ar = ia;
    io:println(ar);

    io:println(getValue());
}

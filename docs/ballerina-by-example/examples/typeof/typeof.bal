import ballerina/io;

function main (string[] args) {
    //Here you create 'any' typed variable.
    any variable;
    //Assign an integer variable to 'any' typed variable.
    variable = 5;

    //Use 'typeof' unary operator to return type of variable.
    typedesc typeOfVariable = (typeof variable);

    //Use 'typeof' unary operator to return type from type name.
    typedesc intType = (typeof int);

    //Check for runtime type equivalency of 'any' typed variable.
    if (typeOfVariable == intType) {
        io:println("This 'variable' is an integer typed variable.");
    } else {
        io:println("This 'variable' is 'NOT' an integer typed variable.");
    }
}
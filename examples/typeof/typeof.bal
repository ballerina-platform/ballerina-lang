function main (string[] args) {
    //Here you create 'any' typed variable.
    any variable;
    //Assign an integer variable to 'any' typed variable.
    variable = 5;

    //Use 'typeof' unary operator to return type of variable.
    type typeOfVariable = (typeof variable);

    //Use 'typeof' unary operator to return type from type name.
    type intType = (typeof int);

    //Check for runtime type equivalency of 'any' typed variable.
    if (typeOfVariable == intType) {
        println("This 'variable' is an integer typed variable.");
    } else {
        println("This 'variable' is 'NOT' an integer typed variable.");
    }
}
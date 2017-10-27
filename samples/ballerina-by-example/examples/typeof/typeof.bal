function main (string[] args) {
    //Here you create 'any' typed variable.
    any variable;
    //Assign an integer variable to 'any' typed variable.
    variable = 5;

    //Here you create integer typed variable.
    int intVariable;

    //Use 'typeof' unary operator to return variable type.
    type typeOfVariable = (typeof variable);
    type typeOfIntVariable = (typeof intVariable);

    //Check for runtime type equivalency of 'any' typed variable.
    if (typeOfVariable == typeOfIntVariable) {
        println("This 'variable' is an integer typed variable.");
    } else {
        println("This 'variable' is 'NOT' an integer typed variable.");
    }
}

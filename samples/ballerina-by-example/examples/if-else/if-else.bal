import ballerina.lang.system;

function main (string[] args) {


    int a = 10;
    int b = 0;

    //This is a basic example
    if (a < b) {
        system:println("b is greater");
    } else {
        system:println("a is greater");
    }

    //If statement can also be used standalone without else
    if (a == 10) {
        system:println("This is number 10");
    }

    //Else-if is also supported
    if (b < 0) {
        system:println("This is a negative number");
    } else if (b > 0) {
        system:println("This is a positive number");
    } else {
        system:println("This is zero");
    }



}

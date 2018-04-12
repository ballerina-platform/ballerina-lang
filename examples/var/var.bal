import ballerina/io;

function divideBy10 (int d) returns (int, int) {
    return (d / 10, d % 10);
}

function main (string[] args) {
    //The variable type is inferred from the initial value. This is the same as 'int k = 5';
    var k = 5;
    io:println(10 + k);

    //Here the type of 'strVar' is 'string'.
    var strVar = "Hello!";
    io:println(strVar);

    //If multiple return values are declared in a function and you want to get these values, define the variables as
    //comma separated values for the `var` variable type. In this example, the variable type is int. This is inferred
    //from the return variable type that is defined in the function.
    var (q, r) = divideBy10(6);
    io:println("06/10: " + "quotient=" + q + " " + "remainder=" + r);

    //To ignore a return value in a multiple assignment statement, use '_'.
    //Ignores the first return value.
    var (q1, _) = divideBy10(57);
    io:println("57/10: " + "quotient=" + q1);

    //Ignores the second return value.
    var (_, r1) = divideBy10(9);
    io:println("09/10: " + "remainder=" + r1);
}

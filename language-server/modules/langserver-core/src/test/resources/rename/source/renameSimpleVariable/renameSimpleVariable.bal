import ballerina/io;
function main(string... args) {
    //simple assignment
    int a = 10;
    int b = a + 20;
    io:println(a);

    //method invocation
    calculate(a, 100);

    //if-else
    if (a > 10){
        a = 100;
    } else {
        a = 50;
    }

    //tuple
    (a, b) = getValue();
}

function calculate(int num1, int num2) returns int {
    return num1 + num2;
}

function getValue() returns (int, int) {
    return (5, 5);
}
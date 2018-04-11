import ballerina/io;

function main (string[] args) {
    //Create an int array of length 0.
    int[] a = [];
    io:println(lengthof a);

    //Create an array with initially assigned values.
    a = [1, 2, 3, 4, 5, 6, 7, 8];
    io:println(lengthof a);

    //Arrays are unbounded in length. They can grow upto any length based on the given index.
    //For example, the length of the following array is 1000.
    a[999] = 23;
    io:println(a[999]);
    io:println(lengthof a);
}

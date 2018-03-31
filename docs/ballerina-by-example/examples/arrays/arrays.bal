import ballerina/io;

function main (string[] args) {
    //Here is how you can create an int array of length 0.
    int[] a = [];
    io:println(lengthof a);

    //Here is how you can create an array with initial values.
    a = [1, 2, 3, 4, 5, 6, 7, 8];
    io:println(lengthof a);

    //Arrays are unbounded in length. The arrays grow to whatever size needed based on the given index.
    //Now the length of the following array should be 1000.
    a[999] = 23;
    io:println(a[999]);
    io:println(lengthof a);
}

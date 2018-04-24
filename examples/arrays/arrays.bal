import ballerina/io;

function main(string... args) {
    // Here we create an `int` array of length 0.
    int[] a = [];
    io:println(lengthof a);

    // Here we assign an array literal.
    int[] b = [1, 2, 3, 4, 5, 6, 7, 8];
    io:println(b[0]);
    io:println(lengthof b);

    // Arrays are unbounded in length. They can grow up to any length based on the given index.
    // For example, the length of the following array is 1000.
    b[999] = 23;
    io:println(b[999]);
    io:println(lengthof b);

    // Here is how you can initialize an array of int arrays.
    int[][] iarray = [[1, 2, 3], [10, 20, 30], [5, 6, 7]];
    io:println(lengthof iarray);
    io:println(lengthof iarray[0]);

    // Initialize the outermost with the implicit default value.
    iarray = [];
    int[] d = [9];
    iarray[0] = d;

    // Print the first value of the two-dimensional array.
    io:println(iarray[0][0]);
}

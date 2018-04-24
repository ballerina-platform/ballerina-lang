import ballerina/io;

function main(string... args) {
    // This is how you can initialize an array of int arrays.
    int[][] xx = [[1, 2, 3], [10, 20, 30], [5, 6, 7]];
    io:println(lengthof xx);
    io:println(lengthof xx[0]);

    // This is how to initialize a three-dimensional array with one value. In this case, the value is
    // a two-dimensional array.
    int[][][] xxx = [xx];
    xxx[0][0][1] = 10;
    io:println(xxx[0][0][1]);

    // Initialize the outermost array of the two dimensional array with
    // an empty value.
    int[][] aa = [];

    // Set the initialized one-dimensional array to the two-dimensional array.
    int[] a = [9];
    aa[0] = a;

    // Print the first value of the two-dimensional array.
    io:println(xx[0][0]);
}

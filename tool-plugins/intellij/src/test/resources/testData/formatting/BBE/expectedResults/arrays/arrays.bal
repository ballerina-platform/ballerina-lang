import ballerina/io;

public function main() {
    // Create an `int` array of length 0.
    int[] a = [];
    io:println(a.length());

    // Create an array specifying an array literal.
    int[] b = [1, 2, 3, 4, 5, 6, 7, 8];
    io:println(b[0]);
    io:println(b.length());

    // Arrays are unbounded in length. They can grow up to any length based on
    // the given index. In this example, the length of the array is 1000.
    b[999] = 23;
    io:println(b[999]);
    io:println(b.length());

    // Initialize a two dimensional `int` array.
    int[][] iarray = [[1, 2, 3], [10, 20, 30], [5, 6, 7]];
    io:println(iarray.length());
    io:println(iarray[0].length());

    // Initialize the outermost array to an empty array.
    iarray = [];
    // Add a new array as the first element.
    int[] d = [9];
    iarray[0] = d;

    // Print the first value of the two-dimensional array.
    io:println(iarray[0][0]);

    // Create an `int` array with the fixed length of five.
    int[5] e = [1, 2, 3, 4, 5];
    io:println(e.length());

    // To infer the size of the array from the array literal, use the following syntax.
    // The length of the array is set to four here.
    int[*] g = [1, 2, 3, 4];
    io:println(g.length());
}

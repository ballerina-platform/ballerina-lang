import ballerina/io;

public function main() {
    // This creates an `int` array of length 0.
    int[] a = [];
    io:println(a.length());

    // This assigns an array literal.
    int[] b = [1, 2, 3, 4, 5, 6, 7, 8];
    io:println(b[0]);
    io:println(b.length());

    // Arrays are unbounded in length. They can grow up to any length based on the given index.
    // In this example, the length of the array is 1000.
    b[999] = 23;
    io:println(b[999]);
    io:println(b.length());

    //This initializes an array of int arrays.
    int[][] iarray = [[1, 2, 3], [10, 20, 30], [5, 6, 7]];
    io:println(iarray.length());
    io:println(iarray[0].length());

    // This initializes the outermost array with the implicit default value.
    iarray = [];
    int[] d = [9];
    iarray[0] = d;

    // This prints the first value of the two-dimensional array.
    io:println(iarray[0][0]);

    // This creates an `int` array with a fixed length of five.
    int[5] e = [1, 2, 3, 4, 5];
    io:println(e.length());

    // To infer the size of the array from the array literal, use the following syntax.
    // This sets the length of the array to four.
    int[!...] g = [1, 2, 3, 4];
    io:println(g.length());
}

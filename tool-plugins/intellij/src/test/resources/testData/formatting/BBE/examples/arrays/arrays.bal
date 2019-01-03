import ballerina/io;

public function main(string... args) {
// This creates an `int` array of length 0.
int[] a = [];
io:println(lengthof a);

// This assigns an array literal.
int[] b = [1, 2, 3, 4, 5, 6, 7, 8];
io:println(b[0]);
io:println(lengthof b);

// Arrays are unbounded in length. They can grow up to any length based on the given index.
// In this example, the length of the array is 1000.
b[999] = 23;
io:println(b[999]);
io:println(lengthof b);

//This initializes an array of int arrays.
int[][] iarray = [[1, 2, 3], [10, 20, 30], [5, 6, 7]];
io:println(lengthof iarray);
io:println(lengthof iarray[0]);

// This initializes the outermost array with the implicit default value.
iarray = [];
int[] d = [9];
iarray[0] = d;

// This prints the first value of the two-dimensional array.
io:println(iarray[0][0]);

// This creates a sealed `int` array of length 5.
int[5] e = [1, 2, 3, 4, 5];
io:println(lengthof e);

// This creates a sealed `int` array of length 5 with default value `0` as member values.
int[5] f;
io:println(lengthof f);

// To infer the size of the sealed array from the array literal, following syntax is used.
int[!...] g = [1, 2, 3, 4];
io:println(lengthof g);
}

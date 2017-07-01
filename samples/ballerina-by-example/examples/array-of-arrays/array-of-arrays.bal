import ballerina.lang.system;

function main (string[] args) {
    // Initialize one dimension array with two values.
    int[] x = [1, 2];
    // Initialize another one dimension array with two values.
    int[] y = [3, 4];

    // Initialize two dimension array with two values. As you can see in this two
    // values previously created one dimensional arrays.
    int[][] xx = [x, y];

    // Initialize three dimensional array with one value. In this case value is a
    // two dimensional array.
    int[][][] xxx = [xx];
    xxx[0][0][1] = 10;
    system:println(xxx[0][0][1]);

    // Initialize the outer most array of the two dimensional array with
    // empty value.
    int[][] aa = [];

    // Set the initialized one dimensional array to the two dimensional array.
    int[] a = [9];
    aa[0] = a;

    // Print the first value of three dimensional array.
    system:println(xx[0][0]);
}

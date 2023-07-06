import ballerina/io;
import ballerina/lang.array;

public function main() {
    // Creates an `int` array of length 0.
    int[] a = [];
    io:println(a.length());

    // Creates an array specifying an array literal.
    int[] b = [1, 2, 3, 4, 5, 6, 7, 8];
    io:println(b[0]);
    io:println(b.length());

    // Arrays support several inbuilt functions such as `.reverse()`, `.pop()`, `.push()`, and `.removeAll()`.
    int[] bReveresed = b.reverse();
    io:println("Reversed: ", bReveresed);

    io:println("Before pop: ", b);
    int poppedValue = b.pop();
    io:println("Popped value: ", poppedValue);
    io:println("After pop: ", b);

    // Arrays are an iterable type and arrays support functional iteration operations such as `.map()`, `.filter()`, and
    // `.reduce()`.
    int[] doubled = b.map(function (int value) returns int {
            return value * 2;
        });
    io:println("Doubled: ", doubled);

    // An array can be sorted using `.sort()`. The `array:DESCENDING` is the `direction` in which sort should be done.
    // The default sort direction is `ASCENDING`. The `isolated function` returns a `string[]` for each
    // member, which is used as a `key` to sort the members. If the member type of the array
    // is not sorted, then the `key` function must be specified.
    // It returns an `array` consisting of the members of `b` in the sorted order.
    int[] sortedArray = b.sort(array:DESCENDING, isolated function (int value)
    returns string[] {
            if (value < 5) {
                return ["A",value.toString()];
            } else {
                return ["B",value.toString()];
            }
        });
    io:println("Sorted Array: ", sortedArray);

    // Unless the length is explicitly specified or is expected to be inferred, arrays are unbounded in length.
    // They can grow up to any length based on the given index.
    // In this example, the length of the array is 1000.
    b[999] = 23;
    io:println(b[999]);
    io:println(b.length());

    // Initializes a two dimensional `int` array.
    int[][] iarray = [[1, 2, 3], [10, 20, 30], [5, 6, 7]];
    io:println(iarray.length());
    io:println(iarray[0].length());

    // Initializes the outermost array to an empty array.
    iarray = [];
    // Adds a new array as the first element.
    int[] d = [9];
    iarray[0] = d;

    // Prints the first value of the two-dimensional array.
    io:println(iarray[0][0]);

    // Creates an `int` array with the fixed length of five.
    int[5] e = [1, 2, 3, 4, 5];
    io:println(e.length());

    // To infer the size of the array from the array literal, use the following syntax.
    // The fixed length of the array is set to four here.
    int[*] g = [1, 2, 3, 4];
    io:println(g.length());
}

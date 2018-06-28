import ballerina/io;

function main(string... args) {
    // A closed integer range `x ... y` generates an array of integers containing values `x` to `y`, `x` and `y`
    // inclusive
    int[] closedRangeArray = 25...28;
    io:println("Length of the array created by the closed integer range 25 ... 28: ",
        lengthof closedRangeArray);
    io:println("First element: ", closedRangeArray[0]);
    io:println("Last element: ", closedRangeArray[lengthof closedRangeArray - 1]);

    // A half open integer range `x ..< y` generates an array of integers containing values `x` to `y`, including `x`
    // but excluding `y`
    int[] halfOpenRangeArray = 25..<28;
    io:println("\nLength of the array created by the half open integer range 25 ..< 28: ",
        lengthof halfOpenRangeArray);
    io:println("First element: ", halfOpenRangeArray[0]);
    io:println("Last element: ", halfOpenRangeArray[lengthof halfOpenRangeArray - 1]);
}

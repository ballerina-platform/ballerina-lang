import ballerina/io;

function testClosedIntRange(int startValue, int endValue) returns int[] {
    int[] returnArray = [];
    int returnArrayIndex = 0;
    foreach var val in startValue ... endValue {
        returnArray[returnArrayIndex] = val;
        returnArrayIndex += 1;
    }
    return returnArray;
}

function testClosedIntRangeAsArray(int startValue, int endValue) returns int[] {
    int[] returnArray = [];
    int returnArrayIndex = 0;
    int[] rangeAsArray = startValue ... endValue;
    foreach var val in rangeAsArray {
        returnArray[returnArrayIndex] = val;
        returnArrayIndex += 1;
    }
    return returnArray;
}

function testHalfOpenIntRange(int startValue, int endValue) returns int[] {
    int[] returnArray = [];
    int returnArrayIndex = 0;
    foreach var val in startValue ..< endValue {
        returnArray[returnArrayIndex] = val;
        returnArrayIndex += 1;
    }
    return returnArray;
}

function testHalfOpenIntRangeAsArray(int startValue, int endValue) returns int[] {
    int[] returnArray = [];
    int returnArrayIndex = 0;
    int[] rangeAsArray = startValue ..< endValue;
    foreach var val in rangeAsArray {
        returnArray[returnArrayIndex] = val;
        returnArrayIndex += 1;
    }
    return returnArray;
}

public function main() {
    // A closed integer range `x ... y` generates an array of integers
    // containing values `x` to `y`, including `x` and `y`.
    int[] closedRangeArray = 25
    ...
    28
    ;
    int arrayLength = closedRangeArray.length();
    io:println("Length of the array created by the closed integer range"
    + " 25 ... 28: ", arrayLength);
    io:println("First element: ", closedRangeArray[0]);
    io:println("Last element: ", closedRangeArray[arrayLength - 1]);


    // A half open integer range `x ..< y` generates an array of integers
    // containing values `x` to `y`, including `x` but excluding `y`.
    int[] halfOpenRangeArray = 25 ..< 28;
    arrayLength = halfOpenRangeArray.length();
    io:println("\nLength of the array created by the half open integer range"
    + " 25 ..< 28: ", arrayLength);
    io:println("First element: ", halfOpenRangeArray[0]);
    io:println("Last element: ",
    halfOpenRangeArray[arrayLength - 1]);
}

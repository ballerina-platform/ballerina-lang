public type Label int; // If the TypeHashVisitor takes in type.flags of `Label` type, there will be runtime faliures.

function testClosedIntRange(int startValue, int endValue) returns int[] {
    int[] returnArray = [];
    int returnArrayIndex = 0;
    foreach var val in startValue ... endValue {
        returnArray[returnArrayIndex] = val;
        returnArrayIndex += 1;
    }
    return returnArray;
}

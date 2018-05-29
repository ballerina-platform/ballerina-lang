function testClosedIntRange(int startValue, int endValue) returns int[] {
    int[] returnArray = [];
    int returnArrayIndex = 0;
    foreach val in startValue ... endValue {
        returnArray[returnArrayIndex] = val;
        returnArrayIndex++;
    }
    return returnArray;
}

function testHalfOpenIntRange(int startValue, int endValue) returns int[] {
    int[] returnArray = [];
    int returnArrayIndex = 0;
    foreach val in startValue ..< endValue {
        returnArray[returnArrayIndex] = val;
        returnArrayIndex++;
    }
    return returnArray;
}

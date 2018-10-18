function testClosedIntRange(int startValue, int endValue) returns int[] {
    int[] returnArray = [];
    int returnArrayIndex = 0;
    foreach val in startValue ... endValue {
        returnArray[returnArrayIndex] = val;
        returnArrayIndex += 1;
    }
    return returnArray;
}

function testClosedIntRangeAsArray(int startValue, int endValue) returns int[] {
    int[] returnArray = [];
    int returnArrayIndex = 0;
    int[] rangeAsArray = startValue ... endValue;
    foreach val in rangeAsArray {
        returnArray[returnArrayIndex] = val;
        returnArrayIndex += 1;
    }
    return returnArray;
}

function testHalfOpenIntRange(int startValue, int endValue) returns int[] {
    int[] returnArray = [];
    int returnArrayIndex = 0;
    foreach val in startValue ..< endValue {
        returnArray[returnArrayIndex] = val;
        returnArrayIndex += 1;
    }
    return returnArray;
}

function testHalfOpenIntRangeAsArray(int startValue, int endValue) returns int[] {
    int[] returnArray = [];
    int returnArrayIndex = 0;
    int[] rangeAsArray = startValue ..< endValue;
    foreach val in rangeAsArray {
        returnArray[returnArrayIndex] = val;
        returnArrayIndex += 1;
    }
    return returnArray;
}

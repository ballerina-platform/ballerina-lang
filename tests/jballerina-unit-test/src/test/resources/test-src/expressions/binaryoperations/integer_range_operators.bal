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
    object {
        public function __iterator() returns object {
                public function next () returns (record {| int value; |}?);
        };
    } rangeAsArray = startValue ... endValue;
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
    object {
            public function __iterator() returns object {
                    public function next () returns (record {| int value; |}?);
            };
        } rangeAsArray = startValue ..< endValue;
    foreach var val in rangeAsArray {
        returnArray[returnArrayIndex] = val;
        returnArrayIndex += 1;
    }
    return returnArray;
}

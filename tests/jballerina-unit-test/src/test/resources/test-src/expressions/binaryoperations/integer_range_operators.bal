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
        *object:Iterable;
        public function iterator() returns object {
                public isolated function next () returns (record {| int value; |}?);
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
            *object:Iterable;
            public function iterator() returns object {
                    public isolated function next () returns (record {| int value; |}?);
            };
        } rangeAsArray = startValue ..< endValue;
    foreach var val in rangeAsArray {
        returnArray[returnArrayIndex] = val;
        returnArrayIndex += 1;
    }
    return returnArray;
}

function testClosedIntRangeOnIntSubTypes() {
    int[] arr = [];
    int arrIndex = 0;

    int:Unsigned32 startValue1 = 12;
    int:Unsigned32 endValue1 = 14;
    object {
        *object:Iterable;
        public function iterator() returns object {
                public isolated function next () returns (record {| int value; |}?);
        };
    } rangeAsArray = startValue1 ... endValue1;
    foreach var val in rangeAsArray {
        arr[arrIndex] = val;
        arrIndex += 1;
    }
    assertEquality(arr.length(), 3);
    assertEquality(arr[0], 12);
    assertEquality(arr[1], 13);
    assertEquality(arr[2], 14);

    int startValue2 = 12;
    int:Unsigned32 endValue2 = 14;
    rangeAsArray = startValue2 ... endValue2;
    arrIndex = 0;
    arr.removeAll();
    foreach var val in rangeAsArray {
        arr[arrIndex] = val;
        arrIndex += 1;
    }
    assertEquality(arr.length(), 3);
    assertEquality(arr[0], 12);
    assertEquality(arr[1], 13);
    assertEquality(arr[2], 14);

    int:Signed32 startValue3 = 12;
    int:Signed32 endValue3 = 14;
    arrIndex = 0;
    arr.removeAll();
    foreach var val in startValue3 ... endValue3 {
        arr[arrIndex] = val;
        arrIndex += 1;
    }
    assertEquality(arr.length(), 3);
    assertEquality(arr[0], 12);
    assertEquality(arr[1], 13);
    assertEquality(arr[2], 14);

    int:Signed32 startValue4 = 12;
    int endValue4 = 14;
    arrIndex = 0;
    arr.removeAll();
    foreach var val in startValue4 ... endValue4 {
        arr[arrIndex] = val;
        arrIndex += 1;
    }
    assertEquality(arr.length(), 3);
    assertEquality(arr[0], 12);
    assertEquality(arr[1], 13);
    assertEquality(arr[2], 14);

    int:Unsigned16 startValue5 = 12;
    int endValue5 = 14;
    rangeAsArray = startValue5 ... endValue5;
    arrIndex = 0;
    arr.removeAll();
    foreach var val in rangeAsArray {
        arr[arrIndex] = val;
        arrIndex += 1;
    }
    assertEquality(arr.length(), 3);
    assertEquality(arr[0], 12);
    assertEquality(arr[1], 13);
    assertEquality(arr[2], 14);

    int startValue6 = 12;
    int:Signed16 endValue6 = 14;
    arrIndex = 0;
    arr.removeAll();
    foreach var val in startValue6 ... endValue6 {
        arr[arrIndex] = val;
        arrIndex += 1;
    }
    assertEquality(arr.length(), 3);
    assertEquality(arr[0], 12);
    assertEquality(arr[1], 13);
    assertEquality(arr[2], 14);

    int:Unsigned8 startValue7 = 12;
    int:Unsigned8 endValue7 = 14;
    rangeAsArray = startValue7 ... endValue7;
    arrIndex = 0;
    arr.removeAll();
    foreach var val in rangeAsArray {
        arr[arrIndex] = val;
        arrIndex += 1;
    }
    assertEquality(arr.length(), 3);
    assertEquality(arr[0], 12);
    assertEquality(arr[1], 13);
    assertEquality(arr[2], 14);

    int:Signed8 startValue8 = 12;
    int:Unsigned16 endValue8 = 14;
    rangeAsArray = startValue8 ... endValue8;
    arrIndex = 0;
    arr.removeAll();
    foreach var val in rangeAsArray {
        arr[arrIndex] = val;
        arrIndex += 1;
    }
    assertEquality(arr.length(), 3);
    assertEquality(arr[0], 12);
    assertEquality(arr[1], 13);
    assertEquality(arr[2], 14);

    int:Signed32 startValue9 = 12;
    int:Signed16 endValue9 = 14;
    rangeAsArray = startValue9 ... endValue9;
    arrIndex = 0;
    arr.removeAll();
    foreach var val in rangeAsArray {
        arr[arrIndex] = val;
        arrIndex += 1;
    }
    assertEquality(arr.length(), 3);
    assertEquality(arr[0], 12);
    assertEquality(arr[1], 13);
    assertEquality(arr[2], 14);

    int:Signed8 startValue10 = -6;
    int:Signed32 endValue10 = -2;
    arrIndex = 0;
    arr.removeAll();
    foreach var val in startValue10 ... endValue10 {
        arr[arrIndex] = val;
        arrIndex += 1;
    }
    assertEquality(arr.length(), 5);
    assertEquality(arr[0], -6);
    assertEquality(arr[1], -5);
    assertEquality(arr[2], -4);
    assertEquality(arr[3], -3);
    assertEquality(arr[4], -2);

    byte startValue11 = 7;
    byte endValue11 = 10;
    arrIndex = 0;
    arr.removeAll();
    foreach var val in startValue11 ... endValue11 {
        arr[arrIndex] = val;
        arrIndex += 1;
    }
    assertEquality(arr.length(), 4);
    assertEquality(arr[0], 7);
    assertEquality(arr[1], 8);
    assertEquality(arr[2], 9);
    assertEquality(arr[3], 10);

    int startValue12 = 7;
    byte endValue12 = 10;
    arrIndex = 0;
    arr.removeAll();
    foreach var val in startValue12 ... endValue12 {
        arr[arrIndex] = val;
        arrIndex += 1;
    }
    assertEquality(arr.length(), 4);
    assertEquality(arr[0], 7);
    assertEquality(arr[1], 8);
    assertEquality(arr[2], 9);
    assertEquality(arr[3], 10);

    int:Signed16|int:Unsigned32 startValue13 = 7;
    int:Signed16|int:Unsigned32 endValue13 = 10;
    arrIndex = 0;
    arr.removeAll();
    foreach var val in startValue13 ... endValue13 {
        arr[arrIndex] = val;
        arrIndex += 1;
    }
    assertEquality(arr.length(), 4);
    assertEquality(arr[0], 7);
    assertEquality(arr[1], 8);
    assertEquality(arr[2], 9);
    assertEquality(arr[3], 10);

    Ints startValue14 = 4;
    Ints endValue14 = 6;
    arrIndex = 0;
    arr.removeAll();
    foreach var val in startValue14 ... endValue14 {
        arr[arrIndex] = val;
        arrIndex += 1;
    }
    assertEquality(arr.length(), 3);
    assertEquality(arr[0], 4);
    assertEquality(arr[1], 5);
    assertEquality(arr[2], 6);
}

function testHalfOpenIntRangeOnIntSubTypes() {
    int[] arr = [];
    int arrIndex = 0;

    int:Unsigned32 startValue1 = 12;
    int endValue1 = 14;
    object {
        *object:Iterable;
        public function iterator() returns object {
                public isolated function next () returns (record {| int value; |}?);
        };
    } rangeAsArray = startValue1 ..< endValue1;
    foreach var val in rangeAsArray {
        arr[arrIndex] = val;
        arrIndex += 1;
    }
    assertEquality(arr.length(), 2);
    assertEquality(arr[0], 12);
    assertEquality(arr[1], 13);

    int:Signed8 startValue2 = 12;
    int:Unsigned16 endValue2 = 14;
    rangeAsArray = startValue2 ..< endValue2;
    arrIndex = 0;
    arr.removeAll();
    foreach var val in rangeAsArray {
        arr[arrIndex] = val;
        arrIndex += 1;
    }
    assertEquality(arr.length(), 2);
    assertEquality(arr[0], 12);
    assertEquality(arr[1], 13);

    int:Signed32 startValue3 = -12;
    int:Signed16 endValue3 = -9;
    arrIndex = 0;
    arr.removeAll();
    foreach var val in startValue3 ..< endValue3 {
        arr[arrIndex] = val;
        arrIndex += 1;
    }
    assertEquality(arr.length(), 3);
    assertEquality(arr[0], -12);
    assertEquality(arr[1], -11);
    assertEquality(arr[2], -10);

    int:Signed8 startValue4 = -12;
    int endValue4 = -9;
    arrIndex = 0;
    arr.removeAll();
    foreach var val in startValue4 ..< endValue4 {
        arr[arrIndex] = val;
        arrIndex += 1;
    }
    assertEquality(arr.length(), 3);
    assertEquality(arr[0], -12);
    assertEquality(arr[1], -11);
    assertEquality(arr[2], -10);

    byte startValue5 = 7;
    byte endValue5 = 10;
    arrIndex = 0;
    arr.removeAll();
    foreach var val in startValue5 ..< endValue5 {
        arr[arrIndex] = val;
        arrIndex += 1;
    }
    assertEquality(arr.length(), 3);
    assertEquality(arr[0], 7);
    assertEquality(arr[1], 8);
    assertEquality(arr[2], 9);

    int startValue6 = 7;
    byte endValue6 = 10;
    arrIndex = 0;
    arr.removeAll();
    foreach var val in startValue6 ..< endValue6 {
        arr[arrIndex] = val;
        arrIndex += 1;
    }
    assertEquality(arr.length(), 3);
    assertEquality(arr[0], 7);
    assertEquality(arr[1], 8);
    assertEquality(arr[2], 9);

    int:Signed16|int:Unsigned32 startValue7 = 7;
    int:Signed16|int:Unsigned32 endValue7 = 10;
    arrIndex = 0;
    arr.removeAll();
    foreach var val in startValue7 ..< endValue7 {
        arr[arrIndex] = val;
        arrIndex += 1;
    }
    assertEquality(arr.length(), 3);
    assertEquality(arr[0], 7);
    assertEquality(arr[1], 8);
    assertEquality(arr[2], 9);

    Ints startValue8 = 4;
    Ints endValue8 = 6;
    arrIndex = 0;
    arr.removeAll();
    foreach var val in startValue8 ..< endValue8 {
        arr[arrIndex] = val;
        arrIndex += 1;
    }
    assertEquality(arr.length(), 2);
    assertEquality(arr[0], 4);
    assertEquality(arr[1], 5);
}

type Ints 2|4|6;

function testIntRangeWithHexInt() {
    int[] arr = [];
    int arrIndex = 0;

    foreach var val in 0x3 ... 0x5 {
        arr[arrIndex] = val;
        arrIndex += 1;
    }
    assertEquality(arr.length(), 3);
    assertEquality(arr[0], 3);
    assertEquality(arr[1], 4);
    assertEquality(arr[2], 5);

    arrIndex = 0;
    arr.removeAll();
    foreach var val in 3 ... 0x5 {
        arr[arrIndex] = val;
        arrIndex += 1;
    }
    assertEquality(arr.length(), 3);
    assertEquality(arr[0], 3);
    assertEquality(arr[1], 4);
    assertEquality(arr[2], 5);

    arrIndex = 0;
    arr.removeAll();
    foreach var val in 0x3 ..< 0x5 {
        arr[arrIndex] = val;
        arrIndex += 1;
    }
    assertEquality(arr.length(), 2);
    assertEquality(arr[0], 3);
    assertEquality(arr[1], 4);

    arrIndex = 0;
    arr.removeAll();
    foreach var val in 0x3 ..< 5 {
        arr[arrIndex] = val;
        arrIndex += 1;
    }
    assertEquality(arr.length(), 2);
    assertEquality(arr[0], 3);
    assertEquality(arr[1], 4);
}

type AssertionError distinct error;

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error AssertionError(ASSERTION_ERROR_REASON,
            message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}

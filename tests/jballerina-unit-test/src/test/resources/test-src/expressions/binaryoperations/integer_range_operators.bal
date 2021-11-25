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
    arr.removeAll();
    foreach var val in startValue11 ... endValue11 {
        arr.push(val);
    }
    assertEquality(arr.length(), 4);
    assertEquality(arr[0], 7);
    assertEquality(arr[1], 8);
    assertEquality(arr[2], 9);
    assertEquality(arr[3], 10);

    int startValue12 = 7;
    byte endValue12 = 10;
    arr.removeAll();
    foreach var val in startValue12 ... endValue12 {
        arr.push(val);
    }
    assertEquality(arr.length(), 4);
    assertEquality(arr[0], 7);
    assertEquality(arr[1], 8);
    assertEquality(arr[2], 9);
    assertEquality(arr[3], 10);

    int:Signed16|int:Unsigned32 startValue13 = 7;
    int:Signed16|int:Unsigned32 endValue13 = 10;
    arr.removeAll();
    foreach var val in startValue13 ... endValue13 {
        arr.push(val);
    }
    assertEquality(arr.length(), 4);
    assertEquality(arr[0], 7);
    assertEquality(arr[1], 8);
    assertEquality(arr[2], 9);
    assertEquality(arr[3], 10);

    IntsV1 startValue14 = 4;
    IntsV1 endValue14 = 6;
    arr.removeAll();
    foreach var val in startValue14 ... endValue14 {
        arr.push(val);
    }
    assertEquality(arr.length(), 3);
    assertEquality(arr[0], 4);
    assertEquality(arr[1], 5);
    assertEquality(arr[2], 6);

    IntsV3 startValue15 = 1;
    byte|IntsV2 endValue15 = 3;
    arr.removeAll();
    foreach var val in startValue15 ... endValue15 {
        arr.push(val);
    }
    assertEquality(arr.length(), 3);
    assertEquality(arr[0], 1);
    assertEquality(arr[1], 2);
    assertEquality(arr[2], 3);

//    int:Signed32 startValue16 = +254;
//    byte|IntsV3 endValue16 = 256;
//    arr.removeAll();
//    foreach var val in startValue16 ... endValue16 {
//        arr.push(val);
//    }
//    assertEquality(arr.length(), 3);
//    assertEquality(arr[0], 254);
//    assertEquality(arr[1], 255);
//    assertEquality(arr[2], 256);

    IntsV5 startValue17 = -4;
    -2 endValue17 = -2;
    arr.removeAll();
    foreach var val in startValue17 ... endValue17 {
        arr.push(val);
    }
    assertEquality(arr.length(), 3);
    assertEquality(arr[0], -4);
    assertEquality(arr[1], -3);
    assertEquality(arr[2], -2);
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
    arr.removeAll();
    foreach var val in startValue5 ..< endValue5 {
        arr.push(val);
    }
    assertEquality(arr.length(), 3);
    assertEquality(arr[0], 7);
    assertEquality(arr[1], 8);
    assertEquality(arr[2], 9);

    int startValue6 = 7;
    byte endValue6 = 10;
    arr.removeAll();
    foreach var val in startValue6 ..< endValue6 {
        arr.push(val);
    }
    assertEquality(arr.length(), 3);
    assertEquality(arr[0], 7);
    assertEquality(arr[1], 8);
    assertEquality(arr[2], 9);

    int:Signed16|int:Unsigned32 startValue7 = 7;
    int:Signed16|int:Unsigned32 endValue7 = 10;
    arr.removeAll();
    foreach var val in startValue7 ..< endValue7 {
        arr.push(val);
    }
    assertEquality(arr.length(), 3);
    assertEquality(arr[0], 7);
    assertEquality(arr[1], 8);
    assertEquality(arr[2], 9);

    IntsV1 startValue8 = 4;
    IntsV1 endValue8 = 6;
    arr.removeAll();
    foreach var val in startValue8 ..< endValue8 {
        arr.push(val);
    }
    assertEquality(arr.length(), 2);
    assertEquality(arr[0], 4);
    assertEquality(arr[1], 5);

    IntsV3 startValue9 = 1;
    byte|IntsV2 endValue9 = 3;
    arr.removeAll();
    foreach var val in startValue9 ..< endValue9 {
        arr.push(val);
    }
    assertEquality(arr.length(), 2);
    assertEquality(arr[0], 1);
    assertEquality(arr[1], 2);

//    int:Signed32 startValue10 = +254;
//    byte|IntsV3 endValue10 = 256;
//    arr.removeAll();
//    foreach var val in startValue10 ..< endValue10 {
//        arr.push(val);
//    }
//    assertEquality(arr.length(), 2);
//    assertEquality(arr[0], 254);
//    assertEquality(arr[1], 255);

    IntsV5 startValue11 = -4;
    -2 endValue11 = -2;
    arr.removeAll();
    foreach var val in startValue11 ..< endValue11 {
        arr.push(val);
    }
    assertEquality(arr.length(), 2);
    assertEquality(arr[0], -4);
    assertEquality(arr[1], -3);
}

type IntsV1 2|4|6|8;

type IntsV2 256|257;

type IntsV3 IntsV2|1|2;

type IntsV4 -2|-4|-6|-8;

type IntsV5 IntsV1|IntsV4;

function testIntRangeWithHexIntLiterals() {
    int[] arr = [];

    foreach var val in 0x3 ... 0x5 {
        arr.push(val);
    }
    assertEquality(arr.length(), 3);
    assertEquality(arr[0], 3);
    assertEquality(arr[1], 4);
    assertEquality(arr[2], 5);

    arr.removeAll();
    foreach var val in 3 ... 0x5 {
        arr.push(val);
    }
    assertEquality(arr.length(), 3);
    assertEquality(arr[0], 3);
    assertEquality(arr[1], 4);
    assertEquality(arr[2], 5);

    arr.removeAll();
    foreach var val in 0x3 ..< 0x5 {
        arr.push(val);
    }
    assertEquality(arr.length(), 2);
    assertEquality(arr[0], 3);
    assertEquality(arr[1], 4);

    arr.removeAll();
    foreach var val in 0x3 ..< 5 {
        arr.push(val);
    }
    assertEquality(arr.length(), 2);
    assertEquality(arr[0], 3);
    assertEquality(arr[1], 4);
}

type IsolatedIterable isolated object {
    public isolated function iterator() returns isolated object {
        public isolated function next() returns record {| int value; |}?;
    };
};

function testIsolatednessOfRangeExprIterableAndIterator() {
    object {} a = 1 ... 2;
    assertTrue(a is object:Iterable);
    assertTrue(a is IsolatedIterable);
    assertFalse(a !is IsolatedIterable);

    int i = 1;
    int j = 5;

    isolated object {
        public isolated function iterator() returns isolated object {
            public isolated function next() returns record {| int value; |}?;
        };
    } _ = i ... j;

    any b = i ..< j;
    assertTrue(b is object:Iterable);
    assertTrue(b is IsolatedIterable);
    assertFalse(b !is IsolatedIterable);
    isolated object {
        public isolated function iterator() returns isolated object {
            public isolated function next() returns record {| int value; |}?;
        };
    } _ = 1 ..< 2;
}

type AssertionError distinct error;

const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(any|error actual) {
    assertEquality(true, actual);
}

function assertFalse(any|error actual) {
    assertEquality(false, actual);
}

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

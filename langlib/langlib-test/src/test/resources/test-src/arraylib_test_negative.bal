function testPushOnFixedLengthArray() {
    int[1] fixedLengthArray = [1];
    fixedLengthArray.push(4);
}

function testPushOnFixedLengthTuple() {
    [int, int] fixedLengthTuple = [1, 2];
    fixedLengthTuple.push(4);
}

function testPopOnFixedLengthArray() {
    int[1] fixedLengthArray = [1];
    int popped = fixedLengthArray.pop();
}

function testPopOnFixedLengthTuple() {
    [int, int] fixedLengthTuple = [1, 2];
    int popped = fixedLengthTuple.pop();
}

function testShift() returns [int[], int] {
    int[] s = [1, 2, 3, 4, 5];
    var e = s.shift();
    return [s, e];
}

function testShiftOnFixedLengthArray() {
    int[1] fixedLengthArray = [1];
    int x = fixedLengthArray.shift();
}

function testUnShiftOnFixedLengthArray() {
    int[1] fixedLengthArray = [1];
    fixedLengthArray.unshift(5);
}

function testShiftOnFixedLengthTuple() {
    [int, int] fixedLengthTuple = [1, 2];
    int popped = fixedLengthTuple.shift();
}

function testUnShiftOnFixedLengthTuple() {
    [int, int] fixedLengthTuple = [1, 2];
    fixedLengthTuple.unshift();
}

// inferred fixed length arrays
function testPushPopShiftUnshitOnInferredFixedLengthArray() {
    int[*] fixedLengthArray = [1, 2];
    fixedLengthArray.push(4);
    int x = fixedLengthArray.pop();
    x = fixedLengthArray.shift();
    fixedLengthArray.unshift();
}

function testPushOnFixedLengthArrayUnions() {
    int[1]|float[1] fixedLengthArray = <int[1]> [1];
    fixedLengthArray.push(4);
}

function testPushOnFixedLengthTupleUnion() {
    [int, int][1] | [float, float][1] fixedLengthArray = <[float, float][1]> [[1.0, 2.3]];
    fixedLengthArray.push(<[float, float]>[1, 2]);
}

// run time panic no compile time error
function testPushOnFixedLengthAndDynamicTupleUnion() {
    [int, int][1] | [float, float][] fixedLengthArray = <[float, float][1]> [[1, 2]];
    fixedLengthArray.push(<[float, float]>[1, 2]);
}

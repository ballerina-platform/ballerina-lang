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

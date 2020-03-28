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

public function main() {
    testPushOnFixedLengthArray();
}

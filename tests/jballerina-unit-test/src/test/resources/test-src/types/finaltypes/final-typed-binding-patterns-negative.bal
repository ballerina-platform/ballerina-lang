type Data record {
    int b;
    string s;
};

type Data2 record {
    int a;
    Data d;
};

function getRecord() returns Data {
    Data d = { b: 1, s: "A" };
    return d;
}

function getNestedRecord() returns Data2 {
    Data d = { b: 1, s: "A" };
    Data2 d2 = { a: 2, d: d };
    return d2;
}

function getTuple() returns [int, string] {
    return [1, "A"];
}

function getNestedTuple() returns [int, [string, float]] {
    return [1, ["A", 5.6]];
}

// ---------------------------------------------------------------------------------------------------------------------

function testFinalRecordVariableWithoutType() {
    final var { b, s: j } = getRecord();
    b = 2;
    j = "B";
}

function testFinalRecordVariableWithType() {
    final Data { b, s: j } = getRecord();
    b = 2;
    j = "B";
}

// ---------------------------------------------------------------------------------------------------------------------

function testFinalNestedRecordVariableWithType1() {
    final var { a, d: {b, s} } = getNestedRecord();
    a = 2;
    s = "B";
}

function testFinalNestedRecordVariableWithType2() {
    final Data2 { a, d: { b, s } } = getNestedRecord();
    a = 2;
    b = 5;
    s = "fd";
}

// ---------------------------------------------------------------------------------------------------------------------

function testFinalTupleVariableWithoutType() {
    final var [i, j]= getTuple();
    i = 2;
    j = "B";
}

function testFinalTupleVariableWithType() {
    final [int, string] [i, j] = getTuple();
    i = 2;
    j = "B";
}

// ---------------------------------------------------------------------------------------------------------------------

function testFinalNestedTupleVariableWithoutType() {
    final var [i, j] = getNestedTuple();
    i = 2;
    j = ["C", 1.4];
}

function testFinalNestedTupleVariableWithType() {
    final [int, [string, float]] [i, [j, k]] = getNestedTuple();
    i = 2;
    j = "B";
    k = 3.4;
}

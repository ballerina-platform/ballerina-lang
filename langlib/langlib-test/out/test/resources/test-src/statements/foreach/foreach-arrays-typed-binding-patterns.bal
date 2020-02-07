type Data record {
    int i;
    string v;
};

type Data2 record {
    int i;
    Data v;
};

type Data3 record {
    int i;
    [int, string] v;
};

// ---------------------------------------------------------------------------------------------------------------------

function testArrayWithSimpleVariableWithoutType() returns string {
    string output = "";

    string[] arr = ["A", "B", "C"];

    int i = 0;
    foreach var v in arr {
        output = output + i.toString() + ":" + v + " ";
        i += 1;
    }
    return output;
}

function testArrayWithSimpleVariableWithType() returns string {
    string output = "";

    string[] arr = ["A", "B", "C"];

    int i = 0;
    foreach string v in arr {
        output = output + i.toString() + ":" + v + " ";
        i += 1;
    }
    return output;
}

function testArrayWithTupleWithoutType() returns string {
    string output = "";

    [int, string][] arr = [[1, "A"], [2, "B"], [3, "C"]];

    foreach var [i, v] in arr {
        output = output + i.toString() + ":" + v + " ";
    }
    return output;
}

function testArrayWithTupleWithType() returns string {
    string output = "";

    [int, string][] arr = [[1, "A"], [2, "B"], [3, "C"]];

    foreach [int, string] [i, v] in arr {
        output = output + i.toString() + ":" + v + " ";
    }
    return output;
}

function testArrayWithTupleInTupleWithoutType() returns string {
    string output = "";

    [int, [string, float]][] arr = [[1, ["A", 2.0]], [2, ["B", 3.0]], [3, ["C", 4.0]]];

    foreach var [i, [s, f]] in arr {
        output = output + i.toString() + ":" + s + ":" + f.toString() + " ";
    }
    return output;
}

function testArrayWithTupleInTupleWithType() returns string {
    string output = "";

    [int, [string, float]][] arr = [[1, ["A", 2.0]], [2, ["B", 3.0]], [3, ["C", 4.0]]];

    foreach [int, [string, float]] [i, [s, f]] in arr {
        output = output + i.toString() + ":" + s + ":" + f.toString() + " ";
    }
    return output;
}

function testArrayWithRecordInTupleWithoutType() returns string {
    string output = "";

    Data d1 = { i: 1, v: "A" };
    Data d2 = { i: 2, v: "B" };
    Data d3 = { i: 3, v: "C" };

    [int, Data][] arr = [[1, d1], [2, d2], [3, d3]];

    foreach var [i, {i: j, v: k}] in arr {
        output = output + i.toString() + ":" + i.toString() + ":" + k.toString() + " ";
    }
    return output;
}

function testArrayWithRecordInTupleWithType() returns string {
    string output = "";

    Data d1 = { i: 1, v: "A" };
    Data d2 = { i: 2, v: "B" };
    Data d3 = { i: 3, v: "C" };

    [int, Data][] arr = [[1, d1], [2, d2], [3, d3]];

    foreach [int, Data] [i, {i: j, v: k}] in arr {
        output = output + i.toString() + ":" + i.toString() + ":" + k.toString() + " ";
    }
    return output;
}

function testArrayWithRecordWithoutType() returns string {
    string output = "";

    Data d1 = { i: 1, v: "A" };
    Data d2 = { i: 2, v: "B" };
    Data d3 = { i: 3, v: "C" };

    Data[] arr = [d1, d2, d3];

    foreach var {i, v} in arr {
        output = output + i.toString() + ":" + v + " ";
    }
    return output;
}

function testArrayWithRecordWithType() returns string {
    string output = "";

    Data d1 = { i: 1, v: "A" };
    Data d2 = { i: 2, v: "B" };
    Data d3 = { i: 3, v: "C" };

    Data[] arr = [d1, d2, d3];

    foreach Data {i, v} in arr {
        output = output + i.toString() + ":" + v + " ";
    }
    return output;
}

function testArrayWithRecordInRecordWithoutType() returns string {
    string output = "";

    Data d11 = { i: 1, v: "A" };
    Data d12 = { i: 2, v: "B" };
    Data d13 = { i: 3, v: "C" };

    Data2 d21 = { i: 1, v: d11 };
    Data2 d22 = { i: 2, v: d12 };
    Data2 d23 = { i: 3, v: d13 };

    Data2[] arr = [d21, d22, d23];

    foreach var {i, v: {i: j, v: k}} in arr {
        output = output + i.toString() + ":" + i.toString() + ":" + k.toString() + " ";
    }
    return output;
}

function testArrayWithRecordInRecordWithType() returns string {
    string output = "";

    Data d11 = { i: 1, v: "A" };
    Data d12 = { i: 2, v: "B" };
    Data d13 = { i: 3, v: "C" };

    Data2 d21 = { i: 1, v: d11 };
    Data2 d22 = { i: 2, v: d12 };
    Data2 d23 = { i: 3, v: d13 };

    Data2[] arr = [d21, d22, d23];

    foreach Data2 {i, v: {i: j, v: k}} in arr {
        output = output + i.toString() + ":" + i.toString() + ":" + k.toString() + " ";
    }
    return output;
}

function testArrayWithTupleInRecordWithoutType() returns string {
    string output = "";

    Data3 d1 = { i: 1, v: [1, "A"] };
    Data3 d2 = { i: 2, v: [2, "B"] };
    Data3 d3 = { i: 3, v: [3, "C"] };

    Data3[] arr = [d1, d2, d3];

    foreach var {i, v: [j, k]} in arr {
        output = output + i.toString() + ":" + i.toString() + ":" + k.toString() + " ";
    }
    return output;
}

function testArrayWithTupleInRecordWithType() returns string {
    string output = "";

    Data3 d1 = { i: 1, v: [1, "A"] };
    Data3 d2 = { i: 2, v: [2, "B"] };
    Data3 d3 = { i: 3, v: [3, "C"] };

    Data3[] arr = [d1, d2, d3];

    foreach Data3 {i, v: [j, k]} in arr {
        output = output + i.toString() + ":" + i.toString() + ":" + k.toString() + " ";
    }
    return output;
}

// ---------------------------------------------------------------------------------------------------------------------

function testEmptyArrayIteration() returns string {
    string output = "";

    string[] arr = [];

    int i = 0;
    foreach var v in arr {
        output = output + i.toString() + ":" + v + " ";
        i += 1;
    }
    return output;
}

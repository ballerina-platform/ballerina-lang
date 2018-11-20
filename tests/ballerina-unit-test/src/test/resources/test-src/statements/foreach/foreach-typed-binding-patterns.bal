string output = "";

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
    (int, string) v;
};

function concatIntString(int i, string s) {
    output = output + i + ":" + s + " ";
}

function concatIntStringFloat(int i, string s, float f) {
    output = output + i + ":" + s + ":" + f + " ";
}

function concatIntIntString(int i1, int i2, string s) {
    output = output + i1+ ":" + i2 + ":" + s + " ";
}

// Test array -------------------------------------------------------------------------------------

function testArrayWithSimpleVariable() returns string {
    output = "";

    string[] arr = ["A", "B", "C"];
    int i = 0;
    foreach var v in arr {
        concatIntString(i, v);
        i += 1;
    }
    return output;
}

function testArrayWithTuple() returns string {
    output = "";

    (int, string)[] arr = [(1, "A"), (2, "B"), (3, "C")];
    foreach var (i, v) in arr {
        concatIntString(i, v);
    }
    return output;
}

function testArrayWithTupleInTuple() returns string {
    output = "";

    (int, (string, float))[] arr = [(1, ("A", 2.0)), (2, ("B", 3.0)), (3, ("C", 4.0))];
    foreach var (i, (s, f)) in arr {
        concatIntStringFloat(i, s, f);
    }
    return output;
}

function testArrayWithRecordInTuple() returns string {
    output = "";

    Data d1 = { i: 1, v: "A" };
    Data d2 = { i: 2, v: "B" };
    Data d3 = { i: 3, v: "C" };
    (int, Data)[] arr = [(1, d1), (2, d2), (3, d3)];
    foreach var (i, {i: j, v: k}) in arr {
        concatIntIntString(i, j, k);
    }
    return output;
}

function testArrayWithRecord() returns string {
    output = "";

    Data d1 = { i: 1, v: "A" };
    Data d2 = { i: 2, v: "B" };
    Data d3 = { i: 3, v: "C" };
    Data[] arr = [d1, d2, d3];
    foreach var {i, v} in arr {
        concatIntString(i, v);
    }
    return output;
}

function testArrayWithRecordInRecord() returns string {
    output = "";

    Data d11 = { i: 1, v: "A" };
    Data d12 = { i: 2, v: "B" };
    Data d13 = { i: 3, v: "C" };

    Data2 d21 = { i: 1, v: d11 };
    Data2 d22 = { i: 2, v: d12 };
    Data2 d23 = { i: 3, v: d13 };

    Data2[] arr = [d21, d22, d23];
    foreach var {i, v: {i: j, v: k}} in arr {
        concatIntIntString(i, j, k);
    }
    return output;
}

function testArrayWithTupleInRecord() returns string {
    output = "";

    Data3 d1 = { i: 1, v: (1, "A") };
    Data3 d2 = { i: 2, v: (2, "B") };
    Data3 d3 = { i: 3, v: (3, "C") };

    Data3[] arr = [d1, d2, d3];
    foreach var {i, v: (j, k)} in arr {
        concatIntIntString(i, j, k);
    }
    return output;
}

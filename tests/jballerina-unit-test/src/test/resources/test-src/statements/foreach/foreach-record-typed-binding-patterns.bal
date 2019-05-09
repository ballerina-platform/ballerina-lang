string output = "";

type Data1 record {
    int i;
    string s;
    float f;
};

type Data2 record {
    int i;
    string s;
    Data1 d;
};

type Data3 record {
    int i;
    (int, string) t;
};

type Data4 record {

};

function concatIntStringAny(int i, string s, any a) {
    output = output + i + ":" + s + ":" + string.convert(a) + " ";
}

// ---------------------------------------------------------------------------------------------------------------------

function testSimpleRecordWithoutType() returns string {
    output = "";

    Data1 d = { i: 1, s: "A", f: 1.0 };

    int i = 0;
    foreach var (k, v) in d {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

function testSimpleRecordWithType() returns string {
    output = "";

    Data1 d = { i: 1, s: "A", f: 1.0 };

    int i = 0;
    foreach (string, any) (k, v) in d {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

// ---------------------------------------------------------------------------------------------------------------------

function testRecordInRecordWithoutType() returns string {
    output = "";

    Data1 d1 = { i: 1, s: "A", f: 1.0 };
    Data2 d2 = { i: 2, s: "B", d: d1 };

    int i = 0;
    foreach var (k, v) in d2 {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

function testRecordInRecordWithType() returns string {
    output = "";

    Data1 d1 = { i: 1, s: "A", f: 1.0 };
    Data2 d2 = { i: 2, s: "B", d: d1 };

    int i = 0;
    foreach (string, any) (k, v) in d2 {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

// ---------------------------------------------------------------------------------------------------------------------

function testTupleInRecordWithoutType() returns string {
    output = "";

    Data3 d3 = { i: 1, t: (2, "A") };

    int i = 0;
    foreach var (k, v) in d3 {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

function testTupleInRecordWithType() returns string {
    output = "";

    Data3 d3 = { i: 1, t: (2, "A") };

    int i = 0;
    foreach (string, any) (k, v) in d3 {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

// ---------------------------------------------------------------------------------------------------------------------

function testEmptyRecordIteration() returns string {
    output = "";

    Data4 d = {};

    int i = 0;
    foreach var (k, v) in d {
        concatIntStringAny(i, k, <any> v);
        i += 1;
    }
    return output;
}

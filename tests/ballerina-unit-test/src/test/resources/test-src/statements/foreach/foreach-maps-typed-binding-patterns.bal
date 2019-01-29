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
    output = output + i1 + ":" + i2 + ":" + s + " ";
}

function concatStringIntString(int s1, int i, string s2) {
    output = output + s1 + ":" + i + ":" + s2 + " ";
}

function concatIntStringAny(int i, string s, any a) {
    output = output + i + ":" + s + ":" + string.convert(a) + " ";
}

function concatIntStringIntString(int i1, string s1, int i2, string s2) {
    output = output + i1 + ":" + s1 + ":" + i2 + ":" + s2 + " ";
}

function concatIntStringIntStringFloat(int i1, string s1, int i2, string s2, float f) {
    output = output + i1 + ":" + s1 + ":" + i2 + ":" + s2 + ":" + f + " ";
}

function concatIntStringIntIntString(int i1, string s1, int i2, int i3, string s2) {
    output = output + i1 + ":" + s1 + ":" + i2 + ":" + i3 + ":" + s2 + " ";
}

function concatIntStringAnyIntString(int i1, string s1, any a, int i2, string s2) {
    output = output + i1 + ":" + s1 + ":" + string.convert(a) + ":" + i2 + ":" + s2 + " ";
}

// ---------------------------------------------------------------------------------------------------------------------

function testUnconstrainedMapWithoutType() returns string {
    output = "";

    map<any> m = { a: "A", b: "B", c: "C" };

    int i = 0;
    foreach var (k, v) in m {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

function testUnconstrainedMapWithType() returns string {
    output = "";

    map<any> m = { a: "A", b: "B", c: "C" };

    int i = 0;
    foreach (string, any) (k, v) in m {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithoutType() returns string {
    output = "";

    map<anydata> m = { a: "A", b: "B", c: "C" };

    int i = 0;
    foreach var (k, v) in m {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithType() returns string {
    output = "";

    map<anydata> m = { a: "A", b: "B", c: "C" };

    int i = 0;
    foreach (string, anydata) (k, v) in m {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithAnyType() returns string {
    output = "";

    map<anydata> m = { a: "A", b: "B", c: "C" };

    int i = 0;
    foreach (string, any) (k, v) in m {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

// ---------------------------------------------------------------------------------------------------------------------

function testUnconstrainedMapWithTupleWithoutType() returns string {
    output = "";

    (int, string) t1 = (1, "A");
    (int, string) t2 = (2, "B");
    (int, string) t3 = (3, "C");

    map<any> m = { a: t1, b: t2, c: t3 };

    int i = 0;
    foreach var (k, v) in m {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

function testUnconstrainedMapWithTupleWithType() returns string {
    output = "";

    (int, string) t1 = (1, "A");
    (int, string) t2 = (2, "B");
    (int, string) t3 = (3, "C");

    map<any> m = { a: t1, b: t2, c: t3 };

    int i = 0;
    foreach (string, any) (k, v) in m {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithTupleWithoutType() returns string {
    output = "";

    (int, string) t1 = (1, "A");
    (int, string) t2 = (2, "B");
    (int, string) t3 = (3, "C");

    map<(int, string)> m = { a: t1, b: t2, c: t3 };

    int i = 0;
    foreach var (k, (u, v)) in m {
        concatIntStringIntString(i, k, u, v);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithTupleWithType() returns string {
    output = "";

    (int, string) t1 = (1, "A");
    (int, string) t2 = (2, "B");
    (int, string) t3 = (3, "C");

    map<(int, string)> m = { a: t1, b: t2, c: t3 };

    int i = 0;
    foreach (string, (int, string)) (k, (u, v)) in m {
        concatIntStringIntString(i, k, u, v);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithTupleWithAnyType() returns string {
    output = "";

    (int, string) t1 = (1, "A");
    (int, string) t2 = (2, "B");
    (int, string) t3 = (3, "C");

    map<(int, string)> m = { a: t1, b: t2, c: t3 };

    int i = 0;
    foreach (string, any) (k, v) in m {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

// ---------------------------------------------------------------------------------------------------------------------

function testUnconstrainedMapWithTupleInTupleWithoutType() returns string {
    output = "";

    (int, (string, float)) t1 = (1, ("A", 2.0));
    (int, (string, float)) t2 = (2, ("B", 3.0));
    (int, (string, float)) t3 = (3, ("C", 4.0));

    map<any> m = { a: t1, b: t2, c: t3 };

    int i = 0;
    foreach var (k, v) in m {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

function testUnconstrainedMapWithTupleInTupleWithType() returns string {
    output = "";

    (int, (string, float)) t1 = (1, ("A", 2.0));
    (int, (string, float)) t2 = (2, ("B", 3.0));
    (int, (string, float)) t3 = (3, ("C", 4.0));

    map<any> m = { a: t1, b: t2, c: t3 };

    int i = 0;
    foreach (string, any) (k, v) in m {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithTupleInTupleWithoutType() returns string {
    output = "";

    (int, (string, float)) t1 = (1, ("A", 2.0));
    (int, (string, float)) t2 = (2, ("B", 3.0));
    (int, (string, float)) t3 = (3, ("C", 4.0));

    map<(int, (string, float))> m = { a: t1, b: t2, c: t3 };

    int i = 0;
    foreach var (k, (u, (v, w))) in m {
        concatIntStringIntStringFloat(i, k, u, v, w);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithTupleInTupleWithType() returns string {
    output = "";

    (int, (string, float)) t1 = (1, ("A", 2.0));
    (int, (string, float)) t2 = (2, ("B", 3.0));
    (int, (string, float)) t3 = (3, ("C", 4.0));

    map<(int, (string, float))> m = { a: t1, b: t2, c: t3 };

    int i = 0;
    foreach (string, (int, (string, float))) (k, (u, (v, w))) in m {
        concatIntStringIntStringFloat(i, k, u, v, w);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithTupleInTupleWithAnyType() returns string {
    output = "";

    (int, (string, float)) t1 = (1, ("A", 2.0));
    (int, (string, float)) t2 = (2, ("B", 3.0));
    (int, (string, float)) t3 = (3, ("C", 4.0));

    map<(int, (string, float))> m = { a: t1, b: t2, c: t3 };

    int i = 0;
    foreach (string, any) (k, v) in m {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

// ---------------------------------------------------------------------------------------------------------------------

function testUnconstrainedMapWithRecordInTupleWithoutType() returns string {
    output = "";

    Data d1 = { i: 1, v: "A" };
    Data d2 = { i: 2, v: "B" };
    Data d3 = { i: 3, v: "C" };

    (int, Data) t1 = (2, d1);
    (int, Data) t2 = (3, d2);
    (int, Data) t3 = (4, d3);

    map<any> m = { a: t1, b: t2, c: t3 };

    int i = 0;
    foreach var (k, v) in m {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

function testUnconstrainedMapWithRecordInTupleWithType() returns string {
    output = "";

    Data d1 = { i: 1, v: "A" };
    Data d2 = { i: 2, v: "B" };
    Data d3 = { i: 3, v: "C" };

    (int, Data) t1 = (2, d1);
    (int, Data) t2 = (3, d2);
    (int, Data) t3 = (4, d3);

    map<any> m = { a: t1, b: t2, c: t3 };

    int i = 0;
    foreach (string, any) (k, v) in m {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithRecordInTupleWithoutType() returns string {
    output = "";

    Data d1 = { i: 1, v: "A" };
    Data d2 = { i: 2, v: "B" };
    Data d3 = { i: 3, v: "C" };

    (int, Data) t1 = (2, d1);
    (int, Data) t2 = (3, d2);
    (int, Data) t3 = (4, d3);

    map<(int, Data)> m = { a: t1, b: t2, c: t3 };

    int i = 0;
    foreach var (k, (u, {i: v, v: w})) in m {
        concatIntStringIntIntString(i, k, u, v, w);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithRecordInTupleWithType() returns string {
    output = "";

    Data d1 = { i: 1, v: "A" };
    Data d2 = { i: 2, v: "B" };
    Data d3 = { i: 3, v: "C" };

    (int, Data) t1 = (2, d1);
    (int, Data) t2 = (3, d2);
    (int, Data) t3 = (4, d3);

    map<(int, Data)> m = { a: t1, b: t2, c: t3 };

    int i = 0;
    foreach (string, (int, Data)) (k, (u, {i: v, v: w})) in m {
        concatIntStringIntIntString(i, k, u, v, w);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithRecordInTupleWithAnyType() returns string {
    output = "";

    Data d1 = { i: 1, v: "A" };
    Data d2 = { i: 2, v: "B" };
    Data d3 = { i: 3, v: "C" };

    (int, Data) t1 = (2, d1);
    (int, Data) t2 = (3, d2);
    (int, Data) t3 = (4, d3);

    map<(int, Data)> m = { a: t1, b: t2, c: t3 };

    int i = 0;
    foreach (string, any) (k, v) in m {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

// ---------------------------------------------------------------------------------------------------------------------

function testUnconstrainedMapWithRecordWithoutType() returns string {
    output = "";

    Data d1 = { i: 1, v: "A" };
    Data d2 = { i: 2, v: "B" };
    Data d3 = { i: 3, v: "C" };

    map<any> m = { a: d1, b: d2, c: d3 };

    int i = 0;
    foreach var (k, v) in m {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

function testUnconstrainedMapWithRecordWithType() returns string {
    output = "";

    Data d1 = { i: 1, v: "A" };
    Data d2 = { i: 2, v: "B" };
    Data d3 = { i: 3, v: "C" };

    map<any> m = { a: d1, b: d2, c: d3 };

    int i = 0;
    foreach (string, any) (k, v) in m {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithRecordWithoutType() returns string {
    output = "";

    Data d1 = { i: 1, v: "A" };
    Data d2 = { i: 2, v: "B" };
    Data d3 = { i: 3, v: "C" };

    map<Data> m = { a: d1, b: d2, c: d3 };

    int i = 0;
    foreach var (k, {i: u, v}) in m {
        concatIntStringIntString(i, k, u, v);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithRecordWithType() returns string {
    output = "";

    Data d1 = { i: 1, v: "A" };
    Data d2 = { i: 2, v: "B" };
    Data d3 = { i: 3, v: "C" };

    map<Data> m = { a: d1, b: d2, c: d3 };

    int i = 0;
    foreach (string, Data) (k, {i: u, v}) in m {
        concatIntStringIntString(i, k, u, v);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithRecordWithAnyType() returns string {
    output = "";

    Data d1 = { i: 1, v: "A" };
    Data d2 = { i: 2, v: "B" };
    Data d3 = { i: 3, v: "C" };

    map<Data> m = { a: d1, b: d2, c: d3 };

    int i = 0;
    foreach (string, any) (k, v) in m {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

// ---------------------------------------------------------------------------------------------------------------------

function testUnconstrainedMapWithRecordInRecordWithoutType() returns string {
    output = "";

    Data d11 = { i: 1, v: "A" };
    Data d12 = { i: 2, v: "B" };
    Data d13 = { i: 3, v: "C" };

    Data2 d21 = { i: 2, v: d11 };
    Data2 d22 = { i: 3, v: d12 };
    Data2 d23 = { i: 4, v: d13 };

    map<any> m = { a: d21, b: d22, c: d23 };

    int i = 0;
    foreach var (k, v) in m {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

function testUnconstrainedMapWithRecordInRecordWithType() returns string {
    output = "";

    Data d11 = { i: 1, v: "A" };
    Data d12 = { i: 2, v: "B" };
    Data d13 = { i: 3, v: "C" };

    Data2 d21 = { i: 2, v: d11 };
    Data2 d22 = { i: 3, v: d12 };
    Data2 d23 = { i: 4, v: d13 };

    map<any> m = { a: d21, b: d22, c: d23 };

    int i = 0;
    foreach (string, any) (k, v) in m {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithRecordInRecordWithoutType() returns string {
    output = "";

    Data d11 = { i: 1, v: "A" };
    Data d12 = { i: 2, v: "B" };
    Data d13 = { i: 3, v: "C" };

    Data2 d21 = { i: 2, v: d11 };
    Data2 d22 = { i: 3, v: d12 };
    Data2 d23 = { i: 4, v: d13 };

    map<Data2> m = { a: d21, b: d22, c: d23 };

    int i = 0;
    foreach var (k, {i: u, v: {i: v, v: w}}) in m {
        concatIntStringIntIntString(i, k, u, v, w);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithRecordInRecordWithType() returns string {
    output = "";

    Data d11 = { i: 1, v: "A" };
    Data d12 = { i: 2, v: "B" };
    Data d13 = { i: 3, v: "C" };

    Data2 d21 = { i: 2, v: d11 };
    Data2 d22 = { i: 3, v: d12 };
    Data2 d23 = { i: 4, v: d13 };

    map<Data2> m = { a: d21, b: d22, c: d23 };

    int i = 0;
    foreach (string, Data2) (k, {i: u, v: {i: v, v: w}}) in m {
        concatIntStringIntIntString(i, k, u, v, w);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithRecordInRecordWithAnyType() returns string {
    output = "";

    Data d11 = { i: 1, v: "A" };
    Data d12 = { i: 2, v: "B" };
    Data d13 = { i: 3, v: "C" };

    Data2 d21 = { i: 2, v: d11 };
    Data2 d22 = { i: 3, v: d12 };
    Data2 d23 = { i: 4, v: d13 };

    map<Data2> m = { a: d21, b: d22, c: d23 };

    int i = 0;
    foreach (string, any) (k, v) in m {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

// ---------------------------------------------------------------------------------------------------------------------

function testUnconstrainedMapWithTupleInRecordWithoutType() returns string {
    output = "";

    Data3 d1 = { i: 1, v: (1, "A") };
    Data3 d2 = { i: 2, v: (2, "B") };
    Data3 d3 = { i: 3, v: (3, "C") };

    map<any> m = { a: d1, b: d2, c: d3 };

    int i = 0;
    foreach var (k, v) in m {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

function testUnconstrainedMapWithTupleInRecordWithType() returns string {
    output = "";

    Data3 d1 = { i: 1, v: (1, "A") };
    Data3 d2 = { i: 2, v: (2, "B") };
    Data3 d3 = { i: 3, v: (3, "C") };

    map<any> m = { a: d1, b: d2, c: d3 };

    int i = 0;
    foreach (string, any) (k, v) in m {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithTupleInRecordWithoutType() returns string {
    output = "";

    Data3 d1 = { i: 1, v: (1, "A") };
    Data3 d2 = { i: 2, v: (2, "B") };
    Data3 d3 = { i: 3, v: (3, "C") };

    map<Data3> m = { a: d1, b: d2, c: d3 };

    int i = 0;
    foreach var (k, {i: u, v: (v, w)}) in m {
        concatIntStringIntIntString(i, k, u, v, w);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithTupleInRecordWithType() returns string {
    output = "";

    Data3 d1 = { i: 1, v: (1, "A") };
    Data3 d2 = { i: 2, v: (2, "B") };
    Data3 d3 = { i: 3, v: (3, "C") };

    map<Data3> m = { a: d1, b: d2, c: d3 };

    int i = 0;
    foreach (string, Data3) (k, {i: u, v: (v, w)}) in m {
        concatIntStringAnyIntString(i, k, u, v, w);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithTupleInRecordWithAnyType() returns string {
    output = "";

    Data3 d1 = { i: 1, v: (1, "A") };
    Data3 d2 = { i: 2, v: (2, "B") };
    Data3 d3 = { i: 3, v: (3, "C") };

    map<Data3> m = { a: d1, b: d2, c: d3 };

    int i = 0;
    foreach (string, any) (k, v) in m {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

// ---------------------------------------------------------------------------------------------------------------------

function testEmptyMapIteration() returns string {
    output = "";

    map<any> m = {};

    int i = 0;
    foreach var (k, v) in m {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

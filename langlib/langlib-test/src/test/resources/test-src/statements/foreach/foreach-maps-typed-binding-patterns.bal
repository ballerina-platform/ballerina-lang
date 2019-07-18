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
    [int, string] v;
};

function concatIntString(int i, string s) {
    output = output + i.toString() + ":" + s + " ";
}

function concatIntStringFloat(int i, string s, float f) {
    output = output + i.toString() + ":" + s + ":" + f.toString() + " ";
}

function concatIntIntString(int i1, int i2, string s) {
    output = output + i1.toString() + ":" + i2.toString() + ":" + s + " ";
}

function concatStringIntString(int s1, int i, string s2) {
    output = output + s1.toString() + ":" + i.toString() + ":" + s2 + " ";
}

function concatIntStringAny(int i, any a) {
    output = output + i.toString() + ":" + a.toString() + " ";
}

function concatIntStringIntString(int i1, int i2, string s2) {
    output = output + i1.toString() + ":" + i2.toString() + ":" + s2 + " ";
}

function concatIntStringIntStringFloat(int i1, int i2, string s2, float f) {
    output = output + i1.toString() + ":" + i2.toString() + ":" + s2 + ":" + f.toString() + " ";
}

function concatIntStringIntIntString(int i1, int i2, int i3, string s2) {
    output = output + i1.toString() + ":" + i2.toString() + ":" + i3.toString() + ":" + s2 + " ";
}

function concatIntStringAnyIntString(int i1, any a, int i2, string s2) {
    output = output + i1.toString() + ":" + a.toString() + ":" + i2.toString() + ":" + s2 + " ";
}

// ---------------------------------------------------------------------------------------------------------------------

function testUnconstrainedMapWithoutType() returns string {
    output = "";

    map<any> m = { a: "A", b: "B", c: "C" };

    int i = 0;
    foreach var v in m {
        concatIntStringAny(i, v);
        i += 1;
    }
    return output;
}

function testUnconstrainedMapWithType() returns string {
    output = "";

    map<any> m = { a: "A", b: "B", c: "C" };

    int i = 0;
    foreach any v in m {
        concatIntStringAny(i, v);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithoutType() returns string {
    output = "";

    map<anydata> m = { a: "A", b: "B", c: "C" };

    int i = 0;
    foreach var v in m {
        concatIntStringAny(i, v);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithType() returns string {
    output = "";

    map<anydata> m = { a: "A", b: "B", c: "C" };

    int i = 0;
    foreach anydata v in m {
        concatIntStringAny(i, v);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithAnyType() returns string {
    output = "";

    map<anydata> m = { a: "A", b: "B", c: "C" };

    int i = 0;
    foreach any v in m {
        concatIntStringAny(i, v);
        i += 1;
    }
    return output;
}

// ---------------------------------------------------------------------------------------------------------------------

function testUnconstrainedMapWithTupleWithoutType() returns string {
    output = "";

    [int, string] t1 = [1, "A"];
    [int, string] t2 = [2, "B"];
    [int, string] t3 = [3, "C"];

    map<any> m = { a: t1, b: t2, c: t3 };

    int i = 0;
    foreach var v in m {
        concatIntStringAny(i, v);
        i += 1;
    }
    return output;
}

function testUnconstrainedMapWithTupleWithType() returns string {
    output = "";

    [int, string] t1 = [1, "A"];
    [int, string] t2 = [2, "B"];
    [int, string] t3 = [3, "C"];

    map<any> m = { a: t1, b: t2, c: t3 };

    int i = 0;
    foreach any v in m {
        concatIntStringAny(i, v);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithTupleWithoutType() returns string {
    output = "";

    [int, string] t1 = [1, "A"];
    [int, string] t2 = [2, "B"];
    [int, string] t3 = [3, "C"];

    map<[int, string]> m = { a: t1, b: t2, c: t3 };

    int i = 0;
    foreach var [u, v] in m {
        concatIntStringIntString(i, u, v);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithTupleWithType() returns string {
    output = "";

    [int, string] t1 = [1, "A"];
    [int, string] t2 = [2, "B"];
    [int, string] t3 = [3, "C"];

    map<[int, string]> m = { a: t1, b: t2, c: t3 };

    int i = 0;
    foreach [int, string] [u, v] in m {
        concatIntStringIntString(i, u, v);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithTupleWithAnyType() returns string {
    output = "";

    [int, string] t1 = [1, "A"];
    [int, string] t2 = [2, "B"];
    [int, string] t3 = [3, "C"];

    map<[int, string]> m = { a: t1, b: t2, c: t3 };

    int i = 0;
    foreach any v in m {
        concatIntStringAny(i, v);
        i += 1;
    }
    return output;
}

// ---------------------------------------------------------------------------------------------------------------------

function testUnconstrainedMapWithTupleInTupleWithoutType() returns string {
    output = "";

    [int, [string, float]] t1 = [1, ["A", 2.0]];
    [int, [string, float]] t2 = [2, ["B", 3.0]];
    [int, [string, float]] t3 = [3, ["C", 4.0]];

    map<any> m = { a: t1, b: t2, c: t3 };

    int i = 0;
    foreach var  v in m {
        concatIntStringAny(i, v);
        i += 1;
    }
    return output;
}

function testUnconstrainedMapWithTupleInTupleWithType() returns string {
    output = "";

    [int, [string, float]] t1 = [1, ["A", 2.0]];
    [int, [string, float]] t2 = [2, ["B", 3.0]];
    [int, [string, float]] t3 = [3, ["C", 4.0]];

    map<any> m = { a: t1, b: t2, c: t3 };

    int i = 0;
    foreach any v in m {
        concatIntStringAny(i, v);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithTupleInTupleWithoutType() returns string {
    output = "";

    [int, [string, float]] t1 = [1, ["A", 2.0]];
    [int, [string, float]] t2 = [2, ["B", 3.0]];
    [int, [string, float]] t3 = [3, ["C", 4.0]];

    map<[int, [string, float]]> m = { a: t1, b: t2, c: t3 };

    int i = 0;
    foreach var [u, [v, w]] in m {
        concatIntStringIntStringFloat(i, u, v, w);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithTupleInTupleWithType() returns string {
    output = "";

    [int, [string, float]] t1 = [1, ["A", 2.0]];
    [int, [string, float]] t2 = [2, ["B", 3.0]];
    [int, [string, float]] t3 = [3, ["C", 4.0]];

    map<[int, [string, float]]> m = { a: t1, b: t2, c: t3 };

    int i = 0;
    foreach [int, [string, float]]  [u, [v, w]] in m {
        concatIntStringIntStringFloat(i, u, v, w);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithTupleInTupleWithAnyType() returns string {
    output = "";

    [int, [string, float]] t1 = [1, ["A", 2.0]];
    [int, [string, float]] t2 = [2, ["B", 3.0]];
    [int, [string, float]] t3 = [3, ["C", 4.0]];

    map<[int, [string, float]]> m = { a: t1, b: t2, c: t3 };

    int i = 0;
    foreach any v in m {
        concatIntStringAny(i, v);
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

    [int, Data] t1 = [2, d1];
    [int, Data] t2 = [3, d2];
    [int, Data] t3 = [4, d3];

    map<any> m = { a: t1, b: t2, c: t3 };

    int i = 0;
    foreach var v in m {
        concatIntStringAny(i, v);
        i += 1;
    }
    return output;
}

function testUnconstrainedMapWithRecordInTupleWithType() returns string {
    output = "";

    Data d1 = { i: 1, v: "A" };
    Data d2 = { i: 2, v: "B" };
    Data d3 = { i: 3, v: "C" };

    [int, Data] t1 = [2, d1];
    [int, Data] t2 = [3, d2];
    [int, Data] t3 = [4, d3];

    map<any> m = { a: t1, b: t2, c: t3 };

    int i = 0;
    foreach any v in m {
        concatIntStringAny(i, v);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithRecordInTupleWithoutType() returns string {
    output = "";

    Data d1 = { i: 1, v: "A" };
    Data d2 = { i: 2, v: "B" };
    Data d3 = { i: 3, v: "C" };

    [int, Data] t1 = [2, d1];
    [int, Data] t2 = [3, d2];
    [int, Data] t3 = [4, d3];

    map<[int, Data]> m = { a: t1, b: t2, c: t3 };

    int i = 0;
    foreach var [u, {i: v, v: w}] in m {
        concatIntStringIntIntString(i, u, v, w);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithRecordInTupleWithType() returns string {
    output = "";

    Data d1 = { i: 1, v: "A" };
    Data d2 = { i: 2, v: "B" };
    Data d3 = { i: 3, v: "C" };

    [int, Data] t1 = [2, d1];
    [int, Data] t2 = [3, d2];
    [int, Data] t3 = [4, d3];

    map<[int, Data]> m = { a: t1, b: t2, c: t3 };

    int i = 0;
    foreach [int, Data] [u, {i: v, v: w}] in m {
        concatIntStringIntIntString(i, u, v, w);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithRecordInTupleWithAnyType() returns string {
    output = "";

    Data d1 = { i: 1, v: "A" };
    Data d2 = { i: 2, v: "B" };
    Data d3 = { i: 3, v: "C" };

    [int, Data] t1 = [2, d1];
    [int, Data] t2 = [3, d2];
    [int, Data] t3 = [4, d3];

    map<[int, Data]> m = { a: t1, b: t2, c: t3 };

    int i = 0;
    foreach any v in m {
        concatIntStringAny(i, v);
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
    foreach var v in m {
        concatIntStringAny(i, v);
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
    foreach any v in m {
        concatIntStringAny(i, v);
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
    foreach var {i: u, v} in m {
        concatIntStringIntString(i, u, v);
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
    foreach Data {i: u, v} in m {
        concatIntStringIntString(i, u, v);
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
    foreach any v in m {
        concatIntStringAny(i, v);
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
    foreach var v in m {
        concatIntStringAny(i, v);
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
    foreach any v in m {
        concatIntStringAny(i, v);
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
    foreach var  {i: u, v: {i: v, v: w}} in m {
        concatIntStringIntIntString(i, u, v, w);
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
    foreach Data2 {i: u, v: {i: v, v: w}} in m {
        concatIntStringIntIntString(i, u, v, w);
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
    foreach any v in m {
        concatIntStringAny(i, v);
        i += 1;
    }
    return output;
}

// ---------------------------------------------------------------------------------------------------------------------

function testUnconstrainedMapWithTupleInRecordWithoutType() returns string {
    output = "";

    Data3 d1 = { i: 1, v: [1, "A"] };
    Data3 d2 = { i: 2, v: [2, "B"] };
    Data3 d3 = { i: 3, v: [3, "C"] };

    map<any> m = { a: d1, b: d2, c: d3 };

    int i = 0;
    foreach var v in m {
        concatIntStringAny(i, v);
        i += 1;
    }
    return output;
}

function testUnconstrainedMapWithTupleInRecordWithType() returns string {
    output = "";

    Data3 d1 = { i: 1, v: [1, "A"] };
    Data3 d2 = { i: 2, v: [2, "B"] };
    Data3 d3 = { i: 3, v: [3, "C"] };

    map<any> m = { a: d1, b: d2, c: d3 };

    int i = 0;
    foreach any v in m {
        concatIntStringAny(i, v);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithTupleInRecordWithoutType() returns string {
    output = "";

     Data3 d1 = { i: 1, v: [1, "A"] };
     Data3 d2 = { i: 2, v: [2, "B"] };
     Data3 d3 = { i: 3, v: [3, "C"] };

    map<Data3> m = { a: d1, b: d2, c: d3 };

    int i = 0;
    foreach var {i: u, v: [v, w]} in m {
        concatIntStringIntIntString(i, u, v, w);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithTupleInRecordWithType() returns string {
    output = "";

    Data3 d1 = { i: 1, v: [1, "A"] };
    Data3 d2 = { i: 2, v: [2, "B"] };
    Data3 d3 = { i: 3, v: [3, "C"] };

    map<Data3> m = { a: d1, b: d2, c: d3 };

    int i = 0;
    foreach Data3 {i: u, v: [v, w]} in m {
        concatIntStringAnyIntString(i, u, v, w);
        i += 1;
    }
    return output;
}

function testConstrainedMapWithTupleInRecordWithAnyType() returns string {
    output = "";

    Data3 d1 = { i: 1, v: [1, "A"] };
    Data3 d2 = { i: 2, v: [2, "B"] };
    Data3 d3 = { i: 3, v: [3, "C"] };

    map<Data3> m = { a: d1, b: d2, c: d3 };

    int i = 0;
    foreach any v in m {
        concatIntStringAny(i, v);
        i += 1;
    }
    return output;
}

// ---------------------------------------------------------------------------------------------------------------------

function testEmptyMapIteration() returns string {
    output = "";

    map<any> m = {};

    int i = 0;
    foreach var v in m {
        concatIntStringAny(i, v);
        i += 1;
    }
    return output;
}

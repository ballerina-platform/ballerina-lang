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

function concatIntStringAny(int i, string s, any a) {
    output = output + i + ":" + s + ":" + <string>a + " ";
}

function concatIntStringIntString(int i1, string s1, int i2, string s2) {
    output = output + i1 + ":" + s1 + ":" + i2 + ":" + s2 + " ";
}

function concatIntStringIntStringFloat(int i1, string s1, int i2, string s2, float f) {
    output = output + i1 + ":" + s1 + ":" + i2 + ":" + s2 + ":" + f + " ";
}

function testUnconstrainedMapWithoutType() returns string {
    output = "";

    map m = { a: "A", b: "B", c: "C" };
    int i = 0;
    foreach var (k, v) in m {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

function testUnconstrainedMapWithType() returns string {
    output = "";

    map m = { a: "A", b: "B", c: "C" };
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

    map m = { a: t1, b: t2, c: t3 };
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

    map m = { a: t1, b: t2, c: t3 };
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

    map m = { a: t1, b: t2, c: t3 };
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

    map m = { a: t1, b: t2, c: t3 };
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

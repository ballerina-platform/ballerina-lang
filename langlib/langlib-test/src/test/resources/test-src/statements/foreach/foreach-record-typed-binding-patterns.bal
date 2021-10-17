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
    [int, string] t;
};

type Data4 record {

};

function concatIntStringAny(int i,  any a) {
    output = output + i.toString() + ":" + a.toString() + " ";
}

// ---------------------------------------------------------------------------------------------------------------------

function testSimpleRecordWithoutType() returns string {
    output = "";

    Data1 d = { i: 1, s: "A", f: 1.0 };

    int i = 0;
    foreach var v in d {
        concatIntStringAny(i, v);
        i += 1;
    }
    return output;
}

function testSimpleRecordWithType() returns string {
    output = "";

    Data1 d = { i: 1, s: "A", f: 1.0 };

    int i = 0;
    foreach any v in d {
        concatIntStringAny(i, v);
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
    foreach var v in d2 {
        concatIntStringAny(i, v);
        i += 1;
    }
    return output;
}

function testRecordInRecordWithType() returns string {
    output = "";

    Data1 d1 = { i: 1, s: "A", f: 1.0 };
    Data2 d2 = { i: 2, s: "B", d: d1 };

    int i = 0;
    foreach any v in d2 {
        concatIntStringAny(i, v);
        i += 1;
    }
    return output;
}

// ---------------------------------------------------------------------------------------------------------------------

function testTupleInRecordWithoutType() returns string {
    output = "";

    Data3 d3 = { i: 1, t: [2, "A"] };

    int i = 0;
    foreach var v in d3 {
        concatIntStringAny(i, v);
        i += 1;
    }
    return output;
}

function testTupleInRecordWithType() returns string {
    output = "";

    Data3 d3 = { i: 1, t: [2, "A"] };

    int i = 0;
    foreach any v in d3 {
        concatIntStringAny(i, v);
        i += 1;
    }
    return output;
}

// ---------------------------------------------------------------------------------------------------------------------

function testEmptyRecordIteration() returns string {
    output = "";

    Data4 d = {};

    int i = 0;
    foreach var v in d {
        concatIntStringAny(i, <any> v);
        i += 1;
    }
    return output;
}

type Foo record {|
|};

function testForeachWithClosedRecordWithNoFields() {
    Foo f = {
    };
    any a = "ABC";
    foreach var x in f {
        a = "DEF";
    }
    assertEquality("ABC", a);
}

type AssertionError distinct error;

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error AssertionError(ASSERTION_ERROR_REASON,
            message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}

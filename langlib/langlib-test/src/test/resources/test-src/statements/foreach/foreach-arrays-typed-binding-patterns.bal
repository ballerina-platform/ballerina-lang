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

// ---------------------------------------------------------------------------------------------------------------------

function testForeachIterationOverReadOnlyArrayMembersOfReadOnlyArrayWithVar() {
    readonly & (int[2])[] x = [[1, 2], [3, 4], [5, 6]];

    int[] arr = [];

    foreach var [a, b] in x {
        int c = a;
        int d = b;
        arr.push(c, d);
    }

    assertEquality(6, arr.length());
    assertEquality([1, 2, 3, 4, 5, 6], arr);
}

function testForeachIterationOverReadOnlyArrayMembersOfNonReadOnlyArrayWithVar() {
    (readonly & int[2])[] x = [[1, 2], [3, 4], [5, 6]];

    int[] arr = [];

    foreach var [a, b] in x {
        int c = a;
        int d = b;
        arr.push(c, d);
    }

    assertEquality(6, arr.length());
    assertEquality([1, 2, 3, 4, 5, 6], arr);
}

function testForeachIterationOverReadOnlyTupleMembersOfReadOnlyArrayWithVar() {
    readonly & ([int, string, boolean])[] x = [[1, "a", true], [3, "bc", false], [5, "def", true]];

    (int|string|boolean)[] arr = [];

    foreach var [a, b, c] in x {
        int d = a;
        string e = b;
        boolean f = c;
        arr.push(f, e, d);
    }

    assertEquality(9, arr.length());
    assertEquality([true, "a", 1, false, "bc", 3, true, "def", 5], arr);
}

function testForeachIterationOverReadOnlyTupleMembersOfReadOnlyTupleWithVar() {
    readonly & [[int, int], [int, int]...] x = [[1, 2], [3, 4], [5, 6]];

    int[] arr = [];

    foreach var [a, b] in x {
        int c = a;
        int d = b;
        arr.push(c, d);
    }

    assertEquality(6, arr.length());
    assertEquality([1, 2, 3, 4, 5, 6], arr);
}

function testForeachIterationOverReadOnlyArrayMembersOfReadOnlyTupleWithVar() {
    readonly & [int[2], string[2], boolean[3]] x = [[1, 2], ["a", "b"], [false, false, true]];

    (int|string|boolean)[] arr = [];

    foreach var [a, b, ...c] in x {
        int|string|boolean d = a;
        int|string|boolean e = b;

        if c.length() != 0 {
            int|string|boolean f = c[0];
            // Can be replaced with the following once https://github.com/ballerina-platform/ballerina-lang/issues/33366 is fixed.
            // boolean f = c[0];

            arr.push(f);
        }

        arr.push(e, d);
    }

    assertEquality(7, arr.length());
    assertEquality([2, 1, "b", "a", true, false, false], arr);
}

function testForeachIterationOverReadOnlyMapOfReadOnlyTupleWithVar() {
    readonly & [map<int>, map<string>...] x = [{a: 1, b: 2}, {a: "hello", c: "world", d: "ballerina"}];

    (int|string)[] arr = [];

    foreach var {...a} in x {
        map<int|string> b = a;
        arr.push(...b.toArray());
    }

    assertEquality(5, arr.length());
    assertTrue(arr.indexOf(1) != ());
    assertTrue(arr.indexOf(2) != ());
    assertTrue(arr.indexOf("hello") != ());
    assertTrue(arr.indexOf("world") != ());
    assertTrue(arr.indexOf("ballerina") != ());
}

function testForeachIterationOverReadOnlyArrayMembersOfReadOnlyArray() {
    readonly & (int[2])[] x = [[1, 2], [3, 4], [5, 6]];

    int[] arr = [];

    foreach int[2] [a, b] in x {
        arr.push(a, b);
    }

    assertEquality(6, arr.length());
    assertEquality([1, 2, 3, 4, 5, 6], arr);

    arr = [];

    foreach [int, int] & readonly [a, b] in x {
        arr.push(a, b);
    }

    assertEquality(6, arr.length());
    assertEquality([1, 2, 3, 4, 5, 6], arr);
}

function testForeachIterationOverReadOnlyArrayMembersOfNonReadOnlyArray() {
    (readonly & int[2])[] x = [[1, 2], [3, 4], [5, 6]];

    int[] arr = [];

    foreach int[2] [a, b] in x {
        arr.push(a, b);
    }

    assertEquality(6, arr.length());
    assertEquality([1, 2, 3, 4, 5, 6], arr);

    arr = [];

    foreach readonly & [int, int] [a, b] in x {
        arr.push(a, b);
    }

    assertEquality(6, arr.length());
    assertEquality([1, 2, 3, 4, 5, 6], arr);
}

function testForeachIterationOverReadOnlyTupleMembersOfReadOnlyArray() {
    readonly & ([int, string, boolean])[] x = [[1, "a", true], [3, "bc", false], [5, "def", true]];

    (int|string|boolean)[] arr = [];

    foreach [int, string, boolean] [a, b, c] in x {
        arr.push(c, b, a);
    }

    assertEquality(9, arr.length());
    assertEquality([true, "a", 1, false, "bc", 3, true, "def", 5], arr);

    arr = [];

    foreach [int, string, boolean] & readonly [a, b, c] in x {
        arr.push(c, b, a);
    }

    assertEquality(9, arr.length());
    assertEquality([true, "a", 1, false, "bc", 3, true, "def", 5], arr);
}

function testForeachIterationOverReadOnlyTupleMembersOfReadOnlyTuple() {
    readonly & [[int, int], [int, int]...] x = [[1, 2], [3, 4], [5, 6]];

    int[] arr = [];

    foreach [int, int] [a, b] in x {
        arr.push(a, b);
    }

    assertEquality(6, arr.length());
    assertEquality([1, 2, 3, 4, 5, 6], arr);

    arr = [];

    foreach [int, int] & readonly [a, b] in x {
        arr.push(a, b);
    }

    assertEquality(6, arr.length());
    assertEquality([1, 2, 3, 4, 5, 6], arr);
}

function testForeachIterationOverReadOnlyArrayMembersOfReadOnlyTuple() {
    readonly & [int[2], string[2], boolean[3]] x = [[1, 2], ["a", "b"], [false, false, true]];

    (int|string|boolean)[] arr = [];

    foreach [int|string|boolean, int|string|boolean, boolean...] [a, b, ...c] in x {
        if c.length() != 0 {
            arr.push(c[0]);
        }

        arr.push(b, a);
    }

    assertEquality(7, arr.length());
    assertEquality([2, 1, "b", "a", true, false, false], arr);
}

function testForeachIterationOverReadOnlyRecordOfNonReadOnlyArray() {
    (readonly & record {| int a; int b; |}|readonly & record {| string[] a; boolean? b = (); |})[] x =
        [{a: 1, b: 2}, {a: ["hello", "world", "ballerina"]}];

    (int|string[]|boolean?)[] arr = [];

    foreach record {| int|string[] a; int|boolean? b; |} {a, b} in x {
        arr.push(a, b);
    }

    assertEquality(4, arr.length());
    assertEquality([1, 2, ["hello", "world", "ballerina"], ()], arr);
}

function assertTrue(anydata actual) {
    assertEquality(true, actual);
}

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}

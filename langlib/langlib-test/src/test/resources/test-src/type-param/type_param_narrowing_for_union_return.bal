function testSimpleUnionReturnParameterNarrowing() {
    int[]|float[] arr = <int[]>[1, 2];
    [int, (int|float)][] y = arr.enumerate();
    assertTrue(true);
}

function testUnionOfMapsReturnParameterNarrowing() {
    map<int>|map<float> m = <map<int>>{"1": 1};
    int|float x = m.get("1");
    assertTrue(true);
}

function testStringIntFloatSimpleAndArrayUnionReturnParameterNarrowing() {
    string | int[] | int | float[] | float arr = <int[]>[1, 2];
    if (arr is int[] | float[]) {
        [int, (int|float)][] y = arr.enumerate();
    }
    assertTrue(true);
}

function testIntFloatSimpleAndMapUnionReturnParameterNarrowing() {
    map<int> | int | map<float> | float  m = <map<int>>{"1": 1};
    if (m is map<int> | map<float>) {
        int | float x = m.get("1");
    }
    assertTrue(true);
}

function testIntFloatSimpleArrayMapUnionReturnParameterNarrowing() {
    map<int> | int[] | map<float> | float[]  m = <map<int>>{"1": 1};
    if (m is map<float> | map<int>) {
        int | float x = m.get("1");
    }
    if (m is int[] | float[]) {
        [int, (int|float)][] y = m.enumerate();
    }
    assertTrue(true);
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(any|error actual) {
    if actual is boolean && actual {
        return;
    }
    panic error(ASSERTION_ERROR_REASON,
                message = "expected 'true', found '" + actual.toString () + "'");
}

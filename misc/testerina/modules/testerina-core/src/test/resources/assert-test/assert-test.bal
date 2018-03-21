import ballerina/test;

function testAssertIntEquals (int a, int b) {
    int answer = 0;
    answer = intAdd(a, b);
    test:assertIntEquals(answer, 8, "intAdd function failed");
}

function testAssertFloatEquals (float a, float b) {
    float float1 = 10.000;
    float float2 = 20.050;
    float answer = floatAdd(a, b);
    test:assertFloatEquals(answer, 30.050, "floatAdd function failed");
}

function testAssertStringEquals (string a, string b) {
    string concatenated = stringConcat(a, b);
    test:assertStringEquals(concatenated, "JohnDoe", "string concatenation failed");
}

function intAdd (int a, int b) (int) {
    return a + b;
}

function floatAdd (float a, float b) (float) {
    return a + b;
}

function stringConcat (string a, string b) (string) {
    return a + b;
}

function testAssertTrue (boolean value) {
    test:assertTrue(value, "assertTrue failed");
}

function testAssertFalse (boolean value) {
    test:assertFalse(value, "assertFalse failed");
}

function testAssertBooleanEquals (boolean x, boolean y) {
    test:assertBooleanEquals(x, y, "assertBooleanEquals failed");
}

function testAssertFail (boolean value) {
    if (value) {
        return;
    }
    test:assertFail("assertFailed");
}

function testAssertStringArrayEquals (int case) {
    string[] x = ["A", "B", "C"];
    string[] y = ["A", "B", "C"];
    if (case == 1) {
        x[3] = "D";
    } else if (case == 2) {
        y[1] = "b";
    } else if (case == 3) {
        y[1] = "b";
        test:assertStringArrayEquals(x, y, "");
    }
    test:assertStringArrayEquals(x, y, "failed");
}

function testAssertIntArrayEquals (int case) {
    int[] x = [1, 2, 3];
    int[] y = [1, 2, 3];
    if (case == 1) {
        x[3] = 4;
    } else if (case == 2) {
        y[1] = 5;
    }
    test:assertIntArrayEquals(x, y, "failed");
}

function testAssertFloatArrayEquals (int case) {
    float[] x = [1.1, 2.2, 3.3];
    float[] y = [1.1, 2.2, 3.3];
    if (case == 1) {
        x[3] = 4.4;
    } else if (case == 2) {
        y[1] = 2.22;
    }
    test:assertFloatArrayEquals(x, y, "failed");
}

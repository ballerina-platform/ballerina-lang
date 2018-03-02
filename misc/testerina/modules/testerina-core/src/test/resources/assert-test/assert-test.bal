import ballerina.test;

function testAssertIntEquals (int a, int b) {
    int answer = 0;
    answer = intAdd(a, b);
    test:assertEquals(answer, 8, "intAdd function failed");
}

function testAssertFloatEquals (float a, float b) {
    float float1 = 10.000;
    float float2 = 20.050;
    float answer = floatAdd(a, b);
    test:assertEquals(answer, 30.050, "floatAdd function failed");
}

function testAssertStringEquals (string a, string b) {
    string concatenated = stringConcat(a, b);
    test:assertEquals(concatenated, "JohnDoe", "string concatenation failed");
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
    test:assertEquals(x, y, "assertBooleanEquals failed");
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
        test:assertEquals(x, y, "failed");
    }
    test:assertEquals(x, y, "failed");
}

function testAssertIntArrayEquals (int case) {
    int[] x = [1, 2, 3];
    int[] y = [1, 2, 3];
    if (case == 1) {
        x[3] = 4;
    } else if (case == 2) {
        y[1] = 5;
    }
    test:assertEquals(x, y, "failed");
}

function testAssertFloatArrayEquals (int case) {
    float[] x = [1.1, 2.2, 3.3];
    float[] y = [1.1, 2.2, 3.3];
    if (case == 1) {
        x[3] = 4.4;
    } else if (case == 2) {
        y[1] = 2.22;
    }
    test:assertEquals(x, y, "failed");
}

function testAssertNotEquals (int case) {
    if (case == 1) {
        string s1 = "abc";
        string s2 = "def";
        test:assertNotEquals(s1, s2, "failed");
    } else if (case == 2) {
        int s1 = 1;
        int s2 = 33;
        test:assertNotEquals(s1, s2, "failed");
    }
}

function testAssertNotEquals2 () {
    json s1 = {"a":"b"};
    json s2 = {"a":"b"};
    test:assertNotEquals(s1, s2, "failed");
}


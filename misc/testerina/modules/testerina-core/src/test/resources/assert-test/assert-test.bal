import ballerina/test;

function testAssertIntEquals (int a, int b) {
    int answer = 0;
    answer = intAdd(a, b);
    test:assertEquals(answer, 8, msg = "intAdd function failed");
}

function testAssertFloatEquals (float a, float b) {
    float float1 = 10.000;
    float float2 = 20.050;
    float answer = floatAdd(a, b);
    test:assertEquals(answer, 30.050);
}

function testAssertStringEquals (string a, string b) {
    string concatenated = stringConcat(a, b);
    test:assertEquals(concatenated, "JohnDoe");
}

function testAssertJsonEquals (json a, json b) {
    test:assertEquals(a, b);
}

function intAdd (int a, int b) returns (int) {
    return a + b;
}

function floatAdd (float a, float b) returns (float) {
    return a + b;
}

function stringConcat (string a, string b) returns (string) {
    return a + b;
}

function testAssertTrue (boolean value) {
    test:assertTrue(value, msg = "assertTrue failed");
}

function testAssertFalse (boolean value) {
    test:assertFalse(value, msg = "assertFalse failed");
}

function testAssertBooleanEquals (boolean x, boolean y) {
    test:assertEquals(x, y, msg = "assertBooleanEquals failed");
}

function testAssertFail (boolean value) {
    if (value) {
        return;
    }
    test:assertFail(msg = "assertFailed");
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
        test:assertEquals(x, y);
    }
    test:assertEquals(x, y);
}

function testAssertIntArrayEquals (int case) {
    int[] x = [1, 2, 3];
    int[] y = [1, 2, 3];
    if (case == 1) {
        x[3] = 4;
    } else if (case == 2) {
        y[1] = 5;
    }
    test:assertEquals(x, y, msg = "failed");
}

function testAssertFloatArrayEquals (int case) {
    float[] x = [1.1, 2.2, 3.3];
    float[] y = [1.1, 2.2, 3.3];
    if (case == 1) {
        x[3] = 4.4;
    } else if (case == 2) {
        y[1] = 2.22;
    }
    test:assertEquals(x, y, msg = "failed");
}

function testAssertNotEquals (int case) {
    if (case == 1) {
        string s1 = "abc";
        string s2 = "def";
        test:assertNotEquals(s1, s2, msg = "failed");
    } else if (case == 2) {
        int s1 = 1;
        int s2 = 33;
        test:assertNotEquals(s1, s2, msg = "failed");
    }
}

function testAssertNotEquals2 () {
    json s1 = {"a":"b"};
    json s2 = {"a":"b"};
    test:assertNotEquals(s1, s2, msg = "failed");
}


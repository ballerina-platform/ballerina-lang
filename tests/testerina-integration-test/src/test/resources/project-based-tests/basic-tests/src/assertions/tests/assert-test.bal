import ballerina/test;

# When executing tests, the tests related to assertTrue should be executed
# first since the other tests use the assertTrue function.
# The tests labelled with group 'p1' therefore should be executed first.

@test:Config {
    groups: ["p1"]
}
function testAssertTrue () {
    test:assertTrue(true, msg = "assertTrue failed");
}

@test:Config {
    groups: ["p1"]
}
function testAssertTrueNegative () {
    error? err = trap test:assertTrue(false, msg = "assertTrue failed");
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertFalse () {
    test:assertFalse(false, msg = "assertFalse failed");
}

@test:Config {}
function testAssertFalseNegative () {
    error? err = trap test:assertFalse(true, msg = "assertFalse failed");
    test:assertTrue(err is error);
}

function testAssertFail () {
    error? err = trap test:assertFail(msg = "assertFailed");
    test:assertTrue(err is error);
    error result = <error>err;
    test:assertTrue(result.reason().toString().startsWith("assertFailed"));
}

@test:Config {}
function testAssertIntEquals () {
    int answer = 0;
    answer = intAdd(6, 2);
    test:assertEquals(answer, 8, msg = "intAdd function failed");
}

@test:Config {}
function testAssertIntEqualsNegative () {
    int answer = 0;
    answer = intAdd(6, 2);
    error? err = trap test:assertEquals(answer, 9, msg = "intAdd function failed");
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertFloatEquals () {
    float float1 = 10.000;
    float float2 = 20.050;
    float answer = floatAdd(float1, float2);
    test:assertEquals(answer, 30.050);
}

@test:Config {}
function testAssertFloatEqualsNegative () {
    float float1 = 10.000;
    float float2 = 20.050;
    float answer = floatAdd(float1, float2);
    error? err = trap test:assertEquals(answer, 40.050);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertStringEquals () {
    string concatenated = stringConcat("John", "Doe");
    test:assertEquals(concatenated, "JohnDoe");
}

@test:Config {}
function testAssertStringEqualsNegative () {
    string concatenated = stringConcat("John", "Doe");
    error? err = trap test:assertEquals(concatenated, "DoeJohn");
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertJsonEquals () {
    json a = {"a" : "b"};
    json b = {"a" : "b"};
    test:assertEquals(a, b);
}

@test:Config {}
function testAssertJsonEqualsNegative () {
    json a = {"a" : "b"};
    json b = {"b" : "a"};
    error? err = trap test:assertEquals(a, b);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertBooleanEquals () {
    boolean a = true;
    test:assertEquals(a, true);
}

@test:Config {}
function testAssertBooleanEqualsNegative () {
    boolean a = true;
    error? err = trap test:assertEquals(a, false);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertIntArrayEquals () {
    int[] x = [1, 2, 3];
    int[] y = [1, 2, 3];
    test:assertEquals(x, y);
}

@test:Config {}
function testAssertIntArrayEqualsNegative () {
    int[] x = [1, 2, 3];
    int[] y = [1, 2, 4];
    error? err = trap test:assertEquals(x, y);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertStringArrayEquals () {
    string[] x = ["A", "B", "C"];
    string[] y = ["A", "B", "C"];
    test:assertEquals(x, y);
}

@test:Config {}
function testAssertStringArrayEqualsNegative () {
    string[] x = ["A", "B", "C"];
    string[] y = ["A", "B", "c"];
    error? err = trap test:assertEquals(x, y);
    test:assertTrue(err is error);

    y = ["A", "B", "D"];
    err = trap test:assertEquals(x, y);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertFloatArrayEquals () {
    float[] x = [1.1, 2.2, 3.3];
    float[] y = [1.1, 2.2, 3.3];
    test:assertEquals(x, y);
}

@test:Config {}
function testAssertFloatArrayEqualsNegative () {
    float[] x = [1.1, 2.2, 3.3];
    float[] y = [1.1, 2.2, 3.0];
    error? err = trap test:assertEquals(x, y);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertStringNotEquals () {
    string s1 = "abc";
    test:assertNotEquals(s1, "def");        
}

@test:Config {}
function testAssertStringNotEqualsNegative () {
    string s1 = "abc";
    error? err = trap test:assertNotEquals(s1, "abc"); 
    test:assertTrue(err is error);       
}

@test:Config {}
function testAssertIntNotEquals () {
    int num = 1;
    test:assertNotEquals(num, 11);        
}

@test:Config {}
function testAssertIntNotEqualsNegative () {
    int num = 1;
    error? err = trap test:assertNotEquals(num, 1); 
    test:assertTrue(err is error);       
}

@test:Config {}
function testAssertFloatNotEquals () {
    float num = 1.1;
    test:assertNotEquals(num, 1.11);        
}

@test:Config {}
function testAssertFloatNotEqualsNegative () {
    float num = 1.1;
    error? err = trap test:assertNotEquals(num, 1.1); 
    test:assertTrue(err is error);       
}

@test:Config {}
function testAssertJsonNotEquals () {
    json a = {"a" : "b"};
    json b = {"b" : "a"};
    test:assertNotEquals(a, b);
}

@test:Config {}
function testAssertJsonNotEqualsNegative () {
    json a = {"a" : "b"};
    json b = {"a" : "b"};
    error? err = trap test:assertNotEquals(a, b);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertBooleanNotEquals () {
    boolean a = true;
    test:assertNotEquals(a, false);
}

@test:Config {}
function testAssertBooleanNotEqualsNegative () {
    boolean a = true;
    error? err = trap test:assertNotEquals(a, true);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertIntArrayNotEquals () {
    int[] x = [1, 2, 3];
    int[] y = [1, 2, 4];
    test:assertNotEquals(x, y);
}

@test:Config {}
function testAssertIntArrayNotEqualsNegative () {
    int[] x = [1, 2, 3];
    int[] y = [1, 2, 3];
    error? err = trap test:assertNotEquals(x, y);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertTypeNotEquals () {
    boolean a = true;
    test:assertNotEquals(a, "true");

    json obj = {"a" : "b"};
    test:assertNotEquals(obj, "{\"a\" : \"b\"}");

    int num = 1;
    test:assertNotEquals(num, "1");
}

@test:Config {}
function testAssertStringArrayNotEquals () {
    string[] x = ["A", "B", "C"];
    string[] y = ["A", "B", "c"];
    test:assertNotEquals(x, y);
}

@test:Config {}
function testAssertStringArrayNotEqualsNegative () {
    string[] x = ["A", "B", "C"];
    string[] y = ["A", "B", "C"];
    error? err = trap test:assertNotEquals(x, y);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertFloatArrayNotEquals () {
    float[] x = [1.1, 2.2, 3.3];
    float[] y = [1.1, 2.2, 3.0];
    test:assertNotEquals(x, y);
}

@test:Config {}
function testAssertFloatArrayNotEqualsNegative () {
    float[] x = [1.1, 2.2, 3.3];
    float[] y = [1.1, 2.2, 3.3];
    error? err = trap test:assertNotEquals(x, y);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertErrorEquals () {
    error err1 = error("errorMessage");
    error err2 = error("errorMessage");
    test:assertEquals(err1, err2);
}

@test:Config {}
function testAssertErrorEqualsNegative () {
    error err1 = error("errorMessage");
    error err2 = error("errorMessage1");
    error? err = trap test:assertEquals(err1, err2);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertErrorNotEquals () {
    error err1 = error("errorMessage");
    error err2 = error("errorMessage1");
    test:assertNotEquals(err1, err2);
}

@test:Config {}
function testAssertErrorNotEqualsNegative () {
    error err1 = error("errorMessage");
    error err2 = error("errorMessage");
    error? err = trap test:assertNotEquals(err1, err2);
    test:assertTrue(err is error);
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

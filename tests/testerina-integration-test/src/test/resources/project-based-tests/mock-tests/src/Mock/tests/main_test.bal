import ballerina/test;
import ballerina/io;
import ballerina/math;
import Mock2;

//
// MOCK FUNCTION OBJECTS
//

@test:Mock {
    functionName : "intAdd"
}
test:MockFunction mock_intAdd = new();

@test:Mock {
    functionName : "stringAdd"
}
test:MockFunction mock_stringAdd = new();

@test:Mock {
    functionName: "floatAdd"
}
test:MockFunction mock_floatAdd = new();


@test:Mock {
    moduleName : "ballerina/math",
    functionName : "absInt"
}
test:MockFunction mock_absInt = new();

@test:Mock {
    moduleName : "mock-tests/Mock2",
    functionName : "intAdd2"
}
test:MockFunction mock2_intAdd = new();

//
//  MOCK FUNCTIONS
//

public function mockIntAdd1(int x, int y) returns (int) {
    return x - y;
}

public function mockIntAdd2(int a, int b) returns (int) {
    return a * b;
}

public function mockIntAdd3(int a, int b) returns (float) {
    return 10.0;
}

public function mockIntAdd4(int a) returns (int) {
    return a;
}

public function mockStringAdd(string str1) returns (string) {
    return "Hello " + str1;
}

public function mockFloatAdd(float a, float b) returns (float) {
    return a - b;
}

public function mockAbsInt(int value) returns (int) {
    return 100;
}

//
// TESTS
//

@test:Config {
}
public function call_Test1() {
    io:println("[call_Test1] Testing .call function with different types of mock functions");

    // IntAdd
    test:when(mock_intAdd).call("mockIntAdd1");
    test:assertEquals(intAdd(10, 6), 4);
    test:assertEquals(callIntAdd(10, 6), 4);

    // StringAdd
    test:when(mock_stringAdd).call("mockStringAdd");
    test:assertEquals(stringAdd("Ibaqu"), "Hello Ibaqu");

     // FloatAdd
     test:when(mock_floatAdd).call("mockFloatAdd");
     test:assertEquals(floatAdd(10.6, 4.5), 6.1);
}

@test:Config {
}
public function call_Test2() {
    io:println("[call_Test2] Test switching mock functions");

    // Set which function to call
    test:when(mock_intAdd).call("mockIntAdd1");
    test:assertEquals(intAdd(10, 6), 4);

    // Switch function to call
    test:when(mock_intAdd).call("mockIntAdd2");
    test:assertEquals(intAdd(10, 6), 60);

    // Switch again
    test:when(mock_intAdd).call("mockIntAdd1");
    test:assertEquals(intAdd(10, 6), 4);
}

@test:Config {
}
public function call_Test3() {
    io:println("[call_Test3] Test invalid mock function");
    test:when(mock_intAdd).call("invalidMockFunction");
    test:assertEquals(intAdd(10, 6), 4);
}

@test:Config {
}
public function call_Test4() {
    io:println("[call_Test4] Test mock function with invalid return type");
    test:when(mock_intAdd).call("mockIntAdd3");
    test:assertEquals(intAdd(10, 6), 4);
}

@test:Config {
}
public function call_Test5() {
    io:println("[call_Test5] Test mock function with invalid parameters");
    test:when(mock_intAdd).call("mockIntAdd4");
    test:assertEquals(intAdd(10, 6), 4);
}

@test:Config {}
public function call_Test6() {
    io:println("[call_Test6] Test mock function in import package");
    test:when(mock_absInt).call("mockAbsInt");
    test:assertEquals(math:absInt(-5), 100);
}

@test:Config {}
public function call_Test7() {
    io:println("[call_Test7] Test mock function in import package in same project");
    test:when(mock2_intAdd).call("mockIntAdd2");
    test:assertEquals(Mock2:intAdd2(10, 5), 50);
}

@test:Config {
}
public function thenReturn_Test1() {
    io:println("[thenReturn_Test1] Test thenReturns");

    test:when(mock_intAdd).thenReturn(5);
    test:assertEquals(intAdd(10, 4), 5);

    test:when(mock_stringAdd).thenReturn("testing");
    test:assertEquals(stringAdd("string"), "testing");

    test:when(mock_floatAdd).thenReturn(10.5);
    test:assertEquals(floatAdd(10, 5), 10.5);
}

@test:Config {
}
public function withArguments_Test1() {
    io:println("[withArguments_Test1] Test withArguments");

    test:when(mock_intAdd).withArguments(20, 14).thenReturn(100);
    test:assertEquals(intAdd(20, 14), 100);

    test:when(mock_stringAdd).withArguments("string1").thenReturn("test");
    test:assertEquals(stringAdd("string1"), "test");

}
import ballerina/test;
import SubtractModule;
import ballerina/math;

//
// TEST CASES
//

// Test Function mocking in Same module
@test:Config {}
function test_AddFunction() {
    int answer = 0;
    answer = intAdd(5, 3);
    test:assertEquals(answer, 2, "Mocking did not take place");
}

// Test Function mocking in Different module
// Calling function directly
@test:Config {}
function test_SubtractFunction() {
    int answer = 0;
    answer = SubtractModule:intSubtract(5, 3);
    test:assertEquals(answer, 8, "Mocking did not take place");
}

// Test Function mocking in Different module
// Calling function directly
@test:Config {}
function test_SubtractFunctionInSameModule() {
    int answer = 0;
    answer = intSubtract(5, 3);
    test:assertEquals(answer, 2, "Mocking did take place, but mocked the wrong function");
}

// Test Function mocking for native functions
@test:Config {}
function test_MockNativeFunction() {
    float answer = 0;
    answer = math:sqrt(5);

    test:assertEquals(answer, 125.0, "Mocking did not take place");
}


//
// FUNCTION MOCKS
//

@test:Mock {
    moduleName : ".", //Remove module name
    functionName : "intAdd"
}
function mockIntAdd(int a, int b) returns (int) {
    return a-b;
}

@test:Mock {
    moduleName : "SubtractModule",
    functionName : "intSubtract"
}
function mockIntSubtract(int a, int b) returns (int) {
    return a+b;
}

@test:Mock {
    moduleName : "ballerina/math",
    functionName : "sqrt"
}
function mocksqrt(float a) returns (float) {
    return a*a*a;
}



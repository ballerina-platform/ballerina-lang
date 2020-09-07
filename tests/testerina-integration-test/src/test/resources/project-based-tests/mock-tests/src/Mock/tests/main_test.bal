// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/test;
import ballerina/io;
import ballerina/math;
import Mock2;

(any|error)[] outputs = [];
int counter = 0;

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

@test:Mock {
    functionName : "intAdd3"
}
test:MockFunction mock_intAdd3 = new();

@test:Mock {
    moduleName: "ballerina/io",
    functionName: "print"
}
test:MockFunction mock_print = new();

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

public function mockIntAdd5((any|error)... args) returns (int) {
    int sum = 0;

    foreach var arg in args {
        if (arg is int) {
            sum -= arg;
        }
    }

    return  sum;
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

public function mockPrint((any|error)... s) {
    outputs[counter] = s[0];
    counter += 1;
}

//
// TESTS
//

@test:Config {
}
public function call_Test1() {
    io:println("\t[call_Test1] Testing .call function with different types of mock functions");

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
    io:println("\t[call_Test2] Test switching mock functions");

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
    io:println("\t[call_Test3] Test invalid mock function");
    test:when(mock_intAdd).call("invalidMockFunction");
    test:assertEquals(intAdd(10, 6), 4);
}

@test:Config {
}
public function call_Test4() {
    io:println("\t[call_Test4] Test mock function with invalid return type");
    test:when(mock_intAdd).call("mockIntAdd3");
    test:assertEquals(intAdd(10, 6), 4);
}

@test:Config {
}
public function call_Test5() {
    io:println("\t[call_Test5] Test mock function with invalid parameters");
    test:when(mock_intAdd).call("mockIntAdd4");
    test:assertEquals(intAdd(10, 6), 4);
}

@test:Config {}
public function call_Test6() {
    io:println("\t[call_Test6] Test mock function in import package");
    test:when(mock_absInt).call("mockAbsInt");
    test:assertEquals(math:absInt(-5), 100);
}

@test:Config {}
public function call_Test7() {
    io:println("\t[call_Test7] Test mock function in import package in same project");
    test:when(mock2_intAdd).call("mockIntAdd2");
    test:assertEquals(Mock2:intAdd2(10, 5), 50);
}

@test:Config {}
public function call_Test8() {
    io:println("\t[call_Test8] Test calling the mock function with varargs");
    test:when(mock_intAdd3).call("mockIntAdd5");
    test:assertEquals(intAdd3(1, 3, 5), -9);
}

@test:Config {}
public function call_Test9() {
    io:println("\t[call_Test9] Test calling the import mock function with varargs");
    test:when(mock_print).call("mockPrint");
    main();
    if (outputs[0] is anydata) {
        test:assertEquals(<anydata>outputs[0], "FunctionMocking Tests");
    } else {
        test:assertExactEquals(outputs[0], "FunctionMocking Tests");
    }
}

@test:Config {
}
public function thenReturn_Test1() {
    io:println("\t[thenReturn_Test1] Test thenReturns");

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
    io:println("\t[withArguments_Test1] Test withArguments");

    test:when(mock_intAdd).withArguments(20, 14).thenReturn(100);
    test:assertEquals(intAdd(20, 14), 100);

    test:when(mock_stringAdd).withArguments("string1").thenReturn("test");
    test:assertEquals(stringAdd("string1"), "test");
}

@test:Config {
}
public function callOriginal_Test1() {
    io:println("\t[callOriginal_Test1] Testing .callOriginal function");

    // IntAdd
    test:when(mock_intAdd).callOriginal();
    test:assertEquals(intAdd(10, 6), 16);
    test:assertEquals(callIntAdd(10, 6), 16);

    // StringAdd
    test:when(mock_stringAdd).callOriginal();
    test:assertEquals(stringAdd("Ibaqu"), "test_Ibaqu");

    // FloatAdd
    test:when(mock_floatAdd).callOriginal();
    test:assertEquals(floatAdd(10.6, 4.5), 15.1);
}

@test:Config {}
public function callOriginal_Test2() {
    io:println("\t[callOriginal_Test2] Test callOriginal function in import package");
    test:when(mock_absInt).callOriginal();
    test:assertEquals(math:absInt(-5), 5);
}

@test:Config {}
public function callOriginal_Test3() {
    io:println("\t[callOriginal_Test3] Test callOriginal with import package in same project");
    test:when(mock2_intAdd).callOriginal();
    test:assertEquals(Mock2:intAdd2(10, 5), 15);
}

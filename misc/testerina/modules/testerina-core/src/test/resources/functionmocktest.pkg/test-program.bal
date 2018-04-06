package functionmocktest.pkg;

import ballerina/test;
import ballerina/io;

@test:Mock {
    packageName : "functionmocktest2.pkg" ,
    functionName : "intAdd"
}
public function mockIntAdd (int a, int b) returns (int) {
    return (a-b);
}

@test:Config{}
function testAssertIntEquals () {
    int answer = 0;
    answer = intAdd(5, 3);
    io:println("Function mocking test");
    test:assertEquals(answer, 2, msg = "function mocking failed");
}

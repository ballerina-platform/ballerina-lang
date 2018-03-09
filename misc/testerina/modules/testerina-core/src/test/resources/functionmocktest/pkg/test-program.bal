package src.test.resources.functionmocktest.pkg;

import ballerina.test;
import ballerina.io;

@test:mock {
    packageName : "src.test.resources.functionmocktest2.pkg" ,
    functionName : "intAdd"
}
public function mockIntAdd (int a, int b) (int c) {
    return a-b;
}

@test:config{}
function testAssertIntEquals () {
    int answer = 0;
    answer = intAdd(5, 3);
    io:println("Function mocking test");
    test:assertEquals(answer, 2, "function mocking failed");
}

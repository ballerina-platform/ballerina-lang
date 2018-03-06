package src.test.resources.functionmocktest.pkg;

import ballerina.test;
import ballerina.io;

@test:mock {
    packageName : "src.test.resources.functionmocktest.pkg" ,
    functionName : "intAdd"
}
function mockIntAdd (int a, int b) (int c) {
    io:println("I'm the mockIntAdd!");
    return a+b;
}

@test:config{}
function testAssertIntEquals () {
    int answer = 0;
    answer = intAdd(5, 3);
    test:assertEquals(answer, 8, "intAdd function failed");
}

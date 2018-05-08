
import ballerina/test;
import ballerina/io;

@test:Config{}
function testAssertIntEquals () {
    int answer = 0;
    answer = intAdd(5, 3);
    io:println("Function mocking reset test");
    test:assertEquals(answer, 8, msg = "function mocking reset failed");
}

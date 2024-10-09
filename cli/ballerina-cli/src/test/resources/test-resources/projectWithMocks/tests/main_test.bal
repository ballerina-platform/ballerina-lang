import ballerina/test;
import projectWithMocks.mod1;

@test:Mock{functionName: "intAdd"}
test:MockFunction intAddMockFn = new ();

@test:Config{}
function testMockedIntAdd() {
    test:when(intAddMockFn).thenReturn(23);
    test:assertEquals(intAdd(10, 5), 23, "Invalid result");

    test:when(intAddMockFn).withArguments(1000, 44).thenReturn(77);
    test:assertEquals(intAdd(1000, 44), 77, "Invalid result");
}

@test:Config{}
function testRealIntSub() {
    test:assertEquals(mod1:intSub(10, 5), 5, "Invalid result");
}

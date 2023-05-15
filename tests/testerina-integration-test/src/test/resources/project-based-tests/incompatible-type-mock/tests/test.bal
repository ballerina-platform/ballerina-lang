import ballerina/test;

function stringHello(int a, int b) returns string {
    return "Hello";
}

@test:Mock {
    functionName: "intAdd"
}
test:MockFunction intAddMockFn = new();

@test:Config
function functionMockingTest() {
    test:when(intAddMockFn).call("stringHello");
    test:assertEquals(intAdd(5,5),0);
}

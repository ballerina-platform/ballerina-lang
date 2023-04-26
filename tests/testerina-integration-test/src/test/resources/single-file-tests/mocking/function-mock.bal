import ballerina/test;

function intSub(int a, int b) returns int{
    return (a-b);
}

function intAdd(int a, int b) returns int {
    return (a+b);

}

@test:Mock { functionName: "intAdd" }
test:MockFunction intAddMockFn = new();

@test:Config{}
function functionMockingTest() {
    test:when(intAddMockFn).call("intSub");
    test:assertEquals(intAdd(5,5),0);
}

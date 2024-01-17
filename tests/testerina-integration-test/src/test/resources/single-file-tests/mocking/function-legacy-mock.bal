import ballerina/test;

function intAdd(int a, int b) returns int {
    return (a+b);

}

@test:Mock { functionName: "intAdd" }
function intAddMock(int a, int b) returns int {
    return a-b;
}

@test:Config{}
function functionMockingTest() {
    test:assertEquals(intAdd(5, 5), 0);
}

import ballerina/test;

import ballerina/os;

@test:Mock {functionName: "intAdd"}
test:MockFunction intAddMockFn = new ();

@test:Mock {
    moduleName: "ballerina/os",
    functionName: "getUserHome"
}
test:MockFunction mockFunc2 = new ();

@test:Config {}
function intAddTest4() {

    test:when(mockFunc2).thenReturn("beta");
    test:assertEquals(os:getUserHome(), "beta");

    test:when(intAddMockFn).thenReturn(210);
    test:assertEquals(intAdd(33, 35), 210);

    test:when(intAddMockFn).withArguments(38, 35).thenReturn(21);
    test:assertEquals(intAdd(38, 35), 21);

    test:when(intAddMockFn).call("intSub");
    test:assertEquals(intAdd(5, 5), 0);

    test:when(intAddMockFn).withArguments(4, 5).call("intMul");
    test:assertEquals(intAdd(4, 5), 20);

    test:when(intAddMockFn).callOriginal();
    test:assertEquals(intAdd(34, 5), 39);

    test:when(intAddMockFn).withArguments(123, 122).callOriginal();
    test:assertEquals(intAdd(123, 122), 245);

}

@test:Config {}
function intAddInsideTest() {
    test:when(intAddMockFn).thenReturn(210);
    test:assertEquals(intAddInside(33, 35), 310);
}

function intSub(int a, int b) returns int {
    return (a - b);
}

function intMul(int a, int b) returns int {
    return (a * b);
}


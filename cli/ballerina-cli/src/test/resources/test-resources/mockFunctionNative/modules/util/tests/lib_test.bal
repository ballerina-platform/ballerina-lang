import ballerina/test;

import winner.util2 as util3;

import ballerina/os;

@test:Mock {functionName: "intAdd1"}
test:MockFunction intAddMockFn = new ();

@test:Mock {
    moduleName: "winner.util2",
    functionName: "intDiv"
}
test:MockFunction mockFunc1 = new ();

@test:Mock {
    moduleName: "ballerina/os",
    functionName: "getUserHome"
}
test:MockFunction mockFunc2 = new ();

@test:Config {}
function intAddTest() {

    test:when(mockFunc1).thenReturn(100);
    test:assertEquals(util3:intDiv(10, 10), 100);

    test:when(mockFunc2).thenReturn("alpha");
    test:assertEquals(os:getUserHome(), "alpha");

    test:when(intAddMockFn).thenReturn(210);
    test:assertEquals(intAdd1(33, 35), 210);

    test:when(intAddMockFn).withArguments(38, 35).thenReturn(21);
    test:assertEquals(intAdd1(38, 35), 21);

    test:when(intAddMockFn).call("intSub");
    test:assertEquals(intAdd1(5, 5), 0);

    test:when(intAddMockFn).withArguments(4, 5).call("intMul");
    test:assertEquals(intAdd1(4, 5), 20);

    test:when(intAddMockFn).callOriginal();
    test:assertEquals(intAdd1(34, 5), 39);

    test:when(intAddMockFn).withArguments(123, 122).callOriginal();
    test:assertEquals(intAdd1(123, 122), 245);

}

@test:Config {}
function intDivInsideTest() {
    test:when(mockFunc1).thenReturn(100);
    test:assertEquals(util3:intDivInside(10, 10), 200);
}

function intSub(int a, int b) returns int {
    return (a - b);
}

function intMul(int a, int b) returns int {
    return (a * b);
}


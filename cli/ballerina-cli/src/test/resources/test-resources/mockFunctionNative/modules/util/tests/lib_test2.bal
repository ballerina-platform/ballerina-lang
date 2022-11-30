import ballerina/test;

import ballerina/os;

@test:Config {}
function intAddTest2() {

    // test:when(mockFunc1).thenReturn(100);
    // test:assertEquals(util3:intDiv(10, 10), 100);

    // test:when(mockFunc2).thenReturn("alpha");
    // test:assertEquals(os:getUsername(), "alpha");

    test:when(intAddMockFn).thenReturn(2900);
    test:assertEquals(intAdd1(33, 35), 2900);

    test:assertEquals(os:getUserHome(), "alpha");
}

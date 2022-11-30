import ballerina/test;

import ballerina/os;

@test:Config {}
function intAddTest2() {

    test:when(intAddMockFn).thenReturn(2900);
    test:assertEquals(intAdd1(33, 35), 2900);

    test:assertEquals(os:getUserHome(), "alpha");
}

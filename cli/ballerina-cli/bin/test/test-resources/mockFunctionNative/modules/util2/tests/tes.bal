import ballerina/test;

import ballerina/os;

@test:Config {}
function intAddTest3() {
    test:assertNotEquals(os:getUserHome(), "alpha");
    test:assertNotEquals(os:getUserHome(), "beta");

}


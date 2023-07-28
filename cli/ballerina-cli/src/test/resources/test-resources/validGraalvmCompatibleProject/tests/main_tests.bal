import ballerina/test;

@test:Config{}
function beforeFunc() {
    test:assertTrue(true, msg = "Failed!");
}

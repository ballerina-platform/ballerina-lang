import ballerina/test;

@test:Config {}
function testFunction1() {
    test:assertEquals(joinStrings(), "test1test2");
}
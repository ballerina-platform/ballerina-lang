import ballerina/test;

@test:Config {}
function testAssertNilEquals() {
    () expected = ();
    test:assertEquals((), expected);
}

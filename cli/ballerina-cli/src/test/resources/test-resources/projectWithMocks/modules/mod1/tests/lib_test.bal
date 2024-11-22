import ballerina/test;

@test:Mock{functionName: "intSub"}
test:MockFunction intSubMockFn = new ();

@test:Config{}
function testMockedIntSub() {
    test:when(intSubMockFn).thenReturn(23);
    test:assertEquals(intSub(10, 5), 23, "Invalid result");

    test:when(intSubMockFn).withArguments(33, 44).thenReturn(77);
    test:assertEquals(intSub(33, 44), 77, "Invalid result");
}

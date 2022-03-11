import ballerina/test;

@test:Config {}
function test_range_1() {
    test:assertEquals(range(0, 5), [0, 1, 2, 3, 4, 5]);
    test:assertEquals(range(5, 0), [5, 4, 3, 2, 1, 0]);
    test:assertEquals(range(0, 0), [0]);
}
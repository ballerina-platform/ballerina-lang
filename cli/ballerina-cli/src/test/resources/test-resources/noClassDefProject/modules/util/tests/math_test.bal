import ballerina/test;

@test:Config {}
function test_indexOfMedian_1() {
    test:assertEquals(indexOfMedian([1, 2, 2, 3, 4, 7, 9]), { a: 3, b: () });
    test:assertEquals(indexOfMedian([1, 2, 2, 3, 4, 7, 7, 9]), { a: 3, b: 4 });
    test:assertEquals(indexOfMedian([0, 1, 1, 2, 2, 2, 4, 7, 14, 16]), { a: 4, b: 5 });
}
@test:Config {}
function test_power_1() {
    test:assertEquals(power(2, 0), 1);
    test:assertEquals(power(2, 1), 2);
    test:assertEquals(power(2, 2), 4);
    test:assertEquals(power(2, 3), 8);
    test:assertEquals(power(2, 4), 16);
    test:assertEquals(power(2, 5), 32);
    test:assertEquals(power(2, 6), 64);
    test:assertEquals(power(2, 7), 128);
    test:assertEquals(power(2, 8), 256);
}
import ballerina/test;

@test:Config {}
public function testRunMain() {
    int[] a = [];
    a[(2147483647-9)] = 10;
}

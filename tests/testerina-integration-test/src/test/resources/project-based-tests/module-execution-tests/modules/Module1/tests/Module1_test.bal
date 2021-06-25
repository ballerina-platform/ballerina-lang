import ballerina/test;

@test:Config {}
public function module1_test1() {
    test:assertEquals(intSubtract(6, 3), 3);
}

@test:Config {
    groups:["g1"]
}
public function module1_test2() {
    test:assertEquals(intSubtract(6, 3), 3);
}

@test:Config {}
public function commonTest_Module1() {
    test:assertEquals(intSubtract(6, 3), 3);
}



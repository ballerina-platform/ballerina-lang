import ballerina/test;

@test:Config {}
public function main_test1() {
    test:assertTrue(true);
}

@test:Config {
    groups:["g1"]
}
public function main_test2() {
    test:assertTrue(true);
}

@test:Config {}
public function main_test3() {
    test:assertTrue(true);
}

@test:Config {
    groups:["g1"],
    enable: false
}
public function main_test4() {
    test:assertTrue(true);
}

@test:Config {}
public function commonTest() {
    test:assertTrue(true);
}

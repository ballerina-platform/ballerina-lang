import ballerina/test;

@test:Config
public function ttt() {
    string result = "a";
    test:assertTrue(result.includes(" "));
}
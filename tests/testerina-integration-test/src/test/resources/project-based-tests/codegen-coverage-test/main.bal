import ballerina/codeModifier;

public function main() {
    codeModifier:doSomething();
}

function intAdd(int a, int b) returns (int) {
    return a + b;
}

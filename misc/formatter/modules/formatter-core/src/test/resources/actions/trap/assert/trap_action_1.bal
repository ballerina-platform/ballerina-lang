function divide(int a, int b) returns int {
    return a / b;
}

public function foo() {
    int|error result = trap divide(1, 0);
}

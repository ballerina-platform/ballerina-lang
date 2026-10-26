int total = sum(1, 2);

public function main() {
    int doubled = twice(total);
    _ = sum(doubled, total);
}

function sum(int a, int b) returns int {
    return a + b;
}

function twice(int value) returns int {
    return sum(value, value);
}

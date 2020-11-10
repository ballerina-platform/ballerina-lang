function printSum(int a, int b) {
}

function sum(int a, int b) returns int {
    return a + b;
}

public function foo() {
    future<int> f1 = start sum(40, 50);
    future<()> f2 = start printSum(40, 50);
}

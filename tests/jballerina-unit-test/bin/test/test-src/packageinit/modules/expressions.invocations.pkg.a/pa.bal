
int a1 = 9;

int b1 = a1 + 9;

int c1 = test();

function test() returns (int) {
    a1 = a1 + 10;
    return a1;
}

public function getA1() returns int {
    return a1;
}

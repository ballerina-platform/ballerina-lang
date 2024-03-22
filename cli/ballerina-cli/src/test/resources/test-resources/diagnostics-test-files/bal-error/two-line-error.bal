public function main() {
    int a = exponentiate(1 + 2 + 3 + 4 + 5, 3 + 4 + 5 +
    "Hello world" + 6 + 7 + 8 + 9 + 10);
    // 
    // 
    // 
    // 
    // 
    // 
    // 
    // 
    // 
    // 
    // 
    // 

    int d = exponentiate(2, 3
    , true, "Hello, World!");
}

public function exponentiate(int base, int exponent) returns int {
    if (exponent == 0) {
        return 1;
    }
    if (exponent % 2 == 0) {
        int halfResult = exponentiate(base, exponent / 2);
        return halfResult * halfResult;
    }
    return base * exponentiate(base, exponent - 1);
}

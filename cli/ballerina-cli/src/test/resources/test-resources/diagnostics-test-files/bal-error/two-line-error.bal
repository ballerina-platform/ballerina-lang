import ballerina/io;

public function main() {
    io:println("Hello, World!", 1 + 2
    + 3.4, true, "Hello, World!");
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

    io:println("Hello, World!", 1 + 2 + exponentiate(2, 3
    , true, "Hello, World!"));
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


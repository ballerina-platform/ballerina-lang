function addSubPrecedence (int a, int b, int c) returns (int) {
    int sum = a - b + c;
    return sum;
}

function addSubMultPrecedence (int a, int b, int c, int d, int e, int f) returns (int) {
    int sum = a * b - c + d * e - f;
    return sum;
}

function multDivisionPrecedence (int a, int b, int c, int d, int e, int f) returns (int) {
    int sum = a * b / c * d * e / f;
    return sum;
}

function addMultPrecedence (int a, int b, int c, int d, int e, int f) returns (int) {
    int sum = a * b * c + d * e + f;
    return sum;
}

function addDivisionPrecedence (int a, int b, int c, int d, int e, int f) returns (int) {
    int sum = a / b / c + d / e + f;
    return sum;
}

function intAdditionAndSubtractionPrecedence(int a, int b, int c, int d) returns (int) {
    return a - b + c - d;
}

function intMultiplicationPrecedence(int a, int b, int c, int d) returns (int) {
    int x = (a + b) * (c * d) + a * b;
    int y = (a + b) * (c * d) - a * b;
    int z = a + b * c * d - a * b;
    return x + y - z;
}

function intDivisionPrecedence(int a, int b, int c, int d) returns (int) {
    int x = (a + b) / (c + d) + a / b;
    int y = (a + b) / (c - d) - a / b;
    int z = a + b / c - d - a / b;
    return x - y + z;
}

function intMultiplicationAndDivisionPrecedence(int a, int b, int c, int d) returns (int) {
    int x = (a + b) * (c + d) + a / b;
    int y = (a + b) / (c - d) - a * b;
    int z = a + b / c - d + a * b;
    return x + y + z;
}

function comparatorPrecedence (int a, int b, int c, int d, int e, int f) returns (boolean) {
    boolean result = (a > b) && (c < d) || (e > f);
    return result;
}
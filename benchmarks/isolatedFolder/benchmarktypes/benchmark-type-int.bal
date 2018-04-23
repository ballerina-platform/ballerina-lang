
public function benchmarkIntAddition() {
    int a = 5;
    int b = 6;
    int c = a + b;
}

public function benchmarkIntMultiplication() {
    int a = 5;
    int b = 6;
    int c = b * a;
}

public function benchmarkIntSubtraction() {
    int a = 5;
    int b = 6;
    int c = b - a;
}

public function benchmarkIntDivision() {
    int a = 15;
    int b = 3;
    int c = a / b;
}

public function benchmarkIntTypesAddition() {
    int b = 10;
    int a = 0xa;
    int c = 0_12;
    int d = 0b1010;
    int e = a + b + c + d;
}

public function benchmarkIntegerTypesMultiplication() {
    int b = 1;
    int a = 0x1;
    int c = 0_1;
    int d = 0b1;
    int e = a * b * c * d;
}

public function benchmarkIntegerTypesSubtraction() {
    int b = 10;
    int a = 0xa;
    int c = 0_12;
    int d = 0b1010;
    int e = (a - b) - (c - d);
}

public function benchmarkIntegerTypesDivision() {
    int b = 10;
    int a = 0xa;
    int c = 0_12;
    int d = 0b1010;
    int e = (a / b) / (c / d);
}


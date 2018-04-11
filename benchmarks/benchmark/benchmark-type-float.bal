package benchmark;

function benchmarkFloatAddition() {
    float b = 9.9;
    float a = 10.1;
    float c = a + b;
}

function benchmarkFloatMultiplication() {
    float a = 2.5;
    float b = 5.5;
    float c = a * b;
}

function benchmarkFloatSubtraction() {
    float a = 25.5;
    float b = 15.5;
    float c = a - b;
}

function benchmarkFloatDivision() {
    float a = 25.5;
    float b = 5.1;
    float c = a / b;
}

function benchmarkFloatAdditionWithReturn() {
    float a = floatAddition();
}

function floatAddition() returns (float) {
    float b = 9.9;
    float a = 10.1;
    return a + b;
}

function benchmarkFloatMultiplicationWithReturn() {
    float a = floatMultiplication();
}

function floatMultiplication() returns (float) {
    float a = 2.5;
    float b = 5.5;
    return a * b;
}

function benchmarkFloatSubtractionWithReturn() {
    float a = floatSubtraction();
}

function floatSubtraction() returns (float) {
    float a = 25.5;
    float b = 15.5;
    return a - b;
}

function benchmarkFloatDivisionWithReturn() {
    float a = floatDivision();
}

function floatDivision() returns (float) {
    float a = 25.5;
    float b = 5.1;
    return a / b;
}

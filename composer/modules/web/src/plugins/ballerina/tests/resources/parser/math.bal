import ballerina/lang.math;

function expTest(float value) (float) {
    return math:exp(value);
}

function powTest(float a, float b) (float) {
    return math:pow(a, b);
}

function randomTest() (float) {
    return math:random();
}

function sqrtTest(float val) (float) {
    return math:sqrt(val);
}

function randomInRangeTest(int a, int b) (int) {
    return math:randomInRange(a, b);
}

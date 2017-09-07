import ballerina.lang.math;

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

function absFloatTest(float a) (float) {
    return math:absFloat(a);
}

function absIntTest(int a) (int) {
    return math:absInt(a);
}

function acosTest(float a) (float) {
    return math:acos(a);
}

function addExactTest(int a, int b) (int) {
    return math:addExact(a, b);
}

function asinTest(float a) (float) {
    return math:asin(a);
}

function atanTest(float a) (float) {
    return math:atan(a);
}

function atan2Test(float a, float b) (float) {
    return math:atan2(a, b);
}

function cbrtTest(float val) (float) {
    return math:cbrt(val);
}

function ceilTest(float val) (float) {
    return math:ceil(val);
}

function copySignTest(float a, float b) (float) {
    return math:copySign(a, b);
}

function cosTest(float val) (float) {
    return math:cos(val);
}

function coshTest(float val) (float) {
    return math:cosh(val);
}

function decrementExactTest(int a) (int) {
    return math:decrementExact(a);
}
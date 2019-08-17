import ballerina/math;

function expTest (float value) returns (float) {
    return math:exp(value);
}

function powTest (float a, float b) returns (float) {
    return math:pow(a, b);
}

function randomTest () returns (float) {
    return math:random();
}

function sqrtTest (float val) returns (float) {
    return math:sqrt(val);
}

function randomInRangeTest (int a, int b) returns (int|math:Error) {
    return math:randomInRange(a, b);
}

function absFloatTest (float a) returns (float) {
    return math:absFloat(a);
}

function absIntTest (int a) returns (int) {
    return math:absInt(a);
}

function acosTest (float a) returns (float) {
    return math:acos(a);
}

function asinTest (float a) returns (float) {
    return math:asin(a);
}

function atanTest (float a) returns (float) {
    return math:atan(a);
}

function atan2Test (float a, float b) returns (float) {
    return math:atan2(a, b);
}

function cbrtTest (float value) returns (float) {
    return math:cbrt(value);
}

function ceilTest (float value) returns (float) {
    return math:ceil(value);
}

function copySignTest (float a, float b) returns (float) {
    return math:copySign(a, b);
}

function cosTest (float value) returns (float) {
    return math:cos(value);
}

function coshTest (float value) returns (float) {
    return math:cosh(value);
}

function expm1Test (float value) returns (float) {
    return math:expm1(value);
}

function floorTest (float value) returns (float) {
    return math:floor(value);
}

function floorDivTest (int a, int b) returns (int|math:Error) {
    return math:floorDiv(a, b);
}

function floorModTest (int a, int b) returns (int|math:Error) {
    return math:floorMod(a, b);
}

function getExponentTest (float val) returns (int) {
    return math:getExponent(val);
}

function hypotTest (float a, float b) returns (float) {
    return math:hypot(a, b);
}

function remainderTest (float a, float b) returns (float) {
    return math:remainder(a, b);
}

function logTest (float value) returns (float) {
    return math:log(value);
}

function log10Test (float value) returns (float) {
    return math:log10(value);
}

function log1pTest (float value) returns (float) {
    return math:log1p(value);
}

function negateExactTest (int value) returns (int|math:Error) {
    return math:negateExact(value);
}

function nextAfterTest (float a, float b) returns (float) {
    return math:nextAfter(a, b);
}

function nextDownTest (float value) returns (float) {
    return math:nextDown(value);
}

function nextUpTest (float value) returns (float) {
    return math:nextUp(value);
}

function rintTest (float value) returns (float) {
    return math:rint(value);
}

function roundTest (float value) returns (int) {
    return math:round(value);
}

function scalbTest (float a, int b) returns (float) {
    return math:scalb(a, b);
}

function signumTest (float value) returns (float) {
    return math:signum(value);
}

function sinTest (float value) returns (float) {
    return math:sin(value);
}

function sinhTest (float value) returns (float) {
    return math:sinh(value);
}

function tanTest (float value) returns (float) {
    return math:tan(value);
}

function tanhTest (float value) returns (float) {
    return math:tanh(value);
}

function toDegreesTest (float value) returns (float) {
    return math:toDegrees(value);
}

function toRadiansTest (float value) returns (float) {
    return math:toRadians(value);
}

function ulpTest (float value) returns (float) {
    return math:ulp(value);
}

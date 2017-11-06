import ballerina.math;

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

function asinTest(float a) (float) {
    return math:asin(a);
}

function atanTest(float a) (float) {
    return math:atan(a);
}

function atan2Test(float a, float b) (float) {
    return math:atan2(a, b);
}

function cbrtTest(float value) (float) {
    return math:cbrt(value);
}

function ceilTest(float value) (float) {
    return math:ceil(value);
}

function copySignTest(float a, float b) (float) {
    return math:copySign(a, b);
}

function cosTest(float value) (float) {
    return math:cos(value);
}

function coshTest(float value) (float) {
    return math:cosh(value);
}

function expm1Test(float value) (float) {
    return math:expm1(value);
}

function floorTest(float value) (float) {
    return math:floor(value);
}

function floorDivTest(int a, int b) (int) {
    return math:floorDiv(a, b);
}

function floorModTest(int a, int b) (int) {
    return math:floorMod(a, b);
}

function getExponentTest(float val) (int) {
    return math:getExponent(val);
}

function hypotTest(float a, float b) (float) {
    return math:hypot(a, b);
}

function IEEEremainderTest(float a, float b) (float) {
    return math:IEEEremainder(a, b);
}

function logTest(float value) (float) {
    return math:log(value);
}

function log10Test(float value) (float) {
    return math:log10(value);
}

function log1pTest(float value) (float) {
    return math:log1p(value);
}

function negateExactTest(int value) (int) {
    return math:negateExact(value);
}

function nextAfterTest(float a, float b) (float) {
    return math:nextAfter(a, b);
}

function nextDownTest(float value) (float) {
    return math:nextDown(value);
}

function nextUpTest(float value) (float) {
    return math:nextUp(value);
}

function rintTest(float value) (float) {
    return math:rint(value);
}

function roundTest(float value) (int) {
    return math:round(value);
}

function scalbTest(float a, int b) (float) {
    return math:scalb(a, b);
}

function signumTest(float value) (float) {
    return math:signum(value);
}

function sinTest(float value) (float) {
    return math:sin(value);
}

function sinhTest(float value) (float) {
    return math:sinh(value);
}

function tanTest(float value) (float) {
    return math:tan(value);
}

function tanhTest(float value) (float) {
    return math:tanh(value);
}

function toDegreesTest(float value) (float) {
    return math:toDegrees(value);
}

function toRadiansTest(float value) (float) {
    return math:toRadians(value);
}

function ulpTest(float value) (float) {
    return math:ulp(value);
}

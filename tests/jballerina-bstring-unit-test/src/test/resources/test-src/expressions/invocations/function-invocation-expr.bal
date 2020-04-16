import ballerina/math;

function testFuncInvocation (int a, int b, int c) returns (int) {
    int x;
    x = 10;
    int val = add(a, b);
    val = add(val, c);
    return add(val, x);
}

function add(int x, int y) returns (int) {
    int z;
    z = x  + y;
    return z;
}

function multiply(int x, int y) returns (int) {
    int z;
    z = x * y;
    return z;
}

function funcInvocationWithinFuncInvocation(int a, int b, int c) returns (int){
    int result;

    result = add( add( add(a, c), b), add(b, c) );
    return result + add(a, b) + add(a, b);
}

function testReturnFuncInvocationWithinFuncInvocation(int a, int b) returns (int){
    return add(a, multiply(a, b));
}

function testReturnNativeFuncInvocationWithinNativeFuncInvocation(float x) returns (float) {
    return math:sqrt(math:pow(x, 2.0));
}

function sum (int a) returns @untainted int {
    int x;
    int val = 0;
    if (a > 0) {
        x = sum(a - 1);
        val =  a + x;
    }
    return val;
}

function getPowerOfN (float a, float n) returns (float) {
    float v = math:pow(a, n);
    return v;
}

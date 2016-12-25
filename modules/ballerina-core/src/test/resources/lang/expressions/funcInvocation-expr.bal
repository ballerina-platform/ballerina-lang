
function testFuncInvocation(int a, int b, int c) (int) {
    int x;
    x = 10;
    a = add(a, b);
    a = add(a, c);
    return add(a, x);
}

function add(int x, int y) (int) {
    int z;
    z = x  + y;
    return z;
}

function sum (int a) (int) {
    int x;
    if (a > 0) {
        x = sum(a - 1);
        a =  a + x;
    }
    return a;
}


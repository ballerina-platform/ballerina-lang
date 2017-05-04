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

function funcInvocationWithinFuncInvocation(int a, int b, int c) (int){
    int result;

    result = add( add( add(a, c), b), add(b, c) );
    return result + add(a, b) + add(a, b);
}

function sum (int a) (int) {
    int x;
    if (a > 0) {
        x = sum(a - 1);
        a =  a + x;
    }
   return a;
}


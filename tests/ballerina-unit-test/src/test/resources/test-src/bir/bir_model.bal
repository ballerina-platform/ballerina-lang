function main (int arg) returns int {
    int a = 10;
    boolean b = a > arg;
    return a;
}

function genComplex (int arg1, int arg2) returns int {
    int a = 10;
    int b = a + arg1;
    int c = a + b + arg2;
    b = b + c;
    return a + b;
}

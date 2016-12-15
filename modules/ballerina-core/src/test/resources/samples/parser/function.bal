function test(int a, int b, int c) (int, int) {

    int x;
    x = 10;
    a = c;
    a = b ;

    while ( a == b ) {
        a = b + c;
    }

    b = c + 1 + x;

    return a + x, b + 1;
}
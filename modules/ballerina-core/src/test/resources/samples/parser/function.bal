function test(int a, int b, int c) (int, int) {

    a = c;
    a = b ;

    while ( a == b ) {
        a = b + c;
    }

    b = c + c;

    return a, b;
}
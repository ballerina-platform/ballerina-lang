function sum (int a) (int) {
    int x;
    if (a > 0) {
        x = sum(a - 1);
        a =  a + x;
    }
    return a;
}
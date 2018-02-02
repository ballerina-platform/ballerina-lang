function whileScope() {
    int a = 0;
    while( a < 5) {
        a = a + 1;
        int b = a + 20;
    }
    int sum = b;
}
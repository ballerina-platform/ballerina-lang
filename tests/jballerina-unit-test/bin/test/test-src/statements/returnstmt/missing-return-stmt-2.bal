function testReturnFromIf() returns (int) {
    int x = 5;

    if (x > 10) {
        return 500;
    } else {
        x = 100;
    }
}

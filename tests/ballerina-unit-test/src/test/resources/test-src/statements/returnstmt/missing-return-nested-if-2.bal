function testReturnFromIf() returns (int) {
    int x = 5;
    int y = 10;

    if (x > 10) {
        if (y < 10) {
            y = 0;
        } else {
            return 0;
        }
    } else {
        return 100;
    }
}

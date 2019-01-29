function testReturnFromIf() returns (int) {
    int x = 5;
    int y = 10;

    if (x > 10) {
        if (y < 10) {
            return -1;
        } else {
            return 0;
        }
    } else {
        if (y > 20) {
            return 30;
        } else {
            y = 50;
        }
    }
}

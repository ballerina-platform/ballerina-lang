function testWhileStmt(int x, int y) (int) {
    int z;

    while(x >= y) {
        y = y + 1;
        z = z + 10;
    }
    return z;
}
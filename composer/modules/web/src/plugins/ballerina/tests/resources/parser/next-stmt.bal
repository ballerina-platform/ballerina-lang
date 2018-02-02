function calculateExp1 (int x, int y) (int) {
    int z = 0;
    while (x > y) {
        y = y + 1;
        if (y == 10) {
            next;
        }
        z = z + 1;
    }
    return z;
}

function nestedNextStmt (int x, int y) (int) {
    int z = 0;
    while (x >= y) {
        y = y + 1;
        int i = 0;
        if (y == 10) {
            next;
        }
        while (i < y) {
            i = i + 1;
            if (i == 10) {
                next;
            }
            z = z + i;
        }
    }
    return z;
}

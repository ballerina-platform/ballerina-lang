function calculateExp1 (int x, int y) returns (int) {
    int z = 0;
    int yCopy = y;
    while (x >= yCopy) {
        yCopy = yCopy + 1;
        if (yCopy == 10) {
            z = 100;
            break;
        } else if (yCopy > 20) {
            z = 1000;
            break;
        }
        z = z + 10;
    }
    break;
    int a = 10;
    return z;
}

function calculateExp2 (int x, int y) returns (int) {
    int z = 0;
    int yCopy = y;
    while (x >= yCopy) {
        yCopy = yCopy + 1;
        if (yCopy == 10) {
            z = 100;
            break;
        } else if (yCopy > 20) {
            z = 1000;
            break;
            int a = 10;
        }
        z = z + 10;
    }
    return z;
}

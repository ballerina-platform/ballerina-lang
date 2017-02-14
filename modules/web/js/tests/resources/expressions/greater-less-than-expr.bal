function testIntRanges(int a) (int) {
        int retunType;
        if (a <= 0) {
            retunType = 1;
        } else if ((a > 0) && (a < 100)) {
            retunType = 2;
        } else if (a >= 100) {
            retunType = 3;
        }
        return retunType;
}

function testFloatRanges(float a) (int) {
        int retunType;
        if (a <= 0.0) {
            retunType = 1;
        } else if ((a > 0.0) && (a < 100.0)) {
            retunType = 2;
        } else if (a >= 101.0) {
            retunType = 3;
        }
        return retunType;
}

function testIntAndFloatCompare(int a, float b) (boolean) {
    return a > b;
}


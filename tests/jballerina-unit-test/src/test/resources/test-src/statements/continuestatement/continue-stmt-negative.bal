function calculateExp1 (int x, int y) returns (int) {
    int z = 0;
    int yCopy = y;
    while (x >= yCopy) {
        yCopy = yCopy + 1;
        if (yCopy == 10) {
            z = 100;
            continue;
        } else if (yCopy > 20) {
            z = 1000;
            continue;
        }
        z = z + 10;
    }
    continue;
    int _ = 10;
    return z;
}

function calculateExp2 (int x, int y) returns (int) {
    int z = 0;
    int yCopy = y;
    while (x >= yCopy) {
        yCopy = yCopy + 1;
        if (yCopy == 10) {
            z = 100;
            continue;
        } else if (yCopy > 20) {
            z = 1000;
            continue;
            int _ = 10;
        }
        z = z + 10;
    }
    return z;
}

function testContinueWithinInternalItr() {
    int[] a = [1, 2, 3];
    int[][] b = [a, a];

    foreach int[] intArr in b {
        intArr.forEach(function(int num) {
            if num == -1 {
                continue;
            }
        });
    }
}

function calculateExp1 (int x, int y) returns (int) {
    int z = 0;
    int yCopy = y;
    while (x > yCopy) {
        yCopy = yCopy + 1;
        if (yCopy == 10) {
            continue;
        }
        z = z + 1;
    }
    return z;
}

function nestedContinueStmt (int x, int y) returns (int) {
    int z = 0;
    int yCopy = y;
    while (x >= yCopy) {
        yCopy = yCopy + 1;
        int i = 0;
        if (yCopy == 10) {
            continue;
        }
        while (i < yCopy) {
            i = i + 1;
            if (i == 10) {
                continue;
            }
            z = z + i;
        }
    }
    return z;
}

string output = "";

function tracePath (string path) {
    output = output + "->" + path;
}

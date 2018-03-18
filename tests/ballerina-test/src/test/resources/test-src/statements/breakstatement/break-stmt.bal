function calculateExp1 (int x, int y) (int) {
    int z;
    while (x >= y) {
        y = y + 1;
        if (y == 10) {
            z = 100;
            break;
        } else if (y > 20) {
            z = 1000;
            break;
        }
        z = z + 10;
    }
    return z;
}

function nestedBreakStmt (int x, int y) (int) {
    int z = 10;
    while (x >= y) {
        y = y + 1;
        if (y >= 10) {
            z = z + 100;
            break;
        }
        z = z + 10;
        while (y < x) {
            z = z + 10;
            y = y + 1;
            if (z >= 40) {
                break;
            }
        }
    }
    return z;
}

string output = "";

function tracePath (string path) {
    output = output + "->" + path;
}

function testFinallyWithWhile (string command) (string) {
    output = "start";
    int i = 0;
    while (i < 5) {
        tracePath("while" + i);
        try {
            tracePath("try" + i);
            if (i == 2 && command == "break") {
                tracePath("break");
                break;
            }
        } finally {
            tracePath("finally" + i);
            i = i + 1;
        }
        tracePath("whileEnd" + (i - 1));
    }
    tracePath("end");
    return output;
}

function testFinallyWithForeach (string command) (string) {
    output = "start";
    foreach i in [ 0..5 ] {
        tracePath("foreach" + i);
        try {
            tracePath("try" + i);
            try {
                tracePath("tryIn" + i);
                if (i == 3 && command == "break") {
                    tracePath("break");
                    break;
                }
            }finally {
                tracePath("finallyIn" + i);
            }
        } finally {
            tracePath("finally" + i);
        }
        tracePath("foreachEnd" + i);
    }
    tracePath("end");
    return output;
}

function calculateExp1 (int x, int y) returns (int) {
    int z;
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
    return z;
}

function nestedBreakStmt (int x, int y) returns (int) {
    int z = 10;
    int yCopy = y;
    while (x >= yCopy) {
        yCopy = yCopy + 1;
        if (yCopy >= 10) {
            z = z + 100;
            break;
        }
        z = z + 10;
        while (yCopy < x) {
            z = z + 10;
            yCopy = yCopy + 1;
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

function testFinallyWithWhile (string command) returns (string) {
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

function testFinallyWithForeach (string command) returns (string) {
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

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
            if (i == 2 && command == "next") {
                tracePath("next");
                next;
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
            if (i == 3 && command == "next") {
                tracePath("next");
                next;
            }
        } finally {
            tracePath("finally" + i);
        }
        tracePath("foreachEnd" + i);
    }
    tracePath("end");
    return output;
}

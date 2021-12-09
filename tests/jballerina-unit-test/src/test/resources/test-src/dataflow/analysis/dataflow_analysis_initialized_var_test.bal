function testInitializedVarWithWhile1() {
    int i;

    while true {
        i = 1;
        break;
    }

    int j = i;
    assertEquality(1, j);
}

function testInitializedVarWithWhile2() {
    int i;
    boolean b = true;

    while true {
        if b {
            i = 1;
        } else {
            i = 2;
        }
        break;
    }

    int j = i;
    assertEquality(1, j);
}

function testInitializedVarWithWhile3() {
    int i;
    boolean b = false;

    while true {
        while b {
            i = 1;
            break;
        }
        i = 2;
        break;
    }

    int j = i;
    assertEquality(2, j);
}

function testInitializedVarWithWhile4() {
    int i;
    boolean b = false;

    while true {
        while true {
            if b {
                i = 2;
            } else {
                i = 3;
            }
            break;
        }
        break;
    }

    int j = i;
    assertEquality(3, j);
}

function testInitializedVarWithWhile5() {
    int i;

    if true {
        while true {
            i = 1;
            break;
        }
    }

    int j = i;
    assertEquality(1, j);
}

function testInitializedVarWithWhile6() {
    int i;
    boolean b = true;

    if true {
        while true {
            boolean c = true;
            if c {
                i = 1;
            } else {
                i = 2;
            }
            break;
        }
    }

    int j = i;
    assertEquality(1, j);
}

function testInitializedVarWithWhile7() {
    int i;
    boolean b = true;

    if b {
        while true {
            boolean c = true;
            while true {
                if c {
                    i = 1;
                } else {
                    i = 2;
                }
                break;
            }
            break;
        }
    } else {
        i = 3;
    }

    int j = i;
    assertEquality(1, j);
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error("expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}

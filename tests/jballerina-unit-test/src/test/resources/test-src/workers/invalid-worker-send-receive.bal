function case1() {
    worker w1 {
        20 -> w2;
    }

    worker w2 {
        "xxx" -> w1;
    }
}

function case2() {
    worker w1 {
        int _ = <- w2;
    }

    worker w2 {
        string _ = <- w1;
    }
}

function case3() {
    boolean foo = true;
    worker w1 {
        if foo {
            2 -> w2;
        } else {
            3 -> w2;
        }
        4 -> w2;
    }

    worker w2 returns error? {
        _ = check <- w1;
        _ = check <- w1;
    }
}

function case4() {
    worker w1 {
        "xxx" -> w2;
    }

    worker w2 {
        _ = <- w1 | w1;
    }
}

function case5() {
    worker w1 {
        "xxx" -> w2;
    }

    worker w2 {
        _ = <- {a: w1, b: w3, c: w3};
    }

    worker w3 {
        "yyy" -> w2;
    }
}

function case6() {
    worker w1 {
    }

    worker w2 {
        _ = <- {a: w1, b: w3, c: w3};
    }

    worker w3 {
        "yyy" -> w2;
    }
}

function case7() {
    fork {
        worker w1 {
            int a = 5;
            int b = 0;
            a -> w2;
            b = <- w3;
        }
        worker w2 {
            int a = 0;
            int b = 15;
            a = <- w1;
            a -> w3;
        }
        worker w3 {
            int a = 0;
            int b = 15;
            a = <- w2;
        }
    }
}

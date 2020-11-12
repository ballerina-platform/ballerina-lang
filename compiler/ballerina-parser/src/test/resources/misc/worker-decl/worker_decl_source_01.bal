function foo() {
    int a = 1;

    @someAnnot {}
    worker w1 {
        int b = 2;
    }

    worker w2 returns string {
        int c = 3;
    }

    int d = 4;
}

function bar() {
    worker w1 {
        int b = 2;
    }

    worker w2 {
        int c = 3;
    }

    int d = 4;
}

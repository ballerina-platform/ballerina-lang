function foo() {
    worker w1 {
        int b = 2;
    } on fail {
        int c = 3;
    }

    worker w2 {

    }

    worker w3 {
        int c = 3;
    } on fail var e {

    }

    worker w4 {
        int c = 3;
    } on fail error<E> e {
        return error("error!");
    }

    @someAnnot {}
    worker w5 returns string {
        int c = 3;
    }

    int d = 4;
}

public function foo() {

    @someAnnotate {}
    worker w1 {
        int i = 100;
        () send = i ->> w2;
    }

    @anotherAnnotate {
        a: "worker",
        b: "2"
    }
    worker w2 {
        int lw;
        lw = <- w1;
    }
}

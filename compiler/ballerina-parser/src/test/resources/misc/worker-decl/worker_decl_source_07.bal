function foo() {
    worker w1 {
        int b = 2;
    } % on % fail {
    }
}

function bar() {
    worker w2 {
        int b = 2;
    } on f
}

function baz() {
    worker w3 {
        int b = 2;
    } on f

    worker w4 {
        int b = 2;
    }
}

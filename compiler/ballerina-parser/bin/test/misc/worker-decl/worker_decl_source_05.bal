function foo() {

    int a = 4;

    worker w1 {
        worker w2 {
        }
    }

    if (true) {
        worker w3 {
        }    
    }

    int b = 4;

    worker w4 {
    }
}

function f1() {
    worker w1 {
        int a = 5;

        _ = from var i in a -> w2
            let any v = a -> w2
            select a -> w2;
    }
}

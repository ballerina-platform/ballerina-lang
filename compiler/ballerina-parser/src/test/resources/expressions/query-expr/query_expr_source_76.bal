function f1() {
    worker w1 {
        int a = 5;

        _ = from var i in [1, 2]
            select (a -> w2);
    }
}

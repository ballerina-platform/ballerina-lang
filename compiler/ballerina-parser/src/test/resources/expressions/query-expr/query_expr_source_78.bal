function f1() {
    worker w1 {
        int a = 5;

        _ = from var i in a -> w2
            join var j in a -> w3
            on i equals j
            select a -> w2;
    }
}

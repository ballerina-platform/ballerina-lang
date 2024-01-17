function f1() {
    worker w1 {
        int a = 5;

        _ = from var i in a -> w2
            from var j in a -> w3
            select a -> w2;
    }
}

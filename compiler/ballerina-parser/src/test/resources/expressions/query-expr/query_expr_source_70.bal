function foo() {
    var v = let var a = from var b in c.d
            select e
        in {f: g.h, i: j.k};

    var list = [
        from int i in [1, 2, 3]
        select i * 2,
        from int i in [1, 2, 3]
        where i % 2 != 0
        select i
    ];
}

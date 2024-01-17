function fn() {
    int x2 = from var {x, y} in [{"x":2, "y":3}, {"x":4, "y":5}]
                collect sum(x);
}

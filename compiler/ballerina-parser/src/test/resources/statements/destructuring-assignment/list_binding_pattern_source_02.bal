function foo() {
    [a, b = x;
    [a, _ ...b] = x;
    [...] = x;
    [a, _, [x, y ...z], ...b] = x;
    [1] = x;
    [[1], b] = x;
    [true, a] = x;
    [{}];
    [{}]
}

function foo() {
    [a, b] = x;
    [a, _, ...b] = x;
    [...b] = x;
    [] = x;
    [a, _, [x, y, ...z], ...b] = x;
}

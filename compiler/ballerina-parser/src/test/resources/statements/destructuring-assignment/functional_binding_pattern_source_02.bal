function foo() {
    [x, error(a, b)] = "hello";
    [x, T(a, b)] = "hello";

    {x: error(a, b)} = "hello";
    {x: T(a, b)} = "hello";

    error(a, T(b, c), d=error(e, f)) = "hello";
    T(a, error(b, c), d=T(e, f)) = "hello";
}

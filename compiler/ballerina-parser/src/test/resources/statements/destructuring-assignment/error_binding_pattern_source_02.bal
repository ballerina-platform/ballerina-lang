function foo() {
    [x, error(a, b)] = "hello";
    [x, error T(a, b)] = "hello";

    {x: error(a, b)} = "hello";
    {x: error T(a, b)} = "hello";

    error(a, error T(b, c), d = error(e, f)) = "hello";
    error T(a, error(b, c), d = error T(e, f)) = "hello";
}

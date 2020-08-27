function foo() {
    error(a, b) = "hello";
    error(a, b=c, ...d) = "hello";

    T(a, b) = "hello";
    T(a, b=c, ...d) = "hello";
}
